package com.anto426.uniapp.navigation.runtime

import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.AppRouteRequirement
import com.anto426.uniapp.navigation.model.requirement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IndependentInformationRoutesTest {
    @Test
    fun projectRemainsPublicAndSeparateFromAuthenticatedAppInformation() {
        assertEquals(AppRouteRequirement.Public, AppRoute.AboutUniApp.requirement)
        assertEquals(AppRouteRequirement.Authenticated, AppRoute.Info.requirement)
        assertNull(legacyAppRoute("com.anto426.uniapp.navigation.model.AppRoute.AboutUniApp"))
    }

    @Test
    fun releaseNotesKeepTheirOwnDestination() {
        assertEquals(AppRouteRequirement.Authenticated, AppRoute.Updates.requirement)
        assertEquals(AppRouteRequirement.Authenticated, AppRoute.Changelog.requirement)
        assertNull(legacyAppRoute("com.anto426.uniapp.navigation.model.AppRoute.Changelog"))
    }
}
