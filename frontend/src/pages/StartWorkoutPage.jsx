// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic and accessibility improvements done with Claude Sonnet (claude.ai)
import { useState, useEffect } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function StartWorkoutPage() {
    const navigate = useNavigate()
    const location = useLocation()

    const routineWorkoutID = location.state?.workoutID || null

    const [availableExercises, setAvailableExercises] = useState([])
    const [selectedExercises, setSelectedExercises] = useState([])
    const [notes, setNotes] = useState("")
    const [duration, setDuration] = useState("")
    const [searchTerm, setSearchTerm] = useState("")
    const [showDropdown, setShowDropdown] = useState(false)
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    useEffect(() => {
        const fetchExercises = async () => {
            try {
                const res = await fetch(`http://localhost:3000/api/exercises`)
                const data = await res.json()
                const exercises = Array.isArray(data.Exercises) ? data.Exercises : []
                setAvailableExercises(exercises)

                if (routineWorkoutID) {
                    const routineRes = await fetch(`http://localhost:3000/api/workout/exercises/${routineWorkoutID}`)
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
                }
            } catch (err) {
                console.error("Error loading exercises:", err)
                setError("Could not load exercises. Is the server running?")
            }
        }

        fetchExercises()
    }, [])

    const filteredExercises = availableExercises.filter(ex =>
        ex.name.toLowerCase().includes(searchTerm.toLowerCase())
    )

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

    const removeExercise = (exerciseID) => {
        setSelectedExercises(selectedExercises.filter(ex => ex.exerciseID !== exerciseID))
    }

    const updateExercise = (exerciseID, field, value) => {
        setSelectedExercises(selectedExercises.map(ex =>
            ex.exerciseID === exerciseID ? { ...ex, [field]: value } : ex
        ))
    }

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

        const today = new Date().toISOString().split("T")[0]

        const body = {
            workout: {
                workoutID: 0,
                createdBy: userID,
                assignedTo: userID,
                date: today,
                duration: duration ? parseInt(duration) : null,
                workoutType: "weights",
                notes: notes || null,
                routeID: null,
                isRoutine: false
            },
            exercises: selectedExercises.map(ex => ({
                workoutExercisesID: 0,
                workoutID: 0,
                exerciseID: ex.exerciseID,
                sets: parseInt(ex.sets) || 1,
                reps: ex.reps ? parseInt(ex.reps) : null,
                weight: ex.weight ? parseFloat(ex.weight) : null
            }))
        }

        try {
            const res = await fetch(`http://localhost:3000/api/log/workout`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            })
            const data = await res.json()

            if (res.ok) {
                setSuccess("Workout saved!")
                setError("")
                setTimeout(() => navigate("/workout-log"), 1500)
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
                    {routineWorkoutID ? "Start Workout" : "Empty Workout"}
                </h2>

                {error && <p className="text-red-700 text-sm mb-4" role="alert">{error}</p>}
                {success && <p className="text-green-700 text-sm mb-4" role="status">{success}</p>}

                <div className="flex gap-4 mb-6">
                    <div className="flex flex-col">
                        <label htmlFor="workout-duration" className="sr-only">Duration in minutes</label>
                        <input
                            id="workout-duration"
                            type="number"
                            placeholder="Duration (mins)"
                            value={duration}
                            onChange={(e) => setDuration(e.target.value)}
                            className="border p-2 rounded w-40"
                        />
                    </div>
                    <div className="flex-1">
                        <label htmlFor="workout-notes" className="sr-only">Notes</label>
                        <input
                            id="workout-notes"
                            placeholder="Notes (optional)"
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                            className="border p-2 rounded w-full"
                        />
                    </div>
                </div>

                {/* Exercise search */}
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

                {/* Exercises — sets, reps and weight */}
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
                            </div>
                        </div>
                    ))
                )}

                <div className="flex gap-4 mt-4">
                    <button
                        onClick={saveWorkout}
                        className="bg-green-700 text-white px-6 py-2 rounded font-semibold hover:bg-green-800"
                    >
                        Save Workout
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