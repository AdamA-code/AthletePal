// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic done with claude sonnet AI
import { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import Sidebar from "../components/Sidebar"

function NewRoutinePage() {
    const navigate = useNavigate()

    const [routineName, setRoutineName] = useState("")
    const [availableExercises, setAvailableExercises] = useState([])
    const [selectedExercises, setSelectedExercises] = useState([])
    const [searchTerm, setSearchTerm] = useState("")
    const [showDropdown, setShowDropdown] = useState(false)
    const [error, setError] = useState("")

    // Fetch the preset exercise list when page loads
    useEffect(() => {
        const fetchExercises = async () => {
            try {
                const res = await fetch("http://localhost:3000/api/exercises")
                const data = await res.json()
                setAvailableExercises(Array.isArray(data.Exercises) ? data.Exercises : [])
            } catch (err) {
                console.error("Error fetching exercises:", err)
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
            sets: 3
        }])
        setSearchTerm("")
        setShowDropdown(false)
    }

    const removeExercise = (exerciseID) => {
        setSelectedExercises(selectedExercises.filter(ex => ex.exerciseID !== exerciseID))
    }

    const updateSets = (exerciseID, value) => {
        setSelectedExercises(selectedExercises.map(ex =>
            ex.exerciseID === exerciseID ? { ...ex, sets: value } : ex
        ))
    }

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
            workout: {
                workoutID: 0,
                createdBy: userID,
                assignedTo: userID,
                date: today,
                duration: null,
                workoutType: "weights",
                notes: routineName,   // routine name stored here
                routeID: null
            },
            exercises: selectedExercises.map(ex => ({
                workoutExercisesID: 0,
                workoutID: 0,
                exerciseID: ex.exerciseID,
                sets: parseInt(ex.sets),
                reps: null,     // not set at routine creation stage
                weight: null    // not set at routine creation stage
            }))
        }

        try {
            const res = await fetch("http://localhost:3000/api/log/workout", {
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

                {error && <p className="text-red-600 text-sm mb-4">{error}</p>}

                <input
                    placeholder="Routine Name (e.g. Upper Day)"
                    value={routineName}
                    onChange={(e) => setRoutineName(e.target.value)}
                    className="border p-2 rounded w-full mb-6"
                />

                {/* Exercise search */}
                <div className="relative mb-6">
                    <input
                        placeholder="Search and add an exercise..."
                        value={searchTerm}
                        onChange={(e) => { setSearchTerm(e.target.value); setShowDropdown(true) }}
                        onFocus={() => setShowDropdown(true)}
                        className="border p-2 rounded w-full"
                    />
                    {showDropdown && searchTerm && (
                        <div className="absolute z-10 bg-white border rounded shadow w-full max-h-48 overflow-y-auto">
                            {filteredExercises.length === 0 ? (
                                <p className="p-2 text-gray-500 text-sm">No exercises found</p>
                            ) : (
                                filteredExercises.map(ex => (
                                    <button
                                        key={ex.exerciseID}
                                        onClick={() => addExercise(ex)}
                                        className="w-full text-left px-3 py-2 hover:bg-blue-50 text-sm"
                                    >
                                        <span className="font-medium">{ex.name}</span>
                                        <span className="text-gray-400 ml-2 text-xs">{ex.category}</span>
                                    </button>
                                ))
                            )}
                        </div>
                    )}
                </div>

                {/* Selected exercises — sets only */}
                {selectedExercises.length === 0 ? (
                    <p className="text-gray-500 mb-6">No exercises added yet.</p>
                ) : (
                    selectedExercises.map(ex => (
                        <div key={ex.exerciseID} className="bg-white p-4 rounded shadow mb-4 flex justify-between items-center">
                            <p className="font-semibold">{ex.name}</p>
                            <div className="flex items-center gap-4">
                                <div className="flex items-center gap-2">
                                    <label className="text-sm text-gray-500">Sets</label>
                                    <input
                                        type="number"
                                        value={ex.sets}
                                        onChange={(e) => updateSets(ex.exerciseID, e.target.value)}
                                        className="border p-1 rounded w-16 text-center"
                                    />
                                </div>
                                <button
                                    onClick={() => removeExercise(ex.exerciseID)}
                                    className="text-red-500 text-sm"
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