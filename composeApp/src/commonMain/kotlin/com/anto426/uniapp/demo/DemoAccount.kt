package com.anto426.uniapp.demo

import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.session.UniUserProfile

/** Public presentation credentials. They unlock synthetic local data only. */
internal object DemoAccount {
    const val USERNAME = "demo@uniapp.local"
    const val PASSWORD = "UniAppDemo2026!"
    private const val SERVER_ID = "uniapp-presentation-demo"
    fun requested(credentials: UniAccountCredentials) = credentials.username.trim().equals(USERNAME, ignoreCase = true)
    fun accepts(credentials: UniAccountCredentials) = requested(credentials) && credentials.password == PASSWORD
    fun isDemo(account: UniAccountSummary?) = account?.serverUserId == SERVER_ID
    suspend fun profile(): UniUserProfile {
        val student = DemoCatalog.load().student
        return UniUserProfile(id = SERVER_ID, displayName = student.fullName, degreeName = student.degreeName.orEmpty(),
            matricola = student.matricola, email = student.email, photoUrl = null, isGuest = false,
            activeProfileId = "demo-student", profiles = listOf(UniCareerProfile(
                profileId = "demo-student", displayName = student.fullName, degreeName = student.degreeName.orEmpty(),
                matricola = student.matricola, departmentName = student.departmentName,
            )))
    }
}
