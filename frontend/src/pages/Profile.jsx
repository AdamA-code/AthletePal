import { Link } from "react-router-dom"

function ProfilePage() {
    const user = {
        fullName: "John Smith",
        email: "email@email.com",
        age: 20,
        accountType: "User",
        goal: "Build muscle",
        height: "180cm",
        weight: "75kg",
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            {/* Sidebar */}
            <aside className="w-64 bg-white shadow-md p-6 flex flex-col">
                <h1 className="text-2xl font-bold text-blue-800 mb-8">AthletePal</h1>
                <nav className="flex flex-col gap-4 text-gray-700">

                    <Link to="/dashboard" className="hover:text-blue-700">
                        Dashboard
                    </Link>
                    
                    <Link to="/workout-log" className="hover:text-blue-700">
                        Workout Logs (Weights)
                    </Link>

                    <Link to="/cardio" className="hover:text-blue-700">
                        Cardio
                    </Link>

                    <Link to="/activity-history" className="hover:text-blue-700">
                        Activity History
                    </Link>

                    <Link to="#">Competitions</Link>
                    <Link to="#">Training Groups</Link>
                    <Link to="#">Statistics / Visualisation</Link>
                    <Link to="/profile" className="bg-blue-100 text-blue-800 px-3 py-2 rounded-lg font-medium">
                        Profile
                    </Link>
                </nav>
            </aside>

            <main className="flex-1 p-8">
                <h1 className="text-3x1 font-bold text-blue-800 mb-6">Profile</h1>
                
                <div className="bg-white rounded-x1 shadow-md p-8 max-w-3x1">
                    <div className="flex flex-col md:flex-row gap-8 items-center md:items-start">
                        <div className="w-32 h-32 rounded-full bg-gray-200 flex items-center justify-center text-gray-500 text-sm">
                            Profile Picture
                        </div>

                        <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <p className="text-sm text-gray-500">Full Name</p>
                                <p className="text-lg font-semibold">{user.fullName}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500">Email</p>
                                <p className="text-lg font-semibold">{user.email}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500">Age</p>
                                <p className="text-lg font-semibold">{user.age}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500">Fitness Goal</p>
                                <p className="text-lg font-semibold">{user.goal}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500">Height</p>
                                <p className="text-lg font-semibold">{user.height}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500">Weight</p>
                                <p className="text-lg font-semibold">{user.weight}</p>
                            </div>

                            <div>
                                <p className="text-sm text-gray-500">Training Style</p>
                                <p className="text-lg font-semibold">{user.trainingStyle}</p>
                            </div>
                        </div>
                    </div>

                    <button className="mt-8 bg-blue-700 text-white px-6 py-2 rounded-lg hover:bg-blue-800">
                        Edit Profile
                    </button>
                </div>
            </main>
        </div>
    )
}

export default ProfilePage