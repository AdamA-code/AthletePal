
import Sidebar from "../components/Sidebar"
import { useEffect, useState } from "react"
import API_BASE_URL from "../config"

function DashboardPage() {
    const [todaysWorkouts, setTodaysWorkouts] = useState([])
    const [recentActivity, setRecentActivity] = useState(null)
    const [upcomingCompetitions, setUpcomingCompetitions] = useState([])
    const [error, setError] = useState("")

    useEffect(() => {
        const email = localStorage.getItem("email")
        if (!email) {
            setError("No email found. Please log in again.")
            return
        }
        { /* Fetch dashboard data from backend */ }
        const fetchDashboardData = async () => {
            try {
                const headers = {
                    "Content-Type": "application/json",
                }

                const [workoutsResponse, activityResponse, competitionsResponse] = await Promise.all([
                    fetch(`${API_BASE_URL}/dashboard/todays-workouts`, {
                        method: "POST",
                        headers,
                        body: JSON.stringify({ email })
                    }),
                    fetch(`${API_BASE_URL}/dashboard/recent-activity`, {
                        method: "POST",
                        headers,
                        body: JSON.stringify({ email })
                    }),
                    fetch(`${API_BASE_URL}/dashboard/upcoming-competitions`, {
                        method: "POST",
                        headers,
                        body: JSON.stringify({ email })
                    })
                ])
                const workoutsData = await workoutsResponse.json()
                const activityData = await activityResponse.json()
                const competitionsData = await competitionsResponse.json()
                
                setTodaysWorkouts(Array.isArray(workoutsData.message) ? workoutsData.message : [])
                setRecentActivity(activityData.message || null)
                setUpcomingCompetitions(Array.isArray(competitionsData.message) ? competitionsData.message : [])
            } catch (err) {
                console.error("Error fetching dashboard data:", err)
                setError("Failed to load dashboard data. Please try again later.")
            }
        }
        fetchDashboardData()
    }, [])

    {/* Main dashboard layout with sections for today's workouts, recent activity, and upcoming competitions */}
    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h2 className="text-3x1 font-bold text-gray-800 mb-6">Dashboard</h2>

                {error && (
                    <p className="text-red-600 text-sm mb-4">
                        {error}
                    </p>
                )}
                <div className="grid grid-cols-1 md:grid-cols-2 x1:grid-cols-3 gap-6">
                    <div className="bg-white rounded-x1 shadow p-6">
                        <h3 className="text-x1 font-semibold mb-3">Today's Workouts</h3>

                        {todaysWorkouts.length > 0 ? (
                            <div className="space-y-2">
                                {todaysWorkouts.map((workout, i) => (
                                    <p key={i} className="text-gray-700">
                                        {workout.name || workout.type || "Workout"}
                                    </p>
                                ))}
                            </div>
                        ) : (
                            <p className="text-gray-600">
                                No workouts scheduled for today.
                            </p>
                        )}
                    </div>

                    <div className="bg-white rounded-x1 shadow p-6">
                        <h3 className="text-x1 font-semibold mb-3">Recent Activity</h3>

                        {recentActivity ? (
                            <div className="space-y-2 text-gray-700">
                                <p>
                                    <span className="font-medium">Workouts:</span>{" "}
                                    {recentActivity.workouts?.length ?? 0}
                                </p>
                                <p>
                                    <span className="font-medium">Competitions:</span>{" "}
                                    {recentActivity.competitions?.length ?? 0}
                                </p>
                                <p>
                                    <span className="font-medium">Routes:</span>{" "}
                                    {recentActivity.routes?.length ?? 0}
                                </p>
                            </div>

                        ) : (
                            <p className="text-gray-600">
                                No recent activity to display.
                            </p>
                        )}
                    </div>

                    <div className="bg-white rounded-x1 shadow p-6">
                        <h3 className="text-x1 font-semibold mb-3">
                            Upcoming Competitions
                        </h3>

                        {upcomingCompetitions.length > 0 ? (
                            <div className="space-y-2">
                                {upcomingCompetitions.map((comp, i) => (
                                    <p key={i} className="text-gray-700">
                                        {comp.name || comp.title || "Competition"}
                                    </p>
                                ))}
                            </div>
                        ) : (
                            <p className="text-gray-600">
                                No upcoming competitions.
                            </p>
                        )}
                    </div>
                </div>
            </main>
        </div>
    )
}

export default DashboardPage