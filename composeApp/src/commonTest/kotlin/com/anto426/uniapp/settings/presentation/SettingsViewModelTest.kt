package com.anto426.uniapp.settings.presentation

import com.anto426.uniapp.data.local.FakeUniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataKey
import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAvailability
import com.anto426.uniapp.security.password.AppPasswordVerifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest : com.anto426.uniapp.testing.ResourceTest() {
    @Test
    fun biometricPreferenceChangesOnlyAfterDeviceAuthentication() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataStore = FakeUniLocalDataStore()
            val authenticator = FakeBiometricAuthenticator(BiometricAuthenticationResult.Authenticated)
            val viewModel =
                SettingsViewModel(
                    localDataStore = dataStore,
                    accountId = ACCOUNT_ID,
                    biometricAuthenticator = authenticator,
                    passwordVerifierFactory = { TEST_VERIFIER },
                )
            advanceUntilIdle()

            viewModel.setBiometricEnabled(true)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isPasswordSetupVisible)
            assertFalse(viewModel.uiState.value.biometricEnabled)
            assertFalse(authenticator.requested)
            viewModel.submitBiometricPassword("test-password", "test-password")
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.biometricEnabled)
            assertTrue(
                dataStore.read(LocalDataScope.Account(ACCOUNT_ID), UniAppDataKeys.BiometricUnlock),
            )
            assertTrue(authenticator.requested)
            assertEquals(TEST_VERIFIER, dataStore.read(LocalDataScope.Account(ACCOUNT_ID), UniAppDataKeys.PasswordVerifier))
            assertFalse(viewModel.uiState.value.isPasswordSetupVisible)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun cancelledAuthenticationDoesNotEnableBiometricUnlock() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataStore = FakeUniLocalDataStore()
            val viewModel =
                SettingsViewModel(
                    localDataStore = dataStore,
                    accountId = ACCOUNT_ID,
                    biometricAuthenticator =
                        FakeBiometricAuthenticator(BiometricAuthenticationResult.Cancelled),
                )
            advanceUntilIdle()

            viewModel.setBiometricEnabled(true)
            viewModel.submitBiometricPassword("test-password", "test-password")
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.biometricEnabled)
            assertFalse(
                dataStore.contains(LocalDataScope.Account(ACCOUNT_ID), UniAppDataKeys.BiometricUnlock),
            )
            assertFalse(dataStore.contains(LocalDataScope.Account(ACCOUNT_ID), UniAppDataKeys.PasswordVerifier))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun invalidPasswordsAndDismissalNeverEnableBiometrics() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataStore = FakeUniLocalDataStore()
            val authenticator = FakeBiometricAuthenticator(BiometricAuthenticationResult.Authenticated)
            val viewModel = SettingsViewModel(dataStore, ACCOUNT_ID, biometricAuthenticator = authenticator)
            advanceUntilIdle()
            viewModel.setBiometricEnabled(true)
            viewModel.submitBiometricPassword("short", "short")
            assertEquals(AppPasswordSetupError.TooShort, viewModel.uiState.value.passwordSetupError)
            viewModel.submitBiometricPassword("test-password", "different-password")
            assertEquals(AppPasswordSetupError.Mismatch, viewModel.uiState.value.passwordSetupError)
            viewModel.submitBiometricPassword("a".repeat(129), "a".repeat(129))
            assertEquals(AppPasswordSetupError.TooLong, viewModel.uiState.value.passwordSetupError)
            viewModel.dismissBiometricPasswordSetup()
            viewModel.submitBiometricPassword("test-password", "test-password")
            advanceUntilIdle()
            assertFalse(authenticator.requested)
            assertFalse(viewModel.uiState.value.isPasswordSetupVisible)
            assertFalse(dataStore.contains(LocalDataScope.Account(ACCOUNT_ID), UniAppDataKeys.BiometricUnlock))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun verifierFailureLeavesBiometricsDisabled() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataStore = FakeUniLocalDataStore()
            val viewModel = SettingsViewModel(
                dataStore, ACCOUNT_ID,
                biometricAuthenticator = FakeBiometricAuthenticator(BiometricAuthenticationResult.Authenticated),
                passwordVerifierFactory = { error("Verifier unavailable") },
            )
            advanceUntilIdle()
            viewModel.setBiometricEnabled(true)
            viewModel.submitBiometricPassword("test-password", "test-password")
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.biometricEnabled)
            viewModel.uiState.first { !it.isBiometricAuthenticating }
            assertFalse(viewModel.uiState.value.isBiometricAuthenticating)
            assertFalse(dataStore.contains(LocalDataScope.Account(ACCOUNT_ID), UniAppDataKeys.BiometricUnlock))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedPasswordSaveCannotEnableBiometrics() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val backingStore = FakeUniLocalDataStore()
            val failingStore = object : UniLocalDataStore by backingStore {
                override suspend fun <T> write(scope: LocalDataScope, key: LocalDataKey<T>, value: T) {
                    if (key.name == UniAppDataKeys.PasswordVerifier.name) error("Storage unavailable")
                    backingStore.write(scope, key, value)
                }
            }
            val viewModel = SettingsViewModel(
                failingStore, ACCOUNT_ID,
                biometricAuthenticator = FakeBiometricAuthenticator(BiometricAuthenticationResult.Authenticated),
                passwordVerifierFactory = { TEST_VERIFIER },
            )
            advanceUntilIdle()
            viewModel.setBiometricEnabled(true)
            viewModel.submitBiometricPassword("test-password", "test-password")
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.biometricEnabled)
            assertFalse(backingStore.contains(LocalDataScope.Account(ACCOUNT_ID), UniAppDataKeys.BiometricUnlock))
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private const val ACCOUNT_ID = "account-test"
private val TEST_VERIFIER = AppPasswordVerifier("00".repeat(16), "00".repeat(32), 600_000)

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
