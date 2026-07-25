#!/usr/bin/env bash
set -e

BASE_URL="http://localhost:8080/api/v1/identity"
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "=========================================="
echo "Smoke Test: Identity Module Bolt 3"
echo "RBAC & Credential Management"
echo "=========================================="

RAND_ID=$((RANDOM % 100000))
CUST_EMAIL="customer_${RAND_ID}@example.com"

echo "\n1. Registering Customer (${CUST_EMAIL})..."
CUST_REG_RES=$(curl -s -X POST "${BASE_URL}/auth/register-customer" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Customer ${RAND_ID}\",
    \"email\": \"${CUST_EMAIL}\",
    \"password\": \"CustomerPass123!\",
    \"phone\": \"+1555${RAND_ID}\"
  }")

echo "Response: ${CUST_REG_RES}"

ACC_NUM=$(echo "${CUST_REG_RES}" | grep -o '"accountNumber":"[^"]*' | cut -d'"' -f4)

if [ -n "${ACC_NUM}" ]; then
  echo "${GREEN}✓ Customer Registered Successfully! Account Number: ${ACC_NUM}${NC}"
else
  echo "${RED}✗ Customer Registration Failed${NC}"
  exit 1
fi

echo "\n2. Logging in as Customer (${CUST_EMAIL})..."
LOGIN_RES=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${CUST_EMAIL}\",
    \"password\": \"CustomerPass123!\"
  }")

echo "Response: ${LOGIN_RES}"

TOKEN=$(echo "${LOGIN_RES}" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo "${LOGIN_RES}" | grep -o '"refreshToken":"[^"]*' | cut -d'"' -f4)
MUST_CHANGE_PWD=$(echo "${LOGIN_RES}" | grep -o '"mustChangePassword":[^,}]*' | cut -d':' -f2)

if [ -n "${TOKEN}" ]; then
  echo "${GREEN}✓ Customer Authentication Successful! JWT Issued.${NC}"
else
  echo "${RED}✗ Customer Authentication Failed${NC}"
  exit 1
fi

if [ -n "${REFRESH_TOKEN}" ]; then
  echo "${GREEN}✓ Refresh Token Issued.${NC}"
else
  echo "${RED}✗ Refresh Token Not Issued${NC}"
  exit 1
fi

echo "\n3. Testing Token Refresh..."
REFRESH_RES=$(curl -s -X POST "${BASE_URL}/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"${REFRESH_TOKEN}\"
  }")

echo "Response: ${REFRESH_RES}"

NEW_TOKEN=$(echo "${REFRESH_RES}" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
NEW_REFRESH_TOKEN=$(echo "${REFRESH_RES}" | grep -o '"refreshToken":"[^"]*' | cut -d'"' -f4)

if [ -n "${NEW_TOKEN}" ]; then
  echo "${GREEN}✓ Token Refresh Successful! New JWT Issued.${NC}"
else
  echo "${RED}✗ Token Refresh Failed${NC}"
  exit 1
fi

if [ -n "${NEW_REFRESH_TOKEN}" ] && [ "${NEW_REFRESH_TOKEN}" != "${REFRESH_TOKEN}" ]; then
  echo "${GREEN}✓ Refresh Token Rotated (new token issued).${NC}"
else
  echo "${RED}✗ Refresh Token Rotation Failed${NC}"
  exit 1
fi

echo "\n4. Testing Logout..."
LOGOUT_RES=$(curl -s -X POST "${BASE_URL}/auth/logout" \
  -H "Authorization: Bearer ${NEW_TOKEN}" \
  -w "%{http_code}")

if echo "${LOGOUT_RES}" | grep -q "204"; then
  echo "${GREEN}✓ Logout Successful (204 No Content).${NC}"
else
  echo "${RED}✗ Logout Failed${NC}"
  echo "Response: ${LOGOUT_RES}"
  exit 1
fi

echo "\n5. Testing Reuse Detection (using old refresh token should fail)..."
REUSE_RES=$(curl -s -X POST "${BASE_URL}/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"${REFRESH_TOKEN}\"
  }")

echo "Response: ${REUSE_RES}"

if echo "${REUSE_RES}" | grep -qi "reuse\|revoked\|invalid"; then
  echo "${GREEN}✓ Reuse Detection Working! Old token rejected.${NC}"
else
  echo "${RED}✗ Reuse Detection May Not Be Working${NC}"
  exit 1
fi

echo "\n6. Testing Change Password..."
# Login again to get a fresh token
LOGIN_RES2=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${CUST_EMAIL}\",
    \"password\": \"CustomerPass123!\"
  }")

TOKEN2=$(echo "${LOGIN_RES2}" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

CHANGE_PWD_RES=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "${BASE_URL}/users/me/password" \
  -H "Authorization: Bearer ${TOKEN2}" \
  -H "Content-Type: application/json" \
  -d "{
    \"currentPassword\": \"CustomerPass123!\",
    \"newPassword\": \"NewCustomerPass456!\"
  }")

HTTP_CODE=$(echo "${CHANGE_PWD_RES}" | grep "HTTP_CODE:" | cut -d':' -f2)
RESPONSE_BODY=$(echo "${CHANGE_PWD_RES}" | grep -v "HTTP_CODE:")

echo "Response: ${RESPONSE_BODY}"
echo "HTTP Status: ${HTTP_CODE}"

if [ "${HTTP_CODE}" = "200" ]; then
  echo "${GREEN}✓ Password Changed Successfully.${NC}"
else
  echo "${RED}✗ Password Change Failed${NC}"
  exit 1
fi

echo "\n7. Verifying New Password Works..."
LOGIN_RES3=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${CUST_EMAIL}\",
    \"password\": \"NewCustomerPass456!\"
  }")

TOKEN3=$(echo "${LOGIN_RES3}" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -n "${TOKEN3}" ]; then
  echo "${GREEN}✓ New Password Verified Successfully.${NC}"
else
  echo "${RED}✗ New Password Verification Failed${NC}"
  exit 1
fi

echo "\n=========================================="
echo "${GREEN}All Bolt 3 Smoke Tests Passed!${NC}"
echo "=========================================="
echo "\nFeatures Tested:"
echo "  ✓ Customer Registration"
echo "  ✓ Login with JWT + Refresh Token"
echo "  ✓ Token Refresh with Rotation"
echo "  ✓ Logout"
echo "  ✓ Reuse Detection"
echo "  ✓ Change Password"
echo "  ✓ Password Verification"
echo "=========================================="
