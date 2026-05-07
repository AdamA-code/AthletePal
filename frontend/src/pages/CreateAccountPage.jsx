// page structure, layout, and frontend -> backend request connections aided with chatgpt chat.openai.com
import { Link } from "react-router-dom"
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import API_BASE_URL from "../config";


function CreateAccountPage() {
  const [fullName, setFullName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [age, setAge] = useState("")
  const [accountType, setAccountType] = useState("")
  const [error, setError] = useState("")
  const navigate = useNavigate()

  // Handles the create account form submission by validating the entered information by checking the password length, then sending a request to the backend to create a new user account. If successful, it redirects the user to the login page, otherwise it shows a user-friendly error message. All returned user details are stored in localStorage for future authenticated requests.
  const handleSubmit = async (event) => {
    event.preventDefault() 
    if (!fullName || !email || !password || !age || !accountType) {
      setError("Please fill in all the fields.")
      return
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters long.")
      return
    }

    setError("")

    try {
      const response = await fetch(`${API_BASE_URL}/auth/create-account`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          name: fullName,
          email,
          password,
          age: Number(age),
          accountType: accountType === "trainer"
        })
      })

      const data = await response.json()

      if (response.ok) {
        setError("")
        navigate("/")
      } else {
        setError(data.message || "Account could not be created.")
      }
    } catch (error) {
      console.error("Create account error:", error)
      setError("Could not connect to server.")
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">

      <main className="bg-white p-8 rounded-xl shadow-lg w-[420px]">

        <h1 className="text-3xl font-bold text-center text-blue-800 mb-6">
          Create Account
        </h1>

        {error && (
          <p className="text-red-700 text-sm mb-4 text-center" role="alert">
            {error}
          </p>
        )}

        <form className="flex flex-col gap-4" onSubmit={handleSubmit}>

          <div className="flex flex-col">
            <label htmlFor="fullname" className="text-sm font-semibold mb-1">Full Name</label>
            <input
              id="fullname"
              type="text"
              placeholder="Enter your name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex flex-col">
            <label htmlFor="email" className="text-sm font-semibold mb-1">Email</label>
            <input
              id="email"
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex flex-col">
            <label htmlFor="password" className="text-sm font-semibold mb-1">Password</label>
            <input
              id="password"
              type="password"
              placeholder="Enter password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex flex-col">
            <label htmlFor="age" className="text-sm font-semibold mb-1">Age</label>
            <input
              id="age"
              type="number"
              placeholder="Enter your age"
              value={age}
              onChange={(e) => setAge(e.target.value)}
              className="border p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <fieldset className="flex flex-col">
            <legend className="text-sm font-semibold mb-1">Account Type</legend>

            <div className="flex gap-4">
              <label className="flex items-center gap-2">
                <input
                  type="radio"
                  name="accountType"
                  value="trainer"
                  checked={accountType === "trainer"}
                  onChange={(e) => setAccountType(e.target.value)}
                />
                Trainer
              </label>

              <label className="flex items-center gap-2">
                <input
                  type="radio"
                  name="accountType"
                  value="user"
                  checked={accountType === "user"}
                  onChange={(e) => setAccountType(e.target.value)}
                />
                User
              </label>
            </div>
          </fieldset>

          <button
            type="submit"
            className="bg-blue-700 text-white p-2 rounded mt-4 hover:bg-blue-800"
          >
            Create Account
          </button>

        </form>

        <p className="text-center mt-4">
          Already have an account?{" "}
          <Link to="/" className="text-blue-700 hover:underline">
            Login
          </Link>
        </p>


      </main>

    </div>
  )
}

export default CreateAccountPage