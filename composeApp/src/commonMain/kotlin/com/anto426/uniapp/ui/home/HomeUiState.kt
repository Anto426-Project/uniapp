package com.anto426.uniapp.ui.home

data class StudentProfileSummary(
    val fullName: String,
    val firstName: String,
    val studentNumber: String,
    val degreeCourse: String,
    val academicYear: String,
    val status: String = "In Corso"
)

data class CareerOverviewSummary(
    val weightedAverage: String,
    val degreeBase110: String,
    val cfuAcquired: Int,
    val cfuTarget: Int,
    val progressPercentage: Float = if (cfuTarget > 0) (cfuAcquired.toFloat() / cfuTarget.toFloat()).coerceIn(0f, 1f) else 0f
)

data class UpcomingDeadlineSummary(
    val title: String,
    val amountFormatted: String,
    val deadlineFormatted: String,
    val isUrgent: Boolean = false,
    val isOverdue: Boolean = false
)

data class AcademicStatsSummary(
    val openExamRoundsCount: Int = 0,
    val unpaidTaxesCount: Int = 0,
    val activeCoursesCount: Int = 0,
    val unreadNoticesCount: Int = 0
)

data class QuickActionItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val iconKey: String,
    val routeKey: String
)

data class HomeDashboardData(
    val profile: StudentProfileSummary,
    val career: CareerOverviewSummary,
    val nextDeadline: UpcomingDeadlineSummary?,
    val stats: AcademicStatsSummary,
    val quickActions: List<QuickActionItem>,
    val isOffline: Boolean = false,
    val lastSyncFormatted: String? = null
)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeDashboardData) : HomeUiState
    data class Error(val message: String, val canRetry: Boolean = true) : HomeUiState
}
