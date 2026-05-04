// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic and accessibility improvements done with Claude Sonnet (claude.ai)
import { useState, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function StartWorkoutPage() {
    const navigate = useNavigate()
    const location = useLocation()

    // Read state passed from WorkoutLogPage when navigating to this page
    const routineWorkoutID = location.state?.workoutID || null
    const isScheduled = location.state?.scheduled || false
    const fromScheduled = location.state?.fromScheduled || false
    const scheduledWorkoutID = location.state?.workoutID || null

    const today = new Date().toISOString().split("T")[0]

    const [availableExercises, setAvailableExercises] = useState([])
    const [selectedExercises, setSelectedExercises] = useState([])
    const [notes, setNotes] = useState("")
    const [duration, setDuration] = useState("")
    const [date, setDate] = useState(today)
    const [searchTerm, setSearchTerm] = useState("")
    const [showDropdown, setShowDropdown] = useState(false)
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    // Fetches the full exercise list on page load
    // If started from a routine preloads its exercises and workout name
    // If started from a scheduled workout prefills the workout name
    useEffect(() => {
        const fetchExercises = async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/api/exercises`)
                const data = await res.json()
                const exercises = Array.isArray(data.Exercises) ? data.Exercises : []
                setAvailableExercises(exercises)

                if (routineWorkoutID) {
                    const routineRes = await fetch(`${API_BASE_URL}/api/workout/exercises/${routineWorkoutID}`)
                    const routineData = await routineRes.json()

                    if (Array.isArray(routineData)) {
                        const preloaded = routineData.map(re => {
                            const match = exercises.find(e => e.exerciseID === re.exerciseID)
                            return {
                                exerciseID: re.exerciseID,
                                name: match ? match.name : `Exercise ${re.exerciseID}`,
                                sets: re.sets || 3,
                                reps: "",
                                weight: ""
                            }
                        })
                        setSelectedExercises(preloaded)
                    }

                    // Fetch the routine workout to get the name stored in the notes field
                    const workoutRes = await fetch(`${API_BASE_URL}/api/workout/${routineWorkoutID}`)
                    const workoutData = await workoutRes.json()
                    if (workoutData && workoutData.Workout && workoutData.Workout.notes) {
                        setNotes(workoutData.Workout.notes)
                    }
                }

                // If started from a scheduled workout fetch its name to prefill the workout name field
                if (fromScheduled && scheduledWorkoutID) {
                    const workoutRes = await fetch(`${API_BASE_URL}/api/workout/${scheduledWorkoutID}`)
                    const workoutData = await workoutRes.json()
                    if (workoutData && workoutData.Workout && workoutData.Workout.notes) {
                        setNotes(workoutData.Workout.notes)
                    }
                }

            } catch (err) {
                console.error("Error loading exercises:", err)
                setError("Could not load exercises. Is the server running?")
            }
        }

        fetchExercises()
    }, [])

    // Filters the full exercise list based on the current search term
    const filteredExercises = availableExercises.filter(ex =>
        ex.name.toLowerCase().includes(searchTerm.toLowerCase())
    )

    // Adds an exercise to the selected list with default sets of 3, reps and weight start empty
    // Prevents adding the same exercise twice
    const addExercise = (exercise) => {
        if (selectedExercises.find(ex => ex.exerciseID === exercise.exerciseID)) return
        setSelectedExercises([...selectedExercises, {
            exerciseID: exercise.exerciseID,
            name: exercise.name,
            sets: 3,
            reps: "",
            weight: ""
        }])
        setSearchTerm("")
        setShowDropdown(false)
    }

    // Removes an exercise from the selected list by filtering out the matching exerciseID
    const removeExercise = (exerciseID) => {
        setSelectedExercises(selectedExercises.filter(ex => ex.exerciseID !== exerciseID))
    }

    // Updates a specific field (sets, reps or weight) for a specific exercise in the selected list
    const updateExercise = (exerciseID, field, value) => {
        setSelectedExercises(selectedExercises.map(ex =>
            ex.exerciseID === exerciseID ? { ...ex, [field]: value } : ex
        ))
    }

    // Validates inputs then saves the workout to the backend with isRoutine: false
    // If started from a scheduled workout deletes the scheduled record after saving
    // Duration is required for non-scheduled workouts to distinguish them from scheduled ones
    const saveWorkout = async () => {
        const userID = parseInt(localStorage.getItem("userID"))
        if (!userID) {
            setError("Not logged in. Please log in again.")
            return
        }
        if (selectedExercises.length === 0) {
            setError("Please add at least one exercise.")
            return
        }
        if (!isScheduled && !duration) {
            setError("Please enter a duration.")
            return
        }

        const body = {
            "workout": {
                "workoutID": 0,
                "createdBy": userID,
                "assignedTo": userID,
                "date": date,
                "duration": duration ? parseInt(duration) : null,
                "workoutType": "weights",
                "notes": notes || null,
                "routeID": null,
                "isRoutine": false
            },
            "exercises": selectedExercises.map(ex => ({
                "workoutExercisesID": 0,
                "workoutID": 0,
                "exerciseID": ex.exerciseID,
                "sets": parseInt(ex.sets) || 1,
                "reps": ex.reps ? parseInt(ex.reps) : null,
                "weight": ex.weight ? parseFloat(ex.weight) : null
            }))
        }

        try {
            const res = await fetch(`${API_BASE_URL}/api/log/workout`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            })
            const data = await res.json()

            if (res.ok) {
                // Delete the original scheduled record so it no longer appears in Scheduled for Today
                if (fromScheduled && scheduledWorkoutID) {
                    await fetch(`${API_BASE_URL}/api/workout/${scheduledWorkoutID}`, {
                        method: "DELETE"
                    })
                }
                setSuccess(isScheduled ? "Workout scheduled!" : "Workout saved!")
                setError("")
                setTimeout(() => navigate("/workout-log"), 2500)
            } else {
                setError(data.message || "Failed to save workout.")
            }
        } catch (err) {
            console.error(err)
            setError("Could not connect to server.")
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            <main className="flex-1 p-8 max-w-3xl">
                <h2 className="text-3xl font-bold mb-6">
                    {isScheduled ? "Schedule Workout" : fromScheduled ? "Start Scheduled Workout" : routineWorkoutID ? "Start Workout" : "Empty Workout"}
                </h2>

                {error && <p className="text-red-700 text-sm mb-4" role="alert">{error}</p>}
                {success && <p className="text-green-700 text-sm mb-4" role="status">{success}</p>}

                <div className="flex gap-4 mb-6 items-end">
                    {isScheduled && (
                        <div className="flex flex-col">
                            <label htmlFor="workout-date" className="text-sm text-gray-700 mb-1">Date</label>
                            <input
                                id="workout-date"
                                type="date"
                                value={date}
                                min={today}
                                onChange={(e) => setDate(e.target.value)}
                                className="border p-2 rounded w-40"
                            />
                        </div>
                    )}
                    {!isScheduled && (
                        <div className="flex flex-col">
                            <label htmlFor="workout-duration" className="text-sm text-gray-700 mb-1">
                                Duration (mins) <span className="text-red-600 text-xs">*required</span>
                            </label>
                            <input
                                id="workout-duration"
                                type="number"
                                placeholder="e.g. 60"
                                value={duration}
                                onChange={(e) => setDuration(e.target.value)}
                                className="border p-2 rounded w-40"
                            />
                        </div>
                    )}
                    <div className="flex-1 flex flex-col">
                        <label htmlFor="workout-notes" className="text-sm text-gray-700 mb-1">
                            {isScheduled ? "Name" : "Workout Name"}
                        </label>
                        <input
                            id="workout-notes"
                            placeholder={isScheduled ? "e.g. Upper Body" : "e.g. Upper Body Session"}
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                            className="border p-2 rounded w-full"
                        />
                    </div>
                </div>

                <div className="relative mb-6">
                    <label htmlFor="exercise-search" className="sr-only">Search and add an exercise</label>
                    <input
                        id="exercise-search"
                        placeholder="Search and add an exercise..."
                        value={searchTerm}
                        onChange={(e) => { setSearchTerm(e.target.value); setShowDropdown(true) }}
                        onFocus={() => setShowDropdown(true)}
                        className="border p-2 rounded w-full"
                    />
                    {showDropdown && searchTerm && (
                        <div className="absolute z-10 bg-white border rounded shadow w-full max-h-48 overflow-y-auto">
                            {filteredExercises.length === 0 ? (
                                <p className="p-2 text-gray-600 text-sm">No exercises found</p>
                            ) : (
                                filteredExercises.map(ex => (
                                    <button
                                        key={ex.exerciseID}
                                        onClick={() => addExercise(ex)}
                                        className="w-full text-left px-3 py-2 hover:bg-blue-50 text-sm"
                                    >
                                        <span className="font-medium">{ex.name}</span>
                                        <span className="text-gray-600 ml-2 text-xs">{ex.category}</span>
                                    </button>
                                ))
                            )}
                        </div>
                    )}
                </div>

                {selectedExercises.length === 0 ? (
                    <p className="text-gray-600 mb-6">No exercises added yet.</p>
                ) : (
                    selectedExercises.map(ex => (
                        <div key={ex.exerciseID} className="bg-white p-4 rounded shadow mb-4">
                            <div className="flex justify-between items-center mb-3">
                                <p className="font-semibold">{ex.name}</p>
                                <button
                                    onClick={() => removeExercise(ex.exerciseID)}
                                    className="text-red-700 text-sm"
                                    aria-label={`Remove ${ex.name}`}
                                >
                                    Remove
                                </button>
                            </div>
                            <div className="flex gap-3">
                                <div className="flex flex-col">
                                    <label htmlFor={`sets-${ex.exerciseID}`} className="text-xs text-gray-700 mb-1">Sets</label>
                                    <input
                                        id={`sets-${ex.exerciseID}`}
                                        type="number"
                                        value={ex.sets}
                                        onChange={(e) => updateExercise(ex.exerciseID, "sets", e.target.value)}
                                        className="border p-2 rounded w-20"
                                    />
                                </div>
                                {!isScheduled && (
                                    <>
                                        <div className="flex flex-col">
                                            <label htmlFor={`reps-${ex.exerciseID}`} className="text-xs text-gray-700 mb-1">Reps</label>
                                            <input
                                                id={`reps-${ex.exerciseID}`}
                                                type="number"
                                                value={ex.reps}
                                                placeholder="optional"
                                                onChange={(e) => updateExercise(ex.exerciseID, "reps", e.target.value)}
                                                className="border p-2 rounded w-24"
                                            />
                                        </div>
                                        <div className="flex flex-col">
                                            <label htmlFor={`weight-${ex.exerciseID}`} className="text-xs text-gray-700 mb-1">Weight (kg)</label>
                                            <input
                                                id={`weight-${ex.exerciseID}`}
                                                type="number"
                                                value={ex.weight}
                                                placeholder="optional"
                                                onChange={(e) => updateExercise(ex.exerciseID, "weight", e.target.value)}
                                                className="border p-2 rounded w-28"
                                            />
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>
                    ))
                )}

                <div className="flex gap-4 mt-4">
                    <button
                        onClick={saveWorkout}
                        className="bg-green-700 text-white px-6 py-2 rounded font-semibold hover:bg-green-800"
                    >
                        {isScheduled ? "Schedule Workout" : "Save Workout"}
                    </button>
                    <button
                        onClick={() => navigate("/workout-log")}
                        className="border border-gray-400 text-gray-600 px-6 py-2 rounded hover:bg-gray-100"
                    >
                        Cancel
                    </button>
                </div>
            </main>
        </div>
    )
}

export default StartWorkoutPage