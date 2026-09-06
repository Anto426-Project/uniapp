package com.anto426.uniapp.data

import kotlinx.coroutines.CancellationException

/** Fixed sources belong to exactly one career, including an explicitly absent profile id. */
internal fun requireDataRequestOwner(
    activeAccountId: String,
    activeProfileId: String?,
    fixedAccountId: String?,
    fixedProfileId: String?,
) {
    if (fixedAccountId != null &&
        (fixedAccountId != activeAccountId || fixedProfileId != activeProfileId)
    ) {
        throw CancellationException("The data source belongs to a previous account or profile")
    }
}
