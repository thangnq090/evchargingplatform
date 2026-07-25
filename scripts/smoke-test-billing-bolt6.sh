#!/usr/bin/env bash
# =============================================================================
# smoke-test-billing-bolt6.sh
# Integration smoke test for Billing & Pricing (Bolt 006-billing-pricing-1)
#
# Verifies:
#   1. After a session completes, an invoice is generated automatically.
#   2. Invoice has correct line items (base fee + markup fee).
#   3. Admin income report returns correct revenue and breakdown.
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-billing-bolt6.sh
# =============================================================================

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASS=0
FAIL=0

# Colours
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

pass()  { echo -e "${GREEN}[PASS]${NC} $1"; ((PASS++)) || true; }
fail()  { echo -e "${RED}[FAIL]${NC} $1"; ((FAIL++)) || true; }
info()  { echo -e "    ${YELLOW}ℹ${NC}  $1"; }

assert_http() {
  local label="$1"
  local expected_status="$2"
  local actual_status="$3"
  local body="$4"
  if [ "$actual_status" -eq "$expected_status" ]; then
    pass "$label (HTTP $actual_status)"
  else
    fail "$label — expected HTTP $expected_status, got $actual_status. Body: $body"
  fi
}

echo ""
echo "=============================================="
echo "  Billing & Pricing Smoke Test"
echo "  Target: $BASE_URL"
echo "=============================================="
echo ""

TIMESTAMP=$(date +%s)
VENDOR_NAME="Billing Smoke Vendor ${TIMESTAMP}"
VENDOR_ADMIN_EMAIL="billing-smoke-vendor-admin-${TIMESTAMP}@evcharging.test"
CUSTOMER_EMAIL="billing-customer-${TIMESTAMP}@evcharging.test"

# -------------------------------------------------------
# STEP 1: Login as Seeded Superadmin (ADMIN)
# -------------------------------------------------------
echo ">>> STEP 1: Login as Superadmin"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "superadmin@evcharging.test",
    "password": "SuperAdmin@Pass1!"
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Superadmin Login" 200 "$STATUS" "$BODY"
SUPERADMIN_JWT=$(echo "$BODY" | jq -r '.data.accessToken // empty')

if [ -z "$SUPERADMIN_JWT" ]; then
  fail "No Superadmin JWT obtained — stopping test."
  exit 1
fi
info "Superadmin JWT obtained."

# -------------------------------------------------------
# STEP 2: Create Vendor
# -------------------------------------------------------
echo ""
echo ">>> STEP 2: Create Vendor"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/vendors" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPERADMIN_JWT" \
  -d "{
    \"vendorName\": \"$VENDOR_NAME\",
    \"adminName\": \"Billing Vendor Admin\",
    \"adminEmail\": \"$VENDOR_ADMIN_EMAIL\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Create Vendor" 201 "$STATUS" "$BODY"
VENDOR_ID=$(echo "$BODY" | jq -r '.data.vendorId // empty')
INVITATION_TOKEN=$(echo "$BODY" | jq -r '.data.invitationToken // empty')
info "Vendor ID: $VENDOR_ID"

# -------------------------------------------------------
# STEP 3: Accept Vendor Invitation
# -------------------------------------------------------
echo ""
echo ">>> STEP 3: Accept Vendor Invitation"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/invitations/accept" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$INVITATION_TOKEN\",
    \"name\": \"Billing Vendor Admin\",
    \"password\": \"VendorAdmin@Pass1!\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Accept Invitation" 201 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 4: Login as Vendor Admin
# -------------------------------------------------------
echo ""
echo ">>> STEP 4: Login as Vendor Admin"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$VENDOR_ADMIN_EMAIL\",
    \"password\": \"VendorAdmin@Pass1!\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Vendor Admin Login" 200 "$STATUS" "$BODY"
VENDOR_ADMIN_JWT=$(echo "$BODY" | jq -r '.data.accessToken // empty')

# -------------------------------------------------------
# STEP 5: Set Admin Markup (15% = 1500 basis points)
# -------------------------------------------------------
echo ""
echo ">>> STEP 5: Set Admin Markup (1500 basis points = 15%)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/admin/vendors/$VENDOR_ID/markup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPERADMIN_JWT" \
  -d '{
    "markupBasisPoints": 1500
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Set Vendor Markup (15%)" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 6: Create Charging Station (unitPriceTenthCents = 2000 = 0.20 EUR/kWh)
# -------------------------------------------------------
echo ""
echo ">>> STEP 6: Create Charging Station (base price 2000 tenth-cents = 0.20 EUR/kWh)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/stations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT" \
  -d '{
    "name": "Billing Smoke Station 1",
    "groupLabel": "BILLING-TEST",
    "unitPriceTenthCents": 2000,
    "location": {
      "latitude": 37.7749,
      "longitude": -122.4194
    },
    "connectors": [
      {
        "type": "CCS",
        "maxPowerKw": 50
      }
    ]
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Create Station" 201 "$STATUS" "$BODY"
STATION_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Station ID: $STATION_ID"
# Expected marked-up rate: 2000 * 1.15 = 2300 tenth-cents = 2.3000 EUR/kWh

# -------------------------------------------------------
# STEP 7: Register & Login as Customer
# -------------------------------------------------------
echo ""
echo ">>> STEP 7: Register Customer"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/register-customer" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Billing Customer\",
    \"email\": \"$CUSTOMER_EMAIL\",
    \"password\": \"CustomerPass123!\",
    \"phone\": \"+15559990000\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Customer" 201 "$STATUS" "$BODY"
CUSTOMER_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Customer ID: $CUSTOMER_ID"

echo ""
echo ">>> STEP 7b: Login as Customer"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$CUSTOMER_EMAIL\",
    \"password\": \"CustomerPass123!\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Customer Login" 200 "$STATUS" "$BODY"
CUSTOMER_JWT=$(echo "$BODY" | jq -r '.data.accessToken // empty')

# -------------------------------------------------------
# STEP 8: Start Charging Session
# -------------------------------------------------------
echo ""
echo ">>> STEP 8: Start Charging Session"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/sessions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d "{
    \"stationId\": \"$STATION_ID\",
    \"connectorId\": 1,
    \"customerId\": \"$CUSTOMER_ID\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Start Session" 201 "$STATUS" "$BODY"
SESSION_ID=$(echo "$BODY" | jq -r '.data.id // empty')
START_TIME=$(echo "$BODY" | jq -r '.data.startTime // empty')
info "Session ID: $SESSION_ID"

# Verify marked-up rate = 2000 * 1.15 = 2300 tenth-cents = 2.3000 EUR
UNIT_RATE_AMOUNT=$(echo "$BODY" | jq -r '.data.unitRate.amount // empty')
if [ "$UNIT_RATE_AMOUNT" = "2.3000" ]; then
  pass "Marked-up unit rate is correct (2.3000 EUR = 0.20 base + 15% markup)"
else
  fail "Incorrect unit rate: expected 2.3000 EUR, got $UNIT_RATE_AMOUNT"
fi

# -------------------------------------------------------
# STEP 9: Record Meter Reading (5 kWh)
# -------------------------------------------------------
echo ""
echo ">>> STEP 9: Record Meter Reading (5 kWh)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/meter-readings" \
  -H "Content-Type: application/json" \
  -d "{
    \"timestamp\": \"$START_TIME\",
    \"energyDeliveredKwh\": 5.0,
    \"powerKw\": 50.0
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Record Meter Reading" 202 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 10: Stop Session
# -------------------------------------------------------
echo ""
echo ">>> STEP 10: Stop Session"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/stop" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d '{}')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Stop Session" 200 "$STATUS" "$BODY"

# Expected: 5 kWh * 2.30 EUR = 11.5000 EUR
FINAL_AMOUNT=$(echo "$BODY" | jq -r '.data.totalAmount.amount // empty')
if [ "$FINAL_AMOUNT" = "11.5000" ]; then
  pass "Session total amount correct (11.5000 EUR = 5 kWh × 2.3000)"
else
  fail "Incorrect session final amount: expected 11.5000, got $FINAL_AMOUNT"
fi

# -------------------------------------------------------
# STEP 11: Wait briefly for async invoice generation
# -------------------------------------------------------
echo ""
echo ">>> STEP 11: Waiting for async invoice generation..."
sleep 2

# -------------------------------------------------------
# STEP 12: Fetch Invoice by Session ID
# -------------------------------------------------------
echo ""
echo ">>> STEP 12: Fetch Invoice for Session"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/billing/invoices/session/$SESSION_ID" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Fetch Invoice" 200 "$STATUS" "$BODY"

# Verify invoice has session_id
INVOICE_SESSION_ID=$(echo "$BODY" | jq -r '.data.sessionId // empty')
if [ "$INVOICE_SESSION_ID" = "$SESSION_ID" ]; then
  pass "Invoice session ID matches"
else
  fail "Invoice session ID mismatch: expected $SESSION_ID, got $INVOICE_SESSION_ID"
fi

# Verify invoice status is PENDING
INVOICE_STATUS=$(echo "$BODY" | jq -r '.data.status // empty')
if [ "$INVOICE_STATUS" = "PENDING" ]; then
  pass "Invoice status is PENDING"
else
  fail "Expected invoice status PENDING, got $INVOICE_STATUS"
fi

# Verify total amount = 11.5000 EUR
INVOICE_TOTAL=$(echo "$BODY" | jq -r '.data.totalAmount // empty')
if [ "$INVOICE_TOTAL" = "11.5000" ]; then
  pass "Invoice total amount correct (11.5000 EUR)"
else
  fail "Incorrect invoice total: expected 11.5000, got $INVOICE_TOTAL"
fi

# Verify line items are present
LINE_ITEM_COUNT=$(echo "$BODY" | jq '.data.lineItems | length')
if [ "$LINE_ITEM_COUNT" -ge 1 ]; then
  pass "Invoice has $LINE_ITEM_COUNT line item(s)"
else
  fail "Invoice has no line items"
fi

# Verify line items contain Base Charging Fee
BASE_FEE_PRESENT=$(echo "$BODY" | jq -r '[.data.lineItems[].description] | any(contains("Base Charging Fee"))')
if [ "$BASE_FEE_PRESENT" = "true" ]; then
  pass "Invoice contains 'Base Charging Fee' line item"
else
  fail "Invoice missing 'Base Charging Fee' line item"
fi

# Verify line items contain Platform Markup Fee
MARKUP_PRESENT=$(echo "$BODY" | jq -r '[.data.lineItems[].description] | any(contains("Platform Markup Fee"))')
if [ "$MARKUP_PRESENT" = "true" ]; then
  pass "Invoice contains 'Platform Markup Fee' line item"
else
  fail "Invoice missing 'Platform Markup Fee' line item"
fi

# -------------------------------------------------------
# STEP 13: Admin Income Report — no vendor filter
# -------------------------------------------------------
echo ""
echo ">>> STEP 13: Admin Income Report (all vendors, today)"
# Use UTC date since all server timestamps are stored in UTC
UTC_DATE=$(TZ=UTC date +%Y-%m-%d)
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE_URL/api/v1/admin/billing/income?startDate=${UTC_DATE}&endDate=${UTC_DATE}" \
  -H "Authorization: Bearer $SUPERADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Admin Income Report (all)" 200 "$STATUS" "$BODY"

SESSION_COUNT=$(echo "$BODY" | jq -r '.data.sessionCount // 0')
if [ "$SESSION_COUNT" -ge 1 ]; then
  pass "Income report contains at least 1 session (got $SESSION_COUNT)"
else
  fail "Income report shows 0 sessions"
fi

TOTAL_REVENUE=$(echo "$BODY" | jq -r '.data.totalRevenue // 0')
info "Total Revenue today: $TOTAL_REVENUE EUR"

BREAKDOWN_COUNT=$(echo "$BODY" | jq '.data.breakdowns | length')
if [ "$BREAKDOWN_COUNT" -ge 1 ]; then
  pass "Income report contains $BREAKDOWN_COUNT vendor breakdown(s)"
else
  fail "Income report missing vendor breakdowns"
fi

# -------------------------------------------------------
# STEP 14: Admin Income Report — filtered by vendor
# -------------------------------------------------------
echo ""
echo ">>> STEP 14: Admin Income Report (filtered by vendor)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE_URL/api/v1/admin/billing/income?startDate=${UTC_DATE}&endDate=${UTC_DATE}&vendorId=${VENDOR_ID}" \
  -H "Authorization: Bearer $SUPERADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Admin Income Report (vendor filter)" 200 "$STATUS" "$BODY"

VENDOR_SESSION_COUNT=$(echo "$BODY" | jq -r '.data.sessionCount // 0')
if [ "$VENDOR_SESSION_COUNT" -ge 1 ]; then
  pass "Vendor-filtered income report shows $VENDOR_SESSION_COUNT session(s)"
else
  fail "Vendor-filtered income report shows 0 sessions"
fi

echo ""
echo "=============================================="
echo "  Billing & Pricing Smoke Test Completed"
echo "  Passed: $PASS, Failed: $FAIL"
echo "=============================================="
echo ""

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
