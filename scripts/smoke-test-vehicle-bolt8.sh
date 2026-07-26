#!/usr/bin/env bash
# =============================================================================
# smoke-test-vehicle-bolt8.sh
# Integration smoke test for Vehicle Management (Bolt 008-vehicle-management-1)
#
# Verifies:
#   1. Customer registration & authentication
#   2. Registering a vehicle with plate & RFID tag (no vendor association needed)
#   3. Listing my active vehicles
#   4. Fetching vehicle detail by ID
#   5. Associating an RFID tag to an active vehicle
#   6. Lookup vehicle by exact RFID
#   7. Plate uniqueness conflict handling (409 Conflict)
#   8. RFID uniqueness conflict handling (409 Conflict)
#   9. De-listing a vehicle (204 No Content) and re-registering the same plate
#  10. Admin vehicle detail and ownership history inspection
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-vehicle-bolt8.sh
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
echo "  Vehicle Management Smoke Test"
echo "  Target: $BASE_URL"
echo "=============================================="
echo ""

TIMESTAMP=$(date +%s)
CUSTOMER_EMAIL="vehicle-customer-${TIMESTAMP}@evcharging.test"
PLATE_1="EV-SMK-${TIMESTAMP: -4}"
RFID_1="RFID-SMK-${TIMESTAMP}"
RFID_2="RFID-SMK-2-${TIMESTAMP}"

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
# STEP 2: Register & Login as Customer
# -------------------------------------------------------
echo ""
echo ">>> STEP 2: Register Customer"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/register-customer" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Vehicle Customer\",
    \"email\": \"$CUSTOMER_EMAIL\",
    \"password\": \"CustomerPass123!\",
    \"phone\": \"+15558880000\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Customer" 201 "$STATUS" "$BODY"
CUSTOMER_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Customer ID: $CUSTOMER_ID"

echo ""
echo ">>> STEP 2b: Login as Customer"
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
# STEP 3: Register Vehicle 1 (Plate + RFID)
# -------------------------------------------------------
echo ""
echo ">>> STEP 3: Register Vehicle 1 (Plate: $PLATE_1, RFID: $RFID_1)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/vehicles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d "{
    \"registrationPlate\": \"$PLATE_1\",
    \"rfidNumber\": \"$RFID_1\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Vehicle 1" 201 "$STATUS" "$BODY"
VEHICLE_1_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Vehicle 1 ID: $VEHICLE_1_ID"

# -------------------------------------------------------
# STEP 4: Verify Active Vehicles List
# -------------------------------------------------------
echo ""
echo ">>> STEP 4: List My Active Vehicles"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/vehicles" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "List My Vehicles" 200 "$STATUS" "$BODY"

VEHICLE_COUNT=$(echo "$BODY" | jq '.data | length')
if [ "$VEHICLE_COUNT" -ge 1 ]; then
  pass "My vehicles list contains $VEHICLE_COUNT item(s)"
else
  fail "My vehicles list is empty"
fi

# -------------------------------------------------------
# STEP 5: Get Vehicle Detail
# -------------------------------------------------------
echo ""
echo ">>> STEP 5: Get Vehicle 1 Detail"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/vehicles/$VEHICLE_1_ID" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Get Vehicle Detail" 200 "$STATUS" "$BODY"

RETRIEVED_PLATE=$(echo "$BODY" | jq -r '.data.registrationPlate // empty')
if [ "$RETRIEVED_PLATE" = "$PLATE_1" ]; then
  pass "Retrieved registration plate matches ($PLATE_1)"
else
  fail "Registration plate mismatch: expected $PLATE_1, got $RETRIEVED_PLATE"
fi

# -------------------------------------------------------
# STEP 6: Lookup Vehicle by RFID
# -------------------------------------------------------
echo ""
echo ">>> STEP 6: Lookup Vehicle by RFID ($RFID_1)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/vehicles/lookup/rfid/$RFID_1" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Lookup Vehicle by RFID" 200 "$STATUS" "$BODY"

RFID_LOOKUP_ID=$(echo "$BODY" | jq -r '.data.id // empty')
if [ "$RFID_LOOKUP_ID" = "$VEHICLE_1_ID" ]; then
  pass "RFID lookup returned correct vehicle ID ($VEHICLE_1_ID)"
else
  fail "RFID lookup mismatch: expected $VEHICLE_1_ID, got $RFID_LOOKUP_ID"
fi

# -------------------------------------------------------
# STEP 7: Register Vehicle 2 (Plate only) & Associate RFID
# -------------------------------------------------------
PLATE_2="EV-ASSOC-${TIMESTAMP: -4}"
echo ""
echo ">>> STEP 7: Register Vehicle 2 (Plate: $PLATE_2, No RFID initially)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/vehicles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d "{
    \"registrationPlate\": \"$PLATE_2\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Vehicle 2" 201 "$STATUS" "$BODY"
VEHICLE_2_ID=$(echo "$BODY" | jq -r '.data.id // empty')

echo ""
echo ">>> STEP 7b: Associate RFID ($RFID_2) to Vehicle 2"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/api/v1/vehicles/$VEHICLE_2_ID/rfid" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d "{
    \"rfidNumber\": \"$RFID_2\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Associate RFID" 200 "$STATUS" "$BODY"

ASSOCIATED_RFID=$(echo "$BODY" | jq -r '.data.rfidNumber // empty')
if [ "$ASSOCIATED_RFID" = "$RFID_2" ]; then
  pass "RFID successfully associated ($RFID_2)"
else
  fail "RFID association failed: expected $RFID_2, got $ASSOCIATED_RFID"
fi

# -------------------------------------------------------
# STEP 8: Test Plate Conflict (409 Conflict)
# -------------------------------------------------------
echo ""
echo ">>> STEP 8: Attempt Duplicate Plate Registration (Conflict Test)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/vehicles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d "{
    \"registrationPlate\": \"$PLATE_1\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Duplicate Plate Conflict" 409 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 9: Test RFID Conflict (409 Conflict)
# -------------------------------------------------------
PLATE_3="EV-RFID-${TIMESTAMP: -4}"
echo ""
echo ">>> STEP 9: Attempt Duplicate RFID Registration (Conflict Test)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/vehicles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d "{
    \"registrationPlate\": \"$PLATE_3\",
    \"rfidNumber\": \"$RFID_1\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Duplicate RFID Conflict" 409 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 10: De-list Vehicle 1 (204 No Content)
# -------------------------------------------------------
echo ""
echo ">>> STEP 10: De-list Vehicle 1 ($VEHICLE_1_ID)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/api/v1/vehicles/$VEHICLE_1_ID" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Delist Vehicle" 204 "$STATUS" "$BODY"

# Verify vehicle 1 is now soft-deleted (no longer listed in active vehicles)
echo ""
echo ">>> STEP 10b: Verify De-listed Vehicle Not in Active List"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/vehicles" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
IS_DELISTED_IN_LIST=$(echo "$BODY" | jq -r "[.data[].id] | contains([\"$VEHICLE_1_ID\"])")
if [ "$IS_DELISTED_IN_LIST" = "false" ]; then
  pass "De-listed vehicle successfully excluded from active list"
else
  fail "De-listed vehicle still appears in active list"
fi

# -------------------------------------------------------
# STEP 11: Re-register De-listed Plate
# -------------------------------------------------------
echo ""
echo ">>> STEP 11: Re-register De-listed Plate ($PLATE_1)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/vehicles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_JWT" \
  -d "{
    \"registrationPlate\": \"$PLATE_1\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Re-register Plate" 201 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 12: Admin Vehicle & Ownership History Inspection
# -------------------------------------------------------
echo ""
echo ">>> STEP 12: Admin Inspect Ownership History ($VEHICLE_1_ID)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/admin/vehicles/$VEHICLE_1_ID/ownership" \
  -H "Authorization: Bearer $SUPERADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Admin Ownership History" 200 "$STATUS" "$BODY"

RECORD_COUNT=$(echo "$BODY" | jq '.data | length')
if [ "$RECORD_COUNT" -ge 1 ]; then
  pass "Admin retrieved $RECORD_COUNT ownership record(s)"
else
  fail "Admin ownership history is empty"
fi

echo ""
echo "=============================================="
echo "  Vehicle Management Smoke Test Completed"
echo "  Passed: $PASS, Failed: $FAIL"
echo "=============================================="
echo ""

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
