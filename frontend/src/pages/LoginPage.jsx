import { Link } from "react-router-dom"

function LoginPage () {
    const handleSubmit = (event) => {
        event.preventDefault();
        console.log("Login submitted");
    };
    return (
        
    <div className="LoginPage">

        <div className="LoginBox">
            <h1 className="LoginHeader text-3xl font-bold text-center text-blue-800 mb-6">AthletePal</h1>

            <form className="form" onSubmit={handleSubmit}>

                <label>Email / Username</label>
                <input type="text" placeholder="Enter email or username" />

                <label>Password</label>
                <input type="password" placeholder="Enter password"/>

                <button type="submit">Login</button>

            </form>

            <button>Forgot password?</button>

            <Link to="/create-account">
                <button>Create Account</button>
            </Link>

        </div>

    </div>

    )
}

export default LoginPage