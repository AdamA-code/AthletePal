// page structure, layout, and frontend -> backend request connections aided with chatgpt chat.openai.com
import { useEffect, useState } from "react"
import { data } from "react-router-dom"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function ActivityHistoryPage() {
    const [searchTerm, setSearchTerm] = useState("")
    const [cardioWorkouts, setCardioWorkouts] = useState([])
    const [weightTraining, setWeightTraining] = useState([])
    const [error, setError] = useState("")
    const [loading, setLoading] = useState(true)
    
    useEffect(() => {
        const userID = localStorage.getItem("userID")

        if (!userID) {
            setError("No user found. Please log in again")
            setLoading(false)
            return
        }

        const fetchActivityHistory = async () => {
            try {
                const response = await fetch(`${API_BASE_URL}/api/activity/${userID}`)
                const data = await response.json()

                const workouts = data.Workouts || []

                setCardioWorkouts(
                    workouts.filter((workout) => 
                        workout.workoutType?.toLowerCase() === "cardio"
                )
                )

                setWeightTraining(
                    workouts.filter((workout) => 
                        workout.workoutType?.toLowerCase() === "weights"
                )
                )
            } catch (err){
                console.error("Activity history error:", err)
                setError("Failed to load activity history")
            } finally {
                setLoading(false)
            }
        }

        {/* Commented out code
        const workouts = data.Workouts || []
        console.log("Fetched workouts:", workouts)
        console.log("Activity history data:", data)
        */}

        fetchActivityHistory()
    }, [])

    const filteredCardio = cardioWorkouts.filter((workout) =>
        `${workout.workoutType || ""} ${workout.date || ""} ${workout.notes || ""}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )

    const filteredWeights = weightTraining.filter((workout) =>
        `${workout.workoutType || ""} ${workout.date || ""} ${workout.notes || ""}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            <div className="flex-1 p-8">
                <h1 className="text-3x1 font-bold text-blue-800 mb-6">
                    Activity History
                </h1>

                {error && <p className="text-red-600 mb-4">{error}</p>}
                {loading && <p className="text-gray-600 mb-4">Loading activity history...</p>}

                <div className="mb-6">
                    <input 
                        type="text"
                        placeholder="Search workouts..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full md:w-96 border border-gray-300 rounded-lg py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                    />
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {/* Cardio Card */}
                    <div className="bg-white rounded-x1 shadow-md p-6">
                        <h2 className="text-2x1 font-semibold mb-4 text-gray-800">
                            Cardio History
                        </h2>

                        <div className="h-[500px] overflow-y-auto pr-2 space-y-4">
                        {filteredCardio.length > 0 ? (
                            filteredCardio.map((workout) => (
                                <div 
                                    key={workout.workoutID}
                                    className="border border-gray-200 rounded-lg p-4 bg-gray-50"
                                >
                                    <p className="text-sm text-gray-500 mb-2">{workout.date}</p>
                                    <h3 className="text-lg font-semibold text-gray-800">
                                        {workout.workoutType || "Cardio Workout"}
                                    </h3>
                                    <p className="text-gray-700">
                                        Duration: {workout.duration} minutes 
                                    </p>
                                    <p className="text-gray-600 mt-2">
                                        {workout.notes || "User Notes added"}
                                    </p>
                                </div>
                            ))
                        ) : (
                            <p className="text-gray-500">No cardio workouts found.</p>
                        )}
                    </div>
                </div>
                {/* Weight Lifting Card */}
          <div className="bg-white rounded-xl shadow-md p-6">
            <h2 className="text-2xl font-semibold mb-4 text-gray-800">
              Weight Lifting History
            </h2>

            <div className="h-[500px] overflow-y-auto pr-2 space-y-4">
              {filteredWeights.length > 0 ? (
                filteredWeights.map((workout) => (
                  <div
                    key={workout.workoutID}
                    className="border border-gray-200 rounded-lg p-4 bg-gray-50"
                  >
                    <p className="text-sm text-gray-500 mb-2">{workout.date}</p>
                    <h3 className="text-lg font-semibold text-gray-800 mb-3">
                      {workout.workoutType || "Weights Workout"}
                    </h3>
                    <p className="text-gray-700">
                        Duration: {workout.duration} minutes
                    </p>
                    <p className="text-gray-600 mt-2">
                        {workout.notes || "No notes added."}
                    </p>
                  </div>
                ))
              ) : (
                <p className="text-gray-500">No weightlifting workouts found.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ActivityHistoryPage