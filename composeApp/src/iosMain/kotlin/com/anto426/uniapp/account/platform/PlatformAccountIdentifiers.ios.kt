package com.anto426.uniapp.account.platform

import platform.Foundation.NSUUID

actual fun generateAccountStorageIdentifier(): String = NSUUID().UUIDString()
