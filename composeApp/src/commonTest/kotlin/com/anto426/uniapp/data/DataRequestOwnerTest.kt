package com.anto426.uniapp.data

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DataRequestOwnerTest {
    @Test
    fun fixedSourceCannotReadAnotherAccountOrCareer() {
        requireDataRequestOwner("account-a", "student", "account-a", "student")
        assertFailsWith<CancellationException> {
            requireDataRequestOwner("account-b", "student", "account-a", "student")
        }
        assertFailsWith<CancellationException> {
            requireDataRequestOwner("account-a", "professor", "account-a", "student")
        }
    }

    @Test
    fun missingFixedProfileDoesNotFollowTheActiveCareer() {
        requireDataRequestOwner("account-a", null, "account-a", null)
        assertFailsWith<CancellationException> {
            requireDataRequestOwner("account-a", "professor", "account-a", null)
        }
    }

    @Test
    fun unboundShellSourceMayResolveTheCurrentOwner() {
        requireDataRequestOwner("account-a", "student", null, null)
        requireDataRequestOwner("account-b", "professor", null, null)
    }
}
