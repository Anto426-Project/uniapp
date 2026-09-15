package com.anto426.uniapp.services.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.toContacts
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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ContactDirectoryRegressionTest : com.anto426.uniapp.testing.ResourceTest() {
    @Test
    fun searchMatchesSecondaryPhoneAddressBuildingAndCity() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val target = contact(
                id = "target",
                phones = listOf("+39 0874 111", "+39 0874 987654"),
                address = "Via Esempio 17",
                building = "Palazzo delle Scienze",
                city = "Pesche",
            )
            val viewModel = ContactsViewModel(source(target, contact(id = "other", city = "Pesche")))
            advanceUntilIdle()

            for (query in listOf("987654", "ESEMPIO 17", "delle Scienze")) {
                viewModel.updateSearchQuery(query)
                assertEquals(listOf("target"), viewModel.uiState.value.visibleContacts.map { it.id }, query)
            }
            viewModel.updateSearchQuery("pesche")
            assertEquals(2, viewModel.uiState.value.visibleContacts.size)
            viewModel.updateSearchQuery("missing-contact")
            assertTrue(viewModel.uiState.value.visibleContacts.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun contactsWithoutCityRemainSelectableAlongsideKnownCities() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = ContactsViewModel(source(
                contact(id = "known", city = "Pesche"),
                contact(id = "unknown", city = null),
                contact(id = "blank", city = "   "),
            ))
            advanceUntilIdle()

            val categories = viewModel.uiState.value.categories
            assertEquals(2, categories.size)
            val unknownCategory = categories.indexOfFirst { it != "Pesche" }
            assertTrue(unknownCategory >= 0)
            assertTrue(categories[unknownCategory].isNotBlank())
            viewModel.selectCategory(unknownCategory)
            assertEquals(setOf("unknown", "blank"), viewModel.uiState.value.visibleContacts.map { it.id }.toSet())
            assertTrue(viewModel.uiState.value.visibleContacts.all { it.city.isEmpty() })

            viewModel.selectCategory(categories.indexOf("Pesche"))
            assertEquals(listOf("known"), viewModel.uiState.value.visibleContacts.map { it.id })
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun detailKeySelectsCorrectContactBeforeLegacyNameAndEmailMatches() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val target = contact(id = "second", phones = listOf("+39 0874 222"))
            val targetKey = listOf(target).toContacts().single().detailKey
            val directory = source(
                contact(id = "first", phones = listOf("+39 0874 111")),
                target,
                contact(id = "decoy", name = "A Contact", email = targetKey),
            )
            val viewModel = ContactDetailViewModel(targetKey, directory)
            advanceUntilIdle()

            assertEquals(FeatureLoadState.Content, viewModel.uiState.value.loadState)
            assertEquals("second", viewModel.uiState.value.contact?.id)
            assertEquals(listOf("+39 0874 222"), viewModel.uiState.value.contact?.phoneNumbers)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun sameApiIdWithDifferentLocationsAndPhonesOpensTheSelectedRecord() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val first = contact(id = "shared", building = "Edificio A", phones = listOf("+39 0874 111"))
            val second = contact(id = "shared", building = "Edificio B", phones = listOf("+39 0874 222"))
            val mapped = listOf(first, second).toContacts()
            assertNotEquals(mapped[0].detailKey, mapped[1].detailKey)
            val directory = source(first, second)

            for (expected in mapped) {
                val viewModel = ContactDetailViewModel(expected.detailKey, directory)
                advanceUntilIdle()

                assertEquals(FeatureLoadState.Content, viewModel.uiState.value.loadState)
                assertEquals("shared", viewModel.uiState.value.contact?.id)
                assertEquals(expected.building, viewModel.uiState.value.contact?.building)
                assertEquals(expected.phoneNumbers, viewModel.uiState.value.contact?.phoneNumbers)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun fallbackKeysSelectCorrectContactWhenApiIdsAreMissing() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val first = contact(id = null, phones = listOf("+39 0874 111"))
            val second = contact(id = null, phones = listOf("+39 0874 222"))
            val mapped = listOf(first, second).toContacts()
            assertNotEquals(mapped[0].detailKey, mapped[1].detailKey)
            val viewModel = ContactDetailViewModel(mapped[1].detailKey, source(first, second))
            advanceUntilIdle()

            assertEquals(FeatureLoadState.Content, viewModel.uiState.value.loadState)
            assertEquals(listOf("+39 0874 222"), viewModel.uiState.value.contact?.phoneNumbers)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun legacyEmailAndNameRoutesStillResolve() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            for (key in listOf("segreteria@example.org", "Segreteria")) {
                val viewModel = ContactDetailViewModel(key, source(contact(id = "legacy")))
                advanceUntilIdle()

                assertEquals(FeatureLoadState.Content, viewModel.uiState.value.loadState)
                assertEquals("legacy", viewModel.uiState.value.contact?.id)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun source(vararg contacts: UniversityContact) = object : FakeUniAppDataSource() {
        override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> = contacts.toList()
    }

    private fun contact(
        id: String?,
        name: String = "Segreteria",
        email: String = "segreteria@example.org",
        phones: List<String> = emptyList(),
        address: String? = null,
        building: String? = null,
        city: String? = null,
    ) = UniversityContact(
        id = id,
        firstName = null,
        lastName = null,
        displayName = name,
        email = email,
        phones = phones,
        organization = "Servizi agli studenti",
        address = address,
        building = building,
        city = city,
    )
}
