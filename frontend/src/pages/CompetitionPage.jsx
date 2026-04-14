import { useEffect, useState } from "react"
import Sidebar from "../components/Sidebar"

function CompetitionPage() {

    const [type, setType] = useState("Running")
    const [name, setName] = useState("")
    const [date, setDate] = useState("")
    const [result, setResult] = useState({})
    const [error, setError] = useState("") // error handling for adding a competition

    const [upcoming, setUpcoming] = useState(() => {
        const saved = localStorage.getItem("upcomingCompetitions")
        return saved ? JSON.parse(saved) : []
    })
    const [completed, setCompleted] = useState(() => {
        const saved = localStorage.getItem("completedCompetitions")
        return saved ? JSON.parse(saved) : []
    })

    useEffect(() => {
        localStorage.setItem("upcomingCompetitions", JSON.stringify(upcoming))
    }, [upcoming])

    useEffect(() => {
        localStorage.setItem("completedCompetitions", JSON.stringify(completed))
    }, [completed]) // added this and the one above and changes to local storage so that once its added to a computer it wont change when a user changes onto a different page and then back to the competition page

    const handleAddCompetition = () => {
        if (!name || !date) {
            setError("Please enter both competition name and date.")
            return
        }
             

        const newComp = {
            id: Date.now(),
            name,
            type,
            date
        }

        setUpcoming([...upcoming, newComp])

        setName("")
        setDate("")
        setError("")

    }

    const handleComplete = (comp) => {
        const resultValue = result[comp.id]

        if (!resultValue) {
            setError("Please enter a result before completing the competition")
            return
        }

        const newCompleted = {
            ...comp,
            result: Number(resultValue)
        }

        setCompleted([...completed, newCompleted])
        setUpcoming(upcoming.filter((c) => c.id !== comp.id))
        setResult((prev) => {
            const updated = { ...prev}
            delete updated[comp.id]
            return updated
        })
        setError("")
    }

    const getPB = (type) => {

        const comps = completed.filter(c => c.type === type)

        if (comps.length === 0) return null

        if (["Squat", "Bench", "Deadlift"].includes(type)) {
            return Math.max(...comps.map((c) => c.result))
        }
        return Math.min(...comps.map(c => c.result))

    }

    return (
        <div className="min-h-screen bg-gray-100 flex">

            <Sidebar />

            <main className="flex-1 p-8 max-w-3xl">

                <h2 className="text-3xl font-bold mb-8">Competitions</h2>

                {error && (
                    <p className="text-red-600 mb-4">{error}</p>
                )}

                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">Add Competitions</h3>

                    <div className="mb-4">
                        <label className="block font-semibold mb-2">Type</label>
                        <select
                            value={type}
                            onChange={(e) => setType(e.target.value)}
                            className="border rounded p-2 w-full"
                        >
                            <option>Squat</option>
                            <option>Bench</option>
                            <option>Deadlift</option>
                            <option>Running</option>
                            <option>Swimming</option>
                            <option>Cycling</option>
                        </select>
                    </div>

                    <div className="mb-4">
                        <label className="block font-semibold mb-2">Competition Name</label>
                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="border rounded p-2 w-full"
                            placeholder="Enter competition name"
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block font-semibold mb-2">Date</label>
                        <input
                            type="date"
                            value={date}
                            onChange={(e) => setDate(e.target.value)}
                            className="border rounded p-2 w-full"
                        />
                    </div>

                    <button
                        onClick={handleAddCompetition}
                        className="bg-blue-500 text-white px-4 py-2 rounded font-semibold hover:bg-blue-600"
                    >
                        Add Competition
                    </button>
                </div>

                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">Upcoming Competitions</h3>

                    {upcoming.length === 0 && <p>No upcoming competitions.</p>}

                    {upcoming.map(comp => (
                        <div key={comp.id} className="bg-white p-4 rounded shadow mb-4">
                            <p className="font-semibold">{comp.name}</p>
                            <p className="text-sm">
                                {comp.type} | {comp.date}
                            </p>

                            <div className="mt-3 flex gap-2">
                                <input
                                    type="number"
                                    placeholder={
                                        ["Squat","Bench","Deadlift"].includes(comp.type)
                                        ? "Weight (kg)"
                                        : "Time (minutes)"
                                    }
                                    value={result[comp.id] || ""}
                                    onChange={(e) => 
                                        setResult({
                                            ...result,
                                            [comp.id]: e.target.value
                                        })}
                                    className="border rounded p-2"
                                />

                                <button
                                    onClick={() => handleComplete(comp)}
                                    className="bg-green-500 text-white px-3 py-2 rounded"
                                >
                                    Complete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>


                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">Completed Competitions</h3>

                    {completed.length === 0 && <p>No completed competitions.</p>}

                    {completed.map(comp => (
                        <div key={comp.id} className="bg-white p-4 rounded shadow mb-4">
                            <p className="font-semibold">{comp.name}</p>
                            <p className="text-sm">
                                {comp.type} | {comp.date} | Result: {comp.result}
                            </p>
                        </div>
                    ))}
                </div>


                <div className="bg-white p-6 rounded-x1 shadow">
                    <h3 className="text-xl font-semibold mb-4">Personal Bests</h3>

                    {["Squat","Bench","Deadlift","Running","Swimming","Cycling"].map(t => {
                        const pb = getPB(t)
                        return (
                            <p key={t} className="mb-2">
                                <strong>{t}:</strong> {pb ? pb : "No record"}
                            </p>
                        )
                    })}
                </div>

            </main>

        </div>
    )

}

export default CompetitionPage