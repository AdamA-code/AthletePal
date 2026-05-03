// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic and accessibility improvements done with Claude Sonnet (claude.ai)
import { useNavigate } from "react-router-dom"
import { useEffect, useState } from "react"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function WorkoutLogPage() {
    const navigate = useNavigate()
    const [routines, setRoutines] = useState([])
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    useEffect(() => {
        const userID = localStorage.getItem("userID")
        if (!userID) {
            setError("Not logged in. Please log in again.")
            return
        }
        fetchRoutines(userID)
    }, [])

    const fetchRoutines = async (userID) => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/workouts/${userID}`)
            const data = await res.json()
            const allWorkouts = Array.isArray(data.Workouts) ? data.Workouts : []
            setRoutines(allWorkouts.filter(w => w.isRoutine === true))
        } catch (err) {
            console.error("Error fetching routines:", err)
            setError("Could not load your routines.")
        }
    }

    const handleDelete = async (workoutID, routineName) => {
        if (!window.confirm(`Are you sure you want to delete "${routineName}"?`)) return

        try {
            const res = await fetch(`${API_BASE_URL}/api/workouts/${workoutID}`, {
                method: "DELETE"
            })
            if (res.ok) {
                setSuccess("Routine deleted.")
                const userID = localStorage.getItem("userID")
                fetchRoutines(userID)
            } else {
                setError("Failed to delete routine.")
            }
        } catch (err) {
            console.error(err)
            setError("Could not connect to server.")
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            <main className="flex-1 p-8">
                <h2 className="text-3xl font-bold mb-6">Workouts</h2>

                {error && <p className="text-red-700 text-sm mb-4" role="alert">{error}</p>}
                {success && <p className="text-green-700 text-sm mb-4" role="status">{success}</p>}

                <button
                    onClick={() => navigate("/start-workout")}
                    className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 mb-8"
                >
                    + Start Empty Workout
                </button>

                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-3">Routines</h3>
                    <button
                        onClick={() => navigate("/new-routine")}
                        className="border border-blue-600 text-blue-600 px-5 py-2 rounded-lg hover:bg-blue-50"
                    >
                        New Routine
                    </button>
                </div>

                <div>
                    <h3 className="text-xl font-semibold mb-4">My Routines</h3>

                    {routines.length === 0 ? (
                        <p className="text-gray-600">No routines created yet.</p>
                    ) : (
                        routines.map((routine) => (
                            <div key={routine.workoutID} className="bg-white p-4 rounded shadow mb-4 flex justify-between items-center">
                                <div>
                                    <p className="font-semibold text-gray-800">
                                        {routine.notes || "Untitled Routine"}
                                    </p>
                                    <p className="text-sm text-gray-600">{routine.date}</p>
                                </div>
                                <div className="flex gap-3 items-center">
                                    <button
                                        onClick={() => navigate("/start-workout", { state: { workoutID: routine.workoutID } })}
                                        className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
                                        aria-label={`Start ${routine.notes || "Untitled Routine"}`}
                                    >
                                        Start
                                    </button>
                                    <button
                                        onClick={() => handleDelete(routine.workoutID, routine.notes || "Untitled Routine")}
                                        className="text-red-700 text-sm hover:underline"
                                        aria-label={`Delete ${routine.notes || "Untitled Routine"}`}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </main>
        </div>
    )
}

export default WorkoutLogPage