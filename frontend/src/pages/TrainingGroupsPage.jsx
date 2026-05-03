// page structure, layout, and frontend -> backend request connections aided with chatgpt chat.openai.com
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function TrainingGroupsPage() {
    const navigate = useNavigate()

    const [groups, setGroups] = useState([])
    const [error, setError] = useState("")
    const [loading, setLoading] = useState(true)

    // Fetches all public training groups and marks groups joined by the logged-in user.
    useEffect(() => {
        const userID = localStorage.getItem("userID")

        if (!userID) {
            setError("No user found. Please log in again.")
            setLoading(false)
            return
        }

        const fetchGroups = async () => {
            try {
                const [allGroupsRes, userGroupsRes] = await Promise.all([
                    fetch(`${API_BASE_URL}/social/groups`),
                    fetch(`${API_BASE_URL}/social/groups/${userID}`)
                ])

                const allGroupsData = await allGroupsRes.json()
                const userGroupsData = await userGroupsRes.json()

                const allGroups = allGroupsData.Groups || []
                const userGroups = userGroupsData["Training Groups"] || []

                const formattedGroups = await Promise.all(
                    allGroups.map(async (group) => {
                        let members = 0

                        try {
                            const membersRes = await fetch(`${API_BASE_URL}/social/group/members/${group.groupID}`)
                            const membersData = await membersRes.json()
                            members = membersData["Group Members"]?.length || 0
                        } catch {
                            members = 0
                        }

                        return {
                            ...group,
                            members,
                            joined: userGroups.some((userGroup) => userGroup.groupID === group.groupID)
                        }
                    })
                )

                setGroups(formattedGroups)
            } catch (err) {
                console.error("Training groups error:", err)
                setError("Failed to load training groups.")
            } finally {
                setLoading(false)
            }
        }

        fetchGroups()
    }, [])

    // Adds the logged-in user to the selected training group.
    const handleJoinGroup = async (groupID) => {
        const userID = localStorage.getItem("userID")

        if (!userID) {
            setError("No user found. Please log in again.")
            return
        }

        try {
            const response = await fetch(`${API_BASE_URL}/social/group/${groupID}/${userID}`, {
                method: "PUT"
            })

            if (response.ok) {
                setGroups((prevGroups) =>
                    prevGroups.map((group) =>
                        group.groupID === groupID
                            ? {
                                ...group,
                                joined: true,
                                members: group.members + 1
                            }
                            : group
                    )
                )
            } else {
                const data = await response.json()
                setError(data.message || "Could not join group.")
            }
        } catch (err) {
            console.error("Join group error:", err)
            setError("Could not connect to server.")
        }
    }

    return (
        <div className="flex-1 min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h1 className="text-3xl font-bold text-gray-800 mb-6">
                    Training Groups
                </h1>

                <p className="text-gray-600 mb-8 max-w-3xl">
                    Join virtual training groups, work towards shared goals, and stay motivated together.
                </p>

                {error && (
                    <p className="text-red-700 text-sm mb-4" role="alert">
                        {error}
                    </p>
                )}

                {loading && (
                    <p className="text-gray-600 mb-4">
                        Loading training groups...
                    </p>
                )}

                <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
                    <div className="xl:col-span-2 space-y-6">
                        {groups.length > 0 ? (
                            groups.map((group) => (
                                <div
                                    key={group.groupID}
                                    className="bg-white rounded-xl shadow-md p-6"
                                >
                                    <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
                                        <div>
                                            <h2 className="text-2xl font-semibold text-gray-800 mb-2">
                                                {group.name}
                                            </h2>

                                            <p className="text-gray-600 mb-4">
                                                {group.description || "No description added."}
                                            </p>

                                            <div className="space-y-2 text-sm text-gray-700">
                                                <p>
                                                    <span className="font-medium">Members:</span>{" "}
                                                    {group.members}
                                                </p>
                                                <p>
                                                    <span className="font-medium">Goal:</span>{" "}
                                                    {group.goal || "No goal added."}
                                                </p>
                                            </div>
                                        </div>

                                        <button
                                            onClick={() => handleJoinGroup(group.groupID)}
                                            disabled={group.joined}
                                            className={`px-5 py-2 rounded-lg whitespace-nowrap text-white ${
                                                group.joined
                                                    ? "bg-green-600 cursor-default"
                                                    : "bg-blue-700 hover:bg-blue-800"
                                            }`}
                                        >
                                            {group.joined ? "Joined" : "Join Group"}
                                        </button>
                                    </div>
                                </div>
                            ))
                        ) : (
                            !loading && (
                                <div className="bg-white rounded-xl shadow-md p-6">
                                    <p className="text-gray-600">
                                        No training groups found. Create the first one.
                                    </p>
                                </div>
                            )
                        )}
                    </div>

                    <div className="space-y-6">
                        <div className="bg-white rounded-xl shadow-md p-6">
                            <h2 className="text-xl font-semibold text-gray-800 mb-4">
                                Group Challenge
                            </h2>
                            <p className="text-gray-600 mb-3">
                                This week's challenge:
                            </p>
                        </div>

                        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                            <p className="font-medium text-blue-800">
                                Complete 20 total group workouts this week
                            </p>
                        </div>

                        <div className="bg-white rounded-xl shadow-md p-6">
                            <h2 className="text-xl font-semibold text-gray-800 mb-4">
                                Create a Group
                            </h2>

                            <p className="text-gray-600 mb-4">
                                Start your own training group, set goals, and invite friends to join you.
                            </p>

                            <button
                                onClick={() => navigate("/training-groups/create")}
                                className="w-full bg-green-600 text-white py-2 rounded-lg hover:bg-green-700"
                            >
                                Create Group
                            </button>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    )
}

export default TrainingGroupsPage