#!/usr/bin/env bash
set -e

BASE_URL="http://localhost:8080/api/v1/identity"
GREEN='\031[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "=========================================="
echo "Smoke Test: Identity Module Bolt 2"
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

if [ -n "${TOKEN}" ]; then
  echo "${GREEN}✓ Customer Authentication Successful! JWT Issued.${NC}"
else
  echo "${RED}✗ Customer Authentication Failed${NC}"
  exit 1
fi

echo "\n=========================================="
echo "${GREEN}All Bolt 2 Smoke Tests Passed!${NC}"
echo "=========================================="
