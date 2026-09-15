package com.anto426.uniapp.data

import com.anto426.uniapp.model.services.ContactData
import com.anto426.unisdk.backend.model.UniversityContact
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ContactMappingTest {
    @Test
    fun preservesDirectoryIdentityNamesPhonesAndLocationFields() {
        val contact = listOf(
            UniversityContact(
                id = " contact-17 ",
                firstName = " Ada Maria ",
                lastName = " Bianchi ",
                displayName = " Ada Maria Bianchi ",
                email = " ada.bianchi@example.org ",
                phones = listOf(" +39 0874 111 ", "", "+39 0874 222", "+39 0874 111"),
                organization = " Dipartimento di Informatica ",
                address = " Via Esempio 17 ",
                building = " Edificio delle Scienze ",
                city = " Pesche ",
            ),
        ).toContacts().single()

        assertEquals("contact-17", contact.id)
        assertEquals("Ada Maria", contact.firstName)
        assertEquals("Bianchi", contact.lastName)
        assertEquals("Ada Maria Bianchi", contact.name)
        assertEquals("ada.bianchi@example.org", contact.email)
        assertEquals(listOf("+39 0874 111", "+39 0874 222"), contact.phoneNumbers)
        assertEquals("+39 0874 111", contact.phone)
        assertEquals("Dipartimento di Informatica", contact.department)
        assertEquals("Via Esempio 17", contact.address)
        assertEquals("Edificio delle Scienze", contact.building)
        assertEquals("Pesche", contact.city)
    }

    @Test
    fun missingOrBlankCityStaysUnknown() {
        for (city in listOf(null, "", "   ")) {
            val contact = listOf(
                UniversityContact(
                    id = null,
                    firstName = null,
                    lastName = null,
                    displayName = "Segreteria",
                    email = null,
                    phones = emptyList(),
                    organization = null,
                    address = null,
                    building = null,
                    city = city,
                ),
            ).toContacts().single()

            assertEquals("", contact.city)
            assertEquals("", contact.address)
            assertEquals("", contact.building)
            assertEquals(emptyList(), contact.phoneNumbers)
        }
    }

    @Test
    fun phoneNumbersIncludeLegacyPhoneWithoutBlankOrDuplicateEntries() {
        val contact = ContactData(
            name = "Segreteria",
            role = "",
            initials = "S",
            phone = " +39 0874 333 ",
            phones = listOf(" +39 0874 111 ", " ", "+39 0874 222", "+39 0874 111"),
        )

        assertEquals(listOf("+39 0874 111", "+39 0874 222", "+39 0874 333"), contact.phoneNumbers)
        assertEquals(listOf("+39 0874 111", "+39 0874 222"), contact.copy(phone = "+39 0874 111").phoneNumbers)
        assertEquals(listOf("+39 0874 333"), contact.copy(phones = emptyList()).phoneNumbers)
    }

    @Test
    fun contactsWithoutApiIdsHaveDistinctKeysWhenTheirContactDetailsDiffer() {
        val original = ContactData(
            name = "Segreteria",
            role = "",
            initials = "S",
            email = "segreteria@example.org",
            phones = listOf("+39 0874 111", "+39 0874 222"),
            address = "Via Esempio 17",
            building = "Edificio A",
        )
        val alternatives = listOf(
            original.copy(phones = listOf("+39 0874 111", "+39 0874 333")),
            original.copy(address = "Via Esempio 18"),
            original.copy(building = "Edificio B"),
        )

        assertEquals(original.detailKey, original.copy().detailKey)
        alternatives.forEach { assertNotEquals(original.detailKey, it.detailKey) }
    }
}
