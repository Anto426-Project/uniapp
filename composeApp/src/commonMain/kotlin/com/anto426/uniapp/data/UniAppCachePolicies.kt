package com.anto426.uniapp.data

/** One freshness policy per dataset, shared by memory and encrypted disk storage. */
data class UniAppCachePolicy(val maxAgeMillis: Long, val retryDelayMillis: Long = 60_000L) {
    init {
        require(maxAgeMillis >= 0L)
        require(retryDelayMillis > 0L)
    }
}

object UniAppCachePolicies {
    val Transport = UniAppCachePolicy(minutes(2))
    val ConnectedDevices = UniAppCachePolicy(minutes(5))
    val ExamRounds = UniAppCachePolicy(minutes(15))
    val StudyPlan = UniAppCachePolicy(hours(6))
    val SurveyCourses = UniAppCachePolicy(minutes(30))
    val Attendance = UniAppCachePolicy(minutes(30))
    val SurveyPage = UniAppCachePolicy(minutes(30))
    val SurveyStatus = UniAppCachePolicy(minutes(30))
    val News = UniAppCachePolicy(hours(1))
    val Taxes = UniAppCachePolicy(hours(2))
    val Career = UniAppCachePolicy(hours(6))
    val ProfessorDashboard = UniAppCachePolicy(minutes(15))
    val StudentDetails = UniAppCachePolicy(hours(6))
    val ProfileImage = UniAppCachePolicy(hours(24 * 7))
    val Contacts = UniAppCachePolicy(hours(24))
    val CourseSyllabus = UniAppCachePolicy(hours(24 * 7))

    private fun minutes(value: Long): Long = value * 60_000L
    private fun hours(value: Long): Long = value * 60L * 60_000L
}

