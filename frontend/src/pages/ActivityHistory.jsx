import { useState } from "react"
function ActivityHistoryPage() {
    const [searchTerm, setSearchTerm] = useState("")

    const cardioWorkouts = [
        {
            id: 1,
            date: "04/03/26",
            type: "Run",
            duration: "1 hour 25mins",
            details: "Park route, moderate pace"
        },
        {
            id: 2,
            date: "02/03/26",
            type: "Cycling",
            duration: "45 mins",
            details: "Stationary bike",
        },
        {
            id: 3,
            date: "06/03/26",
            type: "Walking",
            duration: "45 mins",
            details: "Incline treadmill at 12% gradient",
        },
        {
            id: 4,
            date: "08/03/26",
            type: "Swimming",
            duration: "30 mins",
            details: "30 laps of 50m pool so 600m"
        }
    ]
    const weightTraining = [
        {
            id: 1,
            date: "09/03/26",
            type: "Upper Day",
            duration: "1 hour 45 mins",
            excercises: [
                "Bench press - 60kg 3x12",
                "Bicep Curl - 18kg 2x8",
                "Shoulder Press - 40kg 3x10",
                "Pec Deck - 30kg 2x10",
            ]
        },
        {
            id: 2,
            date: "11/03/26",
            type: "Lower Day",
            duration: "1 hour 15 mins",
            excercises: [
                "Squats - 110kg 2x8",
                "Leg Extension - 75kg 3x12",
                "Hamstring Curl - 60kg 3x10",
                "Calf raises - 120kg 2x10",
            ]
        },
        {
            id: 3,
            date: "13/03/26",
            type: "Push Day",
            excercises: [
                "Incline Dumbbell Press - 30kg 3x12",
                ""
            ]
        }
    ]
}