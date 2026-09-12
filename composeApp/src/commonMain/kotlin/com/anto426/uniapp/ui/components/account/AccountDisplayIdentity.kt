package com.anto426.uniapp.ui.components.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.images.ApplicationImage
import com.anto426.uniapp.project.model.GitHubAuthor
import com.anto426.unisdk.platform.ProjectInfo

/** Presentation only: never written to account storage or passed to university services. */
internal data class CreatorDisplayIdentity(val name: String, val avatarUrl: String?) {
    val initials: String
        get() = name.split(' ').filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }
}

internal fun isCreatorDisplayAccount(account: UniAccountSummary?): Boolean =
    account != null && !account.isGuest && account.serverUserId.trim().equals("a.manocchio10", ignoreCase = true)

internal fun creatorDisplayIdentity(account: UniAccountSummary?, author: GitHubAuthor?): CreatorDisplayIdentity? {
    if (!isCreatorDisplayAccount(account)) return null
    val creator = author?.takeIf { it.login.equals(ProjectInfo.authorLogin, ignoreCase = true) } ?: return null
    return CreatorDisplayIdentity(
        name = creator.name?.trim()?.takeIf(String::isNotBlank) ?: creator.login,
        avatarUrl = creator.avatarUrl?.takeIf(String::isNotBlank),
    )
}

internal data class AccountPresentation(
    val activeAccount: UniAccountSummary? = null,
    val author: GitHubAuthor? = null,
    val image: ApplicationImage? = null,
)

internal val LocalAccountPresentation = staticCompositionLocalOf { AccountPresentation() }

internal data class AccountDisplayIdentity(val name: String, val initials: String, val photo: ByteArray?)

@Composable
internal fun accountDisplayIdentity(
    name: String,
    initials: String,
    photo: ByteArray?,
    account: UniAccountSummary? = LocalAccountPresentation.current.activeAccount,
): AccountDisplayIdentity {
    val presentation = LocalAccountPresentation.current
    val creator = creatorDisplayIdentity(account, presentation.author)
        ?: return AccountDisplayIdentity(name, initials, photo)
    return AccountDisplayIdentity(creator.name, creator.initials, presentation.image?.bytes ?: photo)
}
