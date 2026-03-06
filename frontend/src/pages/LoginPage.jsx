function LoginPage () {
    return (
        
    <div className="LoginPage">

        <div className="LoginBox">
            <h1>AthletePal</h1>

            <form>

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