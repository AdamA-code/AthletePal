// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic and accessibility improvements done with Claude Sonnet (claude.ai)
import { useEffect, useState } from "react"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function CompetitionPage() {
    const [competitions, setCompetitions] = useState([])
    const [name, setName] = useState("")
    const [date, setDate] = useState("")
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")
    const [editingID, setEditingID] = useState(null)
    const [editRank, setEditRank] = useState("")
    const [editMins, setEditMins] = useState("")
    const [editSecs, setEditSecs] = useState("")
    const [resultType, setResultType] = useState("time") // "time" or "score"

    const today = new Date().toISOString().split("T")[0]

    // Splits competitions into three lists based on date comparison with today
    const upcoming = competitions.filter(c => c.date > today)
    const todayComps = competitions.filter(c => c.date === today)
    const completed = competitions.filter(c => c.date < today)

    // Converts a result stored in total seconds to a mm:ss display string
    const formatResult = (seconds) => {
        if (!seconds) return null
        const mins = Math.floor(seconds / 60)
        const secs = seconds % 60
        return `${mins}:${secs.toString().padStart(2, "0")}`
    }

    // Fetches all competitions for the logged in user on page load
    useEffect(() => {
        const userID = localStorage.getItem("userID")
        if (!userID) {
            setError("Not logged in. Please log in again.")
            return
        }
        fetchCompetitions(userID)
    }, [])

    // Fetches all competitions for a given userID and updates the competitions state
    const fetchCompetitions = async (userID) => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/competitions/${userID}`)
            const data = await res.json()
            setCompetitions(Array.isArray(data.Competitions) ? data.Competitions : [])
        } catch (err) {
            console.error("Error fetching competitions:", err)
            setError("Could not load competitions.")
        }
    }

    // Validates inputs and sends a POST request to create a new competition with name and date only
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
            const res = await fetch(`${API_BASE_URL}/api/competition`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    competitionID: 0,
                    userID: userID,
                    name: name,
                    date: date,
                    rank: null,
                    result: null
                })
            })
            const data = await res.json()

            if (res.ok) {
                setSuccess("Competition added!")
                setName("")
                setDate("")
                fetchCompetitions(userID)
            } else {
                setError(data.message || "Failed to add competition.")
            }
        } catch (err) {
            console.error(err)
            setError("Could not connect to server.")
        }
    }

    // Sends a DELETE request to remove a competition and refreshes the list
    const handleDelete = async (competitionID) => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/competition/${competitionID}`, {
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

    // Opens the inline edit form for a competition and pre-fills existing rank and result values
    const startEditing = (comp) => {
        setEditingID(comp.competitionID)
        setEditRank(comp.rank || "")
        setResultType(comp.result ? "time" : "score") // Assume if there's a result it's a time, otherwise score. Adjust as needed based on your data structure.
        if (comp.result) {
            setEditMins(Math.floor(comp.result / 60).toString())
            setEditSecs((comp.result % 60).toString())
        } else {
            setEditMins("")
            setEditSecs("")
        }
    }

    // Closes the inline edit form and clears the edit state
    const cancelEditing = () => {
        setEditingID(null)
        setEditRank("")
        setEditMins("")
        setEditSecs("")
    }

    // Sends a PUT request to update the rank and result of an existing competition
    // Converts mins and secs inputs into total seconds before sending to the backend
    const handleSaveEdit = async (comp) => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/competition/${comp.competitionID}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    name: comp.name,
                    date: comp.date,
                    rank: editRank ? parseInt(editRank) : null,
                    result: (editMins || editSecs)
                        ? (parseInt(editMins || 0) * 60) + parseInt(editSecs || 0)
                        : null
                })
            })

            if (res.ok) {
                setSuccess("Competition updated!")
                setEditingID(null)
                const userID = localStorage.getItem("userID")
                fetchCompetitions(userID)
            } else {
                setError("Failed to update competition.")
            }
        } catch (err) {
            console.error(err)
            setError("Could not connect to server.")
        }
    }

    // Renders a single competition card with optional inline edit form
    // showEdit controls whether the Add/Edit Result button and form are shown
    const renderCompCard = (comp, showEdit) => (
        <div key={comp.competitionID} className="bg-white p-4 rounded shadow mb-4">
            <div className="flex justify-between items-start">
                <div>
                    <p className="font-semibold">{comp.name}</p>
                    <p className="text-sm text-gray-600">{comp.date}</p>
                    {comp.rank && (
                        <p className="text-sm text-gray-600">Position: {comp.rank}</p>
                    )}
                    {comp.result && (
                        <p className="text-sm text-gray-600">Result: {formatResult(comp.result)}</p>
                    )}
                </div>
                <div className="flex gap-3 items-center">
                    {showEdit && editingID !== comp.competitionID && (
                        <button
                            onClick={() => startEditing(comp)}
                            className="text-blue-600 text-sm hover:underline"
                            aria-label={`Edit result for ${comp.name}`}
                        >
                            {comp.rank || comp.result ? "Edit Result" : "Add Result"}
                        </button>
                    )}
                    <button
                        onClick={() => handleDelete(comp.competitionID)}
                        className="text-red-700 text-sm hover:underline"
                        aria-label={`Remove ${comp.name}`}
                    >
                        Remove
                    </button>
                </div>
            </div>

            {/*Inline edit form*/}
            {showEdit && editingID === comp.competitionID && (
                <div className="mt-4 border-t pt-4">
                    <p className="text-sm font-semibold text-gray-700 mb-3">Edit Result</p>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                        <div>
                            <label htmlFor={`edit-rank-${comp.competitionID}`} className="block text-sm text-gray-700 mb-1">
                                Finishing Position (optional)
                            </label>
                            <input
                                id={`edit-rank-${comp.competitionID}`}
                                type="number"
                                value={editRank}
                                onChange={(e) => setEditRank(e.target.value)}
                                placeholder="e.g. 3"
                                className="border rounded p-2 w-full"
                            />
                        </div>
                        <div>
                            <label className="block text-sm text-gray-700 mb-1">
                                Result type
                            </label>
                            
                            <select
                                value={resultType}
                                onChange={(e) => setResultType(e.target.value)}
                                className="border rounded p-2 w-full mb-3"
                            >
                                <option value="time">Time</option>
                                <option value="score">Score / Weight </option>
                            </select>
                            <p className="text-xs text-gray-500 mb-2">
                                For timed events, enter minutes and seconds. For strength events, enter the score or weight in the minutes box and leave seconds blank.
                            </p>
                            
                            <div className="flex gap-2">
                                <div className="flex-1">
                                    <label htmlFor={`edit-mins-${comp.competitionID}`} className="sr-only">Minutes</label>
                                    <input
                                        id={`edit-mins-${comp.competitionID}`}
                                        type="number"
                                        value={editMins}
                                        onChange={(e) => setEditMins(e.target.value)}
                                        placeholder="mins"
                                        className="border rounded p-2 w-full"
                                    />
                                </div>
                                <div className="flex-1">
                                    <label htmlFor={`edit-secs-${comp.competitionID}`} className="sr-only">Seconds</label>
                                    <input
                                        id={`edit-secs-${comp.competitionID}`}
                                        type="number"
                                        value={editSecs}
                                        onChange={(e) => setEditSecs(e.target.value)}
                                        placeholder="secs"
                                        className="border rounded p-2 w-full"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="flex gap-3">
                        <button
                            onClick={() => handleSaveEdit(comp)}
                            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 text-sm"
                        >
                            Save
                        </button>
                        <button
                            onClick={cancelEditing}
                            className="border border-gray-400 text-gray-600 px-4 py-2 rounded hover:bg-gray-100 text-sm"
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            )}
        </div>
    )

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            <main className="flex-1 p-8 max-w-3xl">
                <h2 className="text-3xl font-bold mb-8">Competitions</h2>

                {error && <p className="text-red-700 mb-4" role="alert">{error}</p>}
                {success && <p className="text-green-700 mb-4" role="status">{success}</p>}

                {/*Add competition form — name and date only*/}
                <div className="bg-white p-6 rounded-lg shadow mb-8">
                    <h3 className="text-xl font-semibold mb-4">Add Competition</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                        <div>
                            <label htmlFor="comp-name" className="block text-sm text-gray-700 mb-1">
                                Competition Name
                            </label>
                            <input
                                id="comp-name"
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="e.g. Leeds 10K"
                                className="border rounded p-2 w-full"
                            />
                        </div>
                        <div>
                            <label htmlFor="comp-date" className="block text-sm text-gray-700 mb-1">
                                Date
                            </label>
                            <input
                                id="comp-date"
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                className="border rounded p-2 w-full"
                            />
                        </div>
                    </div>
                    <button
                        onClick={handleAddCompetition}
                        className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
                    >
                        Add Competition
                    </button>
                </div>

                {/*Today's competitions*/}
                {todayComps.length > 0 && (
                    <div className="mb-8">
                        <h3 className="text-xl font-semibold mb-4">
                            Today's Competitions 🏁
                        </h3>
                        {todayComps.map(comp => renderCompCard(comp, true))}
                    </div>
                )}

                {/*Upcoming competitions*/}
                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">Upcoming Competitions</h3>
                    {upcoming.length === 0 ? (
                        <p className="text-gray-600">No upcoming competitions.</p>
                    ) : (
                        upcoming.map(comp => renderCompCard(comp, false))
                    )}
                </div>

                {/*Completed competitions*/}
                <div className="mb-8">
                    <h3 className="text-xl font-semibold mb-4">Completed Competitions</h3>
                    {completed.length === 0 ? (
                        <p className="text-gray-600">No completed competitions.</p>
                    ) : (
                        completed.map(comp => renderCompCard(comp, true))
                    )}
                </div>

            </main>
        </div>
    )
}

export default CompetitionPage