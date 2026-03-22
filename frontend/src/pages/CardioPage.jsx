import { Link } from "react-router-dom"
import { useState } from "react"
import RouteMap from "../components/RouteMap"
import Sidebar from "../components/Sidebar"

function CardioPage() {

    const [exerciseType, setExerciseType] = useState("Walking")
    const [date, setDate] = useState("")
    const [time, setTime] = useState("")
    const [duration, setDuration] = useState("")
    const [notes, setNotes] = useState("")
    const [isPublic, setIsPublic] = useState("no")

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            {/* Main Content */}
            <main className="flex-1 p-8 max-w-3xl">

                <h2 className="text-3xl font-bold mb-8">Cardio</h2>

                {/* Exercise Type */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Exercise Type</label>

                    <select
                        value={exerciseType}
                        onChange={(e) => setExerciseType(e.target.value)}
                        className="border rounded p-2 w-full"
                    >
                        <option>Walking</option>
                        <option>Running</option>
                        <option>Swimming</option>
                        <option>Cycling</option>
                    </select>
                </div>


                {/* Date */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Date</label>

                    <input
                        type="date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        className="border rounded p-2 w-full"
                    />
                </div>


                {/* Time */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Time</label>

                    <input
                        type="time"
                        value={time}
                        onChange={(e) => setTime(e.target.value)}
                        className="border rounded p-2 w-full"
                    />
                </div>


                {/* Duration */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Duration (minutes)</label>

                    <input
                        type="number"
                        value={duration}
                        onChange={(e) => setDuration(e.target.value)}
                        className="border rounded p-2 w-full"
                    />
                </div>


                {/* Notes */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">Notes</label>

                    <textarea
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        className="border rounded p-2 w-full h-24"
                    />
                </div>


                {/* Map Placeholder */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">
                        Record Your Route on the Map
                    </label>

                    <RouteMap />
                </div>


                {/* Public Route */}
                <div className="mb-6">
                    <label className="block font-semibold mb-2">
                        Make Route Public
                    </label>

                    <div className="flex gap-6">

                        <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                value="yes"
                                checked={isPublic === "yes"}
                                onChange={(e) => setIsPublic(e.target.value)}
                            />
                            Yes
                        </label>

                        <label className="flex items-center gap-2">
                            <input
                                type="radio"
                                value="no"
                                checked={isPublic === "no"}
                                onChange={(e) => setIsPublic(e.target.value)}
                            />
                            No
                        </label>

                    </div>
                </div>

            </main>

        </div>
    )
}

export default CardioPage