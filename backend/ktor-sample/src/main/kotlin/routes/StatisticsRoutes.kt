package routes

import io.ktor.http.HttpStatusCode // Import different HTTP status codes for better responses to frontend
import io.ktor.server.request.receive // Import functionality to receive HTTP requests
import io.ktor.server.response.respond // Import functionality to respond to HTTP requests
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete // Import DELETE HTTP request routing functions
import io.ktor.server.routing.get // Import GET HTTP request routing functions
import io.ktor.server.routing.post // Import POST HTTP request routing functions
import io.ktor.server.routing.put // Import PUT HTTP request routing functions
import io.ktor.server.routing.route
import services.DatabaseService // Import the database service
import models.UpdateStatisticRequest as UpdateStatisticRequest // Import model for requesting to update a statistic
import services.DatabaseService.Statistic as Statistic // Import model for a statistic

fun Route.statisticsRoutes() {
    val db = DatabaseService() // Clone the database to the variable db

    /*
    Defines endpoints for CRUD operations for statistical analysis of user performance on the webapp.
    This includes:
        - Adding new statistics
        - Getting, updating and deleting existing statistics
        - Getting different statistics through various filters (i.e. best stat, all stats for an exercise, etc)
     */
    route("/api") {
        // ========================================
        // STAT WRITE ROUTES (ADD, UPDATE, DELETE)
        // ========================================

        /*
        The POST '/statistics' endpoint is responsible for handling HTTP requests for creating and adding new statistics to a
        user's statistics.

        The endpoint expects a valid, syntactically correct HTTP request, with the body containing values for associated user's ID
        (userID), the exercise the statistic is for's ID (statisticID), the statistic's score value (scoreValue) and the date the
        statistic was recorded on (date).

        The endpoint returns:
            - OK (200) and the new statistic's ID (statisticID), if the statistic was successfully created
            - InternalServerError (500) and a relevant message, if the statistic couldn't be created (i.e exercise not found, etc)
            - BadRequest (400) and a relevant message, if a malformed HTTP request was provided
         */
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

        /*
        The PUT '/statistics/{statisticID}' endpoint is responsible for updating a specific, existing statistic
        in the database. This is used for changing details associated with a statistic (the stat's scoreValue
        and/or date).

        The endpoint expects a valid, syntactically correct HTTP request, with the target statistic's ID provided
        at the end of the endpoint's route, and statistic's score value (scoreValue) and recorded date (date)
        provided in the HTTP request's body

        The endpoint returns:
            - OK (200) and a relevant message, if the statistic was successfully updated with the provided information
            - NotFound (404) and a relevant message, if the target statistic for updating could not be found
            - BadRequest (400) and a relevant message, if provided with an invalid, malformed HTTP request
         */
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

        /*
        The DELETE '/statistics/{statisticID}' endpoint is responsible for handling HTTP requests for deleting
        specific, existing statistics from the database.

        The endpoint expects a valid and syntactically correct HTTP request, with a value for target statistic's
        ID (statisticID) passed at the end of the endpoint's route as a parameter.

        The endpoint returns:
            - OK (200) and a relevant message, if the target statistic was successfully found and deleted from the database
            - NotFound (404) and a relevant message, if the target statistic could not be found and hence wasn't deleted
            - BadRequest (400) and a relevant message, if endpoint was given a malformed HTTP request
         */
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

        /*
        The GET '/statistic/exercise/{userID}/{exerciseID}' endpoint is responsible for retrieving the best
        statistic a user has recorded for a specific exercise, and returning it to the frontend for display
        to the user.

        The endpoint expects the user making the request's ID (userID) and the exercise they are looking
        for the best stat for's ID (exerciseID). These are both passed as parameter's at the end of the
        endpoint's route, with userID being followed by exerciseID.

        The endpoint returns:
            - OK (200) and best statistic for the user on specified exercise, if there was any statistic found for the user for the exercise
            - NotFound (404) and a relevant message, if no statistic was found for the user on the specified exercise
            - BadRequest (400) and a relevant message, if a invalid, malformed HTTP request was provided to the endpoint
         */
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

        /*
        The GET '/statistic/{statisticID}' endpoint is responsible for handling requests where the client
        wants to retrieve a specific statistic (dictated by the statisticID parameter) from the database.
        This allows for statistical data to be displayed to the user, where said statistics are recorded
        and available.

        The endpoint expects only the target statistic's ID (statisticID) passed as a parameter at the
        end of the endpoint's route. The value passed must be serialisable into an integer (i.e. not a
        string, double, etc).

        The endpoint returns:
            - OK (200) and the statistic found under statisticID, if the target statistic was successfully found in the database
            - NotFound (404) and a relevent message, if no statistic could be found under the provided statisticID
            - Bad Request (400) and a relevant message, if given a malformed HTTP request (i.e. statisticID parameter of wrong type)
         */
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

        /*
        The GET '/statistic/user/workout-progression/{userID}' endpoint is responsible for retrieving the specified user's
        progression on workouts, recorded over time, upon a valid HTTP request being made to the endpoint.

        The endpoint expects the target user's ID (userID) to be given as a parameter at the end of the endpoint's route; the
        value must also be serialisable into an integer by kotlinx's serialisation plugin (i.e. must be of integer type).

        The endpoint returns:
            - OK (200) and the user's workout progression, if there was sufficient statistical data in the db to calculate progression
            - NotFound (404) and a relevent message, if not enough statistical data was in the db to calculate the progression
            - BadRequest (400) and a relvant message, if the endpoint was provided with a malformed HTTP request
         */
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

        /*
        The GET '/statistic/user/workouts-completed/{userID}' endpoint is responsible for retrieving the total number of
        recorded workouts which a specific user has completed in the lifetime of their account, and returning it to the
        requesting client.

        The endpoint expects the target user's ID (userID) to be given as a parameter at the end of the endpoint's route;
        and also for the provided userID to be serialisable into an integer (i.e. not containing alphabetical characters).

        The endpoint returns:
            - OK (200) and the number of completed workouts (> 0), if there are completed workouts found in the database
            - NotFound (404) and a relevent message, if there are no completed workouts found in the database
            - BadRequest (400) and a relevant message, if a malformed HTTP request is provided to the endpoint
         */
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

        /*
        The GET '/statistic/user/hours-trained/{userID}' endpoint is responsible for finding the total number
        of hours trained (in workouts logged to the database) over the entire lifetime of the account. It is
        important to note that any logged workouts without a duration recorded does not have an impact on the
        number returned.

        The endpoint epxects to be provided with the target user's ID (userID) as a parameter at the end of the
        endpoint's route. This value must be serialisable into an integer.

        The endpoint returns:
            - OK (200) and the number of hours trained (>0), if the total recorded number of hours recorded sums to > 0.0
            - NotFound (404) and a relevant message, if the total number of recorded workout hours is equal to 0 (i.e. none found)
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
         */
        get("/statistic/user/hours-trained/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val hoursTrained = db.getHoursTrained(userID)

                call.respond(HttpStatusCode.OK, mapOf("Hours Trained" to hoursTrained))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        /*
        The GET '/statistic/user/workout-streak/{userID}' endpoint is responsible for retrieving the statistic
        for consecutive days with a workout logged to from the current day the HTTP request is made. (i.e. the
        streak of days with a workout).

        The endpoint expects the target user's ID (userID) to be passed as a parameter at the end of the
        endpoint's route. The value passed should be serialisable into an integer (i.e. contain no alphabetical
        characters).

        The endpoint returns:
            - OK (200) and the number of workout streak, if a streak > 0 is found in the database for the user
            - NotFound (404) and a relevant message, if no streak (i.e. streak = 0) is found in the database for the user
            - BadRequest (400) and a relevant message, if endpoint is given a malformed HTTP request (i.e. userID not serialisable)
         */
        get("/statistic/user/workout-streak/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()

                val streak = db.getCurrentStreak(userID)

                call.respond(HttpStatusCode.OK, mapOf("Current Streak" to streak))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid HTTP request"))
            }
        }

        /*
        The GET '/statistic/user/training-history/{userID}' endpoint is responsible for retrieving the
        target user's all-time training history since the account's initialisation (i.e. creation).

        The endpoint expects the target user's ID (userID) to be provided at the end of the endpoint's
        route, as an integer serialisable input (i.e. contains no alphabetical characters).

        The endpoint returns:
            - OK (200) and the user's training history, if there was training history associated with the user in the database
            - NotFound (404) and a relevant message, if no relevent history associated with the user was found in the database
            - BadRequest (400) and a releavant message, if the endpoint was provided with a malformed HTTP request
         */
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

        /*
        The GET '/statistics/user/exercise-breakdown/{userID}' is responsible for calling the backend
        to retrieve the targetted user's statistical breakdown for recorded exercises from the database.

        The endpoint expects the target user's ID (userID) to be provided as a parameter at the end of
        the endpoint's route, as an integer serialisable value (i.e. contains no alphabetical characters).

        The endpoint returns:
            - OK (200) and a list of all recorded exercises' statistical breakdown, if there were exercises found in the database
            - NotFound (404) and arelevant message, if there were no associated exercises to the user in the databsae to breakdown
            - BadRequest (400), and a relevant message, if the endpoint was provided with a malformed HTTP request
         */
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

        /*
        The GET '/statistics/user/personal-bests/{userID}' endpoint is responsible for calling the backend to
        retrieve the user's personal bests for all recorded exercises in the database, and then returning it
        to the requesting client for display to the user.

        The endpoint expects to be provided with the target user's ID (userID) as a parameter at the end of the
        endpoint's route. The value passed should be serialisable into an integer (i.e. contains no alphabetical
        characters).

        The endpoint returns:
            - OK (200) and a list of all exercises and their PBs for the user, if there were exercises for the user in the database
            - NotFound (404) and a relevant message, if there were no recorded exercises for the specified user
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
         */
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

        /*
        The GET '/statistics/exercise/{userID}/{exerciseID}' endpoint is responsible for calling the
        backend to retrieve all logged statistics for a user on a specified exercise, then returning the
        retrieved data to the requesting client for display to the user.

        The endpoint expects the target user's IS (userID) and target exercise's ID (exerciseID) to both be
        passed as parameters at the end of the endpoint's route (with exerciseID coming after userID). The
        value passed for both of these parameters must be serialisable into an integer (i.e. containing no
        alphabetical characters).

        The endpoint returns:
            - OK (200) and all statisitcs for the user for the exercise, if there were any statistics matching the parameters found
            - NotFound (404) and a relevant message, if there were no statistics for the user for the exercise found in the db
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
         */
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
