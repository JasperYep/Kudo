package com.kudo.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KudoReducerImportTest {

    @Test
    fun addImportedTasks_appendsToTasksOnly() {
        val initial = KudoState(
            coins = 12,
            tasks = listOf(
                KudoTask(id = 1L, title = "Existing", coins = 5)
            ),
            habits = listOf(
                KudoHabit(id = 2L, title = "Habit", coins = 3)
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
        assertEquals(initial.habits, updated.habits)
        assertEquals(listOf("Existing", "Read survey", "Write notes"), updated.tasks.map(KudoTask::title))
        assertEquals(listOf(5, 20, 40), updated.tasks.map(KudoTask::coins))
    }

    @Test
    fun addImportedTasks_assignsIncreasingManualOrder() {
        val initial = KudoState(
            taskSortMode = KudoTaskSortMode.Manual,
            tasks = listOf(
                KudoTask(id = 1L, title = "Existing", coins = 5, order = 8L)
            ),
            habits = listOf(
                KudoHabit(id = 2L, title = "Habit", coins = 3, order = 20L)
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

        assertEquals(listOf(8L, 9L, 10L), updated.tasks.map(KudoTask::order))
    }
}
