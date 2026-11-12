# AthletePal

A full-stack fitness tracking web app for logging workouts, planning routines, tracking cardio routes on a map, recording competition results and training in groups.

Built as a 4-person group project for a second-year software engineering module at the University of Leeds (Feb–May 2026).

## Features

- **Workout logging**: create reusable routines, start empty or preloaded workouts, log sets/reps/weight, and schedule workouts for future dates
- **Cardio and routes**: log running, cycling and swimming sessions, draw routes on an interactive map, and share routes publicly
- **Competitions**: track upcoming, today's and completed competitions with finishing position and result time
- **Statistics**: workout streaks, hours trained, weekly progression and personal bests
- **Training groups**: create and join groups and schedule group sessions
- **Accounts**: secure sign-up and login with salted password hashing, plus profile editing and password changes

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React, Vite, Tailwind CSS, React Router |
| Maps | Leaflet, react-leaflet, react-leaflet-draw, OpenStreetMap |
| Backend | Kotlin, Ktor (REST API, JSON) |
| Database | SQLite |

## My Contributions

I worked on the frontend alongside one other developer, while two teammates built the backend.

- **Workout Logs, New Routine and Start Workout pages**: routine creation and deletion, preloaded workouts, and the full **Schedule Workout** feature (future-dated workouts that appear on the day they're due and are removed once completed)
- **Cardio page and `RouteMap` component**: a reusable map component with draw and view modes, **automatic route distance calculation using the Haversine formula** on GeoJSON coordinates, route selection logic, public/private routes, and a fix for routes failing to save silently
- **Competitions page**: split into Today / Upcoming / Completed, with results only editable for past or same-day events, and inline result editing with mm:ss formatting
- **Profile page**: profile editing and change-password flow
- **Backend integration**: connected my pages to the Ktor API, centralised the API base URL in `config.js`, worked with the backend team on the login response and the `isRoutine` field, and resolved CORS issues
- **Accessibility**: label/input associations, ARIA roles, fieldset/legend grouping and contrast fixes; brought pages to a **100 Lighthouse accessibility score** and 0 WAVE errors
- **Design**: produced the first set of wireframes and applied UX test feedback (clearer required/optional fields, visible labels, longer confirmation messages)

## Running Locally (GitHub Codespaces)

1. Open two terminals in a Codespace
2. In the **Ports** tab, forward port `3000` and set its visibility to **Public**
3. Copy the forwarded address into `API_BASE_URL` in `frontend/src/config.js`
4. Backend terminal:
   ```bash
   cd backend/ktor-sample
   ./gradlew build
   ./gradlew run
   ```
5. Frontend terminal:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
6. Open the localhost link shown in the frontend terminal

If you get an npm version error, run `nvm install 22 && nvm use 22`, delete `node_modules` and `package-lock.json`, and then reinstall.

## Team

- Ben Satterly: backend API endpoints
- Nathaniel Heneghan: database design and services
- Kunaal Mulani: frontend (dashboard, statistics, activity history, training groups, auth pages)
- Adam Ashaq: frontend (see above)

Originally developed in a private team repository; this is a copy for portfolio purposes.
