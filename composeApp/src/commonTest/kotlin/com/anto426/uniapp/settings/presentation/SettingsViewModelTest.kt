package com.anto426.uniapp.settings.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.security.account.AccountSecurityPreferences
import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @Test
    fun biometricPreferenceChangesOnlyAfterDeviceAuthentication() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataSource = PreferenceDataSource()
            val authenticator = FakeBiometricAuthenticator(BiometricAuthenticationResult.Authenticated)
            val viewModel = SettingsViewModel(dataSource, biometricAuthenticator = authenticator)
            advanceUntilIdle()

            viewModel.setBiometricEnabled(true)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.biometricEnabled)
            assertTrue(
                dataSource.preferences
                    .getValue(AccountSecurityPreferences.BIOMETRIC_UNLOCK)
                    .toBoolean(),
            )
            assertTrue(authenticator.requested)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun cancelledAuthenticationDoesNotEnableBiometricUnlock() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataSource = PreferenceDataSource()
            val viewModel =
                SettingsViewModel(
                    dataSource,
                    biometricAuthenticator =
                        FakeBiometricAuthenticator(BiometricAuthenticationResult.Cancelled),
                )
            advanceUntilIdle()

            viewModel.setBiometricEnabled(true)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.biometricEnabled)
            assertFalse(AccountSecurityPreferences.BIOMETRIC_UNLOCK in dataSource.preferences)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class PreferenceDataSource : FakeUniAppDataSource() {
    val preferences = mutableMapOf<String, String>()

    override suspend fun readPreference(key: String): String? = preferences[key]

    override suspend fun writePreference(key: String, value: String) {
        preferences[key] = value
    }
}

private class FakeBiometricAuthenticator(
    private val result: BiometricAuthenticationResult,
) : BiometricAuthenticator {
    var requested = false

    override fun availability(): BiometricAvailability = BiometricAvailability.Available

    override suspend fun authenticate(reason: String): BiometricAuthenticationResult {
        requested = true
        return result
    }
}
