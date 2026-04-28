package routes

import io.ktor.http.HttpStatusCode // Import HTTP status codes for better HTTP responses
import io.ktor.server.request.receive // Import ability to recieve HTTP requests
import io.ktor.server.response.respond // Import ability to respond to recieved HTTP requests
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete // Import handling of DELETE HTTP requests
import io.ktor.server.routing.get // Import handling of GET HTTP requests
import io.ktor.server.routing.post // Import handling of POST HTTP requests
import io.ktor.server.routing.put // Import handling of PUT HTTP requests
import io.ktor.server.routing.route
import models.Activity // Import data class for an instance of Acitivty
import models.LogWorkoutRequest // Import data class for a request to log a workout
import models.UpdateCompetitionRequest // Import data class for a request to update an exisiting competition 
import models.UpdateWorkoutExerciseRequest // Import data class for a request to update an exercise associated with a workout
import models.UpdateWorkoutRequest // Import data class for a request to update an existing workout
import models.WorkoutExerciseResponse // Import data class for a HTTP response for a workout exercise operation
import services.DatabaseService // Import DatabaseService and all functions relatiing to CRUD operations with the database
import services.DatabaseService.Competition as Competition // Import model for a competition
import services.DatabaseService.Route as RunningRoute // Import model for a route (named RunningRoute to avoid variable name clashess)
import services.DatabaseService.Workout as Workout // Import model for a workout
import services.DatabaseService.WorkoutExercise as WorkoutExercise // Import model for a exercise assoicated with a workout

/*
Defines endpoints for activity-related HTTP requests, allowing API requests to be made to the backend
and hence the utilised databsae.
This includes:
    - Logging workouts, and updating said workouts
    - Recording, and updating, cardio
    - Adding competitions to the user's account, and updating details of competitions
    - etc.
*/
fun Route.activityRoutes() {
    val db = DatabaseService() // Import service responsible for following requests


    /*
    Activity Routes ('/api/...')
    Serves as a subdivider in order to seperate following activity-oriented endpoints from other endpoints 
    (such as social, statistics, authentication, etc) with aim of reducing HTTP requests sent to wrong API address
    */
    route("/api") {
        // =====================
        // WORKOUT LOGGING
        // =====================

        /*
        The POST '/log/workout' endpoint is responsible for handling HTTP requests for logging new workouts to a
        user's account, and calling backend functions to store the details of said workouts to the database for
        the user.

        The endpoint expects a valid HTTP request; the HTTP request's body should contain the details required,
        and any optional details, to create an instance of 'Workout' (i.e. createdBy, assignedTo, date, etc) and
        any exercises, and their required fields, which are associated with the workout (such fields include
        exerciseID, sets, reps, weight, etc). All fields passed in the body are expected to be serialisable into
        their respective data types (as defined in the LogWorkoutRequest data model)

        The endpoint returns:
            - OK (200) and the ID of the new workout, if the workout was successfully logged to the database
            - InternalServerError (500) and a relevant message, if something prevented the workout from being logged to the db
            - BadRequest (400) and a relevant message, if the endpoint is provided a malformed HTTP request
        */
        post("/log/workout") {
            try {
                val body = call.receive<LogWorkoutRequest>()
                val workout: Workout = body.workout
                val exercises: List<WorkoutExercise> = body.exercises

                val id = db.logWorkout(workout, exercises)

                if (id != -1) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else if (id == -1) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Workout logging failed"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The PUT '/log/workout' endpoint is responsible for handling HTTP requests for updates of
        existing logged workouts, and calling functions in the backend services to make said updates
        to the databse for storage.

        The endpoint expects all the same parameters as in logging a new workout in the HTTP request's
        body, however in addition it requires the workoutID in order to target the specific workout 
        for updating. Still, the request should have all fields serialisable to their respective data
        types (as dictated in the LogWorkoutRequest data model).

        The endpoint returns:
            - OK (200) and a relevant message, if the target workout was identified and updated with the given fields
            - NotFound (404) and a relevant message, if the target workout could not be found (or hence uppdated) in the db
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
        */
        put("/log/workout") {
            try {
                val body = call.receive<LogWorkoutRequest>()

                val workout: Workout = body.workout
                val exercises: List<WorkoutExercise> = body.exercises

                val updated = db.updateLoggedWorkout(workout, exercises)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Logged Workout Updated"))
                } else if (!updated) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout Not Found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // =====================
        // ACTIVITY
        // =====================

        /*
        The GET '/activity/recent/{email}' endpoint is responsible for handling HTTP requests made
        in order to retrieve recent activity for a given user, and calling backend services functions
        in order to retrieve said data from the database.

        The endpoint expects the target user's email (email) to be provided at the end of the endpoint's
        route as a parameter, in a form which can be serialised into a string (i.e. formed of alphanumeric
        characters).

        The endpoint returns:
            - OK (200) and a list of recent workouts, routes and upcoming competitions, if said data could be found for the user in the db
            - NotFound (404) and a relevant message, if no recent workouts or routes, or upcoming competitions could be found for user in db
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
        */
        get("/activity/recent/{email}") {
            try {
                val email = call.parameters["email"]!!
                val activity =
                    Activity(
                        db.getRecentWorkouts(email),
                        db.getUpcomingCompetitions(email),
                        db.getRecentRoutes(email),
                    )

                if (activity != null) {
                    call.respond(HttpStatusCode.OK, activity)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No recent activity found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all activity for a given user
        /*
        The GET '/activity/{userID}' endpoint is responsible for handling HTTP requests made to get any
        and all activity for a given user since the account's creation. It is also responsible for calling
        backend service(s)' functions to complete this request.

        The endpoint expects the target user's ID (userID) to be provided as a parameter at the end of the
        endpoint's route, in a format which can be serialised into an integer (i.e. contains no alphabetical
        characters, purely numeric).

        The endpoint returns:
            - OK (200) and a list of all activity for workouts, routes, competitions, if said data could be found for the user in db
            - NotFound (404) and a relevant message, if no recorded activity could be found for the user in the database 
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
        */
        get("/activity/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()
                val activity =
                    mapOf(
                        "Workouts" to db.findAllWorkouts(userID),
                        "Upcoming Competitions" to db.findAllCompetitions(userID),
                        "Routes" to db.findAllRoutes(userID),
                    )
                if (activity != null) {
                    call.respond(HttpStatusCode.OK, activity)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No activity found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // =====================
        // WORKOUTS
        // =====================

        // Get recent workouts for a given user
        /*
        The GET '/workouts/recent/{email}' endpoint is responsible for
        */
        get("/workouts/recent/{email}") {
            try {
                val email = call.parameters["email"]!!
                val workouts = db.getRecentWorkouts(email)

                if (workouts != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workouts" to workouts))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No recent workouts found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get workouts for today, for a user
        /*
        The GET '/workouts/today/{email}' endpoint is responsible for
        */
        get("/workouts/today/{email}") {
            try {
                val email = call.parameters["email"]!!
                val workouts = db.getTodaysWorkout(email)

                if (workouts != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workouts" to workouts))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No workouts found for today"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all workouts for a given user
        /*
        The GET '/workouts/{userID}' endpoint is responsible for
        */
        get("/workouts/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()
                val workouts = db.findAllWorkouts(userID)

                if (workouts != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workouts" to workouts))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Could not find workouts"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Get a single workout
        /*
        The GET '/workout/{workoutID}' endpoint is responsible for
        */
        get("/workout/{workoutID}") {
            try {
                val id = call.parameters["workoutID"]!!.toInt()
                val workout = db.findWorkout(id)

                if (workout != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Workout" to workout))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid workout ID"))
            }
        }

        // Add new workout to user account
        /*
        The POST '/workout' endpoint is responsible for
        */
        post("/workout") {
            try {
                val workout = call.receive<Workout>()
                val id = db.createWorkout(workout)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to create workout"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Update specific workout on user account
        /*
        The PUT '/workout/{workoutID}' endpoint is responsible for
        */
        put("/workout/{workoutID}") {
            try {
                val id = call.parameters["workoutID"]!!.toInt()
                val body = call.receive<UpdateWorkoutRequest>()

                val updated = db.updateWorkout(id, body.date ?: "", body.duration, body.workoutType, body.notes, body.routeID)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Workout updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Delete a specific workout from user account
        /*
        The DELETE '/workout/{workoutID}' endpoint is responsible for
        */
        delete("/workout/{workoutID}") {
            try {
                val id = call.parameters["workoutID"]!!.toInt()

                val deleted = db.deleteWorkout(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Workout deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // =====================
        // WORKOUT EXERCISES
        // =====================

        /*
        The POST '/workout/exercises/add-exercise' endpoint is responsible for
        */
        post("/workout/exercises/add-exercise") {
            try {
                val workoutExercise = call.receive<WorkoutExercise>()
                val id = db.addExerciseToWorkout(workoutExercise)

                if (id != -1) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else if (id == -1) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Find all exercises belonging to a given workout
        /*
        The GET '/workout/exercises/{workoutID}' endpoint is responsible for
        */
        get("/workout/exercises/{workoutID}") {
            try {
                val id = call.parameters["workoutID"]!!.toInt()
                val workoutExercises = db.findAllWorkoutExercises(id)

                if (!workoutExercises.isNullOrEmpty()) {
                    call.respond(
                        HttpStatusCode.OK,
                        workoutExercises.map { exercise ->
                            WorkoutExerciseResponse(
                                exerciseID = exercise.exerciseID,
                                sets = exercise.sets,
                                reps = exercise.reps,
                                weight = exercise.weight,
                            )
                        },
                    )
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No exercises found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Get specific exercise in a workout
        /*
        The GET '/workout/exercise/{exerciseWorkoutID}' endpoint is responsible for
        */
        get("/workout/exercise/{exerciseWorkoutID}") {
            try {
                val id = call.parameters["exerciseWorkoutID"]!!.toInt()

                val exercise = db.findWorkoutExercise(id)

                if (exercise != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Exercise" to exercise))
                } else if (exercise == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Exercise not found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Update an exercise in a given workout
        /*
        The PUT '/workout/exercise/{exerciseWorkoutID}' endpoint is responsible for
        */
        put("/workout/exercise/{exerciseWorkoutID}") {
            try {
                val id = call.parameters["exerciseWorkoutID"]!!.toInt()
                val body = call.receive<UpdateWorkoutExerciseRequest>()

                val updated = db.updateWorkoutExercise(id, body.sets, body.reps, body.weight)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Exercise updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Exercise not found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Delete ALL exercises from a workout (i.e. reset workout)
        /*
        The DELETE '/workout/exercises/{workoutID}' endpoint is responsible for
        */
        delete("/workout/exercises/{workoutID}") {
            try {
                val id = call.parameters["workoutID"]!!.toInt()

                val deleted = db.deleteAllExercisesInWorkout(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "All exercises deleted"))
                } else if (!deleted) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Delete an exercise from a workout
        /*
        The DELETE '/workout/exercise/{exerciseWorkoutID}' endpoint is responsible for
        */
        delete("/workout/exercise/{exerciseWorkoutID}") {
            try {
                val id = call.parameters["exerciseWorkoutID"]!!.toInt()

                val deleted = db.deleteWorkoutExercise(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Exercise deleted from workout"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Exercise not found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // =====================
        // COMPETITIONS
        // =====================

        // Get any and all upcoming competitions
        /*
        The GET '/competitions/upcoming/{email}' endpoint is responsible for
        */
        get("/competitions/upcoming/{email}") {
            try {
                val email = call.parameters["email"]!!
                val comps = db.getUpcomingCompetitions(email)

                if (comps != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Competitions" to comps))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No upcoming competitions found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all competitions for a given user
        /*
        The GET '/competitions/{userID}' endpoint is responsible for
        */
        get("/competitions/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()
                val comps = db.findAllCompetitions(userID)

                if (comps != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Competitions" to comps))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No competitions found for user ID"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Get a single competition
        /*
        The GET '/competition/{competitionID}' endpoint is responsible for
        */
        get("/competition/{competitionID}") {
            try {
                val id = call.parameters["competitionID"]!!.toInt()
                val comp = db.findCompetition(id)

                if (comp != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Competition" to comp))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Competition not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid competition ID"))
            }
        }

        // Add new competition to user account
        /*
        The POST '/competition' endpoint is responsible for
        */
        post("/competition") {
            try {
                val comp = call.receive<Competition>()
                val id = db.addComp(comp)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to create competition"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Update a given competition on the users account
        /*
        The PUT '/competition/{competitionID}' endpoint is responsible for
        */
        put("/competition/{competitionID}") {
            try {
                val id = call.parameters["competitionID"]!!.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                val body = call.receive<UpdateCompetitionRequest>()

                val updated = db.updateCompetition(id, body.name, body.date, body.rank, body.result)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Competition updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Competition not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Delete a given competition from the users account
        /*
        The DELETE '/competition/{competitionID}' endpoint is responsible for
        */
        delete("/competition/{competitionID}") {
            try {
                val id = call.parameters["competitionID"]!!.toInt()

                val deleted = db.deleteCompetition(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Competition deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Competition not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // =====================
        // ROUTES
        // =====================

        // Get any recent routes for a user
        /*
        The GET '/routes/recent/{email}' endpoint is responsible for
        */
        get("/routes/recent/{email}") {
            try {
                val email = call.parameters["email"]!!
                val routes = db.getRecentRoutes(email)

                if (routes != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Routes" to routes))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No recent routes found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Get all routes for a given user
        /*
        The GET '/routes/{userID}' endpoint is responsible for
        */
        get("/routes/{userID}") {
            try {
                val userID = call.parameters["userID"]!!.toInt()
                val routes = db.findAllRoutes(userID)

                if (routes != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Routes" to routes))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No routes found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Get a single route
        /*
        The GET '/route/{routeID}' endpoint is responsible for
        */
        get("/route/{routeID}") {
            try {
                val id = call.parameters["routeID"]!!.toInt()
                val route = db.findRoute(id)

                if (route != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Route" to route))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Route not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid user ID"))
            }
        }

        // Add a route to the user account
        /*
        The POST '/route' endpoint is responsible for
        */
        post("/route") {
            try {
                val route = call.receive<RunningRoute>()
                val id = db.addRoute(route)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to create route"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Update a given route on the users account
        /*
        The PUT '/route/{routeID}' endpoint is responsible for
        */
        put("/route/{routeID}") {
            try {
                val id = call.parameters["routeID"]!!.toInt()
                val body = call.receive<RunningRoute>()

                val updated =
                    db.updateRoute(
                        id,
                        body.name,
                        body.distance,
                        body.estimatedTime,
                        body.date,
                        body.isPublic,
                        body.routeData,
                    )

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Route updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Route not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        // Delete a specific route from the users account
        /*
        The DELETE '/route/{routeID}' endpoint is responsible for
        */
        delete("/route/{routeID}") {
            try {
                val id = call.parameters["routeID"]!!.toInt()
                val deleted = db.deleteRoute(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Route deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Route not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }
    }
}
