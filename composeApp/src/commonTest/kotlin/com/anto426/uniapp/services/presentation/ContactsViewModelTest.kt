package com.anto426.uniapp.services.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.unisdk.model.UniversityContact
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
    fun derivesRealCategoriesFromCitiesAndSupportsAllFilter() = runTest {
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
            assertEquals(listOf(1, 1, 1), state.categoryCounts)
            assertEquals(3, state.totalContactsCount)
            assertFalse(state.categories.contains("Tutti"))

            // Default selection shows all contacts across extracted categories
            assertEquals(ContactsUiState.ALL_CATEGORIES_INDEX, state.selectedCategoryIndex)
            assertEquals(3, state.visibleContacts.size)

            // Select Campobasso (index 0)
            viewModel.selectCategory(0)
            val campobassoState = viewModel.uiState.value
            assertEquals(0, campobassoState.selectedCategoryIndex)
            assertEquals(1, campobassoState.visibleContacts.size)
            assertEquals("Mario Rossi", campobassoState.visibleContacts.first().name)

            // Select Pesche (index 1)
            viewModel.selectCategory(1)
            val pescheState = viewModel.uiState.value
            assertEquals(1, pescheState.selectedCategoryIndex)
            assertEquals(1, pescheState.visibleContacts.size)
            assertEquals("Giulia Verdi", pescheState.visibleContacts.first().name)

            // Search query filtering within selected category, then return to All
            viewModel.updateSearchQuery("Verdi")
            assertEquals(1, viewModel.uiState.value.visibleContacts.size)
            viewModel.updateSearchQuery("Inesistente")
            assertTrue(viewModel.uiState.value.visibleContacts.isEmpty())
            viewModel.updateSearchQuery("")
            viewModel.selectCategory(ContactsUiState.ALL_CATEGORIES_INDEX)
            assertEquals(3, viewModel.uiState.value.visibleContacts.size)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun mergesProvinceSuffixAndCaseVariantsIntoSingleCityChip() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val contacts = listOf(
                UniversityContact(
                    id = "c1",
                    firstName = "Mario",
                    lastName = "Rossi",
                    displayName = "Mario Rossi",
                    email = "mario@unimol.it",
                    phones = emptyList(),
                    organization = "Dip. 1",
                    address = null,
                    building = null,
                    city = "Campobasso",
                ),
                UniversityContact(
                    id = "c2",
                    firstName = "Luca",
                    lastName = "Bianchi",
                    displayName = "Luca Bianchi",
                    email = "luca@unimol.it",
                    phones = emptyList(),
                    organization = "Dip. 2",
                    address = null,
                    building = null,
                    city = "Campobasso(CB)",
                ),
                UniversityContact(
                    id = "c3",
                    firstName = "Anna",
                    lastName = "Verdi",
                    displayName = "Anna Verdi",
                    email = "anna@unimol.it",
                    phones = emptyList(),
                    organization = "Dip. 3",
                    address = null,
                    building = null,
                    city = "CAMPOBASSO (CB)",
                ),
                UniversityContact(
                    id = "c4",
                    firstName = "Sara",
                    lastName = "Conti",
                    displayName = "Sara Conti",
                    email = "sara@unimol.it",
                    phones = emptyList(),
                    organization = "Dip. 4",
                    address = null,
                    building = null,
                    city = "Pesche (IS)",
                ),
            )

            val source = object : FakeUniAppDataSource() {
                override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> =
                    contacts
            }

            val viewModel = ContactsViewModel(source)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf("Campobasso", "Pesche"), state.categories)
            assertEquals(listOf(3, 1), state.categoryCounts)

            viewModel.selectCategory(0)
            assertEquals(3, viewModel.uiState.value.visibleContacts.size)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
