package com.kudo.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KudoStateJsonTest {

    @Test
    fun decode_legacyHabitData_discardsActiveHabitAndKeepsReadOnlyHistory() {
        val state = KudoStateJson.decode(
            """
            {
              "coins": 120,
              "tasks": [
                { "id": 1, "title": "Task", "val": 20, "type": 0, "order": 3 },
                { "id": 2, "title": "Legacy habit", "val": 10, "type": 1, "count": 6, "last": 1000, "order": 4 }
              ],
              "store": [{ "id": 3, "title": "Coffee", "cost": 30, "type": 0 }],
              "logs": [
                {
                  "t": 1000,
                  "txt": "Legacy habit",
                  "v": 10,
                  "base": 10,
                  "type": "task",
                  "taskId": 2,
                  "isHabit": true,
                  "itemData": { "id": 2, "title": "Legacy habit", "val": 10, "type": 1, "count": 6, "last": 1000 }
                },
                {
                  "t": 900,
                  "txt": "Completed task",
                  "v": 20,
                  "base": 20,
                  "type": "task",
                  "taskId": 4,
                  "itemData": { "id": 4, "title": "Completed task", "val": 20, "type": 0, "order": 1 }
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(KudoState.SCHEMA_VERSION, state.schemaVersion)
        assertEquals(listOf("Task"), state.tasks.map(KudoTask::title))
        assertEquals(120, state.coins)
        assertEquals("Coffee", state.store.single().title)

        val legacyLog = state.logs.first()
        assertFalse(legacyLog.undoable)
        assertNull(legacyLog.taskId)
        assertNull(legacyLog.itemData)

        val taskLog = state.logs[1]
        assertTrue(taskLog.undoable)
        assertEquals(4L, taskLog.itemData?.id)
        assertEquals(1L, taskLog.itemData?.order)

        val exported = KudoStateJson.encode(state)
        assertFalse(exported.contains("isHabit"))
        assertFalse(exported.contains("Legacy habit\",\"val\""))
        assertFalse(exported.contains("\"count\""))
        assertFalse(exported.contains("\"last\""))
    }

    @Test
    fun undoLog_leavesMigratedLegacyHabitHistoryUntouched() {
        val legacyLog = KudoLogEntry(
            timestamp = 1000L,
            text = "Legacy habit",
            value = 10,
            type = "task",
            undoable = false
        )
        val state = KudoState(coins = 120, logs = listOf(legacyLog))

        assertEquals(state, KudoReducer.undoLog(state, 0))
    }

    @Test
    fun undoLog_restoresNormalTaskAndStoreEntries() {
        val taskLog = KudoLogEntry(
            timestamp = 1000L,
            text = "Task",
            value = 20,
            type = "task",
            taskId = 1L,
            itemData = KudoLogItemData(id = 1L, title = "Task", valAmount = 20)
        )
        val storeLog = KudoLogEntry(
            timestamp = 900L,
            text = "Coffee",
            value = -30,
            type = "store",
            itemData = KudoLogItemData(
                id = 2L,
                title = "Coffee",
                cost = 30,
                storeType = KudoState.STORE_ONCE
            )
        )
        val initial = KudoState(coins = 110, logs = listOf(taskLog, storeLog))

        val taskUndone = KudoReducer.undoLog(initial, 0)
        assertEquals(90, taskUndone.coins)
        assertEquals(listOf("Task"), taskUndone.tasks.map(KudoTask::title))

        val storeUndone = KudoReducer.undoLog(initial, 1)
        assertEquals(140, storeUndone.coins)
        assertEquals(listOf("Coffee"), storeUndone.store.map(KudoStoreItem::title))
    }
}
