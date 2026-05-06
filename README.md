# AthletePal
AthletePal is a fitness tracking platform designed to help users achieve their goals in the gym, in competitions, or out in the open through outdoor activities. AthletePal allows all users, whether casual or professional, to keep track of their progress, schedule workouts, log cardio routes, or even join or create group training sessions - helping users strive for their next goal. Whilst casual users will find joy in tracking their progress from one exercise to another or seeing how their jogging times improve, professional athletes and other serious fitness enthusiasts will be able to view their statistics in competitions and training.

## Tech Stack
- Frontend: JSX, React, Vite, Tailwind
- Backend: Kotlin, Ktor
- Database: SQLite3

## Running the Project
1. Create two seperate terminals in a single codespace
2. Forward a port under the port number 3000, and set to public visibility
3. Copy the forwarded address and set API_BASE_URL (in '/frontend/src/config.js') to the forwarded address
4. In one terminal navigate to '/backend/ktor-sample/' and run './gradlew build' then './gradlew run'
5. In the other terminal navigate to '/frontend/' folder and run 'npm install', then 'npm run dev'
6. Ctrl+Left Click the localhost link given in the frontend terminal after 'npm run dev', directing you to the webapp
