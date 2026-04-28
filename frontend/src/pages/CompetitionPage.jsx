// Frontend-backend connection logic generated with Claude Sonnet (claude.ai)
import { useEffect, useState } from "react"
import Sidebar from "../components/Sidebar"

function CompetitionPage() {
    const [competitions, setCompetitions] = useState([])
    const [name, setName] = useState("")
    const [date, setDate] = useState("")
    const [rank, setRank] = useState("")
    const [resultMins, setResultMins] = useState("")
    const [resultSecs, setResultSecs] = useState("")
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    const today = new Date().toISOString().split("T")[0]

    const formatResult = (seconds) => {
        if (!seconds) return null
        const mins = Math.floor(seconds / 60)
        const secs = seconds % 60
        return `${mins}:${secs.toString().padStart(2, "0")}`
    }

    // Split competitions into upcoming and completed based on date
    const upcoming = competitions.filter(c => c.date >= today)
    const completed = competitions.filter(c => c.date < today)

    useEffect(() => {
        const userID = localStorage.getItem("userID")
        if (!userID) {
            setError("Not logged in. Please log in again.")
            return
        }
        fetchCompetitions(userID)
    }, [])

    const fetchCompetitions = async (userID) => {
        try {
            const res = await fetch(`http://localhost:3000/api/competitions/${userID}`)
            const data = await res.json()
            setCompetitions(Array.isArray(data.Competitions) ? data.Competitions : [])
        } catch (err) {
            console.error("Error fetching competitions:", err)
            setError("Could not load competitions.")
        }
    }

    const handleAddCompetition = async () => {
        const userID = parseInt(localStorage.getItem("userID"))
        if (!userID) {
            setError("Not logged in.")
            return
        }
        if (!name || !date) {
            setError("Please enter both a competition name and date.")
            return
        }

        setError("")
        setSuccess("")

        try {
            const res = await fetch("http://localhost:3000/api/competition", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    competitionID: 0,
                    userID: userID,
                    name: name,
                    date: date,
                    rank: rank ? parseInt(rank) : null,
                    result: (resultMins || resultSecs) ? (parseInt(resultMins || 0) * 60) + parseInt(resultSecs || 0) : null
                })
            })
            const data = await res.json()

            if (res.ok) {
                setSuccess("Competition added!")
                setName("")
                setDate("")
                setRank("")
                setResultMins("")
                setResultSecs("")
                // Refresh the list
                fetchCompetitions(userID)
            } else {
                setError(data.message || "Failed to add competition.")
            }
        } catch (err) {
            console.error(err)
            setError("Could not connect to server.")
        }
    }

    const handleDelete = async (competitionID) => {
        try {
            const res = await fetch(`http://localhost:3000/api/competition/${competitionID}`, {
                method: "DELETE"
            })
            if (res.ok) {
                const userID = localStorage.getItem("userID")
                fetchCompetitions(userID)
            } else {
                setError("Failed to delete competition.")
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
                <h2 className="text-3xl font-bold mb-8">Competitions</h2>

                {error && <p className="text-red-600 mb-4">{error}</p>}
                {success && <p className="text-green-600 mb-4">{success}</p>}

                {/* Add competition form */}
                <div className="bg-white p-6 rounded-lg shadow mb-8">
                    <h3 className="text-xl font-semibold mb-4">Add Competition</h3>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                        <div>
                            <label className="block text-sm text-gray-500 mb-1">Competition Name</label>
                            <input
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="e.g. Leeds 10K"
                                className="border rounded p-2 w-full"
                            />
                        </div>
                        <div>
                            <label className="block text-sm text-gray-500 mb-1">Date</label>
                            <input
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                className="border rounded p-2 w-full"
                            />
                        </div>
                        <div>
                            <label className="block text-sm text-gray-500 mb-1">
                                Finishing Position (optional)
                            </label>
                            <input
                                type="number"
                                value={rank}
                                onChange={(e) => setRank(e.target.value)}
                                placeholder="e.g. 3"
                                className="border rounded p-2 w-full"
                            />
                        </div>
                        <div>
                            <label className="block text-sm text-gray-500 mb-1">
                                Result (optional)
                            </label>
                            <div className="flex gap-2">
                                <input
                                    type="number"
                                    value={resultMins}
                                    onChange={(e) => setResultMins(e.target.value)}
                                    placeholder="mins"
                                    className="border rounded p-2 w-full"
                                />
                                <input
                                    type="number"
                                    value={resultSecs}
                                    onChange={(e) => setResultSecs(e.target.value)}
                                    placeholder="secs"
                                    className="border rounded p-2 w-full"
                                />
                            </div>
                        </div>
                    </div>

                    <button
                        onClick={handleAddCompetition}
                        className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
                    >
                        Add Competition
                    </button>
                </div>

                {/* Upcoming competitions */}
                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">Upcoming Competitions</h3>
                    {upcoming.length === 0 ? (
                        <p className="text-gray-500">No upcoming competitions.</p>
                    ) : (
                        upcoming.map(comp => (
                            <div key={comp.competitionID} className="bg-white p-4 rounded shadow mb-4 flex justify-between items-center">
                                <div>
                                    <p className="font-semibold">{comp.name}</p>
                                    <p className="text-sm text-gray-500">{comp.date}</p>
                                </div>
                                <button
                                    onClick={() => handleDelete(comp.competitionID)}
                                    className="text-red-500 text-sm hover:underline"
                                >
                                    Remove
                                </button>
                            </div>
                        ))
                    )}
                </div>

                {/* Completed competitions */}
                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">Completed Competitions</h3>
                    {completed.length === 0 ? (
                        <p className="text-gray-500">No completed competitions.</p>
                    ) : (
                        completed.map(comp => (
                            <div key={comp.competitionID} className="bg-white p-4 rounded shadow mb-4 flex justify-between items-center">
                                <div>
                                    <p className="font-semibold">{comp.name}</p>
                                    <p className="text-sm text-gray-500">{comp.date}</p>
                                    {comp.rank && (
                                        <p className="text-sm text-gray-600">
                                            Position: {comp.rank}
                                        </p>
                                    )}
                                    {comp.result && (
                                        <p className="text-sm text-gray-600">
                                            Result: {formatResult(comp.result)}
                                        </p>
                                    )}
                                </div>
                                <button
                                    onClick={() => handleDelete(comp.competitionID)}
                                    className="text-red-500 text-sm hover:underline"
                                >
                                    Remove
                                </button>
                            </div>
                        ))
                    )}
                </div>

            </main>
        </div>
    )
}

export default CompetitionPage