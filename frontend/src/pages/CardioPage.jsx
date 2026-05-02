// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic and accessibility improvements done with Claude Sonnet (claude.ai)
import { useState, useEffect } from "react"
import RouteMap from "../components/RouteMap"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

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
    const [selectedRoute, setSelectedRoute] = useState(null)
    const [routeMode, setRouteMode] = useState(null) // null, "draw" or "select"
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    const getWorkoutType = (type) => {
        const map = {
            "Running": "running",
            "Swimming": "swimming",
            "Cycling": "cycling"
        }
        return map[type] || "cardio"
    }

    useEffect(() => {
        const userID = localStorage.getItem("userID")
        if (!userID) return

        const fetchRoutes = async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/api/routes/${userID}`)
                const data = await res.json()
                setPublicRoutes(Array.isArray(data.Routes) ? data.Routes : [])
            } catch (err) {
                console.error("Error fetching routes:", err)
            }
        }

        fetchRoutes()
    }, [])

    const handleSelectRoute = (route) => {
        setSelectedRoute(route)
        setRouteMode("select")
        setDrawnRouteData(null)
        setRouteName("")
        setDistance("")
    }

    const handleClearRoute = () => {
        setRouteMode(null)
        setSelectedRoute(null)
        setDrawnRouteData(null)
        setRouteName("")
        setDistance("")
    }

    const handleRouteDrawn = (data) => {
        setDrawnRouteData(data)
        if (data) setRouteMode("draw")
    }

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

        // If user selected an existing route just use its ID
        if (routeMode === "select" && selectedRoute) {
            savedRouteID = selectedRoute.routeID
        }

        // If user drew their own route save it first
        if (routeMode === "draw" && drawnRouteData && routeName && distance) {
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

                const routeRes = await fetch(`${API_BASE_URL}/api/route`, {
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
                    routeID: savedRouteID
                },
                exercises: []
            }

            const workoutRes = await fetch(`${API_BASE_URL}/api/log/workout`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(workoutBody)
            })

            const workoutData = await workoutRes.json()

            if (workoutRes.ok) {
                setSuccess("Session saved!")
                setError("")
                setDate("")
                setDuration("")
                setNotes("")
                handleClearRoute()
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

                {error && <p className="text-red-700 text-sm mb-4" role="alert">{error}</p>}
                {success && <p className="text-green-700 text-sm mb-4" role="status">{success}</p>}

                {/* Exercise Type */}
                <div className="mb-6">
                    <label htmlFor="exercise-type" className="block font-semibold mb-2">
                        Exercise Type
                    </label>
                    <select
                        id="exercise-type"
                        value={exerciseType}
                        onChange={(e) => setExerciseType(e.target.value)}
                        className="border rounded p-2 w-full"
                    >
                        <option>Running</option>
                        <option>Swimming</option>
                        <option>Cycling</option>
                    </select>
                </div>

                {/* Date */}
                <div className="mb-6">
                    <label htmlFor="cardio-date" className="block font-semibold mb-2">Date</label>
                    <input
                        id="cardio-date"
                        type="date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        className="border rounded p-2 w-full"
                    />
                </div>

                {/* Duration */}
                <div className="mb-6">
                    <label htmlFor="cardio-duration" className="block font-semibold mb-2">
                        Duration (minutes)
                    </label>
                    <input
                        id="cardio-duration"
                        type="number"
                        value={duration}
                        onChange={(e) => setDuration(e.target.value)}
                        className="border rounded p-2 w-full"
                    />
                </div>

                {/* Notes */}
                <div className="mb-6">
                    <label htmlFor="cardio-notes" className="block font-semibold mb-2">Notes</label>
                    <textarea
                        id="cardio-notes"
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        className="border rounded p-2 w-full h-24"
                    />
                </div>

                {/* Route section */}
                <div className="mb-6 bg-white p-4 rounded shadow">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="font-semibold text-lg">Route (optional)</h3>
                        {routeMode && (
                            <button
                                onClick={handleClearRoute}
                                className="text-sm text-red-600 hover:underline"
                            >
                                Clear Route
                            </button>
                        )}
                    </div>

                    {/* No route selected yet — show draw option */}
                    {routeMode === null && (
                        <div>
                            <p className="text-gray-600 text-sm mb-4">
                                Draw your own route below or select one from Available Routes at the bottom of the page
                            </p>
                            <div className="mb-4">
                                <label htmlFor="route-name" className="block font-semibold mb-2">
                                    Route Name
                                </label>
                                <input
                                    id="route-name"
                                    placeholder="e.g. Morning run around the park"
                                    value={routeName}
                                    onChange={(e) => setRouteName(e.target.value)}
                                    className="border rounded p-2 w-full"
                                />
                            </div>
                            <div className="mb-4">
                                <label htmlFor="route-distance" className="block font-semibold mb-2">
                                    Distance (km)
                                </label>
                                <input
                                    id="route-distance"
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
                                <RouteMap onRouteDrawn={handleRouteDrawn} />
                                {drawnRouteData && (
                                    <p className="text-green-700 text-sm mt-2">Route drawn ✓</p>
                                )}
                            </div>
                            <fieldset>
                                <legend className="block font-semibold mb-2">Make Route Public</legend>
                                <div className="flex gap-6">
                                    <label className="flex items-center gap-2">
                                        <input
                                            type="radio"
                                            name="isPublic"
                                            checked={isPublic === true}
                                            onChange={() => setIsPublic(true)}
                                        />
                                        Yes
                                    </label>
                                    <label className="flex items-center gap-2">
                                        <input
                                            type="radio"
                                            name="isPublic"
                                            checked={isPublic === false}
                                            onChange={() => setIsPublic(false)}
                                        />
                                        No
                                    </label>
                                </div>
                            </fieldset>
                        </div>
                    )}

                    {/* Draw mode — route being drawn */}
                    {routeMode === "draw" && (
                        <div>
                            <p className="text-green-700 text-sm mb-2">Route drawn ✓</p>
                            <p className="text-gray-600 text-sm">Press Clear Route to start over or select a different route</p>
                        </div>
                    )}

                    {/* Select mode — existing route selected */}
                    {routeMode === "select" && selectedRoute && (
                        <div>
                            <p className="text-green-700 text-sm mb-2">
                                Using route: <span className="font-semibold">{selectedRoute.name}</span>
                            </p>
                            <p className="text-gray-600 text-sm mb-3">{selectedRoute.distance} km</p>
                            <RouteMap viewRouteData={selectedRoute.routeData} />
                            <p className="text-gray-600 text-sm mt-2">Press Clear Route to draw your own instead</p>
                        </div>
                    )}
                </div>

                {/* Save button */}
                <button
                    onClick={saveCardioSession}
                    className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 mb-12"
                >
                    Save Session
                </button>

                {/* Available routes */}
                <div>
                    <h3 className="text-xl font-semibold mb-4">Available Routes</h3>
                    <p className="text-sm text-gray-600 mb-4">
                        Your routes and public routes from other users
                    </p>

                    {publicRoutes.length === 0 ? (
                        <p className="text-gray-600">No routes available yet.</p>
                    ) : (
                        publicRoutes.map((route) => (
                            <div key={route.routeID} className="bg-white p-4 rounded shadow mb-4">
                                <div className="flex justify-between items-center">
                                    <div>
                                        <p className="font-semibold">{route.name}</p>
                                        <p className="text-sm text-gray-600">
                                            {route.distance} km · {route.date}
                                        </p>
                                        {route.isPublic && (
                                            <span className="text-xs text-blue-600">Public</span>
                                        )}
                                    </div>
                                    <div className="flex gap-2">
                                        {route.routeData && (
                                            <button
                                                onClick={() => setSelectedRoute(
                                                    selectedRoute?.routeID === route.routeID && routeMode !== "select"
                                                        ? null : route
                                                )}
                                                className="text-blue-600 text-sm border border-blue-600 px-3 py-1 rounded hover:bg-blue-50"
                                                aria-label={`View route for ${route.name}`}
                                            >
                                                {selectedRoute?.routeID === route.routeID && routeMode !== "select" ? "Hide" : "View"}
                                            </button>
                                        )}
                                        <button
                                            onClick={() => handleSelectRoute(route)}
                                            disabled={routeMode === "draw"}
                                            className={`text-sm px-3 py-1 rounded ${
                                                routeMode === "draw"
                                                    ? "border border-gray-300 text-gray-400 cursor-not-allowed"
                                                    : routeMode === "select" && selectedRoute?.routeID === route.routeID
                                                    ? "bg-green-600 text-white"
                                                    : "bg-blue-600 text-white hover:bg-blue-700"
                                            }`}
                                            aria-label={`Use route ${route.name}`}
                                        >
                                            {routeMode === "select" && selectedRoute?.routeID === route.routeID
                                                ? "Selected ✓"
                                                : "Use Route"}
                                        </button>
                                    </div>
                                </div>
                                {selectedRoute?.routeID === route.routeID && routeMode !== "select" && (
                                    <div className="mt-4">
                                        <RouteMap viewRouteData={route.routeData} />
                                    </div>
                                )}
                            </div>
                        ))
                    )}
                </div>

            </main>
        </div>
    )
}

export default CardioPage