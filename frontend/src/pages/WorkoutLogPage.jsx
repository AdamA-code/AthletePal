import { Link } from "react-router-dom"

function WorkoutLogPage() {
    return (
        <div className="min-h-screen bg-gray-100 flex">

            {/* Sidebar */}
            <aside className="w-64 bg-white shadow-md p-6 flex flex-col">
                <h1 className="text-2xl font-bold text-blue-800 mb-8">AthletePal</h1>

                <nav className="flex flex-col gap-4 text-gray-700">

                    <Link to="/dashboard" className="hover:text-blue-700">
                        Dashboard
                    </Link>

                    <Link to="/workout-log" className="bg-blue-100 text-blue-800 px-3 py-2 rounded-lg font-medium">
                        Workout Logs (Weights)
                    </Link>

                    <Link to="/cardio" className="hover:text-blue-700">
                        Cardio
                    </Link>

                    <Link to="/activity-history" className="hover:text-blue-700">
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


            {/* Main Content */}
            <main className="flex-1 p-8">

                <h2 className="text-3xl font-bold mb-6">Workouts</h2>

                {/* Start Empty Workout */}
                <button className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 mb-8">
                    + Start Empty Workout
                </button>


                {/* Routines Section */}
                <div className="mb-8">

                    <h3 className="text-xl font-semibold mb-3">
                        Routines
                    </h3>

                    <button className="border border-blue-600 text-blue-600 px-5 py-2 rounded-lg hover:bg-blue-50">
                        New Routine
                    </button>

                </div>


                {/* My Routines (Empty for now) */}
                <div>

                    <h3 className="text-xl font-semibold mb-4">
                        My Routines
                    </h3>

                    <p className="text-gray-500">
                        No routines created yet.
                    </p>

                </div>

            </main>

        </div>
    )
}

export default WorkoutLogPage