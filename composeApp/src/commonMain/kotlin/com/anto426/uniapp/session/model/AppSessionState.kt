package com.anto426.uniapp.session.model

import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.unisdk.backend.model.LoginCareerOption

sealed interface AppSessionState {
    data object Initializing : AppSessionState

    data class SignedOut(val message: String? = null) : AppSessionState

    data object Authenticating : AppSessionState

    data class CareerSelectionRequired(val careers: List<LoginCareerOption>) : AppSessionState

    data class Authenticated(val account: UniAccountSummary) : AppSessionState

    data class ReauthenticationRequired(
        val account: UniAccountSummary,
        val message: String? = null,
    ) : AppSessionState
}
