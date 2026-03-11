function CreateAccountPage() {
  const handleSubmit = (event) => {
    event.preventDefault() 
    console.log("Account created")
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">

      <div className="bg-white p-8 rounded-xl shadow-lg w-[420px]">

        <h1 className="text-3xl font-bold text-center text-blue-800 mb-6">
          Create Account
        </h1>

        <form className="flex flex-col gap-4" onSubmit={handleSubmit}>

          <label>Full Name</label>
          <input
            type="text"
            placeholder="Enter your  name"
            className="border p-2 rounded"
          />

          <label>Email</label>
          <input
            type="email"
            placeholder="Enter your email"
            className="border p-2 rounded"
          />

          <label>Password</label>
          <input
            type="password"
            placeholder="Enter password"
            className="border p-2 rounded"
          />

          <label>Age</label>
          <input
            type="number"
            placeholder="Enter your age"
            className="border p-2 rounded"
          />

          <label>Account Type</label>

          <div className="flex gap-4">

            <label className="flex items-center gap-2">
              <input type="radio" name="accountType" value="trainer" />
              Trainer
            </label>

            <label className="flex items-center gap-2">
              <input type="radio" name="accountType" value="user" />
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

      </div>

    </div>
  )
}

export default CreateAccountPage