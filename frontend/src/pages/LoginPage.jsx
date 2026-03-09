function LoginPage () {
    const handleSubmit = (event) => {
        event.preventDefault();
        console.log("Login submitted");
    };
    return (
        
    <div className="LoginPage">

        <div className="LoginBox">
            <h1 className="LoginHeader text-3xl font-bold text-center text-blue-800 mb-6">AthletePal</h1>

            <form classNameonSubmit={handleSubmit}>

                <label>Email / Username</label>
                <input type="text" placeholder="Enter email or username" />

                <label>Password</label>
                <input type="password" placeholder="Enter password"/>

                <button type="submit">Login</button>

            </form>

            <button>Forgot password?</button>

            <button>Create Account</button>

        </div>

    </div>

    )
}

export default LoginPage