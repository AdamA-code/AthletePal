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
import services.DatabaseService.Exercise as Exercise // Import model for an exercise
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
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
                return@put
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

        /*
        The GET '/workouts/recent/{email}' endpoint is responsible for handling HTTP requests for
        getting recent workouts for a specific user. It is also responsible for calling the backend
        service(s)' functions to complete said request.

        The endpoint expects the target user's email (email) to be passed as a parameter at the end
        of the endpoint's route as a string serialisable value (i.e. consists of alphanumeric characters)

        The endpoint returns:
            - OK (200) and recent workouts found for the target user, if there were recent workout(s) found in the db for the user
            - NotFound (404) and a relevant message, if the target user could not be found OR no recent workouts could be found for the user
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The GET '/workouts/today/{email}' endpoint is responsible for handling HTTP requests made to
        retrieve workouts for a specific user, planned for today, It is also responsible for calling
        the relevent backend service(s)' functions and/or methods to fulfil said request.

        The endpoint requires the target user's email (email) to be given at the end of the endpoint's
        route, the email should be given as a string serialisable input (i.e. consists of alphanumeric
        characters).

        The endpoint returns:
            - OK (200) and todays workout, if there was a workout found for today, for the target user, in the db
            - NotFound (404) and a relevant message, if there was no workout found for today for the user OR the user wasn't found
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The GET '/workouts/{userID}' endpoint is responsible for handling HTTP requests made to get all workouts
        for a specific user. It is also responsible for calling the backend service functions and/or methods to
        fulfil the request.

        The endpoint expects the target user's ID (userID) to be provided as a parameter at the end of the
        endpoint's route. The value passed must be serialisable into an integer.

        The endpoint returns:
            - OK (200) and a list of all workouts for the target user, if the user was found with registered workouts in the db
            - NotFound (404) and a relevant message, if either the user wasn't found OR had no registered workouts to them
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The GET '/workout/{workoutID}' endpoint is responsible for handling HTTP requests to retrieve a
        single, specific workout from the database and calling the backend service's function(s) to
        fulfil said request.

        The endpoint expects to be provided the workoutID corresponding to the target workout, passed as
        a parameter at the end of the endpoint's route. The value passed must be serialisable into an
        integer.

        The endpoint returns:
            - OK (200) and the target workouts information, if there was a workout found with the provided workoutID
            - NotFound (404) and a relevant message, if no workout corresponding with the provided workoutID was found
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The POST '/workout' endpoint is responsible for adding an entirely new workout to a user's account.
        The workout does not include any associated exercises by default, and have to be explicity added with
        other endpoints, referencing the target workout's workoutID.

        The endpoint expects values for the 'Workout' object to be passed, required fields for workout creation
        are createdBy (Int), assignedTo (Int) and date (Str). There are also optional fields duration (Int), workoutType (Str),
        notes (Str), routeID (Int) allowing for more descript workouts to be added to the user account if desired by the client.
        These values are passed in the HTTP request body, and should all be serialisable into their respecive data
        types, as outlined in the Workout model.

        The endpoint returns:
            - OK (200) and the new workout's workoutID, if the workout was successfully created to the user's account with attached fields
            - InternalServerError (500) and a relevant message, if the workout could not be created due to a non-descript services error
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
         */
        post("/workout") {
            try {
                val workout = call.receive<Workout>()
                val id = db.createWorkout(workout)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Failed to create workout"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The PUT '/workout/{workoutID}' endpoint is responsible for updating a specific, existing workout on a users
        account. The workout is updated with all required fields, and does not create a new workout instance.

        The endpoint expects the target workoutID to be given as a integer serialisable parameter at the end of the
        endpoint's route (workoutID). The endpoint also expects the request body to contain date (as a required field)
        as well as duration, workoutType, notes, and routeID (all as optional fields).

        The endpoint returns:
            - OK (200) and a relevant change confirmation message, if the target workout was found and updated
            - NotFound (404) and a relevant message, if the target workout couldn't be found and hence couldn't be updated
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
         */
        put("/workout/{workoutID}") {
            try {
                val id = call.parameters["workoutID"]!!.toInt()
                val body = call.receive<UpdateWorkoutRequest>()

                val updated =
                    db.updateWorkout(
                        id,
                        body.date ?: "",
                        body.duration,
                        body.workoutType,
                        body.notes,
                        body.routeID,
                        body.isRoutine,
                    )

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Workout updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Workout not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        /*
        The DELETE '/workout/{workoutID}' endpoint is responsible for handling HTTP requests for requesting
        deletion of specific workouts from a user's account. It is also responsible for calling relevant
        backend services' functions in order to fulfil this request.

        The endpoint expects the target workout's ID (workoutID) to be provided at the end of the endpoint's
        route, as a integer serialisable value (i.e. only numeric characters, with no .). This value should
        be associated with an existing workout, desired for deletion.

        The endpoint returns:
            - OK (200) and a relevant confirmation message, if the workout could be found in the db and hence deleted
            - NotFound (404) and a relevant message, if the workoutID doesn't correspond with an existing workout and cannot be deleted
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
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
        The POST '/workout/exercises/add-exercise' endpoint is responsible for handling HTTP requests to create new
        exercises associated to a specific, existing workout on a user's account. It is also responsible for calling
        the backend service's functions in order to fulfil this request, passing it the values provided in the request
        for creation.

        The endpoint expects required values workoutID (Int) and exerciseID (Int), however also allows for optional
        fields sets (Int), reps (Int) and weight (Double) for more descript workout exercises to be created. The values
        passed in the request body must be serialisable into their respective data types.

        The endpoint returns:
            - OK (200) and the ID of the new workout exercise, if the workout exercise was successfully created in the database
            - NotFound (404) and a relevant message, if the specified workout could not be found (preventing workout exercise creation)
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
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

        /*
        The GET '/workout/exercises/{workoutID}' endpoint is responsible for handling HTTP requests to retrieve
        all of the workout exercises associated with a specific workout. It is also then responsible for calling
        the required backend service's functions in order to fulfil this request.

        The endpoint expects the target workout's ID to be passed as a parameter at the end of the endpoint's route.
        The value passed for workoutID here must be serialisable into an integer (i.e. consist only of numeric characters).

        The endpoint returns:
            - OK (200) and a list of all associated workout exercises, if the workout was found and contained > 0 exercises
            - NotFound (404) and a relevant message, if the specified workout wasn't found OR contained no exercises
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
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

        /*
        The GET '/workout/exercise/{exerciseWorkoutID}' endpoint is responsible for handling HTTP requests to
        retrieve a specific workout exercise and its details from the database. It is also then responsible
        for calling the backend service's functions in order to fulfil this request.

        The endpoint expects the target workout exercise's ID (exerciseWorkoutID) to be passed as a parameter
        at the end of the endpoint's route, as a integer serialisable value (i.e. contains only numeric characters).

        The endpoint returns:
            - OK (200) and the workout exercise found (and its info), if the workout exercise could be found in the database
            - NotFound (404) and a relevant message, if the specified workout exercise couldn't be found in the database
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The PUT '/workout/exercise/{exerciseWorkoutID}' endpoint is responsible for handling HTTP requests
        to update a specific, existing workout exercise in the database with the provided details. It is
        then also responsible for calling the backend service's functions in order to fulfil this request.

        The endpoint expects the target workout exercise's ID (exerciseWorkoutID) to be passed as a
        parameter at the end of the endpoint's route, as a integer serialisable value. It also expects
        a HTTP request body optionally containing the sets (Int), reps (Int), and weight (Double) fields
        where values passed (default to null) will be used for exercise updating.

        The endpoint returns:
            - OK (200) and a relevant confirmation message, if the specified workout was found and updated in the db
            - NotFound (404) and a relevant message, if the specified workout exercise couldn't be found and hence can't be updated
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The DELETE '/workout/exercises/{workoutID}' endpoint is responsible for handling HTTP requests to delete
        all workout exercises associated with a specific workout (essentially 'resetting' a workout). It is then
        also responsible for calling the backend service's functions in order to fulfil this request in the db
        for the requesting client.

        The endpoint expects the target workout's ID (workoutID) to be passed as a parameter at the end of the
        endpoint's route. The value passed should be serialisable into an integer (i.e. contains only numeric
        characters).

        The endpoint returns:
            - OK (200) and a relevant confirmation message, if the target workout was found in the db and was then deleted
            - NotFound (404) and a relevant message, if the target workout could not be found in the db and hence wasn't deleted
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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
                return@delete
            }
        }

        /*
        The DELETE '/workout/exercise/{exerciseWorkoutID}' endpoint is responsible for handling HTTP requests
        to delete a singular, specific workout exercise from a workout. It is then also responsible for
        calling the backend service's functions in order to fulfil this request in the db for the requesting
        client.

        The endpoint expects the target workout exercise's ID (exerciseWorkoutID) to be provided as a parameter
        at the end of the endpoint's route. The value passed here should be serialisable into an integer (i.e.
        contains only numeric characters).

        The endpoint returns:
            - OK (200) and a relevant confirmation message, if the workout exercise was found and hence deleted from the db
            - NotFound (404) and a relevant message, if the specified workout exercise wasn't found in the db and hance not deleted
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The GET '/exercises' endpoint is responsible for handling HTTP requests made in order to retrieve all of
        the exercises which we have stored in our database. It is therefore also responsible for calling all the
        backend service's function(s) required in order to retrieve these exercises from the database for the
        requesting client.

        The endpoint does not expect any values to be passed in the HTTP request, as we are not filtering or searching
        the exercises in the database in any way; we are purely sending everything in the table.

        The endpoint returns:
            - OK (200) and a list of all exercises, if the database was successfully access and exercises were found in the db table
            - InternalServerError (500) and a relevant message, if there was an internal error in backend services preventing db access
            - BadRequest (400) and a relevant message, if a malformed HTTP request is made to the endpoint
         */
        get("/exercises") {
            try {
                val exercises: List<Exercise> = db.findAllExercises()

                if (!exercises.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Exercises" to exercises))
                } else if (exercises.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Could not find exercises"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // =====================
        // COMPETITIONS
        // =====================

        /*
        The GET '/competitions/upcoming/{email}' endpoint is responsible for handling HTTP requests to get
        all upcoming competitions registered to a specific user. It is then also responsible for calling the
        backend service's function(s) in order to fulfil this request from the db, for the requesting client.

        The endpoint expects the target user's email (email) to be passed as a parameter at the end of the
        endpoint's route. The value passed should be serialisable into a string data type.

        The endpoint returns:
            - OK (200) and a list of any and all upcoming competitions, if the user was found and had competitions upcoming
            - NotFound (404) and a relevant message, if the specified user wasn't found OR had not upcoming competitions
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The GET '/competitions/{userID}' endpoint is responsible for handling HTTP requests to get all competitions
        (both past and upcoming) on a user's account. It is then also responsible for calling the backend service's
        function(s) to fulfil this request from the db, for the requesting client.

        The endpoint expects the target user's ID (userID) to be passed as a parameter at the end of the endpoint's
        route. The value passed here must also be serialisable into an integer (i.e. contain only numeric characters).

        The endpoint returns:
            - OK (200) and a list of all competitions for the user, if the user was found and had competitions registered
            - NotFound (404) and a relevant message, if the user wasn't found OR had no competitions registered to their account
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The GET '/competition/{competitionID}' endpoint is responsible for handling HTTP requests made to get a
        single, specific competition from the database. It is then also responsible for calling the backend
        service's function(s) required to fulfil this request from the db, for the requesting client.

        The endpoint expects the target competition's ID (competitionID) to be passed as a parameter at the end
        of the endpoint's route. The value passed here should also be serialisable into an integer (i.e. contains
        only numeric characters).

        The endpoint returns:
            - OK (200) and the competition found for ID, if the competition correlated to a single competition in the database
            - NotFound (404) and a relevant message, if there could be no competition found in the databse for the provided ID
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The POST '/competition' endpoint is responsible for handling HTTP requests to add a new competition to
        a given user's account. It is then also responsible for calling the backend service's functions to
        fulfil this request in the database for the requesting client.

        The endpoint expects a syntactically valid HTTP request, with a body containing userID (Int), name (Str),
        and date (Str) as well as optionally the rank (Int) and result (Int) of the competition.

        The endpoint returns:
            - OK (200) and the ID of the new competition, if the competition was successfully created with the given values in the db
            - InternalServerError (500) and a relevant message, if a logical error in the backend services prevented competiton creation
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
         */
        post("/competition") {
            try {
                val comp = call.receive<Competition>()
                val id = db.createCompetition(comp)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Failed to create competition"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        /*
        The PUT '/competition/{competitionID}' endpoint is responsible for handling HTTP requests
        made to update a specific, existing competition on a user's account. It is also responsible
        for calling the required backend service's function(s) to fulfil this request in the db
        for the requesting client.

        The endpoint expects the target competition's ID (competitionID) to be passed as an integer
        serialisable parameter at the end of the endpoint's route. It also expects the HTTP request
        body to contain the updated values for name and date, as well as optionally rank and result.

        The endpoint returns:
            - OK (200) and a relevant message, if the specified competition was successfully updated with the new info in the db
            - NotFound (404) and a relevant message, if the target competition wasn't found in the db and hence wasn't updated
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The DELETE '/competition/{competitionID}' endpoint is responsible for handling HTTP requests
        made to delete a specific competition from a user's acocunt in the database. It is also then
        responsible for calling the required backend service's function(s) required to fulfil this
        request in the database for the requesting client.

        The endpoint expects the target competition's ID (competitionID) to be passed as a integer
        serialisable parameter at the end of the endpoint's route.

        The endpoint returns:
            - OK (200) and a relevant confirmation message, if the competition was found and thus deleted from the db
            - NotFound (404) and a relevant message, if the specified competition wasn't found in the db and hence not deleted
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
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

        /*
        The GET '/routes/recent/{email}' endpoint is responsible for handling HTTP requests made to
        retrieve any and all recent routes from the database for a given user. It is then also
        responsible for calling the required backend service's function(s) to fulfil this request
        from the db for the requesting client.

        The endpoint expects the target user's email (email) to be provided as a string serialisable
        parameter at the end of the endpoint's route.

        The endpoint returns:
            - OK (200) and a list of all recent routes found, if the user was identified and had > 0 recent routes recorded
            - NotFound (404) and a relevant message, if the user couldn't be found in the db OR had no recent routes recorded
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
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

        /*
        The GET '/routes/{userID}' endpoint is responsible for handling HTTP requests made to
        retrieve all routes recorded to a given user's acocunt, irrespective of when they were
        recorded. It is then also responsible for calling the required backend service's function(s)
        required to fulfil this request from the databse, for the requesting client.

        The endpoint expects the target user's ID (userID) to be provided as an integer serialisable
        parameter at the end of the endpoint's route.

        The endpoint returns:
            - OK (200) and a list of all routes registered to the user, if the user could be found and had > 0 routes recorded
            - NotFound (404) and a relevant message, if the user could not be found OR had no recorded routes on the account
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The GET '/route/{routeID}' endpoint is responsible for handling HTTP requests to retrieve
        a single, specific route from the database. The endpoint is then also responsible for
        calling the required backend service's function(s) needed to fulfil this request.

        The endpoint expects the target route's ID (routeID) to be provided as a integer serialisable
        parameter at the end of the endpoint's route.

        The endpoint returns:
            - OK (200) and the route found under the given ID, if a route could be found in the database for the specified ID
            - NotFound (404) and a relevant message, if no route could be found in the database under the given routeID
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The POST '/route' endpoint is responsible for handling HTTP requests made to add a new
        route to a user's account. It is then also responsible for calling the required backend
        service's function(s) needed to fulfil this request in the database for the requesting
        client.

        The endpoint expects a syntactically valid HTTP request, with a body containing userID (Int),
        name (Str), distance (Double), date (String), isPublic (Boolean) and routeData (String) as
        required fields, as well as optionally estimatedTime (Double).

        The endpoint returns:
            - OK (200) and the ID of the new route, if the route was successfully created in the db with the given data
            - InternalServerError (500) and a relevant message, if some logic error in the backend service prevented route creation
            - BadRequest (400) and a relevant message, if the endpoint is provided with a malformed HTTP request
         */
        post("/route") {
            try {
                val route = call.receive<RunningRoute>()
                val id = db.createRoute(route)

                if (id > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Failed to create route"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        /*
        The PUT '/route/{routeID}' endpoint is responsible for handling HTTP requests made to update
        a single, specific route on a users account in the database. The endpoint is then responsible
        for calling the required backend service's function(s) required in order to update the route
        in the database, for the requesting client.

        The endpoint expects the target route's ID (routeID) to be passed as an integer serialisable
        parameter at the end of the endpoint's route. It also expects the required fields: name (Str),
        distance (Double), date (Str), isPublic (Boolean), routeData (Str), as well as optional field:
        estimatedTime (Double), to be passed in the body of the HTTP request.

        The endpoint returns:
            - OK (200) and a relevant confirmation message, if the route was identified and updated in the database with the given data
            - NotFound (404) and a relevant message, if the route could not be found in the databse so wasn't updated
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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

        /*
        The DELETE '/route/{routeID}' endpoint is responsible for handling HTTP requests made to delete
        a specific, single route from a user's account in the database. The endpoint is then also
        responsible for calling the backend service's function(s) required in order to fulfil the request
        in the database, for the requesting client.

        The endpoint expects the target route's ID (routeID) to be provided as an integer serialisable
        parameter at the end of the endpoint's route.

        The endpoint returns:
            - OK (200) and a relevant confirmation message, if the route could be found in the database and was then deleted successfully
            - NotFound (404) and a relevant message, if the route could not be found in the database anda hence wasn't deleted
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
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
