package com.anto426.uniapp.didactics.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toContacts
import com.anto426.uniapp.data.toStudyCourse
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: StudyCourse? = null,
    val professorContact: ContactData? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class CourseDetailViewModel(
    private val courseId: String,
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(
        UniAppDataRequests.syllabus(courseId),
        UniAppDataRequests.StudyPlan,
        UniAppDataRequests.Career,
        UniAppDataRequests.Contacts,
    )
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val syllabus = snapshot.require(UniAppDataRequests.syllabus(courseId))
            val studyPlan = runCatching { snapshot.require(UniAppDataRequests.StudyPlan) }.getOrNull()
            val career = runCatching { snapshot.require(UniAppDataRequests.Career) }.getOrNull()
            val contacts = runCatching { snapshot.require(UniAppDataRequests.Contacts).toContacts() }.getOrNull()

            val planCourse = studyPlan?.courses?.firstOrNull {
                it.adsceId?.trim() == courseId.trim() || it.title.trim().equals(syllabus.adDes.trim(), ignoreCase = true)
            }
            val careerExam = career?.exams?.firstOrNull {
                it.adsceId?.trim() == courseId.trim() || it.name.trim().equals(syllabus.adDes.trim(), ignoreCase = true)
            }
            val course = syllabus.toStudyCourse(planCourse, careerExam)
            val matchedContact = findMatchingProfessor(course.professor, contacts)

            mutableUiState.value = CourseDetailUiState(
                course = course,
                professorContact = matchedContact,
                loadState = FeatureLoadState.Content,
            )
            snapshot.state(UniAppDataRequests.syllabus(courseId)).error?.let { throw it }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = CourseDetailUiState(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_il_corso)),
            )
        }
    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }

    private companion object {
        fun findMatchingProfessor(professorName: String, contacts: List<ContactData>?): ContactData? {
            if (professorName.isBlank() || contacts.isNullOrEmpty()) return null
            val cleanProf = professorName.lowercase()
                .replace("prof.ssa", "")
                .replace("prof.", "")
                .replace("dott.ssa", "")
                .replace("dott.", "")
                .replace("ing.", "")
                .trim()
            val profTokens = cleanProf.split(" ", "-", ",").map { it.trim() }.filter { it.length > 2 }
            if (profTokens.isEmpty()) return null

            return contacts.firstOrNull { contact ->
                val cleanContact = contact.name.lowercase()
                val contactTokens = cleanContact.split(" ", "-", ",").map { it.trim() }.filter { it.length > 2 }
                (profTokens.size >= 2 && profTokens.all { token -> cleanContact.contains(token) }) ||
                        (contactTokens.size >= 2 && contactTokens.all { token -> cleanProf.contains(token) }) ||
                        cleanContact == cleanProf
            }
        }
    }
}
