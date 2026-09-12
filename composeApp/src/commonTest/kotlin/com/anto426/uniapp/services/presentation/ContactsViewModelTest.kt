package com.anto426.uniapp.services.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.unisdk.backend.model.UniversityContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ContactsViewModelTest : com.anto426.uniapp.testing.ResourceTest() {

    @Test
    fun derivesRealCategoriesFromCitiesAndRemovesTutti() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dummyContacts = listOf(
                UniversityContact(
                    id = "c1",
                    firstName = "Mario",
                    lastName = "Rossi",
                    displayName = "Mario Rossi",
                    email = "mario.rossi@unimol.it",
                    phones = listOf("+39 0874 111"),
                    organization = "Dipartimento Giuridico",
                    address = null,
                    building = null,
                    city = "Campobasso",
                ),
                UniversityContact(
                    id = "c2",
                    firstName = "Giulia",
                    lastName = "Verdi",
                    displayName = "Giulia Verdi",
                    email = "giulia.verdi@unimol.it",
                    phones = listOf("+39 0874 222"),
                    organization = "Dipartimento di Bioscienze e Territorio",
                    address = null,
                    building = null,
                    city = "Pesche",
                ),
                UniversityContact(
                    id = "c3",
                    firstName = "Sara",
                    lastName = "Conti",
                    displayName = "Sara Conti",
                    email = "sara.conti@unimol.it",
                    phones = listOf("+39 0874 333"),
                    organization = "Dipartimento di Economia",
                    address = null,
                    building = null,
                    city = "Termoli",
                ),
            )

            val source = object : FakeUniAppDataSource() {
                override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> =
                    dummyContacts
            }

            val viewModel = ContactsViewModel(source)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(FeatureLoadState.Content, state.loadState)
            // Real categories extracted: Campobasso, Pesche, Termoli
            assertEquals(listOf("Campobasso", "Pesche", "Termoli"), state.categories)
            assertFalse(state.categories.contains("Tutti"))

            // Default category selected is index 0: Campobasso
            assertEquals(0, state.selectedCategoryIndex)
            assertEquals(1, state.visibleContacts.size)
            assertEquals("Mario Rossi", state.visibleContacts.first().name)

            // Select Pesche (index 1)
            viewModel.selectCategory(1)
            val pescheState = viewModel.uiState.value
            assertEquals(1, pescheState.selectedCategoryIndex)
            assertEquals(1, pescheState.visibleContacts.size)
            assertEquals("Giulia Verdi", pescheState.visibleContacts.first().name)

            // Search query filtering within selected category
            viewModel.updateSearchQuery("Verdi")
            assertEquals(1, viewModel.uiState.value.visibleContacts.size)
            viewModel.updateSearchQuery("Inesistente")
            assertTrue(viewModel.uiState.value.visibleContacts.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }
}
