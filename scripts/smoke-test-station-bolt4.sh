#!/usr/bin/env bash
# =============================================================================
# smoke-test-station-bolt4.sh
# Integration smoke test for Station Management Service (Bolt 004-station-management-1)
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-station-bolt4.sh
#
# Requirements:
#   - curl, jq
#   - Application running and accessible at BASE_URL
#   - Flyway seeded superadmin@evcharging.test / SuperAdmin@Pass1!
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
echo "  Station Management Service Smoke Test"
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
info "Superadmin JWT obtained: ${SUPERADMIN_JWT:0:30}..."

if [ -z "$SUPERADMIN_JWT" ]; then
  fail "No Superadmin JWT obtained — stopping test."
  exit 1
fi

TIMESTAMP=$(date +%s)
VENDOR_NAME="Smoke Vendor ${TIMESTAMP}"
VENDOR_ADMIN_EMAIL="smoke-vendor-admin-${TIMESTAMP}@evcharging.test"

# -------------------------------------------------------
# STEP 2: Create Vendor (using SUPERADMIN_JWT)
# -------------------------------------------------------
echo ""
echo ">>> STEP 2: Create Vendor for Station Owner"
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
info "Vendor ID: $VENDOR_ID"
info "Invitation Token: $INVITATION_TOKEN"

# -------------------------------------------------------
# STEP 3: Accept Vendor Invitation (Public)
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
# STEP 4: Login as Vendor Admin → get VENDOR_ADMIN_JWT
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
info "Vendor Admin JWT: ${VENDOR_ADMIN_JWT:0:30}..."

if [ -z "$VENDOR_ADMIN_JWT" ]; then
  fail "No Vendor Admin JWT obtained — stopping test."
  exit 1
fi

# -------------------------------------------------------
# STEP 5: Create Charging Station (using VENDOR_ADMIN_JWT)
# -------------------------------------------------------
echo ""
echo ">>> STEP 5: Create Charging Station"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/stations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT" \
  -d '{
    "name": "Smoke Station 1",
    "groupLabel": "SOMA-WEST",
    "unitPriceTenthCents": 3500,
    "location": {
      "latitude": 37.7749,
      "longitude": -122.4194
    },
    "connectors": [
      {
        "type": "CCS",
        "maxPowerKw": 150
      },
      {
        "type": "TYPE_2",
        "maxPowerKw": 22
      }
    ]
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Create Station" 201 "$STATUS" "$BODY"
STATION_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Created Station ID: $STATION_ID"

# -------------------------------------------------------
# STEP 6: Get Station Details
# -------------------------------------------------------
echo ""
echo ">>> STEP 6: Get Station Details"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/stations/$STATION_ID" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Get Station" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 7: List Vendor Stations
# -------------------------------------------------------
echo ""
echo ">>> STEP 7: List Vendor Stations"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/stations" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "List Stations" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 8: Update Station
# -------------------------------------------------------
echo ""
echo ">>> STEP 8: Update Station (Name & Price)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/api/v1/stations/$STATION_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT" \
  -d '{
    "name": "Updated Smoke Station 1",
    "unitPriceTenthCents": 4000
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Update Station" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 9: Change Station Status
# -------------------------------------------------------
echo ""
echo ">>> STEP 9: Change Station Status to MAINTENANCE"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/stations/$STATION_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT" \
  -d '{
    "status": "MAINTENANCE"
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Change Station Status" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 10: Find Nearby Stations (Authenticated)
# -------------------------------------------------------
echo ""
echo ">>> STEP 10: Find Nearby Stations (radius 5km, status ALL)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/stations/nearby?lat=37.77&lng=-122.42&radiusKm=5&status=ALL" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Find Nearby Stations" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 11: Configure Vendor Markup (using SUPERADMIN_JWT)
# -------------------------------------------------------
echo ""
echo ">>> STEP 11: Set Vendor Markup to 5% (500 basis points)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/admin/vendors/$VENDOR_ID/markup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPERADMIN_JWT" \
  -d '{
    "markupBasisPoints": 500
  }')
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Set Vendor Markup" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 12: Get Vendor Markup (using VENDOR_ADMIN_JWT)
# -------------------------------------------------------
echo ""
echo ">>> STEP 12: Get Vendor Markup"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/admin/vendors/$VENDOR_ID/markup" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Get Vendor Markup" 200 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 13: Soft Delete Station
# -------------------------------------------------------
echo ""
echo ">>> STEP 13: Soft Delete Station"
RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/api/v1/stations/$STATION_ID" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT")
STATUS=$(echo "$RESPONSE" | tail -n 1)
if [ "$STATUS" -eq 244 ] || [ "$STATUS" -eq 204 ]; then
  pass "Delete Station (HTTP $STATUS)"
else
  fail "Delete Station — expected HTTP 204/244, got $STATUS"
fi

# -------------------------------------------------------
# Summary
# -------------------------------------------------------
echo ""
echo "=============================================="
echo "  Results  →  PASS: $PASS  |  FAIL: $FAIL"
echo "=============================================="
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
