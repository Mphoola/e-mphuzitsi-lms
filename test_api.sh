#!/bin/bash

echo "=== Testing API with both JSON and Form Data ==="

echo "1. Testing JSON registration..."
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName": "John", "lastName": "Doe", "email": "john.doe@example.com", "password": "password123"}' \
  -s | jq .

echo -e "\n2. Testing Form Data registration..."
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "firstName=Jane&lastName=Smith&email=jane.smith@example.com&password=password456" \
  -s | jq .

echo -e "\n3. Testing JSON login..."
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@example.com", "password": "password123"}' \
  -s | jq .

echo -e "\n4. Testing Form Data login..."
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=jane.smith@example.com&password=password456" \
  -s | jq .

echo -e "\n5. Testing 404 error (invalid route)..."
curl -X GET http://localhost:8080/api/nonexistent \
  -s | jq .

echo -e "\n6. Testing 405 error (method not allowed)..."
curl -X DELETE http://localhost:8080/api/auth/register \
  -s | jq .

echo -e "\n7. Testing 415 error (unsupported media type)..."
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?><user><firstName>Test</firstName></user>' \
  -s | jq .

echo -e "\n8. Testing 400 error (malformed JSON)..."
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Test", invalid json}' \
  -s | jq .

echo -e "\n=== API Testing Complete ==="
