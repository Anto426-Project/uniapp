package com.anto426.uniapp.testing

import com.anto426.uniapp.localization.appStringReadDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
actual abstract class ResourceTest actual constructor() {
    @Before
    fun installInlineStringReadDispatcher() {
        appStringReadDispatcher = Dispatchers.Unconfined
    }

    @After
    fun restoreDefaultStringReadDispatcher() {
        appStringReadDispatcher = Dispatchers.Default
    }
}
