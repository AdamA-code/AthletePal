package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.* // Import functionalityy to read JSON requests
import io.ktor.server.response.* // Import functionality to send responses to frontend
import io.ktor.http.*

import models.TodayWorkoutsRequest
import models.UpcomingCompetitionsRequest
import models.RecentActivityRequest
import models.UpdateWorkoutRequest

import models.Activity
import services.DatabaseService.Competition as Competition
import services.DatabaseService.Workout as Workout
import services.DatabaseService.Route as RunningRoute

import services.DatabaseService

fun Route.activityRoutes(){
    val db = DatabaseService() // Import service responsible for following requests

    route("/api") {

        // =====================
        // ACTIVITY
        // =====================

        // Get recent activity of any sort for a given user
        get("/activity/recent/{email}"){
            try {
                val email = call.parameters["email"]!!
                val activity = mapOf(
                    "Recent Workouts" to db.getRecentWorkouts(email),
                    "Upcoming Competitions" to db.getUpcomingCompetitions(email),
                    "Recent Routes" to db.getRecentRoutes(email)
                    )
                if (activity != null) {
                    call.respond(HttpStatusCode.OK, activity)
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No recent activity found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all activity for a given user
        get("/activity/{userId}"){
            try {
                val userId = call.parameters["userId"]!!.toInt()
                val activity = mapOf(
                    "Workouts" to db.findAllWorkouts(userId),
                    "Upcoming Competitions" to db.findAllCompetitions(userId),
                    "Routes" to db.findAllRoutes(userId)
                )
                if (activity != null) {
                    call.respond(HttpStatusCode.OK, activity)
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No activity found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }
        
        // =====================
        // WORKOUTS
        // =====================

        // Get recent workouts for a given user
        get("/workouts/recent/{email}") {
            try {
                val email = call.parameters["email"]!!
                val workouts = db.getRecentWorkouts(email)

                if (workouts != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workouts" to workouts))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No recent workouts found"))
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        get("/workouts/today/{email}") {
            try {
                val email = call.parameters["email"]!!
                val workouts = db.getTodaysWorkout(email)

                if (workouts != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workouts" to workouts))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No workouts found for today"))
                }

            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all workouts for a given user
        get("/workouts/{userId}"){
            try {
                val userId = call.parameters["userId"]!!.toInt()
                val workouts = db.findAllWorkouts(userId)

                if (workouts != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workouts" to workouts))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Could not find workouts"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Get a single workout
        get("/workout/{id}"){
            try {
                val id = call.parameters["id"]!!.toInt()
                val workout = db.findWorkout(id)

                if (workout != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workout" to workout))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid workout ID"))
            }
        }

        // Add new workout to user account
        post("/workouts"){
            try {
                val workout = call.receive<Workout>()
                val id = db.createWorkout(workout)

                if (id > 0){
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                }
                else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to create workout"))
                }
            }
            catch (e: Exception){
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Update specific workout on user account
        put("/workouts/{id}"){
            try {
                val id = call.parameters["id"]!!.toInt()
                val body = call.receive<UpdateWorkoutRequest>()

                val updated = db.updateWorkout(id, body.date ?: "", body.duration, body.workoutType, body.notes)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Workout updated"))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Delete a specific workout from user account
        delete("/workouts/{id}"){
            try {
                val id = call.parameters["id"]!!.toInt()

                val deleted = db.deleteWorkout(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Workout deleted"))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // =====================
        // COMPETITIONS
        // =====================

        // Get any and all upcoming competitions
        get("/competitions/upcoming/{email}") {
            try {
                val email = call.parameters["email"]!!
                val comps = db.getUpcomingCompetitions(email)

                if (comps != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Competitions" to comps))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No upcoming competitions found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all competitions for a given user
        get("/competitions/{userId}"){
            try{
                val userId = call.parameters["userId"]!!.toInt()
                val comps = db.findAllCompetitions(userId)

                if (comps != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Competitions" to comps))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No competitions found for user ID"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Get a single competition
        get("/competition/{id}"){
            try {
                val id = call.parameters["id"]!!.toInt()
                val comp = db.findCompetition(id)

                if (comp != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Competition" to comp))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Competition not found"))
                }
            }
            catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid competition ID"))
            }
        }

        // Add new competition to user account
        post("/competitions"){
            try {
                val comp = call.receive<Competition>()
                val id = db.addComp(comp)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                }
                else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to create competition"))
                }
            }
            catch (e: Exception){
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Update a given competition on the users account
        put("/competitions/{id}"){
            try {
                val id = call.parameters["id"]!!.toInt()
                val body = call.receive<Competition>()

                val updated = db.updateCompetition(id, body.name, body.date, body.rank, body.result)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Competition updated"))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Competition not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Delete a given competition from the users account
        delete("/competitions/{id}"){
            try {
                val id = call.parameters["id"]!!.toInt()

                val deleted = db.deleteCompetition(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Competition deleted"))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Competition not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // =====================
        // ROUTES
        // =====================

        // Get any recent routes for a user
        get("/routes/recent/{email}") {
            try {
                val email = call.parameters["email"]!!
                val routes = db.getRecentRoutes(email)

                if (routes != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Routes" to routes))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No recent routes found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all routes for a given user
        get("/routes/{userId}"){
            try {
                val userId = call.parameters["userId"]!!.toInt()
                val routes = db.findAllRoutes(userId)

                if (routes != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Routes" to routes))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No routes found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Get a single route
        get("/route/{id}"){
            try {
                val id = call.parameters["id"]!!.toInt()
                val route = db.findRoute(id)

                if (route != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Route" to route))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Route not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Add a route to the user account
        post("/routes"){
            try {
                val route = call.receive<RunningRoute>()
                val id = db.addRoute(route)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                }
                else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to create route"))
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Update a given route on the users account
        put("/routes/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()
                val body = call.receive<RunningRoute>()

                val updated = db.updateRoute(
                    id,
                    body.name,
                    body.distance,
                    body.estimatedTime,
                    body.date,
                    body.isPublic
                )

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Route updated"))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Route not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Delete a specific route from the users account
        delete("/routes/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()
                val deleted = db.deleteRoute(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Route deleted"))
                }
                else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Route not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }
    }
}