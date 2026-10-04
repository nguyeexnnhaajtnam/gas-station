# Deploy demo (free)

| Part | Service | Free tier notes |
|---|---|---|
| Frontend (Angular) | Vercel Hobby | `*.vercel.app` domain, custom domain allowed |
| Backend (Spring Boot, Docker) | Render free web service | 512 MB RAM; spins down after 15 min without requests, ~1 min cold start |
| Postgres | Neon free | 0.5 GB; compute suspends when idle and resumes automatically |

The browser only talks to Vercel; `frontend/vercel.json` proxies `/api/*` to Render, so no CORS setup is needed in the browser.

## 1. Postgres on Neon

1. Create a project at neon.com, region **AWS Asia Pacific (Singapore)**.
2. From the connection details take host, database, user, password. Convert to JDBC form:
   `jdbc:postgresql://<host>/<database>?sslmode=require`

Flyway creates the schema on first backend start.

## 2. Backend on Render

1. render.com → **New → Blueprint** → pick this GitHub repo (uses `render.yaml`: Docker, free plan, Singapore, root `backend/`).
2. Fill the environment variables when prompted:

| Variable | Value |
|---|---|
| `DATABASE_URL` | JDBC URL from step 1 |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | Neon user / password |
| `SEENPRO_BASE_URL` | same value as local `.env.dev` |
| `CORS_ORIGINS` | `https://<your-app>.vercel.app` |

Optional: `AUTH_TOKEN_TTL` (default `12h`).

3. Wait for the deploy, then open `https://<service>.onrender.com/actuator/health` → `{"status":"UP"}`.

Secrets live only in Render's environment settings — never commit them.

## 3. Frontend on Vercel

1. In `frontend/vercel.json`, set the `/api` rewrite destination to the Render service URL; commit and push.
2. vercel.com → **Add New → Project** → import the repo, **Root Directory = `frontend`**. Build settings come from `vercel.json`.
3. Open `https://<your-app>.vercel.app` and log in.

## Before a demo

- Open the site ~1–2 minutes early: the backend may be asleep (first request wakes it).
- Every backend restart/sleep drops the in-memory SeenPro session and access tokens → log in again.
- All `/api/**` endpoints except login require a bearer token; `/actuator/health` stays public.
