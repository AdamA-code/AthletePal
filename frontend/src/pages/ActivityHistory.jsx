import { useState } from "react"
import { Link } from "react-router-dom"
import Sidebar from "../components/Sidebar"

function ActivityHistoryPage() {
    const [searchTerm, setSearchTerm] = useState("")

    const cardioWorkouts = [
        {
            id: 1,
            date: "04/03/26",
            type: "Run",
            duration: "1 hour 25mins",
            details: "Park route, moderate pace"
        },
        {
            id: 2,
            date: "02/03/26",
            type: "Cycling",
            duration: "45 mins",
            details: "Stationary bike",
        },
        {
            id: 3,
            date: "06/03/26",
            type: "Walking",
            duration: "45 mins",
            details: "Incline treadmill at 12% gradient",
        },
        {
            id: 4,
            date: "08/03/26",
            type: "Swimming",
            duration: "30 mins",
            details: "30 laps of 50m pool so 600m"
        }
    ]
    const weightTraining = [
        {
            id: 1,
            date: "09/03/26",
            type: "Upper Day",
            duration: "1 hour 45 mins",
            excercises: [
                "Bench press - 60kg 3x12",
                "Bicep Curl - 18kg 2x8",
                "Shoulder Press - 40kg 3x10",
                "Pec Deck - 30kg 2x10",
            ]
        },
        {
            id: 2,
            date: "11/03/26",
            type: "Lower Day",
            duration: "1 hour 15 mins",
            excercises: [
                "Squats - 110kg 2x8",
                "Leg Extension - 75kg 3x12",
                "Hamstring Curl - 60kg 3x10",
                "Calf raises - 120kg 2x10",
            ]
        },
        {
            id: 3,
            date: "13/03/26",
            type: "Push Day",
            excercises: [
                "Incline Dumbbell Press - 30kg 3x12",
                ""
            ]
        }
    ]

    const filteredCardio = cardioWorkouts.filter((workout) =>
        `${workout.type} ${workout.date} ${workout.details}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )

    const filteredWeights = weightTraining.filter((workout) =>
        `${workout.type} ${workout.date} ${workout.excercises.join(" ")}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
    )

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            <div className="max-w-7x1 mx-auto">
                <h1 className="text-3x1 font-bold text-blue-800 mb-6">
                    Activity History
                </h1>

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
                                    key={workout.id}
                                    className="border border-gray-200 rounded-lg p-4 bg-gray-50"
                                >
                                    <p className="text-sm text-gray-500 mb-2">{workout.date}</p>
                                    <h3 className="text-lg font-semibold text-gray-800">
                                        Exercise Type: {workout.type}
                                    </h3>
                                    <p className="text-gray-700">
                                        Duration: {workout.duration}
                                    </p>
                                    <p className="text-gray-600 mt-2">{workout.details}</p>

                                    <div className="mt-4 h-40 border border-gray-300 rounded-md flex items-center justify-center text-gray-400 text-sm bg-white">
                                        Cardio route / image placeholder
                                    </div>
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
                    key={workout.id}
                    className="border border-gray-200 rounded-lg p-4 bg-gray-50"
                  >
                    <p className="text-sm text-gray-500 mb-2">{workout.date}</p>
                    <h3 className="text-lg font-semibold text-gray-800 mb-3">
                      {workout.type}
                    </h3>

                    <div className="space-y-2">
                      {workout.excercises.map((exercise, index) => (
                        <p key={index} className="text-gray-700">
                          {exercise}
                        </p>
                      ))}
                    </div>
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