import { useState } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import Sidebar from "../components/Sidebar"

function StartWorkoutPage() {

    const location = useLocation()
    const routine = location.state 

    const [exercises, setExercises] = useState(
        routine ? routine.exercises : []
    )

    const [name, setName] = useState(routine ? routine.name : "")
    const [notes, setNotes] = useState("")

    const addExercise = () => {
        setExercises([...exercises, {id: Date.now(), name: "", sets: []}])
    }

    const deleteExercise = (id) => {
        setExercises(exercises.filter(ex => ex.id !== id))
    }

    const updateExerciseName = (id, value) => {
        setExercises(exercises.map(ex =>
            ex.id === id ? {...ex, name:value} : ex
        ))
    }

    const addSet = (exerciseId) => {
        setExercises(exercises.map(ex => {
            if (ex.id === exerciseId) {
                return {
                    ...ex, sets: [...ex.sets, {reps: "", weight: ""}]
                }
            }
            return ex
        }))
    }

    const deleteSet = (exerciseId, index) => {
        setExercises(exercises.map(ex => {
            if (ex.id === exerciseId) {
                const updated = ex.sets.filter((_, i) => i !== index)
                return {...ex, sets:updated}
            }
            return ex
        }))
    }

    const updateSet = (exerciseId, index, field, value) => {
        setExercises(exercises.map(ex => {
            if (ex.id === exerciseId) {
                const updated = [...ex.sets]
                updated[index][field] = value
                return {...ex, sets: updated}
            }
            return ex
        }))
    }

    const saveWorkout = async () => {
        const workout = {name, notes, exercises}

        try {
            await fetch("/api/workouts", {
                method: "POST",
                headers: {"Content-Type":"application/json"},
                body: JSON.stringify(workout)
            })
            alert("Workout Saved!")
        } catch (err) {
            console.error(err)
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8 max-w-3xl">

                <h2 className="text-3xl font-bold mb-6">Workout</h2>

                <input
                placeholder="Workout Name"
                value = {name}
                onChange={(e) => setName(e.target.value)}
                className="border p-2 rounded w-full mb-3"
                />

                <textarea
                placeholder="Notes"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="border p-2 rounded w-full mb-6"
                />

                {exercises.map(ex => (
                    <div key={ex.id} className="bg-white p-4 rounded shadow mb-4">
                        <div className="flex justify-between mb-2">
                            <input
                            placeholder="Exercise Name"
                            value = {ex.name}
                            onChange={(e) =>
                                updateExerciseName(ex.id, e.target.value)
                            }
                            className="border p-2 rounded w-full mr-2"
                            />

                            <button onClick={() => deleteExercise(ex.id)} className="text-red-500">
                                X
                            </button>
                        </div>

                        {ex.sets.map((set, i) => (
                            <div key={i} className="flex gap-2 mb-2">

                                <input
                                placeholder="Reps"
                                value={set.reps}
                                onChange={(e) => updateSet(ex.id, i, "reps", e.target.value)}
                                className="border p-2 rounded w-1/3"
                                />

                                <input
                                placeholder="Weight"
                                value={set.weight}
                                onChange={(e) => updateSet(ex.id, i, "weight", e.target.value)}
                                className="border p-2 rounded w-1/3"
                                />

                                <button
                                onClick={() => deleteSet(ex.id, i)}
                                className="text-red-500"
                                >
                                    X
                                </button>
                            </div>
                        ))}

                        <button onClick={() => addSet(ex.id)} className="text-blue-600">
                            + Add Set
                        </button>

                    </div>
                ))}

                <button onClick={addExercise} className="bg-blue-500 text-white px-4 py-2 rounded mr-3">
                    + Add Exercise
                </button>

                <button onClick={saveWorkout} className="bg-gree-500 text-white px-4 py-2 rounded">
                    Save Workout
                </button>

            </main>
        </div>
    )

}

export default StartWorkoutPage