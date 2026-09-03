package com.anto426.uniapp.navigation.model

enum class AppRouteRequirement {
    BootstrapOnly,
    SignedOutOnly,
    Public,
    Authenticated,
}

val AppRoute.requirement: AppRouteRequirement
    get() =
        when (this) {
            AppRoute.Bootstrap -> AppRouteRequirement.BootstrapOnly
            AppRoute.Login -> AppRouteRequirement.SignedOutOnly
            AppRoute.AboutUniApp,
            AppRoute.Privacy,
            AppRoute.Terms,
            AppRoute.Cookies,
            AppRoute.Author,
            -> AppRouteRequirement.Public

            else -> AppRouteRequirement.Authenticated
        }
