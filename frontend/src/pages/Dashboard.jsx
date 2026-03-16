import { Link } from "react-router-dom"

function DashboardPage() {
    return (
        <div className="min-h-screen bg-gray-100 flex">
            <aside className="w-64 bg-white shadow-md p-6 flex flex-col">
                <h1 className="text-2x1 font-bold text-blue-800 mb-8">AthletePal</h1>
                <nav className="flex flex-col gap-4 text-gray-700">
                    <Link to="/dashboard" className="bg-blue-100 text-blue-800 px-3 py-2 rounded-lg font-medium">
                        Dashboard
                    </Link>

                    <Link to="/workout-log" className="hover:text-blue-700">
                        Workout Logs (Weights)
                    </Link>

                    <Link to="#" className="hover:text-blue-700">
                        Cardio
                    </Link>

                    <Link to="#" className="hover:text-blue-700">
                        Activity History
                    </Link>

                    <Link to="#" className="hover:text-blue-700">
                        Competitions
                    </Link>

                    <Link to="#" className="hover:text-blue-700">
                        Training Groups
                    </Link>

                    <Link to="#" className="hover:text-blue-700">
                        Statistics / Visualisation
                    </Link>

                    <Link to="#" className="hover:text-blue-700">
                        Profile
                    </Link>
                </nav>
            </aside>

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