#!/usr/bin/env bash
# =============================================================================
# smoke-test-identity-bolt1.sh
# Integration smoke test for Identity & Access Service (Bolt 001-identity-service-1)
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-identity-bolt1.sh
#
# Requirements:
#   - curl, jq
#   - Application running and accessible at BASE_URL
#   - Flyway V2 migration seeded superadmin@evcharging.test / SuperAdmin@Pass1!
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
echo "  Identity Service Smoke Test"
echo "  Target: $BASE_URL"
echo "=============================================="
echo ""

# -------------------------------------------------------
# STEP 1: Login as Seeded Superadmin
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

# Generate unique email per test run so rerun is idempotent
TIMESTAMP=$(date +%s)
NEW_ADMIN_EMAIL="smoke-admin-${TIMESTAMP}@evcharging.test"

# -------------------------------------------------------
# STEP 2: Register New Admin (Protected — requires SUPERADMIN_JWT)
# -------------------------------------------------------
echo ""
echo ">>> STEP 2: Register New Admin (using Superadmin JWT)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/register-admin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPERADMIN_JWT" \
  -d "{
    \"name\": \"Smoke Admin\",
    \"email\": \"$NEW_ADMIN_EMAIL\",
    \"password\": \"SmokeAdmin@Pass1!\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Admin" 201 "$STATUS" "$BODY"
NEW_ADMIN_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Registered New Admin ID: $NEW_ADMIN_ID ($NEW_ADMIN_EMAIL)"

# -------------------------------------------------------
# STEP 3: Attempt Duplicate Admin Registration → expect 409
# -------------------------------------------------------
echo ""
echo ">>> STEP 3: Duplicate Admin Registration → expect 409"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/register-admin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPERADMIN_JWT" \
  -d "{
    \"name\": \"Dup Admin\",
    \"email\": \"$NEW_ADMIN_EMAIL\",
    \"password\": \"SmokeAdmin@Pass1!\"
  }")
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Duplicate Admin Registration" 409 "$STATUS" ""

# -------------------------------------------------------
# STEP 4: Login as Newly Registered Admin → get NEW_ADMIN_JWT
# -------------------------------------------------------
echo ""
echo ">>> STEP 4: Login as Newly Created Admin"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$NEW_ADMIN_EMAIL\",
    \"password\": \"SmokeAdmin@Pass1!\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "New Admin Login" 200 "$STATUS" "$BODY"
NEW_ADMIN_JWT=$(echo "$BODY" | jq -r '.data.accessToken // empty')
info "New Admin JWT obtained: ${NEW_ADMIN_JWT:0:30}..."

if [ -z "$NEW_ADMIN_JWT" ]; then
  fail "No New Admin JWT obtained — stopping test."
  exit 1
fi

# -------------------------------------------------------
# STEP 5: Create Vendor (using NEW_ADMIN_JWT)
# -------------------------------------------------------
echo ""
echo ">>> STEP 5: Create Vendor (using Newly Created Admin's JWT)"
VENDOR_NAME="Smoke Vendor ${TIMESTAMP}"
VENDOR_ADMIN_EMAIL="smoke-vendor-admin-${TIMESTAMP}@evcharging.test"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/vendors" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $NEW_ADMIN_JWT" \
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
# STEP 6: Accept Vendor Invitation (Public)
# -------------------------------------------------------
echo ""
echo ">>> STEP 6: Accept Vendor Invitation"
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
VENDOR_ADMIN_USER_ID=$(echo "$BODY" | jq -r '.data.id // empty')
info "Vendor Admin User ID: $VENDOR_ADMIN_USER_ID"
info "Role: $(echo "$BODY" | jq -r '.data.role // empty')"

# -------------------------------------------------------
# STEP 7: Duplicate Invitation Acceptance → expect 409
# -------------------------------------------------------
echo ""
echo ">>> STEP 7: Duplicate Invitation Acceptance → expect 409"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/invitations/accept" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$INVITATION_TOKEN\",
    \"name\": \"Duplicate\",
    \"password\": \"AnotherPass1!\"
  }")
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Duplicate Invitation Acceptance" 409 "$STATUS" ""

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
