// page structure and layout generated with chatgpt chat.openai.com
// frontend-backend connection logic and accessibility improvements done with Claude Sonnet (claude.ai)
import { useState, useEffect } from "react"
import Sidebar from "../components/Sidebar"
import API_BASE_URL from "../config"

function ProfilePage() {
    const [user, setUser] = useState(null)
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    const [isEditing, setIsEditing] = useState(false)
    const [gender, setGender] = useState("")
    const [age, setAge] = useState("")
    const [height, setHeight] = useState("")
    const [weight, setWeight] = useState("")

    const [isChangingPassword, setIsChangingPassword] = useState(false)
    const [oldPassword, setOldPassword] = useState("")
    const [newPassword, setNewPassword] = useState("")
    const [confirmPassword, setConfirmPassword] = useState("")

    // Fetches the logged in users profile data on page load using email from localStorage
    // Pre-fills the edit form state with existing values so they appear when Edit Profile is clicked
    useEffect(() => {
        const email = localStorage.getItem("email")
        if (!email) {
            setError("Not logged in. Please log in again.")
            return
        }

        const fetchUser = async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/auth/user/${email}`)
                const data = await res.json()
                if (res.ok) {
                    setUser(data.User)
                    setGender(data.User.gender || "")
                    setAge(data.User.age || "")
                    setHeight(data.User.height || "")
                    setWeight(data.User.weight || "")
                } else {
                    setError("Could not load profile.")
                }
            } catch (err) {
                console.error(err)
                setError("Could not connect to server.")
            }
        }

        fetchUser()
    }, [])

    // Sends a PUT request to update the users profile details
    // Updates the displayed user state immediately without needing to refetch from the backend
    const saveProfile = async () => {
        const email = localStorage.getItem("email")
        setError("")
        setSuccess("")

        try {
            const res = await fetch(`${API_BASE_URL}/update/account`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    "email": email,
                    "gender": gender || null,
                    "age": age ? parseInt(age) : null,
                    "height": height ? parseFloat(height) : null,
                    "weight": weight ? parseFloat(weight) : null
                })
            })
            const data = await res.json()

            if (res.ok) {
                setSuccess("Profile updated successfully!")
                setIsEditing(false)
                setUser(prev => ({ ...prev, gender, age, height, weight }))
            } else {
                setError(data.message || "Failed to update profile.")
            }
        } catch (err) {
            console.error(err)
            setError("Could not connect to server.")
        }
    }

    // Validates that new passwords match and meet minimum length before sending a PUT request
    // confirmPassword is only used for frontend validation and is not sent to the backend
    const savePassword = async () => {
        const email = localStorage.getItem("email")
        setError("")
        setSuccess("")

        if (newPassword !== confirmPassword) {
            setError("New passwords do not match.")
            return
        }
        if (newPassword.length < 8) {
            setError("New password must be at least 8 characters.")
            return
        }

        try {
            const res = await fetch(`${API_BASE_URL}/update/password/${email}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    "oldPassword": oldPassword,
                    "newPassword": newPassword
                })
            })
            const data = await res.json()

            if (res.ok) {
                setSuccess("Password updated successfully!")
                setIsChangingPassword(false)
                setOldPassword("")
                setNewPassword("")
                setConfirmPassword("")
            } else {
                setError(data.message || "Failed to update password.")
            }
        } catch (err) {
            console.error(err)
            setError("Could not connect to server.")
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex">
            <Sidebar />
            <main className="flex-1 p-8">
                <h1 className="text-3xl font-bold mb-6">Profile</h1>

                {error && <p className="text-red-700 mb-4" role="alert">{error}</p>}
                {success && <p className="text-green-700 mb-4" role="status">{success}</p>}

                {!user ? (
                    <p className="text-gray-600">Loading profile...</p>
                ) : (
                    <div className="bg-white rounded-lg shadow-md p-8 max-w-3xl">

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                            <div>
                                <p className="text-sm text-gray-600">Full Name</p>
                                <p className="text-lg font-semibold">{user.name}</p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-600">Email</p>
                                <p className="text-lg font-semibold">{user.email}</p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-600">Account Type</p>
                                <p className="text-lg font-semibold">
                                    {user.accountType ? "Personal Trainer" : "User"}
                                </p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-600">Gender</p>
                                <p className="text-lg font-semibold">{user.gender || "Not set"}</p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-600">Age</p>
                                <p className="text-lg font-semibold">{user.age || "Not set"}</p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-600">Height (cm)</p>
                                <p className="text-lg font-semibold">{user.height || "Not set"}</p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-600">Weight (kg)</p>
                                <p className="text-lg font-semibold">{user.weight || "Not set"}</p>
                            </div>
                        </div>

                        {isEditing ? (
                            <div className="border-t pt-6 mb-6">
                                <h2 className="text-xl font-semibold mb-4">Edit Profile</h2>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                    <div>
                                        <label htmlFor="profile-gender" className="block text-sm text-gray-700 mb-1">Gender</label>
                                        <select
                                            id="profile-gender"
                                            value={gender}
                                            onChange={(e) => setGender(e.target.value)}
                                            className="border rounded p-2 w-full"
                                        >
                                            <option value="">Prefer not to say</option>
                                            <option value="Male">Male</option>
                                            <option value="Female">Female</option>
                                            <option value="Other">Other</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label htmlFor="profile-age" className="block text-sm text-gray-700 mb-1">Age</label>
                                        <input
                                            id="profile-age"
                                            type="number"
                                            value={age}
                                            onChange={(e) => setAge(e.target.value)}
                                            className="border rounded p-2 w-full"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="profile-height" className="block text-sm text-gray-700 mb-1">Height (cm)</label>
                                        <input
                                            id="profile-height"
                                            type="number"
                                            value={height}
                                            onChange={(e) => setHeight(e.target.value)}
                                            className="border rounded p-2 w-full"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="profile-weight" className="block text-sm text-gray-700 mb-1">Weight (kg)</label>
                                        <input
                                            id="profile-weight"
                                            type="number"
                                            value={weight}
                                            onChange={(e) => setWeight(e.target.value)}
                                            className="border rounded p-2 w-full"
                                        />
                                    </div>
                                </div>
                                <div className="flex gap-4">
                                    <button
                                        onClick={saveProfile}
                                        className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
                                    >
                                        Save Changes
                                    </button>
                                    <button
                                        onClick={() => setIsEditing(false)}
                                        className="border border-gray-400 text-gray-600 px-6 py-2 rounded hover:bg-gray-100"
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <button
                                onClick={() => setIsEditing(true)}
                                className="bg-blue-700 text-white px-6 py-2 rounded-lg hover:bg-blue-800 mr-4"
                            >
                                Edit Profile
                            </button>
                        )}

                        {isChangingPassword ? (
                            <div className="border-t pt-6 mt-6">
                                <h2 className="text-xl font-semibold mb-4">Change Password</h2>
                                <div className="flex flex-col gap-4 max-w-sm mb-4">
                                    <div>
                                        <label htmlFor="old-password" className="block text-sm text-gray-700 mb-1">Current Password</label>
                                        <input
                                            id="old-password"
                                            type="password"
                                            placeholder="Current password"
                                            value={oldPassword}
                                            onChange={(e) => setOldPassword(e.target.value)}
                                            className="border rounded p-2 w-full"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="new-password" className="block text-sm text-gray-700 mb-1">New Password</label>
                                        <input
                                            id="new-password"
                                            type="password"
                                            placeholder="New password"
                                            value={newPassword}
                                            onChange={(e) => setNewPassword(e.target.value)}
                                            className="border rounded p-2 w-full"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="confirm-password" className="block text-sm text-gray-700 mb-1">Confirm New Password</label>
                                        <input
                                            id="confirm-password"
                                            type="password"
                                            placeholder="Confirm new password"
                                            value={confirmPassword}
                                            onChange={(e) => setConfirmPassword(e.target.value)}
                                            className="border rounded p-2 w-full"
                                        />
                                    </div>
                                </div>
                                <div className="flex gap-4">
                                    <button
                                        onClick={savePassword}
                                        className="bg-gray-700 text-white px-6 py-2 rounded hover:bg-gray-800"
                                    >
                                        Update Password
                                    </button>
                                    <button
                                        onClick={() => setIsChangingPassword(false)}
                                        className="border border-gray-400 text-gray-600 px-6 py-2 rounded hover:bg-gray-100"
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <button
                                onClick={() => setIsChangingPassword(true)}
                                className="mt-4 bg-gray-700 text-white px-6 py-2 rounded-lg hover:bg-gray-800"
                            >
                                Change Password
                            </button>
                        )}
                    </div>
                )}
            </main>
        </div>
    )
}

export default ProfilePage