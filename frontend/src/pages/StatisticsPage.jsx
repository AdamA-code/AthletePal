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
    const [availableExercises, setAvailableExercises] = useState([])
    const [exerciseSearch, setExerciseSearch] = useState("")
    const [selectedExercise, setSelectedExercise] = useState(null)
    const [scoreValue, setScoreValue] = useState("")
    const [pbSuccess, setPbSuccess] = useState("")
    const [showExerciseDropdown, setShowExerciseDropdown] = useState(false)

    useEffect(() => {
        const userID = localStorage.getItem("userID")

        if (!userID) {
            setError("No user found. Please log in again.")
            return
        }

        const fetchStatistics = async () => {
            try {
                const [workoutRes, hoursRes, streakRes, historyRes, pbRes, exercisesRes, competitionsRes, allWorkoutsRes] = await Promise.all([
                    fetch(`${API_BASE_URL}/api/statistic/user/workouts-completed/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistic/user/hours-trained/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistic/user/workout-streak/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistic/user/training-history/${userID}`),
                    fetch(`${API_BASE_URL}/api/statistics/user/personal-bests/${userID}`),
                    fetch(`${API_BASE_URL}/api/exercises`),
                    fetch(`${API_BASE_URL}/api/competitions/${userID}`),
                    fetch(`${API_BASE_URL}/api/workouts/${userID}`)
                ])

            const workoutData = await workoutRes.json()
            const hoursData = await hoursRes.json()
            const streakData = await streakRes.json()
            const historyData = await historyRes.json()
            const pbsData = await pbRes.json()
            const exercisesData = await exercisesRes.json()
            const competitionsData = await competitionsRes.json()
            const allWorkoutsData = await allWorkoutsRes.json()
            
            {/* Console Logs for testing purposes 
            console.log("Workout data:", workoutData)
            console.log("Hours data:", hoursData)
            console.log("Streak data:", streakData)
            console.log("History data:", historyData)
            console.log("Personal bests data:", pbsData)
            */}

            setSummaryStats([
                { 
                    label: "Total Workouts", 
                    value: Array.isArray(allWorkoutsData.Workouts)
                        ? allWorkoutsData.Workouts.filter((workout) =>
                            workout.duration !== null && workout.isRoutine !== true
                        ).length
                        : 0
                },
                { 
                    label: "Hours Trained",
                    value: Number(hoursData["Hours Trained"] || 0).toFixed(1)
                },
                { label: "Current Streak", value: streakData["Current Streak"] || 0 },
                { label: "Personal Bests", value: pbsData["Personal Bests"]?.length || 0 }
            ])
                const allWorkouts = Array.isArray(allWorkoutsData.Workouts)
                    ? allWorkoutsData.Workouts
                    : []

                const completedWorkouts = allWorkouts.filter((workout) =>
                    workout.duration !== null &&
                    workout.isRoutine !== true
                )

                const groupedByDate = completedWorkouts.reduce((acc, workout) => {
                    const date = workout.date || "Unknown Date"

                    if (!acc[date]) {
                        acc[date] = {
                            week: date,
                            workouts: 0,
                            hours: 0
                        }
                    }

                    acc[date].workouts += 1
                    acc[date].hours += Number(workout.duration || 0) / 60

                    return acc
                }, {})

                const formattedProgression = Object.values(groupedByDate).sort((a, b) =>
                    a.week.localeCompare(b.week)
                )

                setProgressionData(formattedProgression)
                setPersonalBests(pbsData["Personal Bests"] || [])
                setAvailableExercises(Array.isArray(exercisesData.Exercises) ? exercisesData.Exercises : [])
                setCompetitionStats(Array.isArray(competitionsData.Competitions) ? competitionsData.Competitions : [])
            } catch (err) {
                console.error("Error fetching statistics:", err)
                setError("Failed to fetch statistics.")
            }
        }

        fetchStatistics()
    }, [])

    // Adds a personal best for the selected exercise.
    const handleAddPersonalBest = async (event) => {
        event.preventDefault()

        const userID = localStorage.getItem("userID")

        if (!userID) {
            setError("No user found. Please log in again.")
            return
        }

        if (!selectedExercise || !scoreValue) {
            setError("Please select an exercise and enter a personal best value.")
            return
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/statistics`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    statisticID: 0,
                    userID: Number(userID),
                    exerciseID: selectedExercise.exerciseID,
                    scoreValue: Number(scoreValue),
                    date: new Date().toISOString().split("T")[0]
                })
            })

            const data = await response.json()

            if (response.ok) {
                setPersonalBests([
                    ...personalBests,
                    {
                        exerciseID: selectedExercise.exerciseID,
                        activity: selectedExercise.name,
                        scoreValue: Number(scoreValue)
                    }
                ])

                setExerciseSearch("")
                setSelectedExercise(null)
                setScoreValue("")
                setError("")
                setPbSuccess("Personal best added successfully.")
            } else {
                setPbSuccess("")
                setError(data.message || "Failed to add personal best.")
            }
        } catch (err) {
            console.error("Add PB error:", err)
            setPbSuccess("")
            setError("Could not connect to server.")
        }
    }
    const filteredExercises = availableExercises.filter((exercise) =>
        exercise.name.toLowerCase().includes(exerciseSearch.toLowerCase())
    )

    const maxWorkouts = Math.max(...progressionData.map(w => Number(w.workouts || 0)), 1)
    const maxHours = Math.max(...progressionData.map(w => Number(w.hours || 0)), 1)
    const formatCompetitionResult = (seconds) => {
        if (!seconds) return "No result recorded"

        const mins = Math.floor(seconds / 60)
        const secs = seconds % 60
        return `${mins}:${secs.toString().padStart(2, "0")}`
    }
    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h1 className="text-3xl font-bold text-blue-800 mb-6">
                    Statistics / Visualisation
                </h1>
                {error && (
                    <p className="text-red-600 text-sm mb-4">
                        {error}
                    </p>
                )}

                {/* Summary */}
                <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
                    {summaryStats.map((stat, index) => (
                        <div key={index} className="bg-white rounded-xl shadow-md p-6">
                            <p className="text-sm text-gray-500 mb-2">{stat.label}</p>
                            <h3 className="text-2xl font-bold text-gray-800">{stat.value}</h3>
                        </div>
                    ))}
                </div>

                {/* Main graphs and progression area */}
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-8 mb-8">
                    {/* Exercise breakdown */}
                    <div className="bg-white rounded-xl shadow-md p-6">
                        <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                            Workout Progression Over Time
                        </h2>

                        <div className="space-y-4">
                            {progressionData.length === 0 ? (
                                <p className="text-gray-500 text-sm">
                                    No training data yet.
                                </p>
                            ) : (
                                progressionData.map((week, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex justify-between mb-1">
                                        <span className="font-medium text-gray-800">{week.week || week.date || "Unknown date"}</span>
                                        <span className="text-sm text-gray-500">
                                            {Number(week.workouts || 1)} workouts
                                        </span>
                                    </div>

                                        <div className="w-full bg-gray-200 rounded-full h-3">
                                            <div
                                                className="bg-blue-600 h-3 rounded-full transition-all"
                                                style={{ width: `${(week.workouts / maxWorkouts) * 100}%` }}
                                            />
                                        </div>
                                    </div>
                                )))}
                        </div>
                    </div>

                    {/* History */}
                    <div className="bg-white rounded-xl shadow-md p-6">
                        <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                            Hours Trained Over Time
                        </h2>

                        <div className="space-y-4">
                            {progressionData.length === 0 ? (
                                <p className="text-gray-500 text-sm">
                                    No training data yet.
                                </p>
                            ) : (
                                progressionData.map((week, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex justify-between mb-1">
                                        <span className="font-medium text-gray-800">{week.week || week.date || "Unknown date"}</span>
                                        <span className="text-sm text-gray-500">
                                            {Number(week.hours || (week.duration / 60) || 0).toFixed(1)} hrs
                                        </span>
                                    </div>

                                        <div className="w-full bg-gray-200 rounded-full h-3">
                                            <div
                                                className="bg-green-500 h-3 rounded-full transition-all"
                                                style={{ width: `${(week.hours / maxHours) * 100}%` }}
                                            />
                                        </div>
                                    </div>
                                )))}
                        </div>
                    </div>
                </div>

                {/* Competition and PBs */}
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                    <div className="bg-white rounded-xl shadow-md p-6">
                        <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                            Competition Tracking
                        </h2>

                        <div className="space-y-4">
                            {competitionStats.map((competition, index) => (
                                <div
                                    key={index}
                                    className="border border-gray-200 rounded-lg p-4 flex justify-between items-center"
                                >
                                    <div>
                                        <p className="font-semibold text-gray-800">{competition.name}</p>
                                        <p className="text-sm text-gray-500">{competition.date}</p>
                                    </div>

                                    <div className="text-right">
                                        <p className="text-sm text-gray-500">
                                            {competition.rank ? `Rank: ${competition.rank}` : "No rank recorded"}
                                        </p>
                                        <p className="font-medium text-gray-800">
                                            {competition.result ? `Result: ${formatCompetitionResult(competition.result)}` : "No result recorded"}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="bg-white rounded-xl shadow-md p-6">
                        <h2 className="text-2xl font-semibold text-gray-800 mb-4">
                            Personal Bests
                        </h2>

                        <form onSubmit={handleAddPersonalBest} className="mb-6 space-y-3">
                            <div className="relative">
                                <input
                                    type="text"
                                    aria-label="Search exercise"
                                    placeholder="Search exercise e.g. Bench Press"
                                    value={exerciseSearch}
                                    onChange={(e) => {
                                        setExerciseSearch(e.target.value)
                                        setSelectedExercise(null)
                                        setShowExerciseDropdown(true)
                                    }}
                                    onFocus={() => setShowExerciseDropdown(true)}
                                    className="border rounded p-2 w-full"
                                />

                                {showExerciseDropdown && exerciseSearch && (
                                    <div className="absolute z-10 bg-white border rounded shadow w-full max-h-40 overflow-y-auto">
                                        {filteredExercises.length > 0 ? (
                                            filteredExercises.map((exercise) => (
                                                <button
                                                    key={exercise.exerciseID}
                                                    type="button"
                                                    onClick={() => {
                                                        setSelectedExercise(exercise)
                                                        setExerciseSearch(exercise.name)
                                                        setShowExerciseDropdown(false)
                                                    }}
                                                    className="block w-full text-left px-3 py-2 hover:bg-blue-50"
                                                >
                                                    <span className="font-medium">{exercise.name}</span>
                                                    <span className="text-gray-500 text-sm ml-2">
                                                        {exercise.category}
                                                    </span>
                                                </button>
                                            ))
                                        ) : (
                                            <p className="px-3 py-2 text-gray-500">No exercises found.</p>
                                        )}
                                    </div>
                                )}
                            </div>

                            <input
                                type="number"
                                aria-label="Personal best value"
                                placeholder="PB value e.g. 100"
                                value={scoreValue}
                                onChange={(e) => setScoreValue(e.target.value)}
                                className="border rounded p-2 w-full"
                            />

                            {pbSuccess && (
                                <p className="text-green-700 text-sm">{pbSuccess}</p>
                            )}

                            <button
                                type="submit"
                                className="bg-blue-700 text-white px-4 py-2 rounded hover:bg-blue-800"
                            >
                                Add Personal Best
                            </button>
                        </form>

                        <div className="space-y-4">
                            {personalBests.map((pb, index) => (
                                <div
                                    key={index}
                                    className="border border-gray-200 rounded-lg p-4 flex justify-between"
                                >
                                    <span className="font-medium text-gray-700">
                                        {
                                            pb.activity ||
                                            pb.name ||
                                            availableExercises.find((exercise) => exercise.exerciseID === pb.exerciseID)?.name ||
                                            `Exercise ${pb.exerciseID}`
                                        }

                                    </span>
                                    <span className="font-bold text-gray-800">
                                        {pb.best || pb.scoreValue}
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