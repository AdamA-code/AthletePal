package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import models.Activity
import models.LogWorkoutRequest
import models.UpdateCompetitionRequest
import models.UpdateWorkoutExerciseRequest
import models.UpdateWorkoutRequest
import models.WorkoutExerciseResponse
import services.DatabaseService
import services.DatabaseService.Competition as Competition
import services.DatabaseService.Route as RunningRoute
import services.DatabaseService.Workout as Workout
import services.DatabaseService.WorkoutExercise as WorkoutExercise

fun Route.activityRoutes() {
    val db = DatabaseService() // Import service responsible for following requests

    route("/api") {
        // =====================
        // WORKOUT LOGGING
        // =====================

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

        // Get recent activity of any sort for a given user
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

        // Add an exercise to a existing workout
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
