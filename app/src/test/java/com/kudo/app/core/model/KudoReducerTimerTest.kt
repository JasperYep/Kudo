package com.kudo.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KudoReducerTimerTest {

    @Test
    fun toggleTimer_startingTaskPausesPreviouslyRunningTask() {
        val initial = KudoState(
            tasks = listOf(
                KudoTask(
                    id = 1L,
                    title = "A",
                    valAmount = 0,
                    type = KudoState.TYPE_TASK,
                    isTimerRunning = true,
                    accumulatedTimeMillis = 30_000L,
                    lastTimerStart = 1_000L
                ),
                KudoTask(
                    id = 2L,
                    title = "B",
                    valAmount = 0,
                    type = KudoState.TYPE_TASK
                )
            )
        )

        val updated = KudoReducer.toggleTimer(initial, id = 2L, now = 61_000L)
        val taskA = updated.tasks.first { it.id == 1L }
        val taskB = updated.tasks.first { it.id == 2L }

        assertFalse(taskA.isTimerRunning)
        assertEquals(90_000L, taskA.accumulatedTimeMillis)
        assertEquals(0L, taskA.lastTimerStart)
        assertTrue(taskB.isTimerRunning)
        assertEquals(61_000L, taskB.lastTimerStart)
        assertEquals(1, updated.tasks.count { it.type == KudoState.TYPE_TASK && it.isTimerRunning })
    }

    @Test
    fun toggleTimer_pausingRunningTaskLeavesNoRunningTimers() {
        val initial = KudoState(
            tasks = listOf(
                KudoTask(
                    id = 1L,
                    title = "A",
                    valAmount = 0,
                    type = KudoState.TYPE_TASK,
                    isTimerRunning = true,
                    lastTimerStart = 1_000L
                )
            )
        )

        val updated = KudoReducer.toggleTimer(initial, id = 1L, now = 31_000L)
        val task = updated.tasks.single()

        assertFalse(task.isTimerRunning)
        assertEquals(30_000L, task.accumulatedTimeMillis)
        assertEquals(0, updated.tasks.count { it.type == KudoState.TYPE_TASK && it.isTimerRunning })
    }
}
