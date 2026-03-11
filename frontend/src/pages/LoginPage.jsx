import { Link, useNavigate } from "react-router-dom"
import { useState } from "react";

function LoginPage () {
    const navigate = useNavigate()

    const handleSubmit = (event) => {
        event.preventDefault();
        navigate("/dashboard");
    };

    const [emailOrUsername, setEmailOrUsername] = useState("")
    const [password, setPassword] = useState("")
    return (
        
        <div className="LoginPage min-h-screen flex items-center justify-center bg-gray-100 px-4">

            <div className="LoginBox bg-white p-10 rounded-x1 shadow-lg w-full max-w-md">
                <h1 className="LoginHeader text-3xl font-bold text-center text-blue-800 mb-6">AthletePal</h1>

                <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
                    <div className="flex flex-col">
                        <label htmlFor="emailOrUsername" className="text-sm font-semibold mb-1">
                            Email / Username
                        </label>
                        <input 
                            id="emailOrUsername"
                            type="text"
                            placeholder="Enter email or username"
                            value={emailOrUsername}
                            onChange={(e) => setEmailOrUsername(e.target.value)}
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
                        className="Login bg-blue-500 text-white py-2 rounded-md font-semibold hover:bg-blue-600 transition"
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