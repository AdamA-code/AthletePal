package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import services.DatabaseService // Import the database service
import models.UpdateStatisticRequest as UpdateStatisticRequest
import services.DatabaseService.Statistic as Statistic // Import model for a statistic

fun Route.statisticsRoutes() {
    val db = DatabaseService() // Clone the database to the variable db

    route("/api") {
        // ========================================
        // STAT WRITE ROUTES (ADD, UPDATE, DELETE)
        // ========================================

        // Add new statistic route
        post("/statistics") {
            try {
                val stat = call.receive<Statistic>()
                val id = db.addStatistic(stat)

                if (id != -1) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else if (id == -1) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Internal Server Error occured"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Update a given statistic
        put("/statistics/{statisticID}") {
            try {
                val id = call.parameters["statisticID"]!!.toInt()
                val body = call.receive<UpdateStatisticRequest>()

                val updated = db.updateStatistic(id, body.scoreValue, body.date)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Statistic updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Statistic not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Delete a specific statistic
        delete("/statistics/{statisticID}") {
            try {
                val id = call.parameters["statisticID"]!!.toInt()

                val deleted = db.deleteStatistic(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Statistic deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Statistic not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // ========================================
        // STAT READ ROUTES
        // ========================================

        // Get only the best stat for a user, for an exercise

        get("/statistic/exercise/{userID}/{exerciseID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()
                val exerciseID = call.parameters["exerciseID"]!!.toInt()

                val bestStat = db.getBestStat(userID, exerciseID)

                if (bestStat != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Best Stat" to bestStat))
                } else if (bestStat == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No statistic found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get a specific statistic

        get("/statistic/{statisticID}") {
            try {
                val statisticID = call.parameters["statisticID"]!!.toInt()

                val stat = db.findStatistic(statisticID)

                if (stat != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Statistic" to stat))
                } else if (stat == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No statistic found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get progression for a workout
        get("/statistic/user/workout-progression/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val progression = db.getWorkoutProgression(userID)

                if (progression.isNullOrEmpty() == false) {
                    call.respond(HttpStatusCode.OK, mapOf("Progression" to progression))
                } else if (progression.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No progression found for user"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get a users total workouts completed
        get("/statistic/user/workouts-completed/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val completedWorkouts = db.getTotalWorkoutsCompleted(userID)

                if (completedWorkouts > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("Completed Workouts" to completedWorkouts))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No completed workouts found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get hours trained by a user
        get("/statistic/user/hours-trained/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val hoursTrained = db.getHoursTrained(userID)

                call.respond(HttpStatusCode.OK, mapOf("Hours Trained" to hoursTrained))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get a users current workout streak
        get("/statistic/user/workout-streak/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val streak = db.getCurrentStreak(userID)

                call.respond(HttpStatusCode.OK, mapOf("Current Streak" to streak))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get a users weekly training history
        get("/statistic/user/training-history/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val trainingHistory = db.getWeeklyTrainingHistory(userID)

                if (trainingHistory.isNullOrEmpty() == false) {
                    call.respond(HttpStatusCode.OK, mapOf("Training History" to trainingHistory))
                } else if (trainingHistory.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No history found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get a users exercise breakdown
        get("/statistics/user/exercise-breakdown/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val exerciseBreakdown = db.getExerciseBreakdown(userID)

                if (exerciseBreakdown.isEmpty() == false) {
                    call.respond(HttpStatusCode.OK, mapOf("Exercise Breakdown" to exerciseBreakdown))
                } else if (exerciseBreakdown.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No exercise breakdown found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get a users PBs
        get("/statistics/user/personal-bests/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val personalBests = db.getPersonalBests(userID)

                if (personalBests.isNullOrEmpty() == false) {
                    call.respond(HttpStatusCode.OK, mapOf("Personal Bests" to personalBests))
                } else if (personalBests.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No personal bests found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        // Get all statistics for a user, for an exercise
        get("/statistics/exercise/{userID}/{exerciseID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()
                val exerciseID = call.parameters["exerciseID"]!!.toInt()

                val stats = db.findAllStatistics(userID, exerciseID)

                if (stats.isNullOrEmpty() == false) {
                    call.respond(HttpStatusCode.OK, mapOf("Statistics" to stats))
                } else if (stats.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Statistics not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }
    }
}
