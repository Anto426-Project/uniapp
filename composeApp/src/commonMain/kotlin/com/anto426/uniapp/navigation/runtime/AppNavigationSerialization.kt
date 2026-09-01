package com.anto426.uniapp.navigation.runtime

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.anto426.uniapp.navigation.model.AppRoute
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Explicit route registration required to restore a Navigation 3 stack on iOS. */
internal val AppNavigationSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.Bootstrap::class, AppRoute.Bootstrap.serializer())
                    subclass(AppRoute.Login::class, AppRoute.Login.serializer())
                    subclass(AppRoute.Home::class, AppRoute.Home.serializer())
                    subclass(AppRoute.Services::class, AppRoute.Services.serializer())
                    subclass(AppRoute.Didactics::class, AppRoute.Didactics.serializer())
                    subclass(AppRoute.Teachings::class, AppRoute.Teachings.serializer())
                    subclass(AppRoute.Theses::class, AppRoute.Theses.serializer())
                    subclass(AppRoute.Reports::class, AppRoute.Reports.serializer())
                    subclass(AppRoute.TeachingDetail::class, AppRoute.TeachingDetail.serializer())
                    subclass(AppRoute.ProfessorExamDetail::class, AppRoute.ProfessorExamDetail.serializer())
                    subclass(AppRoute.ThesisDetail::class, AppRoute.ThesisDetail.serializer())
                    subclass(AppRoute.ReportDetail::class, AppRoute.ReportDetail.serializer())
                    subclass(AppRoute.Settings::class, AppRoute.Settings.serializer())
                    subclass(AppRoute.Accounts::class, AppRoute.Accounts.serializer())
                    subclass(AppRoute.Info::class, AppRoute.Info.serializer())
                    subclass(AppRoute.Theme::class, AppRoute.Theme.serializer())
                    subclass(AppRoute.Colors::class, AppRoute.Colors.serializer())
                    subclass(AppRoute.Taxes::class, AppRoute.Taxes.serializer())
                    subclass(AppRoute.Grades::class, AppRoute.Grades.serializer())
                    subclass(AppRoute.Statistics::class, AppRoute.Statistics.serializer())
                    subclass(AppRoute.Contacts::class, AppRoute.Contacts.serializer())
                    subclass(AppRoute.ContactDetail::class, AppRoute.ContactDetail.serializer())
                    subclass(AppRoute.Transport::class, AppRoute.Transport.serializer())
                    subclass(AppRoute.TransportCatalog::class, AppRoute.TransportCatalog.serializer())
                    subclass(AppRoute.TransportBooking::class, AppRoute.TransportBooking.serializer())
                    subclass(AppRoute.TicketDetail::class, AppRoute.TicketDetail.serializer())
                    subclass(AppRoute.ReservationDetail::class, AppRoute.ReservationDetail.serializer())
                    subclass(AppRoute.Transcripts::class, AppRoute.Transcripts.serializer())
                    subclass(AppRoute.Exams::class, AppRoute.Exams.serializer())
                    subclass(AppRoute.ExamsHistory::class, AppRoute.ExamsHistory.serializer())
                    subclass(AppRoute.StudyPlan::class, AppRoute.StudyPlan.serializer())
                    subclass(AppRoute.CourseDetail::class, AppRoute.CourseDetail.serializer())
                    subclass(AppRoute.Questionnaires::class, AppRoute.Questionnaires.serializer())
                    subclass(AppRoute.Questionnaire::class, AppRoute.Questionnaire.serializer())
                    subclass(AppRoute.Badge::class, AppRoute.Badge.serializer())
                    subclass(AppRoute.Attendance::class, AppRoute.Attendance.serializer())
                    subclass(AppRoute.Privacy::class, AppRoute.Privacy.serializer())
                    subclass(AppRoute.Terms::class, AppRoute.Terms.serializer())
                    subclass(AppRoute.Cookies::class, AppRoute.Cookies.serializer())
                    subclass(AppRoute.Updates::class, AppRoute.Updates.serializer())
                    subclass(AppRoute.Changelog::class, AppRoute.Changelog.serializer())
                    subclass(AppRoute.News::class, AppRoute.News.serializer())
                    subclass(AppRoute.NewsDetail::class, AppRoute.NewsDetail.serializer())
                    subclass(AppRoute.Devices::class, AppRoute.Devices.serializer())
                    subclass(AppRoute.Language::class, AppRoute.Language.serializer())
                    subclass(AppRoute.Author::class, AppRoute.Author.serializer())
                }
            }
    }
