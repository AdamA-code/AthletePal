import Sidebar from "../components/Sidebar"

function StatisticsPage() {
    const summaryStats = [
        { label: "Total Workouts", value: 48 },
        { label: "Hours Trained", value: 72 },
        { label: "Calories Burned", value: 18450 }, 
        { label: "Current Streak", value: "6 days"}
    ]

    const exerciseBreakdown = [
        { type: "Running", sessions: 14, percentage: "29%" },
        { type: "Cycling", sessions: 8, percentage: "17%" },
        { type: "Swimming", sessions: 5, percentage: "10%" },
        { type: "Weight Training", sessions: 21, percentage: "44%" }
    ]

    const weeklyHistory = [
        { week: "Week 1", workouts: 4, hours: 6 },
        { week: "Week 2", workouts: 5, hours: 8 },
        { week: "Week 3", workouts: 3, hours: 5 },
        { week: "Week 4", workouts: 6, hours: 9},
    ]
    
    const competitionStats = [
        { event: "Leeds 10K", date: "12/02/26", result: "48:21", status: "Completed" },
        { event: "Yorkshire Triathlon", date: "28/03/26", result: "Upcoming", status: "Planned" }
    ]

    const personalBests = [
        { activity: "5k run", best: "22:48" },
        { activity: "10k run", best: "50:21" },
        { activity: "Bench Press", best: "125kg" },
        { activity: "Squat", best: "190kg" },
        { activity: "Deadlift", best: "230kg" },
    ]

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />

            <main className="flex-1 p-8">
                <h1 className="text-3x1 font-bold text-blue-800 mb-6">
                    Statistics / Visualisation
                </h1>

                {/* Summary */}
                <div className="grid grid-cols-1 sm:grid-cols-2 x1:grid-cols-4 gap-6 mb-8">
                    {summaryStats.map((stat, index) => (
                        <div key={index} className="bg-white rounded-x1 shadow-md p-6">
                            <p className="text-sm text-gray-500 mb-2">{stat.label}</p>
                            <p className="text-2x1 font-bold text-gray-800">{stat.value}</p>
                        </div>
                    ))}
                </div>

                {/* Main Section */}
                <div className="grid grid-cols-1 x1:grid-cols-2 gap-8 mb-8">
                    {/* Exercise breakdown */}
                    <div className="bg-white rounded-x1 shadow-md p-6">
                        <h2 className="text-2x1 font-semibold text-gray-800 mb-4">
                            Exercise Breakdown
                        </h2>

                        <div className="space-y-4">
                            {exerciseBreakdown.map((item, index) => (
                                <div key={index}>
                                    <div className="flex justify-between text-sm mb-1">
                                        <span className="font-medium text-gray-700">{item.type}</span>
                                        <span className="text-gray-500">
                                            {item.sessions} sessions • {item.percentage}
                                        </span>
                                    </div>

                                    <div className="w-full bg-gray-200 rounded-full h-3">
                                        <div
                                            className="bg-blue-600 h-3 rounded-full"
                                            style={{ width: item.percentage }}
                                        ></div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* History */}
                    <div className="bg-white rounded-x1 shadow-md p-6">
                        <h2 className="text-2x1 font-semibold text-gray-800 mb-4">
                            Weekly Training History
                        </h2>

                        <div className="space-y-4">
                            {weeklyHistory.map((week, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex justify-between mb-2">
                                        <span className="font-semibold text-gray-800">{week.week}</span>
                                        <span className="text-sm text-gray-500">{week.hours} hrs</span>
                                    </div>

                                    <p className="text-gray-700 mb-2">{week.workouts} Workouts Completed</p>

                                    <div className="w-full bg-gray-200 rounded-full h-3">
                                        <div
                                            className="bg-green-500 h-3 rounded-full"
                                            style={{ width: `${week.workouts * 15}%` }}
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
                                    <span className="font-medium text-gray-700">{pb.activity}</span>
                                    <span className="font-bold text-gray-800">{pb.best}</span>
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