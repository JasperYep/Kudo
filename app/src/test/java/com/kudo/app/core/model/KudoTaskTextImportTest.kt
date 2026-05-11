package com.kudo.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KudoTaskTextImportTest {

    @Test
    fun parse_importsMarkdownBulletsWithTrailingValues() {
        val parsed = KudoTaskTextImport.parse(
            """
            - 看一下两篇综述 20
            - 整理一下调研方法和测试对象 40
            """.trimIndent()
        )

        assertEquals(
            listOf(
                KudoTaskImportDraft("看一下两篇综述", 20),
                KudoTaskImportDraft("整理一下调研方法和测试对象", 40)
            ),
            parsed
        )
    }

    @Test
    fun parse_acceptsNumberedAndCheckboxLists() {
        val parsed = KudoTaskTextImport.parse(
            """
            1. Read survey 10
            2) [ ] Write notes 30
            * [x] Send summary 5
            """.trimIndent()
        )

        assertEquals(
            listOf(
                KudoTaskImportDraft("Read survey", 10),
                KudoTaskImportDraft("Write notes", 30),
                KudoTaskImportDraft("Send summary", 5)
            ),
            parsed
        )
    }

    @Test
    fun parse_titleOnlyLinesDefaultToZero() {
        val parsed = KudoTaskTextImport.parse(
            """
            Draft method section

            + Prepare benchmark table
            """.trimIndent()
        )

        assertEquals(
            listOf(
                KudoTaskImportDraft("Draft method section", 0),
                KudoTaskImportDraft("Prepare benchmark table", 0)
            ),
            parsed
        )
    }

    @Test
    fun parse_skipsBlankAndNumberOnlyLines() {
        val parsed = KudoTaskTextImport.parse(
            """

            - 20
            40
            Real task 7
            """.trimIndent()
        )

        assertEquals(listOf(KudoTaskImportDraft("Real task", 7)), parsed)
    }
}
