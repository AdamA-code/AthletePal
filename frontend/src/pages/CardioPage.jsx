// frontend-backend connection logic done with claude sonnet AI
import { useState, useEffect } from "react"
import RouteMap from "../components/RouteMap"
import Sidebar from "../components/Sidebar"

function CardioPage() {

    const [exerciseType, setExerciseType] = useState("Running")
    const [date, setDate] = useState("")
    const [duration, setDuration] = useState("")
    const [notes, setNotes] = useState("")
    const [isPublic, setIsPublic] = useState(false)
    const [routeName, setRouteName] = useState("")
    const [distance, setDistance] = useState("")
    const [drawnRouteData, setDrawnRouteData] = useState(null)
    const [publicRoutes, setPublicRoutes] = useState([])
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    // Map exercise type to valid backend workoutType
    const getWorkoutType = (type) => {
        const map = {
            "Walking": "cardio",
            "Running": "running",
            "Swimming": "swimming",
            "Cycling": "cycling"
        }
        return map[type] || "cardio"
    }

    // Fetch public and own routes when page loads
    useEffect(() => {
        const userID = localStorage.getItem("userID")
        if (!userID) return

        const fetchRoutes = async () => {
            try {
                const res = await fetch(`http://localhost:3000/api/routes/${userID}`)
                const data = await res.json()
                setPublicRoutes(Array.isArray(data.Routes) ? data.Routes : [])
            } catch (err) {
                console.error("Error fetching routes:", err)
            }
        }

        fetchRoutes()
    }, [])

    const saveCardioSession = async () => {
        const userID = parseInt(localStorage.getItem("userID"))
        if (!userID) {
            setError("Not logged in. Please log in again.")
            return
        }
        if (!date) {
            setError("Please enter a date.")
            return
        }
        if (!duration) {
            setError("Please enter a duration.")
            return
        }

        setError("")
        let savedRouteID = null

        // Step 1 — if a route was drawn AND has a name and distance, save it first
        if (drawnRouteData && routeName && distance) {
            try {
                const routeBody = {
                    routeID: 0,
                    userID: userID,
                    name: routeName,
                    distance: parseFloat(distance),
                    estimatedTime: duration ? parseFloat(duration) : null,
                    date: date,
                    isPublic: isPublic,
                    routeData: drawnRouteData
                }

                const routeRes = await fetch("http://localhost:3000/api/route", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(routeBody)
                })

                const routeData = await routeRes.json()

                if (routeRes.ok) {
                    savedRouteID = routeData.ID
                } else {
                    setError("Could not save route: " + (routeData.message || "Unknown error"))
                    return
                }
            } catch (err) {
                console.error("Error saving route:", err)
                setError("Could not connect to server.")
                return
            }
        }

        // Step 2 — save the cardio workout
        try {
            const workoutBody = {
                workout: {
                    workoutID: 0,
                    createdBy: userID,
                    assignedTo: userID,
                    date: date,
                    duration: parseInt(duration),
                    workoutType: getWorkoutType(exerciseType),
                    notes: notes || null,
                    routeID: savedRouteID  // links to the route if one was saved
                },
                exercises: []  // no exercises for cardio
            }

            const workoutRes = await fetch("http://localhost:3000/api/log/workout", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(workoutBody)
            })

            const workoutData = await workoutRes.json()

            if (workoutRes.ok) {
                setSuccess("Session saved!")
                setError("")
                // Reset form
                setDate("")
                setDuration("")
                setNotes("")
                setRouteName("")
                setDistance("")
                setDrawnRouteData(null)
                setIsPublic(false)
            } else {
                setError(workoutData.message || "Failed to save session.")
            }
        } catch (err) {
            console.error("Error saving workout:", err)
            setError("Could not connect to server.")
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            <main className="flex-1 p-8 max-w-3xl">

                <h2 className="text-3xl font-bold mb-8">Cardio</h2>

                {error && <p className="text-red-600 text-sm mb-4">{error}</p>}
                {success && <p className="text-green-600 text-sm mb-4">{success}</p>}

                {/* Exercise Type */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Exercise Type</label>
                    <select
                        value={exerciseType}
                        onChange={(e) => setExerciseType(e.target.value)}
                        className="border rounded p-2 w-full"
                    >
                        <option>Walking</option>
                        <option>Running</option>
                        <option>Swimming</option>
                        <option>Cycling</option>
                    </select>
                </div>

                {/* Date */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Date</label>
                    <input
                        type="date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        className="border rounded p-2 w-full"
                    />
                </div>

                {/* Duration */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Duration (minutes)</label>
                    <input
                        type="number"
                        value={duration}
                        onChange={(e) => setDuration(e.target.value)}
                        className="border rounded p-2 w-full"
                    />
                </div>

                {/* Notes */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Notes</label>
                    <textarea
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        className="border rounded p-2 w-full h-24"
                    />
                </div>

                {/* Route section */}
                <div className="mb-6 bg-white p-4 rounded shadow">
                    <h3 className="font-semibold text-lg mb-4">Route (optional)</h3>

                    <div className="mb-4">
                        <label className="block font-semibold mb-2">Route Name</label>
                        <input
                            placeholder="e.g. Morning run around the park"
                            value={routeName}
                            onChange={(e) => setRouteName(e.target.value)}
                            className="border rounded p-2 w-full"
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block font-semibold mb-2">Distance (km)</label>
                        <input
                            type="number"
                            placeholder="e.g. 5.2"
                            value={distance}
                            onChange={(e) => setDistance(e.target.value)}
                            className="border rounded p-2 w-full"
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block font-semibold mb-2">
                            Draw Your Route on the Map
                        </label>
                        <RouteMap onRouteDrawn={setDrawnRouteData} />
                        {drawnRouteData && (
                            <p className="text-green-600 text-sm mt-2">Route drawn ✓</p>
                        )}
                    </div>

                    {/* Make public */}
                    <div>
                        <label className="block font-semibold mb-2">Make Route Public</label>
                        <div className="flex gap-6">
                            <label className="flex items-center gap-2">
                                <input
                                    type="radio"
                                    checked={isPublic === true}
                                    onChange={() => setIsPublic(true)}
                                />
                                Yes
                            </label>
                            <label className="flex items-center gap-2">
                                <input
                                    type="radio"
                                    checked={isPublic === false}
                                    onChange={() => setIsPublic(false)}
                                />
                                No
                            </label>
                        </div>
                    </div>
                </div>

                {/* Save button */}
                <button
                    onClick={saveCardioSession}
                    className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 mb-12"
                >
                    Save Session
                </button>

                {/* Public and own routes */}
                <div>
                    <h3 className="text-xl font-semibold mb-4">Available Routes</h3>
                    <p className="text-sm text-gray-500 mb-4">
                        Your routes and public routes from other users
                    </p>

                    {publicRoutes.length === 0 ? (
                        <p className="text-gray-500">No routes available yet.</p>
                    ) : (
                        publicRoutes.map((route) => (
                            <div key={route.routeID} className="bg-white p-4 rounded shadow mb-4">
                                <div className="flex justify-between items-center">
                                    <div>
                                        <p className="font-semibold">{route.name}</p>
                                        <p className="text-sm text-gray-500">
                                            {route.distance} km · {route.date}
                                        </p>
                                        {route.isPublic && (
                                            <span className="text-xs text-blue-500">Public</span>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>

            </main>
        </div>
    )
}

export default CardioPage