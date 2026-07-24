# Swagger UI & OpenAPI 3 Interactive Guide

## 1. Title & Executive Summary
**Interactive API Documentation, Schema Discovery, & Authorization Testing**

This document details the integration of Springdoc OpenAPI 3 UI in the UPI Simulator, explaining interactive testing flows, bearer token authorization, and schema inspection.

---

## 2. Accessing Swagger UI
When running locally or via Docker Compose, access the interactive portal at:
- **Interactive UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`

---

## 3. Interactive Authorization Flow

1. Open `http://localhost:8080/swagger-ui.html`.
2. Expand **Authentication APIs** and execute `POST /api/users/register` to onboard a user.
3. Execute `POST /api/auth/login` to receive a JWT bearer token.
4. Copy the `token` string from the HTTP 200 JSON response body.
5. Click the green **Authorize** button at the top right of the Swagger UI interface.
6. Enter `Bearer <your_token_string>` into the value field and click **Authorize**.
7. All subsequent API calls executed within Swagger UI will automatically attach the authorization header.

---

## 4. Tag Groups Overview
- **Authentication APIs**: User onboarding and token emission.
- **Bank Account APIs**: Account linkage and balance management.
- **UPI APIs**: Virtual Payment Address handle creation.
- **Transaction APIs**: Money transfer, history, and summaries.
- **Payment Request APIs**: Collect requests lifecycle.
