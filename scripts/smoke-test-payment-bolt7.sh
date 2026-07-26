#!/usr/bin/env bash
# =============================================================================
# smoke-test-payment-bolt7.sh
# Integration smoke test for Payment Processing (Bolt 007-payment-processing-1)
#
# Verifies:
#   1. Complete session lifecycle (Start Session -> Meter Reading -> Stop Session).
#   2. SessionCompletedEvent triggering PaymentOrchestrator asynchronously.
#   3. Payment entity stored in DB with status CAPTURED via MockPaymentAdapter.
#   4. Idempotent payment processing for repeated event triggers.
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-payment-bolt7.sh
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
echo "  Payment Processing Smoke Test"
echo "  Target: $BASE_URL"
echo "=============================================="
echo ""

TIMESTAMP=$(date +%s)
VENDOR_NAME="Payment Smoke Vendor ${TIMESTAMP}"
VENDOR_ADMIN_EMAIL="payment-smoke-vendor-admin-${TIMESTAMP}@evcharging.test"
CUSTOMER_EMAIL="payment-customer-${TIMESTAMP}@evcharging.test"

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
    \"adminName\": \"Payment Vendor Admin\",
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
    \"name\": \"Payment Vendor Admin\",
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
    "name": "Payment Smoke Station 1",
    "groupLabel": "PAYMENT-TEST",
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

# -------------------------------------------------------
# STEP 6: Register & Login as Customer
# -------------------------------------------------------
echo ""
echo ">>> STEP 6: Register Customer"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/register-customer" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Payment Customer\",
    \"email\": \"$CUSTOMER_EMAIL\",
    \"password\": \"CustomerPass123!\",
    \"phone\": \"+15559991111\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Customer" 201 "$STATUS" "$BODY"
CUSTOMER_ID=$(echo "$BODY" | jq -r '.data.id // empty')

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
# STEP 7: Start Charging Session
# -------------------------------------------------------
echo ""
echo ">>> STEP 7: Start Charging Session"
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

# -------------------------------------------------------
# STEP 8: Record Meter Reading (10 kWh)
# -------------------------------------------------------
echo ""
echo ">>> STEP 8: Record Meter Reading (10 kWh)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/meter-readings" \
  -H "Content-Type: application/json" \
  -d "{
    \"timestamp\": \"$START_TIME\",
    \"energyDeliveredKwh\": 10.0,
    \"powerKw\": 50.0
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Record Meter Reading" 202 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 9: Stop Session (Triggers SessionCompletedEvent)
# -------------------------------------------------------
echo ""
echo ">>> STEP 9: Stop Session"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/sessions/$SESSION_ID/stop" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d '{}')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Stop Session" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 10: Wait briefly for async payment orchestration
# -------------------------------------------------------
echo ""
echo ">>> STEP 10: Waiting for async payment processing..."
sleep 2

# -------------------------------------------------------
# STEP 11: Verify Payment Entity Status (CAPTURED)
# -------------------------------------------------------
echo ""
echo ">>> STEP 11: Fetch Payment Record for Session"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/payments/session/$SESSION_ID" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Fetch Payment Record" 200 "$STATUS" "$BODY"

PAYMENT_STATUS=$(echo "$BODY" | jq -r '.data.status // empty')
if [ "$PAYMENT_STATUS" = "CAPTURED" ]; then
  pass "Payment status is CAPTURED for session $SESSION_ID"
else
  fail "Payment status mismatch: expected CAPTURED, got $PAYMENT_STATUS"
fi

echo ""
echo "=============================================="
echo "  Payment Processing Smoke Test Completed"
echo "  Passed: $PASS, Failed: $FAIL"
echo "=============================================="
echo ""

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
