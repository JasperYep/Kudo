package com.kudo.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KudoReducerSubtaskTest {

    @Test
    fun updateTask_splitsRewardEqually() {
        val state = KudoState(
            tasks = listOf(
                KudoTask(
                    id = 1L,
                    title = "Launch",
                    coins = 12,
                    kind = KudoTaskKind.Task
                )
            )
        )

        val updated = KudoReducer.updateTask(
            state = state,
            id = 1L,
            title = "Launch",
            coins = 12,
            dueAtEpochMillis = null,
            subtaskDrafts = listOf(
                KudoSubtaskDraft("Plan"),
                KudoSubtaskDraft("Build"),
                KudoSubtaskDraft("Ship")
            ),
            now = 100L
        )

        assertEquals(listOf(4, 4, 4), updated.tasks.first().subtasks.map(KudoSubtask::coins))
    }

    @Test
    fun updateTask_splitsRewardWithRemainder() {
        val state = KudoState(
            tasks = listOf(
                KudoTask(
                    id = 1L,
                    title = "Launch",
                    coins = 10,
                    kind = KudoTaskKind.Task
                )
            )
        )

        val updated = KudoReducer.updateTask(
            state = state,
            id = 1L,
            title = "Launch",
            coins = 10,
            dueAtEpochMillis = null,
            subtaskDrafts = listOf(
                KudoSubtaskDraft("Plan"),
                KudoSubtaskDraft("Build"),
                KudoSubtaskDraft("Ship")
            ),
            now = 100L
        )

        // 10 / 3 = 3 remainder 1 → [4, 3, 3]
        assertEquals(listOf(4, 3, 3), updated.tasks.first().subtasks.map(KudoSubtask::coins))
    }

    @Test
    fun completeSubtask_awardsPartialCoinsAndKeepsTask() {
        val task = seededTaskState().tasks.first()

        val completed = KudoReducer.completeSubtask(
            state = seededTaskState(),
            taskId = task.id,
            subtaskId = task.subtasks.first().id,
            now = 200L
        )

        val updatedTask = completed.tasks.first()
        assertEquals(4, completed.coins)
        assertEquals(1, updatedTask.completedSubtaskCount)
        assertEquals(8, updatedTask.remainingCoins)
        assertTrue(updatedTask.subtasks.first().isCompleted)
    }

    @Test
    fun completeTask_withStartedSubtasksOnlyPaysRemainder() {
        val initial = seededTaskState()
        val firstPass = KudoReducer.completeSubtask(
            state = initial,
            taskId = 1L,
            subtaskId = initial.tasks.first().subtasks.first().id,
            now = 200L
        )

        val finished = KudoReducer.completeTask(firstPass, 1L, now = 300L)

        assertEquals(12, finished.coins)
        assertTrue(finished.tasks.isEmpty())
        assertEquals(8, finished.logs.first().baseCoins)
    }

    @Test
    fun undoLog_restoresTaskSnapshotForSubtaskCompletion() {
        val initial = seededTaskState()
        val progressed = KudoReducer.completeSubtask(
            state = initial,
            taskId = 1L,
            subtaskId = initial.tasks.first().subtasks.first().id,
            now = 200L
        )

        val undone = KudoReducer.undoLog(progressed, 0)

        assertEquals(0, undone.coins)
        assertEquals(1, undone.tasks.size)
        assertFalse(undone.tasks.first().subtasks.first().isCompleted)
        assertTrue(undone.logs.isEmpty())
    }

    @Test
    fun updateTask_afterProgressKeepsLockedRewardStructure() {
        val initial = seededTaskState()
        val progressed = KudoReducer.completeSubtask(
            state = initial,
            taskId = 1L,
            subtaskId = initial.tasks.first().subtasks.first().id,
            now = 200L
        )

        val updated = KudoReducer.updateTask(
            state = progressed,
            id = 1L,
            title = "Launch v2",
            coins = 99,
            dueAtEpochMillis = 5_000L,
            subtaskDrafts = listOf(
                KudoSubtaskDraft("Different")
            ),
            now = 300L
        )

        val task = updated.tasks.first()
        assertEquals("Launch v2", task.title)
        assertEquals(12, task.coins)
        assertEquals(5_000L, task.dueAtEpochMillis)
        assertEquals(listOf("Plan", "Build", "Ship"), task.subtasks.map(KudoSubtask::title))
        assertEquals(listOf(4, 4, 4), task.subtasks.map(KudoSubtask::coins))
        assertTrue(task.subtasks.first().isCompleted)
    }

    private fun seededTaskState(): KudoState {
        val updated = KudoReducer.updateTask(
            state = KudoState(
                tasks = listOf(
                    KudoTask(
                        id = 1L,
                        title = "Launch",
                        coins = 12,
                        kind = KudoTaskKind.Task
                    )
                )
            ),
            id = 1L,
            title = "Launch",
            coins = 12,
            dueAtEpochMillis = null,
            subtaskDrafts = listOf(
                KudoSubtaskDraft("Plan"),
                KudoSubtaskDraft("Build"),
                KudoSubtaskDraft("Ship")
            ),
            now = 100L
        )
        assertEquals(listOf(4, 4, 4), updated.tasks.first().subtasks.map(KudoSubtask::coins))
        return updated
    }
}
