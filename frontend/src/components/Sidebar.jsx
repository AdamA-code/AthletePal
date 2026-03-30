import { Link, useLocation } from "react-router-dom"

function Sidebar() {
    const location = useLocation()

    const linkClass = (path) =>
        location.pathname === path
        ? "bg-blue-100 text-blue-800 px-3 py-2 rounded-lg font-medium"
        : "hover:text-blue-700 px-3 py-2 rounded-lg"
        
    return (
        <aside className="w-64 bg-white shadow-md p-6 flex flex-col">
            <h1 className="text-2x1 font-bold text-blue-800 mb-8">AthletePal</h1>

            <nav className="flex flex-col gap-2 text-gray-700">
                <Link to="/dashboard" className={linkClass("/dashboard")}>
                    Dashboard
                </Link>

                <Link to="/workout-log" className={linkClass("/workout-log")}>
                    Workout Logs
                </Link>

                <Link to="/cardio" className={linkClass("/cardio")}>
                    Cardio
                </Link>

                <Link to="/activity-history" className={linkClass("/activity-history")}>
                    Activity History
                </Link>

                <Link to="/competitions" className={linkClass("/comptetions")}>
                    Competitions
                </Link>

                <Link to="/training-groups" className={linkClass("/training-groups")}>
                    Training Groups
                </Link>

                <Link to="/statistics" className={linkClass("/statistics")}>
                    Statistics / Visualisation
                </Link>

                <Link to="/profile" className={linkClass("/profile")}>
                    Profile
                </Link>

                <Link to="/statistics" className={linkClass("/statistics")}>
                    Statistics / Visualisation
                </Link>
            </nav>
        </aside>
    )
}

export default Sidebar