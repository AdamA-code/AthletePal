// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic and accessibility improvements done with Claude Sonnet (claude.ai)
import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function NewRoutinePage() {
    const navigate = useNavigate()

    const [routineName, setRoutineName] = useState("")
    const [availableExercises, setAvailableExercises] = useState([])
    const [selectedExercises, setSelectedExercises] = useState([])
    const [searchTerm, setSearchTerm] = useState("")
    const [showDropdown, setShowDropdown] = useState(false)
    const [error, setError] = useState("")

    // Fetches all available preset exercises from the backend on page load
    useEffect(() => {
        const fetchExercises = async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/api/exercises`)
                const data = await res.json()
                setAvailableExercises(Array.isArray(data.Exercises) ? data.Exercises : [])
            } catch (err) {
                console.error("Error fetching exercises:", err)
                setError("Could not load exercises. Is the server running?")
            }
        }
        fetchExercises()
    }, [])

    // Filters the full exercise list based on what the user has typed in the search box
    const filteredExercises = availableExercises.filter(ex =>
        ex.name.toLowerCase().includes(searchTerm.toLowerCase())
    )

    // Adds an exercise to the selected list with a default of 3 sets
    // Prevents duplicates by checking if the exercise is already in the list
    const addExercise = (exercise) => {
        if (selectedExercises.find(ex => ex.exerciseID === exercise.exerciseID)) {
            setError(`${exercise.name} is already added to this routine.`)
            setShowDropdown(false)
            setSearchTerm("")
            return
        }
        setSelectedExercises([...selectedExercises, {
            exerciseID: exercise.exerciseID,
            name: exercise.name,
            sets: 3
        }])
        setError("")
        setSearchTerm("")
        setShowDropdown(false)
    }

    // Removes an exercise from the selected list by filtering out the matching exerciseID
    const removeExercise = (exerciseID) => {
        setSelectedExercises(selectedExercises.filter(ex => ex.exerciseID !== exerciseID))
    }

    // Updates the sets value for a specific exercise in the selected list
    const updateSets = (exerciseID, value) => {
        setSelectedExercises(selectedExercises.map(ex =>
            ex.exerciseID === exerciseID ? { ...ex, sets: value } : ex
        ))
    }

    // Validates inputs then saves the routine to the backend as a workout with isRoutine: true
    // Reps and weight are null at this stage as they are filled in when the routine is started
    const saveRoutine = async () => {
        const userID = parseInt(localStorage.getItem("userID"))
        if (!userID) {
            setError("Not logged in. Please log in again.")
            return
        }
        if (!routineName.trim()) {
            setError("Please give your routine a name.")
            return
        }
        if (selectedExercises.length === 0) {
            setError("Please add at least one exercise.")
            return
        }

        const today = new Date().toISOString().split("T")[0]

        const body = {
            "workout": {
                "workoutID": 0,
                "createdBy": userID,
                "assignedTo": userID,
                "date": today,
                "duration": null,
                "workoutType": "weights",
                "notes": routineName,
                "routeID": null,
                "isRoutine": true
            },
            "exercises": selectedExercises.map(ex => ({
                "workoutExercisesID": 0,
                "workoutID": 0,
                "exerciseID": ex.exerciseID,
                "sets": parseInt(ex.sets),
                "reps": null,
                "weight": null
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
                navigate("/workout-log")
            } else {
                setError(data.message || "Failed to save routine.")
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
                <h2 className="text-3xl font-bold mb-6">New Routine</h2>

                {error && <p className="text-red-700 text-sm mb-4" role="alert">{error}</p>}

                <div className="mb-6">
                    <label htmlFor="routine-name" className="block text-sm font-semibold text-gray-700 mb-1">
                        Routine Name <span className="text-red-600 font-normal">*required</span>
                    </label>
                    <input
                        id="routine-name"
                        placeholder="e.g. Upper Day"
                        value={routineName}
                        onChange={(e) => setRoutineName(e.target.value)}
                        className="border p-2 rounded w-full"
                    />
                </div>

                <div className="relative mb-6">
                    <label htmlFor="exercise-search" className="sr-only">Search exercises</label>
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
                        <div key={ex.exerciseID} className="bg-white p-4 rounded shadow mb-4 flex justify-between items-center">
                            <p className="font-semibold">{ex.name}</p>
                            <div className="flex items-center gap-4">
                                <div className="flex items-center gap-2">
                                    <label htmlFor={`sets-${ex.exerciseID}`} className="text-sm text-gray-700">Sets</label>
                                    <input
                                        id={`sets-${ex.exerciseID}`}
                                        type="number"
                                        value={ex.sets}
                                        onChange={(e) => updateSets(ex.exerciseID, e.target.value)}
                                        className="border p-1 rounded w-16 text-center"
                                    />
                                </div>
                                <button
                                    onClick={() => removeExercise(ex.exerciseID)}
                                    className="text-red-700 text-sm"
                                    aria-label={`Remove ${ex.name}`}
                                >
                                    Remove
                                </button>
                            </div>
                        </div>
                    ))
                )}

                <div className="flex gap-4 mt-6">
                    <button
                        onClick={saveRoutine}
                        className="bg-blue-600 text-white px-6 py-2 rounded font-semibold hover:bg-blue-700"
                    >
                        Save Routine
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

export default NewRoutinePage