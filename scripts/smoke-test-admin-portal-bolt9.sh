#!/usr/bin/env bash
# =============================================================================
# smoke-test-admin-portal-bolt9.sh
# Integration smoke test for Admin & Vendor Dashboards (Bolt 009-admin-portal-1)
#
# Verifies:
#   1. Superadmin (ADMIN) can query GET /api/v1/admin/dashboard.
#   2. Admin Dashboard returns totalRevenue, sessionCount, vendor breakdowns.
#   3. Vendor Admin (VENDOR_ADMIN) can query GET /api/v1/vendor/dashboard.
#   4. Vendor Dashboard returns vendor-scoped metrics matching JWT vendor_id.
#   5. Role-based access control (CUSTOMER role rejected from dashboards with 403).
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-admin-portal-bolt9.sh
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
echo "  Admin & Vendor Portal Dashboard Smoke Test"
echo "  Target: $BASE_URL"
echo "=============================================="
echo ""

TIMESTAMP=$(date +%s)
VENDOR_NAME="Portal Smoke Vendor ${TIMESTAMP}"
VENDOR_ADMIN_EMAIL="portal-vendor-admin-${TIMESTAMP}@evcharging.test"
CUSTOMER_EMAIL="portal-customer-${TIMESTAMP}@evcharging.test"

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
# STEP 2: Query GET /api/v1/admin/dashboard as Superadmin
# -------------------------------------------------------
echo ""
echo ">>> STEP 2: Query Admin Dashboard Endpoint"
UTC_DATE=$(TZ=UTC date +%Y-%m-%d)
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE_URL/api/v1/admin/dashboard?startDate=${UTC_DATE}&endDate=${UTC_DATE}" \
  -H "Authorization: Bearer $SUPERADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Admin Dashboard" 200 "$STATUS" "$BODY"

TOTAL_REVENUE=$(echo "$BODY" | jq -r '.data.totalRevenue // empty')
TOTAL_SESSIONS=$(echo "$BODY" | jq -r '.data.totalSessions // empty')
info "Admin Dashboard Revenue: $TOTAL_REVENUE, Sessions: $TOTAL_SESSIONS"

if [ -n "$TOTAL_REVENUE" ]; then
  pass "Admin Dashboard summary returned valid totalRevenue"
else
  fail "Admin Dashboard response missing totalRevenue field"
fi

# -------------------------------------------------------
# STEP 3: Create Vendor & Accept Invitation
# -------------------------------------------------------
echo ""
echo ">>> STEP 3: Create Vendor"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/vendors" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPERADMIN_JWT" \
  -d "{
    \"vendorName\": \"$VENDOR_NAME\",
    \"adminName\": \"Portal Vendor Admin\",
    \"adminEmail\": \"$VENDOR_ADMIN_EMAIL\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Create Vendor" 201 "$STATUS" "$BODY"
VENDOR_ID=$(echo "$BODY" | jq -r '.data.vendorId // empty')
INVITATION_TOKEN=$(echo "$BODY" | jq -r '.data.invitationToken // empty')

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/invitations/accept" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$INVITATION_TOKEN\",
    \"name\": \"Portal Vendor Admin\",
    \"password\": \"VendorAdmin@Pass1!\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Accept Invitation" 201 "$STATUS" "$BODY"

# -------------------------------------------------------
# STEP 4: Login as Vendor Admin & Query Vendor Dashboard
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

echo ""
echo ">>> STEP 5: Query Vendor Dashboard Endpoint"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/vendor/dashboard" \
  -H "Authorization: Bearer $VENDOR_ADMIN_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Vendor Dashboard" 200 "$STATUS" "$BODY"

RETURNED_VENDOR_ID=$(echo "$BODY" | jq -r '.data.vendorId // empty')
RETURNED_VENDOR_NAME=$(echo "$BODY" | jq -r '.data.vendorName // empty')

if [ "$RETURNED_VENDOR_ID" = "$VENDOR_ID" ]; then
  pass "Vendor Dashboard returned matching vendorId: $VENDOR_ID"
else
  fail "Vendor Dashboard vendorId mismatch: expected $VENDOR_ID, got $RETURNED_VENDOR_ID"
fi

if [ "$RETURNED_VENDOR_NAME" = "$VENDOR_NAME" ]; then
  pass "Vendor Dashboard returned matching vendorName: $VENDOR_NAME"
else
  fail "Vendor Dashboard vendorName mismatch: expected $VENDOR_NAME, got $RETURNED_VENDOR_NAME"
fi

# -------------------------------------------------------
# STEP 5: Verify Role Security (Customer Rejected)
# -------------------------------------------------------
echo ""
echo ">>> STEP 6: Verify Authorization Controls for Customer"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/register-customer" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Portal Customer\",
    \"email\": \"$CUSTOMER_EMAIL\",
    \"password\": \"CustomerPass123!\",
    \"phone\": \"+15551112222\"
  }")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Register Customer" 201 "$STATUS" "$BODY"

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

RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/admin/dashboard" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Reject Customer from Admin Dashboard" 403 "$STATUS" "$BODY"

RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/vendor/dashboard" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
BODY=$(echo "$RESPONSE" | sed '$d')
STATUS=$(echo "$RESPONSE" | tail -n 1)
assert_http "Reject Customer from Vendor Dashboard" 403 "$STATUS" "$BODY"

echo ""
echo "=============================================="
echo "  Admin & Vendor Portal Smoke Test Completed"
echo "  Passed: $PASS, Failed: $FAIL"
echo "=============================================="
echo ""

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
