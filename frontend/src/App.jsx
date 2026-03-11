import { BrowserRouter, Routes, Route } from "react-router-dom"
import LoginPage from "./pages/LoginPage"
import CreateAccountPage from "./pages/CreateAccountPage"
import DashboardPage from "./pages/Dashboard"

function App() {
  return (
    <BrowserRouter>

      <Routes>

        <Route path="/" element={<LoginPage />} />

        <Route path="/create-account" element={<CreateAccountPage />} />

        <Route path="dashboard" element={<DashboardPage />} />
      </Routes>

    </BrowserRouter>
  )
}

export default App