import { Link } from "react-router-dom"
import { useState } from "react";
import { useNavigate } from "react-router-dom";


function CreateAccountPage() {
  const handleSubmit = (event) => {
    event.preventDefault() 
    if (!fullName || !email || !password || !age || !accountType) {
      setError("Please fill in all the fields.")
      return
    }

    setError("")

    console.log({
      fullName,
      email,
      password,
      age,
      accountType
    })

    navigate("/")
  }
  const [fullName, setFullName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [age, setAge] = useState("")
  const [accountType, setAccountType] = useState("")
  const [error, setError] = useState("")
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">

      <div className="bg-white p-8 rounded-xl shadow-lg w-[420px]">

        <h1 className="text-3xl font-bold text-center text-blue-800 mb-6">
          Create Account
        </h1>

        {error && (
          <p className="text-red-600 text-sm mb-4 text-center">{error}</p>
        )}

        <form className="flex flex-col gap-4" onSubmit={handleSubmit}>

          <label>Full Name</label>
          <input
            type="text"
            placeholder="Enter your  name"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            className="border p-2 rounded"
          />

          <label>Email</label>
          <input
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="border p-2 rounded"
          />

          <label>Password</label>
          <input
            type="password"
            placeholder="Enter password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="border p-2 rounded"
          />

          <label>Age</label>
          <input
            type="number"
            placeholder="Enter your age"
            value={age}
            onChange={(e) => setAge(e.target.value)}
            className="border p-2 rounded"
          />

          <label>Account Type</label>

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


      </div>

    </div>
  )
}

export default CreateAccountPage