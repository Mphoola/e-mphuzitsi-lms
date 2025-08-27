#!/bin/bash

echo "🚀 Testing E-Empuzitsi Authentication Endpoints"
echo "================================================"

BASE_URL="http://localhost:8080"

echo ""
echo "1. Testing Health Endpoint..."
curl -X GET "$BASE_URL/api/health" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n" | jq .

echo ""
echo "2. Testing Health Check Endpoint..."
curl -X GET "$BASE_URL/api/health/check" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n" | jq .

echo ""
echo "3. Testing User Registration..."
curl -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }' \
  -w "\nStatus: %{http_code}\n" | jq .

echo ""
echo "4. Testing User Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | jq .

# Extract JWT token for subsequent requests
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')

if [ "$JWT_TOKEN" != "null" ] && [ "$JWT_TOKEN" != "" ]; then
  echo ""
  echo "5. Testing Authenticated Endpoint (Get User Profile)..."
  curl -X GET "$BASE_URL/api/auth/me" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -w "\nStatus: %{http_code}\n" | jq .

  echo ""
  echo "6. Testing Test Endpoint (Admin)..."
  curl -X GET "$BASE_URL/api/test/admin" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -w "\nStatus: %{http_code}\n" | jq .

  echo ""
  echo "7. Testing Test Endpoint (Student)..."
  curl -X GET "$BASE_URL/api/test/student" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -w "\nStatus: %{http_code}\n" | jq .
else
  echo "❌ Login failed - no token received"
fi

echo ""
echo "✅ Endpoint testing completed!"
