#!/usr/bin/env bash
# =============================================================================
# smoke-test-session-bolt5.sh
# Integration smoke test for Session Management Service (Bolt 005-session-management-1)
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-session-bolt5.sh
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
echo "  Session Management Service Smoke Test"
echo "  Target: $BASE_URL"
echo "=============================================="
echo ""

# -------------------------------------------------------
# STEP 1: Login as Seeded Superadmin (ADMIN)
# -------------------------------------------------------
echo ">>> STEP 1: Login as Seeded Superadmin"
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
info "Superadmin JWT obtained."

if [ -z "$SUPERADMIN_JWT" ]; then
  fail "No Superadmin JWT obtained — stopping test."
  exit 1
fi

TIMESTAMP=$(date +%s)
VENDOR_NAME="Session Smoke Vendor ${TIMESTAMP}"
VENDOR_ADMIN_EMAIL="session-smoke-vendor-admin-${TIMESTAMP}@evcharging.test"

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
    \"adminName\": \"Vendor Admin\",
    \"adminEmail\": \"$VENDOR_ADMIN_EMAIL\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Create Vendor" 201 "$STATUS" "$BODY"
VENDOR_ID=$(echo "$BODY" | jq -r '.data.vendorId // empty')
INVITATION_TOKEN=$(echo "$BODY" | jq -r '.data.invitationToken // empty')

# -------------------------------------------------------
# STEP 3: Accept Vendor Invitation
# -------------------------------------------------------
echo ""
echo ">>> STEP 3: Accept Vendor Invitation"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/invitations/accept" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$INVITATION_TOKEN\",
    \"name\": \"Vendor Admin\",
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
# STEP 5: Create Charging Station
# -------------------------------------------------------
echo ""
echo ">>> STEP 5: Create Charging Station"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/stations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT" \
  -d '{
    "name": "Session Smoke Station 1",
    "groupLabel": "SOMA-WEST",
    "unitPriceTenthCents": 3000,
    "location": {
      "latitude": 37.7749,
      "longitude": -122.4194
    },
    "connectors": [
      {
        "type": "CCS",
        "maxPowerKw": 150
      }
    ]
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Create Station" 201 "$STATUS" "$BODY"
STATION_ID=$(echo "$BODY" | jq -r '.data.id // empty')
CONNECTOR_ID=$(echo "$BODY" | jq -r '.data.connectors[0].id // empty')
info "Created Station ID: $STATION_ID"
info "Connector ID: $CONNECTOR_ID"

# -------------------------------------------------------
# STEP 6: Configure Vendor Markup (using Superadmin)
# -------------------------------------------------------
echo ""
echo ">>> STEP 6: Configure Vendor Markup (1500 basis points = 15%)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/admin/vendors/$VENDOR_ID/markup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPERADMIN_JWT" \
  -d '{
    "markupBasisPoints": 1500
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Set Vendor Markup" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 7: Register a Customer
# -------------------------------------------------------
echo ""
echo ">>> STEP 7: Register Customer"
CUSTOMER_EMAIL="session-customer-${TIMESTAMP}@evcharging.test"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/register-customer" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Session Customer\",
    \"email\": \"$CUSTOMER_EMAIL\",
    \"password\": \"CustomerPass123!\",
    \"phone\": \"+15551234567\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Customer" 201 "$STATUS" "$BODY"
CUSTOMER_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Customer ID: $CUSTOMER_ID"

# -------------------------------------------------------
# STEP 8: Login as Customer
# -------------------------------------------------------
echo ""
echo ">>> STEP 8: Login as Customer"
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
# STEP 9: Start Charging Session (using Customer JWT)
# -------------------------------------------------------
echo ""
echo ">>> STEP 9: Start Charging Session"
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
info "Start Time: $START_TIME"

# Assert marked-up unit rate (3000 tenth cents * 1.15 = 3450 tenth cents = 3.4500 EUR)
UNIT_RATE_AMOUNT=$(echo "$BODY" | jq -r '.data.unitRate.amount // empty')
if [ "$UNIT_RATE_AMOUNT" = "3.4500" ]; then
  pass "Marked-up Rate (3.4500 EUR matches 3000 base + 15% markup)"
else
  fail "Incorrect unit rate amount: expected 3.4500, got $UNIT_RATE_AMOUNT"
fi

# -------------------------------------------------------
# STEP 10: Record Meter Reading
# -------------------------------------------------------
echo ""
echo ">>> STEP 10: Record Meter Reading"
READING_TIME="$START_TIME"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/meter-readings" \
  -H "Content-Type: application/json" \
  -d "{
    \"timestamp\": \"$READING_TIME\",
    \"energyDeliveredKwh\": 10.5,
    \"powerKw\": 22.0
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Record Meter Reading" 202 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 11: Stop Session
# -------------------------------------------------------
echo ""
echo ">>> STEP 11: Stop Session"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/stop" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d '{}')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Stop Session" 200 "$STATUS" "$BODY"

# Assert final cost: 10.5 kWh * 3.45 EUR = 36.2250 EUR
FINAL_AMOUNT=$(echo "$BODY" | jq -r '.data.totalAmount.amount // empty')
if [ "$FINAL_AMOUNT" = "36.2250" ]; then
  pass "Final Amount matches expected (36.2250 EUR)"
else
  fail "Incorrect final amount: expected 36.2250, got $FINAL_AMOUNT"
fi

# -------------------------------------------------------
# STEP 12: Get Customer Session History
# -------------------------------------------------------
echo ""
echo ">>> STEP 12: Get Customer Session History"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/sessions/history?customerId=$CUSTOMER_ID" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Get Session History" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 13: Get Vendor Session Report
# -------------------------------------------------------
echo ""
echo ">>> STEP 13: Get Vendor Session Report"
DATE_STR=$(date +%Y-%m-%d)
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/sessions/report?stationId=$STATION_ID&date=$DATE_STR" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Get Vendor Session Report" 200 "$STATUS" "$BODY"

echo ""
echo "=============================================="
echo "  Smoke Test Completed"
echo "  Passed: $PASS, Failed: $FAIL"
echo "=============================================="
echo ""

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
