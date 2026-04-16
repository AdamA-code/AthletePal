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
import java.time.LocalDate
import services.AuthService

class DatabaseServiceTest {

    @Test
    fun testFullCRUDFlow() {

        val ds = DatabaseService()
        ds.deleteUser("integration@test.com")

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

        assertTrue(ds.updateAccount(email, "Female", 26, 181.0, 76.0))

        // Update password test (voluntary)

        val authService = AuthService()
        val oldPassword = "hash"
        val newPassword = "newPassword"

        // Set a proper hashed password first
        val key = authService.generateSalt()
        val hash = authService.hashPassword(oldPassword, key)
        ds.updatePassword(email, oldPassword, newPassword)

        // Update user with proper hash directly so we have a known state
        val stmt = ds.db.connection.prepareStatement(
            "UPDATE users SET passwordHash = ?, passwordKey = ? WHERE email = ?"
        )
        stmt.setString(1, hash)
        stmt.setString(2, key)
        stmt.setString(3, email)
        stmt.executeUpdate()
        stmt.close()

        // Wrong old password
        val fail = ds.updatePassword(email, "wrongPassword", newPassword)
        assertFalse(fail)

        // Correct password
        val success = ds.updatePassword(email, oldPassword, newPassword)
        assertTrue(success)

        // New password should now work
        assertTrue(authService.verifyPassword(newPassword, ds.findUser(email)!!.passwordKey, ds.findUser(email)!!.passwordHash))

        // Old password shouldn't work
        assertFalse(authService.verifyPassword(oldPassword, ds.findUser(email)!!.passwordKey, ds.findUser(email)!!.passwordHash))

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
                category = "Weights",
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
                duration = 60,
                workoutType = null
            )
        )

        assertTrue(workoutId > 0)

        val workouts = ds.findAllWorkouts(userId)
        assertTrue(workouts.isNotEmpty())

        assertTrue(
            ds.updateWorkout(
                workoutID = workoutId,
                date = "2026-01-02",
                duration = 75,
                workoutType = null
            )
        )

        // =========================
        // 4. WORKOUT EXERCISE
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

        // cascade: assertTrue(ds.deleteWorkoutExercise(workoutExercisesId))

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

    @Test
    fun testDashboardFunctions() {

        val ds = DatabaseService()

        val email = "dashboard@test.com"

        val userId = ds.createUser(
            User(
                userID = 0,
                name = "Dashboard Test",
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

        // Set up

        val today = LocalDate.now().toString()
        val future = "2050-12-01"
        val past = "2025-01-01"

        // Today
        val workoutId = ds.createWorkout(
            Workout(0, userId, userId, today, 60, null)
        )

        // Past
        ds.createWorkout(
            Workout(0, userId, userId, past, 45, null)
        )

        // Future
        ds.addComp(
            Competition(0, userId, "Future Comp", future, 0)
        )

        // Competition (past)
        ds.addComp(
            Competition(0, userId, "Past Comp", past, 1)
        )

        // Routes
        ds.addRoute(
            Route(0, userId, "Route 1", 5.0, 30.0, today)
        )

        ds.addRoute(
            Route(0, userId, "Route 2", 6.0, 35.0, past)
        )

        // Tests

        // Today test
        val todays = ds.getTodaysWorkout(email)
        assertTrue(todays.any { it.date == today })

        // Future Test
        val upcoming = ds.getUpcomingCompetitions(email)
        assertTrue(upcoming.any { it.date == future })
        assertTrue(upcoming.none { it.date == past })

        // Recent Routes
        val routes = ds.getRecentRoutes(email)
        assertTrue(routes.isNotEmpty())

        // Recent Comps
        val recentComps = ds.getRecentCompetitions(email)
        assertTrue(recentComps.isNotEmpty())

        // Cleanup
        assertTrue(ds.deleteUser(email))
    }

    @Test
    fun testGetRecentWorkoutsByType() {
        val ds = DatabaseService()
        val email = "recenttype@test.com"

        ds.deleteUser(email)

        val userId = ds.createUser(
            User(
                userID = 0,
                name = "Recent By Type User",
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

        // Need at least 4 to test cutoff is 3
        ds.createWorkout(Workout(0, userId, userId, "2025-01-01", 30, "cardio"))
        ds.createWorkout(Workout(0, userId, userId, "2025-02-01", 35, "cardio"))
        ds.createWorkout(Workout(0, userId, userId, "2025-03-01", 40, "cardio"))
        ds.createWorkout(Workout(0, userId, userId, "2025-04-01", 45, "cardio"))

        ds.createWorkout(Workout(0, userId, userId, "2025-01-01", 60, "weights"))
        ds.createWorkout(Workout(0, userId, userId, "2025-02-01", 65, "weights"))
        ds.createWorkout(Workout(0, userId, userId, "2025-03-01", 70, "weights"))
        ds.createWorkout(Workout(0, userId, userId, "2025-04-01", 75, "weights"))

        val recent = ds.getRecentWorkoutsByType(userId)

        // Only 3 of each returned
        assertEquals(3, recent["cardio"]?.size)
        assertEquals(3, recent["weights"]?.size)

        // Most recent first
        val cardio = recent["cardio"]!!
        assertTrue(cardio[0].date > cardio[1].date)
        assertTrue(cardio[1].date > cardio[2].date)

        assertTrue(ds.deleteUser(email))
    }

    @Test
    fun testStatsFunctions() {
        val ds = DatabaseService()
        val email = "statspage@test.com"


        val userId = ds.createUser(
            User(
                userID = 0,
                name = "Stats Test",
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

        val past1 = "2025-01-01"
        val past2 = "2025-06-15"
        val future = "2050-12-01"

        // Past cardio workouts
        ds.createWorkout(Workout(0, userId, userId, past1, 30, "cardio"))
        ds.createWorkout(Workout(0, userId, userId, past2, 45, "cardio"))

        // Past weights workout
        ds.createWorkout(Workout(0, userId, userId, past1, 60, "weights"))

        // Future workout (should not appear)
        ds.createWorkout(Workout(0, userId, userId, future, 50, "cardio"))

        // Workout with no type (should be excluded from progression but not workout Count)
        ds.createWorkout(Workout(0, userId, userId, past1, 20, null))


        // Test it splits the types
        val progression = ds.getWorkoutProgression(userId)

        assertTrue(progression.containsKey("cardio"))
        assertTrue(progression.containsKey("weights"))
        assertEquals(2, progression["cardio"]?.size)
        assertEquals(1, progression["weights"]?.size)

        // Check assends right way
        val cardio = progression["cardio"]!!
        assertTrue(cardio[0].date < cardio[1].date)

        // Didn't include future
        assertTrue(progression["cardio"]!!.none { it.date >= LocalDate.now().toString() })

        // Only counts completed
        val total = ds.getTotalWorkoutsCompleted(userId)
        assertEquals(4, total) // 2 cardio + 1 weights + 1 null

        // Cleanup
        assertTrue(ds.deleteUser(email))
    }

    @Test
    fun testPasswordReset() {
        val ds = DatabaseService()
        val email = "reset@test.com"

        ds.deleteUser(email)

        val authService = AuthService()
        val oldPassword = "oldPassword"

        val userId = authService.registerUser("Reset User", email, oldPassword, false)
        assertTrue(userId > 0)

        // Gen token doesn't work as no user
        assertNull(ds.generatePasswordResetToken("fakeUser@test.com"))

        // Gen token valid user
        val token = ds.generatePasswordResetToken(email)
        assertNotNull(token)

        // Wrong token
        val fail = ds.resetPasswordWithToken(email, "wrongtoken", "newPassword")
        assertFalse(fail)

        // Correct token
        val newPassword = "newPassword"
        val success = ds.resetPasswordWithToken(email, token!!, newPassword)
        assertTrue(success)

        // New password should work
        val user = ds.findUser(email)!!
        assertTrue(authService.verifyPassword(newPassword, user.passwordKey, user.passwordHash))

        // Old password
        assertFalse(authService.verifyPassword(oldPassword, user.passwordKey, user.passwordHash))

        // Token should be used
        val reusedToken = ds.resetPasswordWithToken(email, token, "password")
        assertFalse(reusedToken)

        // Cleanup
        assertTrue(ds.deleteUser(email))
    }
}