package com.anto426.uniapp.session

import com.anto426.unisdk.backend.model.LoginCareerOption
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.session.UniUserProfile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectedCareerIdentityTest {
    @Test
    fun sameMatricolaInTwoCoursesCannotRelabelTheReturnedCareer() {
        val informatics = LoginCareerOption(
            displayName = "Student", degreeName = "Informatica", matricola = "001234", cdsId = "10",
        )
        val mathematics = informatics.copy(degreeName = "Matematica", cdsId = "11")
        val returned = UniUserProfile(
            id = "student", displayName = "Student", degreeName = "Informatica",
            matricola = "001234", email = null, photoUrl = null, isGuest = false,
            activeProfileId = informatics.profileId,
            profiles = listOf(UniCareerProfile(
                profileId = informatics.profileId, displayName = "Student", degreeName = "Informatica",
                matricola = "001234", cdsId = "10",
            )),
        )

        assertTrue(returned.matchesSelectedCareer(informatics))
        assertFalse(returned.matchesSelectedCareer(mathematics))
        assertTrue(returned.matchesSelectedCareer(null))
    }

}
