import { Link } from "react-router-dom"
import { useNavigate } from "react-router-dom"
import { useEffect, useState } from "react"
import Sidebar from "../components/Sidebar"

function WorkoutLogPage() {
    const navigate = useNavigate()
    
    const [routines, setRoutine] = useState([])
    
    return (
        <div className="min-h-screen bg-gray-100 flex">

            <Sidebar/>

            <main className="flex-1 p-8">

                <h2 className="text-3xl font-bold mb-6">Workouts</h2>

                <button 
                onClick={() => navigate("/start-workout")} 
                className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 mb-8"
                >
                    + Start Empty Workout
                </button>

                <div className="mb-8">

                    <h3 className="text-xl font-semibold mb-3">
                        Routines
                    </h3>

                    <button 
                        onClick={() => navigate("/new-routine")}
                        className="border border-blue-600 text-blue-600 px-5 py-2 rounded-lg hover:bg-blue-50"
                    >
                        New Routine
                    </button>

                </div>

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