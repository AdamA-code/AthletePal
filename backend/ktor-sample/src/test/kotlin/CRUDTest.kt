package test

import services.*
import kotlin.test.*

class DatabaseServiceTest {

    @Test
    fun testFullCRUDFlow() {

        // =========================
        // 1. USER
        // =========================
        val email = "integration@test.com"

        val userId = createUser(
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

        val user = findUser(email)
        assertNotNull(user)
        assertEquals(email, user.email)

        assertTrue(
            updateAccount(email, "Female", 26, 181.0, 76.0)
        )

        // =========================
        // 2. EXERCISE
        // =========================
        val exerciseId = createExercise(
            Exercise(
                exerciseID = 0,
                name = "Bench Press",
                category = "Chest",
                description = "Integration test exercise"
            )
        )

        assertTrue(exerciseId > 0)

        val exercises = findAllExercises()
        assertTrue(exercises.any { it.name == "Bench Press" })

        assertTrue(
            updateExercise(
                name = "Bench Press",
                category = "Back",
                description = "Updated"
            )
        )

        // =========================
        // 3. WORKOUT
        // =========================
        val workoutId = createWorkout(
            Workout(
                workoutID = 0,
                createdBy = userId,
                assignedTo = userId,
                date = "2026-01-01",
                duration = 60
            )
        )

        assertTrue(workoutId > 0)

        val workouts = findAllWorkouts(userId)
        assertTrue(workouts.isNotEmpty())

        assertTrue(
            updateWorkout(
                workoutID = workoutId,
                date = "2026-01-02",
                duration = 75
            )
        )

        // =========================
        // 4. WORKOUT EXERCISE (FK SAFE)
        // =========================
        val workoutExercisesId = addExerciseToWorkout(
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
        
        val workoutExercises = findAllWorkoutExercises(workoutId)
        assertTrue(workoutExercises.isNotEmpty())

        assertTrue(
            updateWorkoutExercise(
                workoutExercisesID = workoutExercisesId,
                sets = 4,
                reps = 12,
                weight = 85.0
            )
        )

        assertTrue(deleteWorkoutExercise(workoutExercisesId))

        // =========================
        // 5. STATISTICS
        // =========================
        val statId = addStatistic(
            Statistic(
                statisticID = 0,
                userID = userId,
                exerciseID = exerciseId,
                scoreValue = 100.0,
                date = "2026-01-01"
            )
        )

        assertTrue(statId > 0)

        val stats = findAllStatistics(userId, exerciseId)
        assertTrue(stats.isNotEmpty())

        val stat = stats.first()

        assertTrue(
            updateStatistic(
                statisticID = stat.statisticID,
                scoreValue = 110.0,
                date = "2026-01-02"
            )
        )

        assertTrue(deleteStatistic(stat.statisticID))

        // =========================
        // 6. COMPETITION
        // =========================
        val compId = addComp(
            Competition(
                competitionID = 0,
                userID = userId,
                name = "Test Comp",
                date = "2026-01-01",
                rank = 1
            )
        )

        assertTrue(compId > 0)

        val comps = findAllCompetitions(userId)
        assertTrue(comps.isNotEmpty())

        val comp = comps.first()

        assertTrue(
            updateCompetition(
                competitionID = comp.competitionID,
                name = "Updated Comp",
                date = "2026-01-02",
                rank = 2
            )
        )

        assertTrue(deleteCompetition(comp.competitionID))

        // =========================
        // 7. ROUTE
        // =========================
        val routeId = addRoute(
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

        val routes = findAllRoutes(userId)
        assertTrue(routes.isNotEmpty())

        val route = routes.first()

        assertTrue(
            updateRoute(
                routeID = route.routeID,
                name = "Updated Route",
                distance = 6.0,
                estimatedTime = 32.0,
                date = "2026-01-02"
            )
        )

        assertTrue(deleteRoute(route.routeID))

        // =========================
        // 8. CALENDAR
        // =========================
        val calendarId = addCalendarEvent(
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

        val events = findAllCalendarEvents(userId, "2026-01-01")
        assertTrue(events.isNotEmpty())

        val event = events.first()

        assertTrue(
            updateCalendarEvent(
                calendarID = event.calendarID,
                status = "COMPLETED",
                scheduledDate = "2026-01-02",
                name = "Updated Event"
            )
        )

        assertTrue(deleteCalendarEvent(event.calendarID))

        // End it (cascading)
        assertTrue(deleteUser(email))
        assertTrue(deleteExercise(exerciseId))
    }
}