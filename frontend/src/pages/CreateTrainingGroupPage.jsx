// page structure, layout, and frontend -> backend request connections aided with chatgpt chat.openai.com
import { useState } from "react"
import { useNavigate } from "react-router-dom"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function CreateTrainingGroupPage() {
    const navigate = useNavigate()

    const [name, setName] = useState("")
    const [description, setDescription] = useState("")
    const [goal, setGoal] = useState("")
    const [error, setError] = useState("")

    // Sends a new training group to the backend for the logged-in user.
    const handleCreateGroup = async (event) => {
        event.preventDefault()

        const userID = localStorage.getItem("userID")

        if (!userID) {
            setError("No user found. Please log in again.")
            return
        }

        if (!name) {
            setError("Please enter a group name.")
            return
        }

        try {
            const response = await fetch(`${API_BASE_URL}/social/group`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    groupID: 0,
                    createdBy: Number(userID),
                    name,
                    description,
                    goal
                })
            })

            const data = await response.json()

            if (response.ok) {
                navigate("/training-groups")
            } else {
                setError(data.message || "Could not create group.")
            }
        } catch (err) {
            console.error("Create group error:", err)
            setError("Could not connect to server.")
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h1 className="text-3xl font-bold text-gray-800 mb-6">
                    Create Training Group
                </h1>

                <div className="bg-white rounded-xl shadow-md p-6 max-w-2xl">
                    {error && (
                        <p className="text-red-700 text-sm mb-4" role="alert">
                            {error}
                        </p>
                    )}

                    <form onSubmit={handleCreateGroup} className="space-y-4">
                        <div>
                            <label htmlFor="groupName" className="block font-semibold mb-1">
                                Group Name
                            </label>
                            <input
                                id="groupName"
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="e.g. Leeds 10K Prep Group"
                                className="border rounded p-2 w-full"
                            />
                        </div>

                        <div>
                            <label htmlFor="description" className="block font-semibold mb-1">
                                Description
                            </label>
                            <textarea
                                id="description"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                placeholder="Describe what the group is for"
                                className="border rounded p-2 w-full"
                            />
                        </div>

                        <div>
                            <label htmlFor="goal" className="block font-semibold mb-1">
                                Group Goal
                            </label>
                            <input
                                id="goal"
                                type="text"
                                value={goal}
                                onChange={(e) => setGoal(e.target.value)}
                                placeholder="e.g. Complete a 10K in under 55 minutes"
                                className="border rounded p-2 w-full"
                            />
                        </div>

                        <div className="flex gap-3">
                            <button
                                type="submit"
                                className="bg-blue-700 text-white px-5 py-2 rounded-lg hover:bg-blue-800"
                            >
                                Create Group
                            </button>

                            <button
                                type="button"
                                onClick={() => navigate("/training-groups")}
                                className="border border-gray-300 px-5 py-2 rounded-lg hover:bg-gray-50"
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            </main>
        </div>
    )
}

export default CreateTrainingGroupPage