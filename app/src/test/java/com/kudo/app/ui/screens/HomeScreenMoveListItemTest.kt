package com.kudo.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeScreenMoveListItemTest {

    @Test
    fun moveListItem_movingDownUsesTargetIndexFromReorderState() {
        val result = moveListItem(
            list = listOf(1, 2, 3, 4),
            fromIndex = 0,
            toIndex = 2
        )

        assertEquals(listOf(2, 3, 1, 4), result)
    }

    @Test
    fun moveListItem_movingUpUsesDropTargetIndex() {
        val result = moveListItem(
            list = listOf(1, 2, 3, 4),
            fromIndex = 3,
            toIndex = 1
        )

        assertEquals(listOf(1, 4, 2, 3), result)
    }

    @Test
    fun moveListItem_invalidIndexesReturnOriginalList() {
        val input = listOf(1, 2, 3)

        assertEquals(input, moveListItem(input, fromIndex = -1, toIndex = 1))
        assertEquals(input, moveListItem(input, fromIndex = 1, toIndex = 3))
        assertEquals(input, moveListItem(input, fromIndex = 1, toIndex = 1))
    }
}
