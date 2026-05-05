package test

import services.AuthService
import services.DatabaseService
import services.DatabaseService.Competition
import services.DatabaseService.Exercise
import services.DatabaseService.GroupSession
import services.DatabaseService.Route
import services.DatabaseService.Statistic
import services.DatabaseService.TrainingGroup
import services.DatabaseService.User
import services.DatabaseService.Workout
import services.DatabaseService.WorkoutExercise
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DatabaseServiceTest {
    @Test
    fun testFullCRUDFlow() {
        val ds = DatabaseService()
        ds.deleteUser("integration@test.com")

        // =========================
        // User CRUD
        // =========================
        val email = "integration@test.com"

        val userId =
            ds.createUser(
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
                    accountType = false,
                ),
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
        val stmt =
            ds.db.connection.prepareStatement(
                "UPDATE users SET passwordHash = ?, passwordKey = ? WHERE email = ?",
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
        // Exercise CRUD
        // =========================
        var exerciseId =
            ds.createExercise(
                Exercise(
                    exerciseID = 0,
                    name = "Test exercise",
                    category = "weights",
                    description = "Integration test exercise",
                ),
            )

        assertTrue(exerciseId > 0)

        val exercises = ds.findAllExercises()
        assertTrue(exercises.any { it.name == "Test exercise" })

        assertTrue(
            ds.updateExercise(
                name = "Test exercise",
                category = "weights",
                description = "Updated",
            ),
        )

        assertTrue(
            ds.deleteExercise(
                exerciseId,
            ),
        )

        // =========================
        // Workout CRUD
        // =========================
        val workoutId =
            ds.createWorkout(
                Workout(
                    workoutID = 0,
                    createdBy = userId,
                    assignedTo = userId,
                    date = "2026-01-01",
                    duration = 60,
                    workoutType = null,
                    notes = null,
                ),
            )

        assertTrue(workoutId > 0)

        val workouts = ds.findAllWorkouts(userId)
        assertTrue(workouts.isNotEmpty())

        assertTrue(
            ds.updateWorkout(
                workoutID = workoutId,
                date = "2026-01-02",
                duration = 75,
                workoutType = null,
                notes = null,
                routeID = null,
                isRoutine = false,
            ),
        )

        // =========================
        // Workout Exercise CRUD
        // =========================
        println("ExerciseID: $exerciseId")
        println("WorkoutID: $workoutId")

        exerciseId = (1..20000).firstOrNull {
            ds.findExercise(it) != null
        } ?: throw IllegalStateException("No exercises in database")

        val workoutExercisesId =
            ds.addExerciseToWorkout(
                WorkoutExercise(
                    workoutExercisesID = 0,
                    workoutID = workoutId,
                    exerciseID = exerciseId,
                    sets = 3,
                    reps = 10,
                    weight = 80.0,
                ),
            )

        assertTrue(workoutExercisesId > 0)

        val workoutExercises = ds.findAllWorkoutExercises(workoutId)
        assertTrue(workoutExercises.isNotEmpty())

        assertTrue(
            ds.updateWorkoutExercise(
                workoutExercisesID = workoutExercisesId,
                sets = 4,
                reps = 12,
                weight = 85.0,
            ),
        )

        assertTrue(ds.deleteWorkoutExercise(workoutExercisesId))

        // =========================
        // Stas CRUD
        // =========================
        val statId =
            ds.createStatistic(
                Statistic(
                    statisticID = 0,
                    userID = userId,
                    exerciseID = exerciseId,
                    scoreValue = 100.0,
                    date = "2026-01-01",
                ),
            )

        assertTrue(statId > 0)

        val stats = ds.findAllStatistics(userId, exerciseId)
        assertTrue(stats.isNotEmpty())

        val stat = stats.first()

        assertTrue(
            ds.updateStatistic(
                statisticID = stat.statisticID ?: 0,
                scoreValue = 110.0,
                date = "2026-01-02",
            ),
        )

        assertTrue(ds.deleteStatistic(stat.statisticID ?: 0))

        // =========================
        // Competition CRUD
        // =========================
        val compId =
            ds.createCompetition(
                Competition(
                    competitionID = 0,
                    userID = userId,
                    name = "Test Comp",
                    date = "2026-01-01",
                    rank = 1,
                    result = 5,
                ),
            )

        assertTrue(compId > 0)

        val comps = ds.findAllCompetitions(userId)
        assertTrue(comps.isNotEmpty())

        val comp = comps.first()

        assertTrue(
            ds.updateCompetition(
                competitionID = comp.competitionID!!,
                name = "Updated Comp",
                date = "2026-01-02",
                rank = 2,
                result = 3,
            ),
        )

        assertTrue(ds.deleteCompetition(comp.competitionID))

        // =========================
        // Routes CRUD
        // =========================
        val routeId =
            ds.createRoute(
                Route(
                    routeID = 0,
                    userID = userId,
                    name = "Test Route",
                    distance = 5.0,
                    estimatedTime = 30.0,
                    date = "2026-01-01",
                    isPublic = false,
                    routeData = "Fake route data",
                ),
            )

        assertTrue(routeId > 0)

        val routes = ds.findAllRoutes(userId)
        assertTrue(routes.isNotEmpty())

        val route = routes.first()

        assertTrue(
            ds.updateRoute(
                routeID = route.routeID!!,
                name = "Updated Route",
                distance = 6.0,
                estimatedTime = 32.0,
                date = "2026-01-02",
                isPublic = true,
                routeData = "Still fake route data",
            ),
        )

        assertTrue(ds.deleteRoute(route.routeID))
    }

    @Test
    fun testDashboardFunctions() {
        val ds = DatabaseService()

        val email = "dashboard@test.com"
        ds.deleteUser(email)

        val userId =
            ds.createUser(
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
                    accountType = false,
                ),
            )

        assertTrue(userId > 0)

        // Set up

        val today = LocalDate.now().toString()
        val future = "2050-12-01"
        val past = "2025-01-01"

        // Today
        val workoutId =
            ds.createWorkout(
                Workout(0, userId, userId, today, 60, null, null),
            )

        // Past
        ds.createWorkout(
            Workout(0, userId, userId, past, 45, null),
        )

        // Future
        ds.createCompetition(
            Competition(0, "Weights", userId, "Future Comp", future, null, null),
        )

        // Competition (past)
        ds.createCompetition(
            Competition(0, "Weights", userId, "Past Comp", past, 1, 500),
        )

        // Routes
        ds.createRoute(
            Route(0, userId, "Route 1", 5.0, 30.0, today, false, "Big loop somewhere on map"),
        )

        ds.createRoute(
            Route(0, userId, "Route 2", 6.0, 35.0, past, true, "Big square somewhere on map"),
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

        val userId =
            ds.createUser(
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
                    accountType = false,
                ),
            )
        assertTrue(userId > 0)

        // Need at least 4 to test cutoff is 3
        ds.createWorkout(Workout(0, userId, userId, "2025-01-01", 30, "cardio", null, null))
        ds.createWorkout(Workout(0, userId, userId, "2025-02-01", 35, "cardio", null, null))
        ds.createWorkout(Workout(0, userId, userId, "2025-03-01", 40, "cardio", null, null))
        ds.createWorkout(Workout(0, userId, userId, "2025-04-01", 45, "cardio", null, null))

        ds.createWorkout(Workout(0, userId, userId, "2025-01-01", 60, "weights", null, null))
        ds.createWorkout(Workout(0, userId, userId, "2025-02-01", 65, "weights", null, null))
        ds.createWorkout(Workout(0, userId, userId, "2025-03-01", 70, "weights", null, null))
        ds.createWorkout(Workout(0, userId, userId, "2025-04-01", 75, "weights", null, null))

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
        ds.deleteUser(email)

        val userId =
            ds.createUser(
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
                    accountType = false,
                ),
            )
        assertTrue(userId > 0)

        val past1 = "2025-01-01"
        val past2 = "2025-06-15"
        val future = "2050-12-01"

        // Past cardio workouts
        ds.createWorkout(Workout(0, userId, userId, past1, 30, "cardio", "Test notes", null))
        ds.createWorkout(Workout(0, userId, userId, past2, 45, "cardio", "Test notes 2", null))

        // Past weights workout
        ds.createWorkout(Workout(0, userId, userId, past1, 60, "weights", null))

        // Future workout (should not appear)
        ds.createWorkout(Workout(0, userId, userId, future, 50, "cardio", null))

        // Workout with no type (should be excluded from progression but not workout Count)
        ds.createWorkout(Workout(0, userId, userId, past1, 20, null, null))

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
        val fail = ds.resetPasswordWithToken("wrongtoken", "newPassword")
        assertFalse(fail)

        // Correct token
        val newPassword = "newPassword"
        val success = ds.resetPasswordWithToken(token, newPassword)
        assertTrue(success)

        // New password should work
        val user = ds.findUser(email)!!
        assertTrue(authService.verifyPassword(newPassword, user.passwordKey, user.passwordHash))

        // Old password
        assertFalse(authService.verifyPassword(oldPassword, user.passwordKey, user.passwordHash))

        // Token should be used
        val reusedToken = ds.resetPasswordWithToken(token, "password")
        assertFalse(reusedToken)

        // Cleanup
        assertTrue(ds.deleteUser(email))
    }

    @Test
    fun testWorkoutLogFunctions() {
        val ds = DatabaseService()
        val email = "workoutlog@test.com"

        ds.deleteUser(email)

        val userId =
            ds.createUser(
                User(
                    userID = 0,
                    name = "Workout Log User",
                    email = email,
                    passwordHash = "hash",
                    passwordKey = "key",
                    gender = "Male",
                    age = 25,
                    height = 180.0,
                    weight = 75.0,
                    accountType = false,
                ),
            )
        assertTrue(userId > 0)

        // Create exercises
        val benchPressID = ds.createExercise(Exercise(0, "Test exercise", "weights", null))
        val squatID = ds.createExercise(Exercise(0, "Test exercise 2", "weights", null))
        val runID = ds.createExercise(Exercise(0, "Test exercise 3", "running", null))

        assertTrue(benchPressID > 0)
        assertTrue(squatID > 0)
        assertTrue(runID > 0)

        val today = LocalDate.now().toString()

        // Weights workout
        val workoutID =
            ds.logWorkout(
                Workout(0, userId, userId, today, 60, "weights", "Log this exercise"),
                listOf(
                    WorkoutExercise(0, 0, benchPressID, 3, 10, 100.0),
                    WorkoutExercise(0, 0, squatID, 3, 10, 80.0),
                ),
            )
        assertTrue(workoutID > 0)

        // Check exercises are linked
        val exercises = ds.findAllWorkoutExercises(workoutID)
        assertEquals(2, exercises.size)
        assertTrue(exercises.any { it.exerciseID == benchPressID })
        assertTrue(exercises.any { it.exerciseID == squatID })

        // Update tests

        // Update workout
        val updated =
            ds.updateLoggedWorkout(
                Workout(workoutID, userId, userId, today, 90, "weights", null, null),
                listOf(
                    WorkoutExercise(0, workoutID, benchPressID, 4, 8, 110.0),
                    WorkoutExercise(0, workoutID, runID, null, null, null),
                ),
            )
        assertTrue(updated)

        // Old exercises should be replaced
        val updatedExercises = ds.findAllWorkoutExercises(workoutID)
        assertEquals(2, updatedExercises.size)
        assertTrue(updatedExercises.any { it.exerciseID == benchPressID })
        assertTrue(updatedExercises.any { it.exerciseID == runID })
        assertTrue(updatedExercises.none { it.exerciseID == squatID })

        assertTrue(ds.deleteUser(email))
        assertTrue(ds.deleteExercise(benchPressID))
        assertTrue(ds.deleteExercise(runID))
        assertTrue(ds.deleteExercise(squatID))
    }

    @Test
    fun testPersonalBests() {
        val ds = DatabaseService()
        val email = "pbs@test.com"
        val today = LocalDate.now().toString()

        ds.deleteUser(email)

        val userId =
            ds.createUser(
                User(
                    userID = 0,
                    name = "PB User",
                    email = email,
                    passwordHash = "hash",
                    passwordKey = "key",
                    gender = "Male",
                    age = 25,
                    height = 180.0,
                    weight = 75.0,
                    accountType = false,
                ),
            )
        assertTrue(userId > 0)

        val squatID = ds.createExercise(Exercise(0, "Test Squat", "weights", null))
        val benchPressID = ds.createExercise(Exercise(0, "Test bench press", "weights", null))

        // Add a statistic for bench press
        ds.createStatistic(Statistic(0, userId, benchPressID, 100.0, today))
        ds.createStatistic(Statistic(0, userId, benchPressID, 110.0, today)) // Highscore
        ds.createStatistic(Statistic(0, userId, squatID, 80.0, today))

        val pbs = ds.getPersonalBests(userId)
        assertTrue(pbs.isNotEmpty())

        // PB should be 110
        val benchPB = pbs.find { it.exerciseID == benchPressID }
        assertNotNull(benchPB)
        assertEquals(110.0, benchPB.scoreValue, 0.01) // MArgin of change since double

        // Squat should be 80
        val squatPB = pbs.find { it.exerciseID == squatID }
        assertNotNull(squatPB)
        assertEquals(80.0, squatPB.scoreValue, 0.01)

        // Cleanup
        assertTrue(ds.deleteUser(email))
        assertTrue(ds.deleteExercise(benchPressID))
        assertTrue(ds.deleteExercise(squatID))
    }

    @Test
    fun testTrainingGroup() {
        val ds = DatabaseService()
        val creatorEmail = "creator@test.com"
        val memberEmail = "member@test.com"

        ds.deleteUser(creatorEmail)
        ds.deleteUser(memberEmail)

        val creatorID =
            ds.createUser(
                User(0, "Group Creator", creatorEmail, "hash", "key", "Male", 25, 180.0, 75.0, false),
            )
        val memberID =
            ds.createUser(
                User(0, "Group Member", memberEmail, "hash", "key", "Female", 22, 165.0, 60.0, false),
            )
        assertTrue(creatorID > 0)
        assertTrue(memberID > 0)

        // Create group

        val groupID =
            ds.createGroup(
                TrainingGroup(0, creatorID, "Test Group", "A test group", "Run a 5k"),
            )
        assertTrue(groupID > 0)

        // Creator should be automatically added
        val members = ds.findGroupMembers(groupID)
        assertEquals(1, members.size)
        assertTrue(members.any { it.userID == creatorID })

        // Find group

        val group = ds.findGroup(groupID)
        assertNotNull(group)
        assertEquals("Test Group", group.name)
        assertEquals("A test group", group.description)
        assertEquals("Run a 5k", group.goal)

        // Update group

        assertTrue(ds.updateGroup(groupID, "Updated Group", "Updated description", "Run a 10k"))
        val updated = ds.findGroup(groupID)
        assertNotNull(updated)
        assertEquals("Updated Group", updated.name)
        assertEquals("Run a 10k", updated.goal)

        // Join group

        assertTrue(ds.joinGroup(groupID, memberID))
        val newMembers = ds.findGroupMembers(groupID)
        assertEquals(2, newMembers.size)
        assertTrue(newMembers.any { it.userID == memberID })

        //  Find all groups for user

        val creatorGroups = ds.findAllGroups()
        assertTrue(creatorGroups.any { it.groupID == groupID })

        val memberGroups = ds.findAllGroups()
        assertTrue(memberGroups.any { it.groupID == groupID })

        // Join table sessions tests

        val future = LocalDate.now().plusDays(7).toString()
        val past = "2025-01-01"

        // Create a workout
        val workoutID =
            ds.createWorkout(
                Workout(0, creatorID, creatorID, future, 60, "cardio", "Future training group session", null),
            )
        val pastWorkoutID =
            ds.createWorkout(
                Workout(0, creatorID, creatorID, past, 45, "weights", "Past session", null),
            )
        assertTrue(workoutID > 0)
        assertTrue(pastWorkoutID > 0)

        val upcomingSessionID =
            ds.addGroupSession(
                GroupSession(0, groupID, workoutID, future),
            )
        val pastSessionID =
            ds.addGroupSession(
                GroupSession(0, groupID, pastWorkoutID, past),
            )
        assertTrue(upcomingSessionID > 0)
        assertTrue(pastSessionID > 0)

        // Only future sessions should show
        val upcoming = ds.getUpcomingGroupSessions(groupID)
        assertTrue(upcoming.any { it.sessionID == upcomingSessionID })
        assertTrue(upcoming.none { it.scheduledDate < LocalDate.now().toString() })

        // Past sessions should only return past
        val pastSessions = ds.getPastGroupSessions(groupID)
        assertTrue(pastSessions.any { it.sessionID == pastSessionID })
        assertTrue(pastSessions.none { it.scheduledDate >= LocalDate.now().toString() })

        // Delete session
        assertTrue(ds.deleteGroupSession(upcomingSessionID))
        assertTrue(ds.getUpcomingGroupSessions(groupID).none { it.sessionID == upcomingSessionID })

        // Leave group
        assertTrue(ds.leaveGroup(groupID, memberID))
        val afterLeave = ds.findGroupMembers(groupID)
        assertEquals(1, afterLeave.size)
        assertTrue(afterLeave.none { it.userID == memberID })

        // Delete group
        assertTrue(ds.deleteGroup(groupID))
        assertNull(ds.findGroup(groupID))

        // Cleanup
        assertTrue(ds.deleteUser(creatorEmail))
        assertTrue(ds.deleteUser(memberEmail))
    }
}
