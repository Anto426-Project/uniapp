package com.anto426.uniapp.demo

import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.session.UniUserProfile
import com.anto426.uniapp.project.model.GitHubAuthor
import com.anto426.unisdk.platform.ProjectInfo

/** Public presentation credentials. They unlock synthetic local data only. */
internal object DemoAccount {
    const val USERNAME = "demo@uniapp.local"
    const val PASSWORD = "UniAppDemo2026!"
    val avatarFallback: String get() = "https://avatars.githubusercontent.com/${ProjectInfo.authorLogin}"
    private const val SERVER_ID = "uniapp-presentation-demo"
    fun requested(credentials: UniAccountCredentials) = credentials.username.trim().equals(USERNAME, ignoreCase = true)
    fun accepts(credentials: UniAccountCredentials) = requested(credentials) && credentials.password == PASSWORD
    fun isDemo(account: UniAccountSummary?) = account?.serverUserId == SERVER_ID
    fun developerName(author: GitHubAuthor?): String =
        author?.takeIf { it.login.equals(ProjectInfo.authorLogin, ignoreCase = true) }
            ?.let { it.name?.trim()?.takeIf(String::isNotBlank) ?: it.login } ?: ProjectInfo.authorLogin

    suspend fun profile(author: GitHubAuthor? = null): UniUserProfile {
        val student = DemoCatalog.load().student
        val developer = author?.takeIf { it.login.equals(ProjectInfo.authorLogin, ignoreCase = true) }
        val name = developerName(developer)
        return UniUserProfile(id = SERVER_ID, displayName = name, degreeName = student.degreeName.orEmpty(),
            matricola = student.matricola, email = student.email, photoUrl = developer?.avatarUrl?.takeIf(String::isNotBlank) ?: avatarFallback, isGuest = false,
            activeProfileId = "demo-student", profiles = listOf(UniCareerProfile(
                profileId = "demo-student", displayName = name, degreeName = student.degreeName.orEmpty(),
                matricola = student.matricola, departmentName = student.departmentName,
            )))
    }
}
