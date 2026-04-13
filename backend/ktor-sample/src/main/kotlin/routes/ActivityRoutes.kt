package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.* // Import functionalityy to read JSON requests
import io.ktor.server.response.* // Import functionality to send responses to frontend

import models.TodayWorkoutsRequest
import models.UpcomingCompetitionsRequest
import models.RecentActivityRequest
import models.Activity
import models.Competitions
import mmodels.Workout

import services.DatabaseService

fun Route.activityRoutes(){
    val databaseService = DatabaseService() // Import service responsible for following requests

    route("/dashboard"){


        post("/recent-activity"){
            val recentActivityRequest = call.receive<RecentActivityRequest>() // Extract data from request

            if ( (recentActivityRequest.email != null) ){ // If request contains some user identifier (i.e. username or email)
                val recentActivity = Activity(
                workouts = databaseService.getRecentWorkouts(recentActivityRequest.email),
                competitions = databaseService.getRecentCompetitions(recentActivityRequest.email),
                routes = databaseService.getRecentRoutes(recentActivityRequest.email)
                ) // Call relevent functions in database service
                call.respond(mapOf("message" to recentActivity))
            }
            else {
                call.respond(mapOf("message" to "Error: No email provided")) // Return error to frontend if no user identifier given
            }
        }


        post("/todays-workouts"){
            val todaysWorkoutRequest = call.receive<TodayWorkoutsRequest>() // Extract data from request recieved

            if ( (todaysWorkoutRequest.email != null) ){ // If request contains some user identifier (i.e. username or email)
                val todaysWorkout: List<Workout> = databaseService.getTodaysWorkout(todaysWorkoutRequest.email) // Call TodaysWorkout function in database service
                call.respond(mapOf("message" to todaysWorkout))
            }
            else {
                call.respond(mapOf("message" to "Error: No email provided")) // Return error to fronend if no user identifier given
            }
        }


        post("/upcoming-competitions"){
            val upcomingCompetitionsRequest = call.receive<UpcomingCompetitionsRequest>() // Extract data from request in format from models

            if ( (upcomingCompetitionsRequest.email != null) ){ // If request contains some user identifier (username or email)
                val upcomingCompetitions: List<Competition> = databaseService.getUpcomingCompetitions(upcomingCompetitionsRequest.email) // Call UpcomingCompetitions function in database service
                call.respond(mapOf("message" to upcomingCompetitions))
            }
            else {
                call.respond(mapOf("message" to "Error: No email provided")) // Return error if no user identifier given
            }
        }
    }
}