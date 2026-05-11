package com.kudo.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KudoReducerImportTest {

    @Test
    fun addImportedTasks_appendsNormalTasksOnly() {
        val initial = KudoState(
            coins = 12,
            tasks = listOf(
                KudoTask(
                    id = 1L,
                    title = "Existing",
                    valAmount = 5,
                    type = KudoState.TYPE_TASK
                ),
                KudoTask(
                    id = 2L,
                    title = "Habit",
                    valAmount = 3,
                    type = KudoState.TYPE_HABIT
                )
            ),
            store = listOf(KudoStoreItem(id = 3L, title = "Coffee", cost = 8))
        )

        val updated = KudoReducer.addImportedTasks(
            state = initial,
            drafts = listOf(
                KudoTaskImportDraft("Read survey", 20),
                KudoTaskImportDraft("Write notes", 40)
            ),
            now = 100L
        )

        assertEquals(12, updated.coins)
        assertEquals(initial.store, updated.store)
        assertEquals(listOf("Existing", "Habit", "Read survey", "Write notes"), updated.tasks.map(KudoTask::title))
        assertEquals(listOf(KudoState.TYPE_TASK, KudoState.TYPE_HABIT, KudoState.TYPE_TASK, KudoState.TYPE_TASK), updated.tasks.map(KudoTask::type))
        assertEquals(listOf(5, 3, 20, 40), updated.tasks.map(KudoTask::valAmount))
    }

    @Test
    fun addImportedTasks_assignsIncreasingManualOrder() {
        val initial = KudoState(
            taskSortMode = KudoState.TASK_SORT_MANUAL,
            tasks = listOf(
                KudoTask(
                    id = 1L,
                    title = "Existing",
                    valAmount = 5,
                    type = KudoState.TYPE_TASK,
                    order = 8L
                ),
                KudoTask(
                    id = 2L,
                    title = "Habit",
                    valAmount = 3,
                    type = KudoState.TYPE_HABIT,
                    order = 20L
                )
            )
        )

        val updated = KudoReducer.addImportedTasks(
            state = initial,
            drafts = listOf(
                KudoTaskImportDraft("A", 1),
                KudoTaskImportDraft("B", 2)
            ),
            now = 100L
        )

        assertEquals(listOf(8L, 20L, 9L, 10L), updated.tasks.map(KudoTask::order))
    }
}
