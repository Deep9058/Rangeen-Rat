# Bareilly Raas Dandiya Mahotsav — Vercel Full-Stack Website

This project contains:

- `frontend/` — responsive event website and poster
- `backend/` — Spring Boot 3.5.5 + Razorpay API
- `vercel.json` — Vercel Services configuration for frontend + Spring Boot backend
- `backend/Dockerfile.vercel` — builds and runs Spring Boot on Vercel
- `frontend/Dockerfile.vercel` — serves the static frontend through nginx on Vercel

## Event

- Bareilly Raas Dandiya Mahotsav
- 18 October 2026, Sunday
- 6:00 PM – 11:00 PM
- Trivatinath Mandir Ground, near GRM School, Bareilly
- Stall booking / passes: 9143692436

## Deploy the whole project to Vercel

Vercel now supports Vercel Services and containerized HTTP servers, including Spring Boot. This repository is structured as two Vercel services on one domain:

- `/` → frontend service
- `/api/*` → Spring Boot backend service

### 1. Push this folder to GitHub

Push the complete `rangeen-raat-fullstack` folder to a GitHub repository.

### 2. Import the repository in Vercel

In Vercel, choose **Add New → Project**, import the GitHub repository, and deploy it.

The root directory must remain the repository root so Vercel can read `vercel.json`.

### 3. Add production environment variables

In **Vercel Project → Settings → Environment Variables**, add these variables for the backend service:

```text
RAZORPAY_KEY_ID=rzp_live_xxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxx
ADMIN_API_TOKEN=<long-random-admin-token>
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/rangeenraat?useSSL=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<db-user>
SPRING_DATASOURCE_PASSWORD=<db-password>
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

Do not put `RAZORPAY_KEY_SECRET` in `frontend/index.html` or any browser JavaScript.

### 4. Use a persistent external database

The included H2 fallback is only for local/demo use. Vercel compute is ephemeral, so a file-based H2 database should not be treated as permanent booking storage.

For production, use an external MySQL database and set the `SPRING_DATASOURCE_*` variables above. Make sure the database accepts connections from your deployed backend.

### 5. Test after deployment

Open:

```text
https://YOUR-DOMAIN.vercel.app/
https://YOUR-DOMAIN.vercel.app/api/health
https://YOUR-DOMAIN.vercel.app/api/passes
```

Expected health response:

```json
{"status":"UP","service":"rangeen-raat-backend"}
```

### 6. Test Razorpay

Use Razorpay test keys first. The frontend calls:

```text
POST /api/payment/create-order
POST /api/payment/verify
```

The backend calculates the pass price, creates the Razorpay order, and verifies the Razorpay signature before marking a booking as `PAID`.

Switch to live Razorpay keys only after the complete test payment flow works.

## Local development

### Backend

Windows:

```text
run-backend-windows.bat
```

Or from `backend/`:

```bash
mvn spring-boot:run
```

The local backend runs on `http://localhost:8080`.

### Frontend

The frontend uses relative `/api/...` URLs, so it is intended to run behind the same domain in production. For local standalone testing, serve `frontend/` with a small static server and point API requests to a local backend if required.

## API endpoints

Public:

- `GET /api/health`
- `GET /api/passes`
- `POST /api/auth/login` — validates a 10-digit Indian mobile number and uses it as the customer ID
- `GET /api/bookings?phone=<10-digit-phone>` — returns bookings linked to that phone number
- `POST /api/payment/create-order`
- `POST /api/payment/verify`

Admin:

- `GET /api/admin/bookings`
- `GET /api/admin/bookings/{reference}`

Admin requests must include:

```text
X-Admin-Token: <ADMIN_API_TOKEN>
```

The admin API is disabled until `ADMIN_API_TOKEN` is configured.

## Customer login and booking history

The website now has a **Login** flow using the customer's 10-digit mobile number. The same number is stored on every booking and is used as the customer ID, so all bookings made with that number appear in **My Bookings**. The phone number is remembered in the browser so returning users can immediately see their previous tickets.

> **Security note:** this implementation is phone-number identification, not password/OTP authentication. Before using it to expose real customer booking history publicly, add OTP verification (or another authenticated session) through an SMS/authentication provider.

## Important production notes

1. Use Razorpay live keys only in Vercel environment variables.
2. Never expose the Razorpay secret key in frontend code.
3. Use a persistent external database for bookings.
4. Configure an HTTPS Razorpay webhook separately for additional payment-state reliability.
5. Keep `ADMIN_API_TOKEN` secret and use a long random value.
6. Review Vercel logs and Razorpay dashboard after the first production test booking.
