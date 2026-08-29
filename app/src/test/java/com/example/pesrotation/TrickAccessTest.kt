package com.example.pesrotation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrickAccessTest {
    @Test
    fun trickAccessExpiry_isThreeHoursAfterActivation() {
        val now = 1_000L

        assertEquals(now + TrickAccessDurationMillis, trickAccessExpiry(now))
    }

    @Test
    fun trickAccess_isActiveOnlyBeforeExpiry() {
        val expiresAt = 10_000L

        assertTrue(isTrickAccessActive(expiresAt, now = 9_999L))
        assertTrue(!isTrickAccessActive(expiresAt, now = expiresAt))
        assertTrue(!isTrickAccessActive(expiresAt, now = 10_001L))
    }

    @Test
    fun activatingAgain_extendsAccessFromLatestActivation() {
        val firstExpiry = trickAccessExpiry(1_000L)
        val renewedExpiry = trickAccessExpiry(2_000L)

        assertTrue(renewedExpiry > firstExpiry)
    }
}
