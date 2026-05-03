// page structure, layout, and frontend -> backend request connections aided with chatgpt chat.openai.com
import Sidebar from "../components/Sidebar"
import { useEffect, useState } from "react"
import API_BASE_URL from "../config"

function StatisticsPage() {
    const [summaryStats, setSummaryStats] = useState([])
    const [progressionData, setProgressionData] = useState([])
    const [personalBests, setPersonalBests] = useState([])
    const [error, setError] = useState("")
    const [competitionStats, setCompetitionStats] = useState([])
    const [exerciseID, setExerciseID] = useState("")
    const [scoreValue, setScoreValue] = useState("")
    const [pbSuccess, setPbSuccess] = useState("")

    useEffect(() => {
        const userID = localStorage.getItem("userID")

        if (!userID) {
            setError("No user found. Please log in again.")
            return
        }
        
        const fetchStatistics = async () => {
            try {
                const [workoutRes, hoursRes, streakRes, historyRes, pbRes] = await Promise.all([
                    fetch(`${API_BASE_URL}/api/statistic/user/workouts-completed/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistic/user/hours-trained/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistic/user/workout-streak/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistic/user/training-history/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistics/user/personal-bests/${userID}`)
                ])

            const workoutData = await workoutRes.json()
            const hoursData = await hoursRes.json()
            const streakData = await streakRes.json()
            const historyData = await historyRes.json()
            const pbsData = await pbRes.json()
            
            {/* Console Logs for testing purposes 
            console.log("Workout data:", workoutData)
            console.log("Hours data:", hoursData)
            console.log("Streak data:", streakData)
            console.log("History data:", historyData)
            console.log("Personal bests data:", pbsData)
            */}

            setSummaryStats([
                { label: "Total Workouts", value: workoutData["Completed Workouts"] || 0 },
                { 
                    label: "Hours Trained",
                    value: Number(hoursData["Hours Trained"] || 0).toFixed(1)
                },
                { label: "Current Streak", value: streakData["Current Streak"] || 0 },
                { label: "Personal Bests", value: pbsData["Personal Bests"]?.length || 0 }
            ])

                setProgressionData(historyData["Training History"] || [])
                setPersonalBests(pbsData["Personal Bests"] || [])
                setCompetitionStats([])
            } catch (err) {
                console.error("Error fetching statistics:", err)
                setError("Failed to fetch statistics.")
            }
        }

        fetchStatistics()
    }, [])

    const handleAddPersonalBest = async (event) => {
        event.preventDefault()
        const userID = localStorage.getItem("userID")
        if (!userID) {
            setError("No user found. Please log in again.")
            return
        }

        if (!exerciseID || !scoreValue) {
            setError("Please enter both exerciseID")
        }
    }
    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h1 className="text-3x1 font-bold text-blue-800 mb-6">
                    Statistics / Visualisation
                </h1>
                {error && (
                    <p className="text-red-600 text-sm mb-4">
                        {error}
                    </p>
                )}

                {/* Summary */}
                <div className="grid grid-cols-1 sm:grid-cols-2 x1:grid-cols-4 gap-6 mb-8">
                    {summaryStats.map((stat, index) => (
                        <div key={index} className="bg-white rounded-x1 shadow-md p-6">
                            <p className="text-sm text-gray-500 mb-2">{stat.label}</p>
                            <p className="text-2x1 font-bold text-gray-800">{stat.value}</p>
                        </div>
                    ))}
                </div>

                {/* Main graphs and progression area */}
                <div className="grid grid-cols-1 x1:grid-cols-2 gap-8 mb-8">
                    {/* Exercise breakdown */}
                    <div className="bg-white rounded-x1 shadow-md p-6">
                        <h2 className="text-2x1 font-semibold text-gray-800 mb-4">
                            Workout Progression Over Time
                        </h2>

                        <div className="space-y-4">
                            {progressionData.map((week, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex justify-between mb-2">
                                        <span className="font-semibold text-gray-800">{week.week}</span>
                                        <span className="text-gray-500">
                                            {week.workouts} workouts
                                        </span>
                                    </div>

                                    <div className="w-full bg-gray-200 rounded-full h-3">
                                        <div
                                            className="bg-blue-600 h-3 rounded-full"
                                            style={{ width: `${week.workouts * 15}%` }}
                                        ></div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* History */}
                    <div className="bg-white rounded-x1 shadow-md p-6">
                        <h2 className="text-2x1 font-semibold text-gray-800 mb-4">
                            Hours Trained Over Time
                        </h2>

                        <div className="space-y-4">
                            {progressionData.map((week, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex justify-between mb-2">
                                        <span className="font-semibold text-gray-800">
                                            {week.week}
                                        </span>
                                        <span className="text-sm text-gray-500">
                                            {week.hours} hrs
                                        </span>
                                    </div>

                                    <p className="text-gray-700 mb-2">
                                        {week.workouts} Workouts Completed
                                    </p>

                                    <div className="w-full bg-gray-200 rounded-full h-3">
                                        <div
                                            className="bg-green-500 h-3 rounded-full"
                                            style={{ width: `${week.hours * 10}%` }}
                                        ></div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Competition and PBs */}
                <div className="grid grid-cols-1 x1:grid-cols-2 gap-8">
                    <div className="bg-white rounded-x1 shadow-md p-6">
                        <h2 className="text-2x1 font-semibold text-gray-800 mb-4">
                            Competition Tracking
                        </h2>

                        <div className="space-y-4">
                            {competitionStats.map((competition, index) => (
                                <div
                                    key={index}
                                    className="border border-gray-200 rounded-lg p-4 flex justify-between items-center"
                                >
                                    <div>
                                        <p className="font-semibold text-gray-800">{competition.event}</p>
                                        <p className="text-sm text-gray-500">{competition.date}</p>
                                    </div>

                                    <div className="text-right">
                                        <p className="text-sm text-gray-500">
                                            {competition.status}
                                        </p>
                                        <p className="font-medium text-gray-800">
                                            {competition.result}
                                        </p>
                                    </div>
                                </div>                      
                            ))}
                        </div>
                    </div>

                    <div className="bg-white rounded-x1 shadow-md p-6">
                        <h2 className="text-2x1 font-semibold text-gray-800 mb-4">
                            Personal Bests
                        </h2>

                        <div className="space-y-4">
                            {personalBests.map((pb, index) => (
                                <div 
                                    key={index}
                                    className="border border-gray-200 rounded-lg p-4 flex justify-between"
                                >
                                    <span className="font-medium text-gray-700">
                                        {pb.activity || `Exercise ${pb.exerciseID}`}
                                    </span>
                                    <span className="font-bold text-gray-800">
                                        {pb.best || pb.storeValue}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    )
}

export default StatisticsPage