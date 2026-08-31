package com.anto426.uniapp.account.platform

import java.util.UUID

actual fun generateAccountStorageIdentifier(): String = UUID.randomUUID().toString()
