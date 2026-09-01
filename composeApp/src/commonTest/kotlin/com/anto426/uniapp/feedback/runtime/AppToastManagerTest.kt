package com.anto426.uniapp.feedback.runtime

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppToastManagerTest {
    @Test
    fun messagesAreBufferedAndDeliveredInOrder() = runTest {
        val manager = AppToastManager()
        manager.success("Salvato")
        manager.error("Errore")

        val messages = manager.messages.take(2).toList()

        assertEquals(listOf("Salvato", "Errore"), messages.map { it.text })
        assertEquals(listOf(AppToastKind.Success, AppToastKind.Error), messages.map { it.kind })
    }
}
