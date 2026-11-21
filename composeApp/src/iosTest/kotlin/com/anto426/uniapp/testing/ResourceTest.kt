package com.anto426.uniapp.testing

import com.anto426.uniapp.localization.appStringReadDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

actual abstract class ResourceTest actual constructor() {
    @BeforeTest
    fun installInlineStringReadDispatcher() {
        appStringReadDispatcher = Dispatchers.Unconfined
    }

    @AfterTest
    fun restoreDefaultStringReadDispatcher() {
        appStringReadDispatcher = Dispatchers.Default
    }
}
