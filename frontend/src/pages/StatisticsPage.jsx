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
}