import { BrowserRouter, Routes, Route } from "react-router-dom"
import LoginPage from "./pages/LoginPage"
import CreateAccountPage from "./pages/CreateAccountPage"
import DashboardPage from "./pages/Dashboard"
import WorkoutLogPage from "./pages/WorkoutLogPage"
import CardioPage from "./pages/CardioPage"
import ActivityHistoryPage from "./pages/ActivityHistory"
import ProfilePage from "./pages/Profile"
import StatisticsPage from "./pages/StatisticsPage"

function App() {
  return (
    <BrowserRouter>

      <Routes>

        <Route path="/" element={<LoginPage />} />

        <Route path="/create-account" element={<CreateAccountPage />} />

        <Route path="dashboard" element={<DashboardPage />} />

        <Route path="/workout-log" element={<WorkoutLogPage />} />

        <Route path="/cardio" element={<CardioPage />} />

        <Route path="/activity-history" element={<ActivityHistoryPage />} />

        <Route path="/profile" element={<ProfilePage />} />

        <Route path="/statistics" element={<StatisticsPage />} />

        {/* Add more routes as needed */}
      </Routes>

    </BrowserRouter>
  )
}

export default App