import { useState } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import Sidebar from "../components/Sidebar"

function NewRoutinePage() {

    const navigate = useNavigate()

    const [name, setName] = useState("")
    const [exercises, setExercises] = useState([])

    const addExercise = () => {
        setExercises([...exercises, {id: Date.now(), name: "", sets: []}])
    }

    const updateExerciseName = (id, value) => {
        setExercises(exercises.map(ex => 
            ex.id === id ? {...ex, name: value} : ex
        ))
    }

    const addSet = (exerciseId) => {
        setExercises(exercises.map(ex => {
            if (ex.id === exerciseId) {
                return {
                    ...ex,
                    sets: [...ex.sets, {reps: "", weights: ""}]
                }
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

    const saveRoutine = async() => {

        const routine = {name, exercises}

        try {
            await fetch("/api/routines", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(routine)
            })
            alert("Routine Saved")
            navigate("/workout-log")
        } catch (err) {
            console.error(err)
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar/>

            <main className="flex-1 p-8 max-w-3xl">

                <h2 className="text-3xl font-bold mb-6">
                    New Routine
                </h2>

                <input
                placeholder="Routine Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="border p-2 rounded w-full mb-4"
                />

                {exercises.map(ex => (
                    <div key={ex.id} className="bg-white p-4 rounded shadow mb-4">

                        <input
                        placeholder="Exercise Name"
                        value={ex.name}
                        onChange={(e) =>
                            updateExerciseName(ex.id, e.target.value)
                        }
                        className="border p-2 rounded w-full mb-2"
                        />

                        {ex.sets.map((set, i) => (
                            <div key={i} className="flex gap-2 mb-2">
                                <input
                                placeholder="Reps"
                                value={set.reps}
                                onChange={(e) =>
                                    updateSet(ex.id, i, "reps", e.target.value)
                                }
                                className="border p-2 rounded w-1/2"
                                />

                                <input
                                placeholder="Weight"
                                value={set.weight}
                                onChange={(e) => 
                                    updateSet(ex.id, i, "weight", e.target.value)
                                }
                                className="border p-2 rounded w-1/2"
                                />

                            </div>
                        ))}

                        <button onClick={() => addSet(ex.id)} className="text-blue-600">
                            Add Set
                        </button>
                    </div>
                ))}

                <button onClick={addExercise} className="bg-blue-500 text-white px-4 py-2 rounded mr-3">
                    Add Exercise
                </button>

                <button onClick={saveRoutine} className="bg-blue-500 text-white px-4 py-2 rounded">
                    Save Routine
                </button>
            </main>
        </div>
    )
}

export default NewRoutinePage