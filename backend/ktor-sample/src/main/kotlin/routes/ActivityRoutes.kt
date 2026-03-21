package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.* // Import functionalityy to read JSON requests
import io.ktor.server.response.* // Import functionality to send responses to frontend

import models.TodayWorkoutsRequest
import models.UpcomingCompetitionsRequest
import models.RecentActivityRequest
import models.Activity

import services.DatabaseService

fun Route.activityRoutes(){
    val databaseService = DatabaseService()

    route("/dashboard"){
        post("/recent-activity"){
            val recentActivityRequest = call.receive<RecentActivityRequest>()

            if ( (recentActivityRequest.email != null) || (recentActivityRequest.username != null) ){
                val recentActivity: List<Activity> = DatabaseService.RecentActivity(recentActivityRequest.email, recentActivityRequest.username)
                call.respond(mapOf("message" to recentActivity))
            }
            else {
                call.respond(mapOf("message" to "Error: No email or username provided"))
            }
        }
        post("/todays-workouts"){
            
        }
        post("/upcoming-competitions"){}
    }
}