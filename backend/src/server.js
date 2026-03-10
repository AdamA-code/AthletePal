import express from "express"
import cors from "cors"
import dotenv from "dotenv"
import { prototype } from "node:events"

dotenv.config()

const app = express() // Create an Express application

app.use(cors()) // Allow the backend server to be accessible by the frontend

app.use(express.json()) // Allow server to read JSON (as requests given as such)

/*
ROUTES
*/


/*
START SERVER AND LISTEN
*/

app.set('port', process.env.PORT || 3000) // Set web server port to that which is read in .env (but if nothing default to 3000)
app.listen(PORT, () => { // Server idle listen to any requests from port
    console.log(`Server running on port number ${PORT}`) // Upon successful server initialisation, log to console
})
