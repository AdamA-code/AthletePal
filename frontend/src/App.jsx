import { BrowserRouter, Routes, Route } from "react-router-dom"
import LoginPage from "./pages/LoginPage"
import CreateAccountPage from "./pages/CreateAccountPage"
import DashboardPage from "./pages/Dashboard"
import WorkoutLogPage from "./pages/WorkoutLogPage"
import CardioPage from "./pages/CardioPage"

function App() {
  return (
    <BrowserRouter>

      <Routes>

        <Route path="/" element={<LoginPage />} />

        <Route path="/create-account" element={<CreateAccountPage />} />

        <Route path="dashboard" element={<DashboardPage />} />

        <Route path="/workout-log" element={<WorkoutLogPage />} />

        <Route path="/cardio" element={<CardioPage />} />
      </Routes>

    </BrowserRouter>
  )
}

export default App