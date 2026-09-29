package com.anto426.uniapp.ui.components.account

internal data class AccountDisplayIdentity(val name: String, val initials: String, val photo: ByteArray?)

internal fun accountDisplayIdentity(
    name: String,
    initials: String,
    photo: ByteArray?,
): AccountDisplayIdentity = AccountDisplayIdentity(name, initials, photo)
