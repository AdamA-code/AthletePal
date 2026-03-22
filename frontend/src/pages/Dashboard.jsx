import { Link } from "react-router-dom"
import Sidebar from "../components/Sidebar"

function DashboardPage() {
    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h2 className="text-3x1 font-bold text-gray-800 mb-6">Dashboard</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 x1:grid-cols-3 gap-6">
                    <div className="bg-white rounded-x1 shadow p-6">
                        <h3 className="text-x1 font-semibold mb-3">Today's Workouts</h3>
                        <p className="text-gray-600">
                            Your planned workouts will appear here.
                        </p>
                    </div>

                    <div className="bg-white rounded-x1 shadow p-6">
                        <h3 className="text-x1 font-semibold mb-3">Recent Activity</h3>
                        <p className="text-gray-600">
                            Your recent activity will appear here.
                        </p>
                    </div>

                    <div className="bg-white rounded-x1 shadow p-6">
                        <h3 className="text-x1 font-semibold mb-3">Recent Activity</h3>
                        <p className="text-gray-600">
                            Your recent activity will appear here.
                        </p>
                    </div>

                    <div className="bg-white rounded-x1 shadow p-6">
                        <h3 className="text-x1 font-semibold mb-3">
                            Upcoming Completions / Cancels
                        </h3>
                        <p className="text-gray-600">
                            Upcoming updates will appear here.
                        </p>
                    </div>
                </div>
            </main>
        </div>
    )
}

export default DashboardPage