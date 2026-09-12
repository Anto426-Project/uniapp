package com.anto426.uniapp.account

import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.project.model.GitHubAuthor
import com.anto426.uniapp.ui.components.account.creatorDisplayIdentity
import com.anto426.unisdk.platform.ProjectInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CreatorDisplayIdentityTest {
    private val account = UniAccountSummary(
        accountId = "local-account", serverUserId = "a.manocchio10", displayName = "Nome universitario",
        degreeName = "Informatica", matricola = "12345", email = "student@example.invalid",
        photoUrl = "https://university.example.invalid/photo", isGuest = false,
    )
    private val author = GitHubAuthor(
        login = ProjectInfo.authorLogin, name = "Nome Creatore", url = "https://github.com/${ProjectInfo.authorLogin}",
        avatarUrl = "https://avatars.githubusercontent.com/u/123",
    )

    @Test
    fun creatorAccountGetsPublicIdentityWithoutChangingUniversityProfile() {
        val before = account.copy()
        val identity = assertNotNull(creatorDisplayIdentity(account, author))
        assertEquals(author.name, identity.name)
        assertEquals(author.avatarUrl, identity.avatarUrl)
        assertEquals("NC", identity.initials)
        assertEquals(before, account)
    }

    @Test
    fun matchingUsesCompleteAuthenticatedIdentifier() {
        assertNotNull(creatorDisplayIdentity(account.copy(serverUserId = " A.MANOCCHIO10 "), author))
        assertNull(creatorDisplayIdentity(account.copy(serverUserId = "a.manocchio100"), author))
        assertNull(creatorDisplayIdentity(account.copy(serverUserId = "other", displayName = "a.manocchio10"), author))
        assertNull(creatorDisplayIdentity(account.copy(serverUserId = "other", email = "a.manocchio10@example.invalid"), author))
    }

    @Test
    fun logoutGuestsAndOtherAccountsNeverReceiveOverride() {
        assertNull(creatorDisplayIdentity(null, author))
        assertNull(creatorDisplayIdentity(account.copy(isGuest = true), author))
        assertNull(creatorDisplayIdentity(account.copy(serverUserId = "other"), author))
    }

    @Test
    fun missingOrUnrelatedGithubProfileKeepsUniversityIdentity() {
        assertNull(creatorDisplayIdentity(account, null))
        assertNull(creatorDisplayIdentity(account, author.copy(login = "someone-else")))
    }

    @Test
    fun publicLoginIsFallbackAndUpdatedMetadataIsUsed() {
        assertEquals(author.login, creatorDisplayIdentity(account, author.copy(name = "  "))?.name)
        val changed = assertNotNull(creatorDisplayIdentity(account, author.copy(name = " Nuovo Nome ", avatarUrl = "")))
        assertEquals("Nuovo Nome", changed.name)
        assertNull(changed.avatarUrl)
    }
}
