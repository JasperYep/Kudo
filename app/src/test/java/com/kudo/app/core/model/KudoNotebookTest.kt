package com.kudo.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class KudoNotebookTest {

    @Test
    fun addNote_insertsNewNoteAtFront() {
        val initial = KudoState()

        val (updated, id) = KudoReducer.addNote(initial, now = 100L)

        assertEquals(1, updated.notes.size)
        assertEquals(id, updated.notes.first().id)
    }

    @Test
    fun updateNoteTitle_changesTitleAndMovesNoteToFront() {
        val initial = KudoState(
            notes = listOf(
                KudoNote(id = 1L, title = "Old", content = "", updatedAt = 1L),
                KudoNote(id = 2L, title = "Second", content = "", updatedAt = 2L)
            )
        )

        val updated = KudoReducer.updateNoteTitle(initial, id = 1L, title = "New", now = 10L)

        assertEquals(listOf(1L, 2L), updated.notes.map(KudoNote::id))
        assertEquals("New", updated.notes.first().title)
        assertEquals(10L, updated.notes.first().updatedAt)
    }

    @Test
    fun updateNoteContent_returnsSameStateWhenTextUnchanged() {
        val initial = KudoState(
            notes = listOf(
                KudoNote(id = 1L, title = "", content = "same", updatedAt = 1L)
            )
        )

        val updated = KudoReducer.updateNoteContent(initial, id = 1L, content = "same")

        assertSame(initial, updated)
    }

    @Test
    fun deleteNote_removesMatchingNote() {
        val initial = KudoState(
            notes = listOf(
                KudoNote(id = 1L, title = "A", content = "", updatedAt = 1L),
                KudoNote(id = 2L, title = "B", content = "", updatedAt = 2L)
            )
        )

        val updated = KudoReducer.deleteNote(initial, 1L)

        assertEquals(listOf(2L), updated.notes.map(KudoNote::id))
    }
}
