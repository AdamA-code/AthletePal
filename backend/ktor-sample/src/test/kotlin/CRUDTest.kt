package test

import java.sql.Connection // Interface for database connection
import java.sql.DriverManager // To connect to the DB
import java.sql.PreparedStatement // Precompiled SQL statement
import services.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val connection = DriverManager.getConnection("jdbc:sqlite:${System.getProperty("user.dir")}/src/main/resources/users.db")

class DatabaseServiceTest {

    // -----------------------------
    // USER TESTS
    // -----------------------------

    @Test
    fun testUserCRUD() {

        val email = "testuser@unit.com"

        val user = User(
            userID = 0,
            name = "Test User",
            email = email,
            passwordHash = "hash",
            passwordKey = "key",
            gender = "Male",
            age = 25,
            height = 180.0,
            weight = 75.0,
            accountType = false
        )

        // CREATE
        val created = createUser(user)
        assertTrue(created)

        // READ
        val found = findUser(email)
        assertNotNull(found)
        assertEquals(email, found.email)

        // UPDATE
        val updated = updateAccount(
            email,
            "Female",
            26,
            181.0,
            76.0
        )
        assertTrue(updated)

        // DELETE
        val deleted = deleteUser(email)
        assertTrue(deleted)

        // Needed for later tests
        createUser(user)
    }

    // -----------------------------
    // EXERCISE TESTS
    // -----------------------------

    @Test
    fun testExerciseCRUD() {

        val name = "Test Exercise"

        val exercise = Exercise(
            exerciseID = 1,
            name = name,
            category = "Chest",
            description = "Unit test exercise"
        )

        // CREATE
        val created = createExercise(exercise)
        assertTrue(created)

        // READ ALL
        val all = findAllExercises()
        assertTrue(all.isNotEmpty())

        // UPDATE
        val updated = updateExercise(
            name = name,
            category = "Back",
            description = "Updated"
        )
        assertTrue(updated)

        // (no delete by ID, so delete by name)
        val deleted = deleteExercise(name)
        assertTrue(deleted)

        //Needed for later
        createExercise(exercise)
    }

    // -----------------------------
    // WORKOUT TESTS
    // -----------------------------

    @Test
    fun testWorkoutCRUD() {

        val workout = Workout(
            createdBy = 1,
            assignedTo = 1,
            date = "2026-01-01",
            duration = 60
        )

        val created = createWorkout(workout)
        assertTrue(created)

        val workouts = findAllWorkouts(1)
        assertNotNull(workouts)

        if (workouts.isNotEmpty()) {
            val id = 1

            val updated = updateWorkout(
                workoutID = id,
                date = "2026-01-02",
                duration = 70
            )

            assertTrue(updated)

            val deleted = deleteWorkout(id)
            assertTrue(deleted)
        }
        // needed for later
        createWorkout(workout)
    }

    // -----------------------------
    // WORKOUT EXERCISE
    // -----------------------------

    @Test
    fun testWorkoutExerciseCRUD() {

        val we = WorkoutExercise(
            workoutExerciseID = 0,
            workoutID = 1,
            exerciseID = 1,
            sets = 3,
            reps = 10,
            weight = 80.0
        )

        val added = addExerciseToWorkout(we)
        assertTrue(added)

        val list = findAllWorkoutExercises(1)
        assertNotNull(list)

        if (list.isNotEmpty()) {
            val id = list.first().workoutExerciseID

            val updated = updateWorkoutExercise(
                workoutExerciseID = id,
                sets = 4,
                reps = 12,
                weight = 85.0
            )

            assertTrue(updated)

            val deleted = deleteWorkoutExercise(1)
            assertTrue(deleted)
        }
    }

    // -----------------------------
    // STATISTICS
    // -----------------------------

    @Test
    fun testStatisticsCRUD() {

        val stat = Statistic(
            statisticsID = 0,
            userID = 1,
            exerciseID = 1,
            scoreValue = 100.0,
            date = "2026-01-01"
        )

        val added = addStatistic(stat)
        assertTrue(added)

        val list = findAllStatistics(1, 1)
        assertNotNull(list)

        if (list.isNotEmpty()) {
            val id = list.first().statisticsID

            val updated = updateStatistic(
                statisticsID = id,
                scoreValue = 110.0,
                date = "2026-01-02"
            )

            assertTrue(updated)

            val deleted = deleteStatistic(id)
            assertTrue(deleted)
        }
    }

    // -----------------------------
    // COMPETITIONS
    // -----------------------------

    @Test
    fun testCompetitionCRUD() {

        val comp = Competition(
            competitionID = 0,
            userID = 1,
            name = "Test Comp",
            date = "2026-01-01",
            rank = 1
        )

        val added = addComp(comp)
        assertTrue(added)

        val list = findAllCompetitions(1)
        assertNotNull(list)

        if (list.isNotEmpty()) {
            val id = list.first().competitionID

            val updated = updateCompetition(
                competitionID = id,
                name = "Updated Comp",
                date = "2026-01-02",
                rank = 2
            )

            assertTrue(updated)

            val deleted = deleteCompetition(id)
            assertTrue(deleted)
        }

        // Needed for later tests
        addComp(comp)
    }

    // -----------------------------
    // ROUTES
    // -----------------------------

    @Test
    fun testRouteCRUD() {

        val route = Route(
            routeID = 0,
            userID = 1,
            name = "Test Route",
            distance = 5.0,
            estimatedTime = 30.0,
            date = "2026-01-01"
        )

        val added = addRoute(route)
        assertTrue(added)

        val list = findAllRoutes(1)
        assertNotNull(list)

        if (list.isNotEmpty()) {
            val id = list.first().routeID

            val updated = updateRoute(
                routeID = id,
                name = "Updated Route",
                distance = 6.0,
                estimatedTime = 32.0,
                date = "2026-01-02"
            )

            assertTrue(updated)

            val deleted = deleteRoute(id)
            assertTrue(deleted)
        }
    }

    // -----------------------------
    // CALENDAR
    // -----------------------------

    @Test
    fun testCalendarCRUD() {

        val event = CalendarEvent(
            calendarID = 0,
            userID = 1,
            eventType = "WORKOUT",
            eventID = 1,
            status = "NOT_COMPLETED",
            scheduledDate = "2026-01-01",
            name = "Test Event"
        )

        val added = addCalendarEvent(event)
        assertTrue(added)

        val list = findAllCalendarEvents(1, "2026-01-01")
        assertNotNull(list)

        if (list.isNotEmpty()) {
            val id = list.first().calendarID

            val updated = updateCalendarEvent(
                calendarID = id,
                status = "COMPLETED",
                scheduledDate = "2026-01-02",
                name = "Updated Event"
            )

            assertTrue(updated)

            val deleted = deleteCalendarEvent(id)
            assertTrue(deleted)
        }
    }
}