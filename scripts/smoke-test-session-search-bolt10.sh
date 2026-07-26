#!/usr/bin/env bash
# =============================================================================
# smoke-test-session-search-bolt10.sh
# Integration smoke test for Session Search Service (Bolt 010-session-search-1)
#
# Usage:
#   BASE_URL=http://localhost:8080 bash scripts/smoke-test-session-search-bolt10.sh
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
echo "  Session Full-Text Search Smoke Test"
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
ADMIN_JWT=$(echo "$BODY" | jq -r '.data.accessToken // empty')
info "Superadmin JWT obtained."

# -------------------------------------------------------
# STEP 2: Register & Login Customer User (for RBAC test)
# -------------------------------------------------------
echo ">>> STEP 2: Register & Login Customer User"
CUST_EMAIL="customer-search-test-${RANDOM}@evcharging.test"
REG_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/customers/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'"$CUST_EMAIL"'",
    "password": "CustomerPass1!",
    "fullName": "Search Test Customer",
    "phone": "+1555987654"
  }')
REG_BODY=$(echo "$REG_RESPONSE" | sed '$d')
REG_STATUS=$(echo "$REG_RESPONSE" | tail -n 1)
assert_http "Customer Registration" 201 "$REG_STATUS" "$REG_BODY"

CUST_LOGIN=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/identity/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'"$CUST_EMAIL"'",
    "password": "CustomerPass1!"
  }')
CUST_LOGIN_BODY=$(echo "$CUST_LOGIN" | sed '$d')
CUST_LOGIN_STATUS=$(echo "$CUST_LOGIN" | tail -n 1)
assert_http "Customer Login" 200 "$CUST_LOGIN_STATUS" "$CUST_LOGIN_BODY"
CUSTOMER_JWT=$(echo "$CUST_LOGIN_BODY" | jq -r '.data.accessToken // empty')
info "Customer JWT obtained."

# -------------------------------------------------------
# STEP 3: Perform Admin Session Search (No Query Parameter)
# -------------------------------------------------------
echo ">>> STEP 3: Admin Session Search - All Sessions"
SEARCH_ALL=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/sessions/search" \
  -H "Authorization: Bearer $ADMIN_JWT")
SEARCH_ALL_BODY=$(echo "$SEARCH_ALL" | sed '$d')
SEARCH_ALL_STATUS=$(echo "$SEARCH_ALL" | tail -n 1)
assert_http "Admin Search All Sessions" 200 "$SEARCH_ALL_STATUS" "$SEARCH_ALL_BODY"

# -------------------------------------------------------
# STEP 4: Perform Admin Session Search (With Query Term)
# -------------------------------------------------------
echo ">>> STEP 4: Admin Session Search - With Partial Term 'AUD'"
SEARCH_QUERY=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/sessions/search?q=AUD" \
  -H "Authorization: Bearer $ADMIN_JWT")
SEARCH_QUERY_BODY=$(echo "$SEARCH_QUERY" | sed '$d')
SEARCH_QUERY_STATUS=$(echo "$SEARCH_QUERY" | tail -n 1)
assert_http "Admin Search Query 'AUD'" 200 "$SEARCH_QUERY_STATUS" "$SEARCH_QUERY_BODY"

# -------------------------------------------------------
# STEP 5: Verify RBAC - Customer User Search (Expect 403 Forbidden)
# -------------------------------------------------------
echo ">>> STEP 5: Customer User Search (RBAC 403 Check)"
CUST_SEARCH=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/sessions/search?q=AUD" \
  -H "Authorization: Bearer $CUSTOMER_JWT")
CUST_SEARCH_BODY=$(echo "$CUST_SEARCH" | sed '$d')
CUST_SEARCH_STATUS=$(echo "$CUST_SEARCH" | tail -n 1)
assert_http "Customer Access Denied" 403 "$CUST_SEARCH_STATUS" "$CUST_SEARCH_BODY"

# -------------------------------------------------------
# SUMMARY REPORT
# -------------------------------------------------------
echo ""
echo "=============================================="
echo "  Smoke Test Results"
echo "  Passed: $PASS"
echo "  Failed: $FAIL"
echo "=============================================="

if [ "$FAIL" -gt 0 ]; then
  exit 1
else
  exit 0
fi
