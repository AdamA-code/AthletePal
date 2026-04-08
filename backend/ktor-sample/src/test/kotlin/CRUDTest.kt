package test

import services.DatabaseService
import services.*
import kotlin.test.*
//Import classes
import services.DatabaseService.User
import services.DatabaseService.Exercise
import services.DatabaseService.Workout
import services.DatabaseService.WorkoutExercise
import services.DatabaseService.Statistic
import services.DatabaseService.Competition
import services.DatabaseService.Route
import services.DatabaseService.CalendarEvent

class DatabaseServiceTest {

    @Test
    fun testFullCRUDFlow() {

        val ds = DatabaseService()

        // =========================
        // 1. USER
        // =========================
        val email = "integration@test.com"

        val userId = ds.createUser(
            User(
                userID = 0,
                name = "Integration User",
                email = email,
                passwordHash = "hash",
                passwordKey = "key",
                gender = "Male",
                age = 25,
                height = 180.0,
                weight = 75.0,
                accountType = false
            )
        )

        assertTrue(userId > 0)

        val user = ds.findUser(email)
        assertNotNull(user)
        assertEquals(email, user.email)

        assertTrue(
            ds.updateAccount(email, "Female", 26, 181.0, 76.0)
        )

        // =========================
        // 2. EXERCISE
        // =========================
        val exerciseId = ds.createExercise(
            Exercise(
                exerciseID = 0,
                name = "Bench Press",
                category = "Chest",
                description = "Integration test exercise"
            )
        )

        assertTrue(exerciseId > 0)

        val exercises = ds.findAllExercises()
        assertTrue(exercises.any { it.name == "Bench Press" })

        assertTrue(
            ds.updateExercise(
                name = "Bench Press",
                category = "Back",
                description = "Updated"
            )
        )

        // =========================
        // 3. WORKOUT
        // =========================
        val workoutId = ds.createWorkout(
            Workout(
                workoutID = 0,
                createdBy = userId,
                assignedTo = userId,
                date = "2026-01-01",
                duration = 60
            )
        )

        assertTrue(workoutId > 0)

        val workouts = ds.findAllWorkouts(userId)
        assertTrue(workouts.isNotEmpty())

        assertTrue(
            ds.updateWorkout(
                workoutID = workoutId,
                date = "2026-01-02",
                duration = 75
            )
        )

        // =========================
        // 4. WORKOUT EXERCISE (FK SAFE)
        // =========================
        val workoutExercisesId = ds.addExerciseToWorkout(
            WorkoutExercise(
                workoutExercisesID = 0,
                workoutID = workoutId,
                exerciseID = exerciseId,
                sets = 3,
                reps = 10,
                weight = 80.0
            )
        )

        assertTrue(workoutExercisesId > 0)
        
        val workoutExercises = ds.findAllWorkoutExercises(workoutId)
        assertTrue(workoutExercises.isNotEmpty())

        assertTrue(
            ds.updateWorkoutExercise(
                workoutExercisesID = workoutExercisesId,
                sets = 4,
                reps = 12,
                weight = 85.0
            )
        )

        assertTrue(ds.deleteWorkoutExercise(workoutExercisesId))

        // =========================
        // 5. STATISTICS
        // =========================
        val statId = ds.addStatistic(
            Statistic(
                statisticID = 0,
                userID = userId,
                exerciseID = exerciseId,
                scoreValue = 100.0,
                date = "2026-01-01"
            )
        )

        assertTrue(statId > 0)

        val stats = ds.findAllStatistics(userId, exerciseId)
        assertTrue(stats.isNotEmpty())

        val stat = stats.first()

        assertTrue(
            ds.updateStatistic(
                statisticID = stat.statisticID,
                scoreValue = 110.0,
                date = "2026-01-02"
            )
        )

        assertTrue(ds.deleteStatistic(stat.statisticID))

        // =========================
        // 6. COMPETITION
        // =========================
        val compId = ds.addComp(
            Competition(
                competitionID = 0,
                userID = userId,
                name = "Test Comp",
                date = "2026-01-01",
                rank = 1
            )
        )

        assertTrue(compId > 0)

        val comps = ds.findAllCompetitions(userId)
        assertTrue(comps.isNotEmpty())

        val comp = comps.first()

        assertTrue(
            ds.updateCompetition(
                competitionID = comp.competitionID,
                name = "Updated Comp",
                date = "2026-01-02",
                rank = 2
            )
        )

        assertTrue(ds.deleteCompetition(comp.competitionID))

        // =========================
        // 7. ROUTE
        // =========================
        val routeId = ds.addRoute(
            Route(
                routeID = 0,
                userID = userId,
                name = "Test Route",
                distance = 5.0,
                estimatedTime = 30.0,
                date = "2026-01-01"
            )
        )

        assertTrue(routeId > 0)

        val routes = ds.findAllRoutes(userId)
        assertTrue(routes.isNotEmpty())

        val route = routes.first()

        assertTrue(
            ds.updateRoute(
                routeID = route.routeID,
                name = "Updated Route",
                distance = 6.0,
                estimatedTime = 32.0,
                date = "2026-01-02"
            )
        )

        assertTrue(ds.deleteRoute(route.routeID))

        // =========================
        // 8. CALENDAR
        // =========================
        val calendarId = ds.addCalendarEvent(
            CalendarEvent(
                calendarID = 0,
                userID = userId,
                eventType = "WORKOUT",
                eventID = workoutId,
                status = "NOT_COMPLETED",
                scheduledDate = "2026-01-01",
                name = "Test Event"
            )
        )

        assertTrue(calendarId > 0)

        val events = ds.findAllCalendarEvents(userId, "2026-01-01")
        assertTrue(events.isNotEmpty())

        val event = events.first()

        assertTrue(
            ds.updateCalendarEvent(
                calendarID = event.calendarID,
                status = "COMPLETED",
                scheduledDate = "2026-01-02",
                name = "Updated Event"
            )
        )

        assertTrue(ds.deleteCalendarEvent(event.calendarID))

        // End it (cascading)
        assertTrue(ds.deleteUser(email))
        assertTrue(ds.deleteExercise(exerciseId))
    }
}