import { useState } from "react"
import Sidebar from "../components/Sidebar"

function TrainingGroupsPage() {
    const [groups, setGroups] = useState([
        {
            id: 1,
            name: "Leeds 10K Prep Group",
            description: "A group for runners preparing for upcoming 10K races.",
            members: 18,
            goal: "Complete Leeds 10K in under 55 minutes",
            nextSession: "Tuesday, 7:00 AM",
            joined: false
        },
        {
            id: 2,
            name: "Hypertrophy Lifters",
            description: "A group focused on muscle building, progressive overload, and consistency.",
            members: 24,
            goal: "Train at least 3 per week consistently",
            nextSession: "Wednesday, 5:00 PM",
            joined: false
        },
        {
            id: 3,
            name: "Begginer Fitness Community",
            description: "A supportive group for users building healthy exercise habits.",
            members: 31,
            goal: "Complete 3 workouts per week",
            nextSession: "Thursday, 6:30 PM",
            joined: false
        }
    ])

    const handleJoinGroup = (groupId) => {
        setGroups((prevGroups) =>
            prevGroups.map((group) =>
                group.id === groupId && !group.joined
                    ? { 
                        ...group, 
                        joined: true, 
                        members: group.members + 1 
                    }
                    : group
            )
        )
    }

    return (
        <div className="flex-1 min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h1 className="text-3x1 font-bold text-gray-800 mb-6">
                    Training Groups
                </h1>

                <p className="text-gray-600 mb-8 max-w-3x1">
                    Join virtual training groups, work towards shared goals, and stay motivated together.
                </p>

                <div className="grid grid-cols-1 x1:grid-cols-3 gap-8">
                    {/* Main group card */}
                    <div className="x1:col-span-2 space-y-6">
                        {groups.map((group) => (
                            <div 
                                key={group.id} 
                                className="bg-white rounded-x1 shadow-md p-6"
                            >
                                <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
                                    <div>
                                        <h2 className="text-2x1 font-semibold text-gray-800 mb-2">
                                            {group.name}
                                        </h2>
                                        <p className="text-gray-600 mb-4">
                                            {group.description}
                                        </p>

                                        <div className="space-y-2 text-sm text-gray-700">
                                            <p>
                                                <span className="font-medium">
                                                    Members:
                                                </span> 
                                                {group.members}
                                            </p>
                                            <p>
                                                <span className="font-medium">
                                                    Goal:
                                                </span> 
                                                {group.goal}
                                            </p>
                                            <p>
                                                <span className="font-medium">
                                                    Next Session:
                                                </span> 
                                                {group.nextSession}
                                            </p>
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => handleJoinGroup(group.id)}
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
                        ))}
                    </div>

                    {/* Sidebar info cards */}
                    <div className="space-y-6">
                        <div className="bg-white rounded-x1 shadow-md p-6">
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
                        <div className="bg-white rounded-x1 shadow-md p-6">
                            <h2 className="text-x1 font-semibold text-gray-800 mb-4">
                                Create a Group
                            </h2>

                            <p className="text-gray-600 mb-4">
                                Start your own training group, set goals, and invite friends to join you on your fitness journey.
                            </p>

                            <button
                                onClick={() => alert("Create group feature coming soon")}
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

