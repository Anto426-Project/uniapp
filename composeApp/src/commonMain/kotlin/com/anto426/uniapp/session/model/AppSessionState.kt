package com.anto426.uniapp.session.model

import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.unisdk.backend.model.LoginCareerOption

sealed interface AppSessionState {
    data object Initializing : AppSessionState

    /** No SDK session has been resumed yet; device authentication must succeed first. */
    data class UnlockRequired(
        val account: UniAccountSummary,
        val fallbackAccount: UniAccountSummary? = null,
    ) : AppSessionState

    data class SignedOut(val message: String? = null) : AppSessionState

    data object Authenticating : AppSessionState

    /** A new SDK session is being verified; private routes must show loading. */
    data object Switching : AppSessionState

    data class CareerSelectionRequired(val careers: List<LoginCareerOption>) : AppSessionState

    data class Authenticated(val account: UniAccountSummary) : AppSessionState

    data class ReauthenticationRequired(
        val account: UniAccountSummary,
        val message: String? = null,
    ) : AppSessionState
}
