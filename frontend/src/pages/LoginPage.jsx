import { Link, useNavigate } from "react-router-dom"
import { useState } from "react";

function LoginPage () {
    const navigate = useNavigate()

    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [error, setError] = useState("")

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!email || !password) {
            setError("Please enter both email and password")
            return
        }

        // Implemented logic to communicate with backend for fethcing the username and password and validating the user.
        try {
            const response = await fetch("http://localhost:3000/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ 
                    email, 
                    password 
                })
            })

            const data = await response.json()

            if (data.message === "Login Successful") {
                // Store the email in localStorage or context for future authenticated requests
                localStorage.setItem("email", email)
                localStorage.setItem("userID", data.userID)
                localStorage.setItem("name", data.name)
                setError("")
                navigate("/dashboard")
            } else {
                setError("Invalid email or password") // validation failed, show user-friendly error message
            }
        } catch (error) { 
            console.error("Error during login:", error)
            setError("Could not connect to server.")
        } // Above used to make sure that the user doesn't get an error code which doesn't look good on the UI and instead gets a user-friendly message.
    }

    return (
        
        <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">

            <div className="bg-white p-10 rounded-x1 shadow-lg w-full max-w-md">
                <h1 className="text-3xl font-bold text-center text-blue-800 mb-6">
                    AthletePal
                </h1>

                {error && (
                    <p className="text-red-600 text-sm mb-4 text-center">{error}</p>
                )}
                <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
                    <div className="flex flex-col">
                        <label htmlFor="email" className="text-sm font-semibold mb-1">
                            Email
                        </label>
                        <input 
                            id="email"
                            type="email"
                            placeholder="Enter email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="border rounded-md p-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    <div className="flex flex-col">
                        <label htmlFor="password" className="text-sm font-semibold mb-1">
                            Password
                        </label>
                        <input 
                            id="password"
                            type="password" 
                            placeholder="Enter password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="border rounded-md p-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <button 
                        type="submit"
                        className="bg-blue-500 text-white py-2 rounded-md font-semibold hover:bg-blue-600 transition"
                    >
                        Login
                    </button>
                </form>

                <button
                    type="button"
                    className="text-blue-500 text-sm mt-4 hover:underline"
                >
                    Forgot password?
                </button>

                <Link to="/create-account">
                    <button
                        type="button"
                        className="mt-3 w-full border border-blue-500 text-blue-500 py-2 rounded-md font-semibold hover:bg-blue-50"
                    >
                        Create Account
                    </button>
                </Link>
            </div>
        </div>
    );
}

export default LoginPage