package com.example.pesrotation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetAccessTest {
    @Test
    fun presetAccessExpiry_isThreeHoursAfterActivation() {
        val now = 1_000L

        assertEquals(now + PresetAccessDurationMillis, presetAccessExpiry(now))
    }

    @Test
    fun presetAccess_isActiveOnlyBeforeExpiry() {
        val expiresAt = 10_000L

        assertTrue(isPresetAccessActive(expiresAt, now = 9_999L))
        assertTrue(!isPresetAccessActive(expiresAt, now = expiresAt))
        assertTrue(!isPresetAccessActive(expiresAt, now = 10_001L))
    }

    @Test
    fun activatingAgain_extendsAccessFromLatestActivation() {
        val firstExpiry = presetAccessExpiry(1_000L)
        val renewedExpiry = presetAccessExpiry(2_000L)

        assertTrue(renewedExpiry > firstExpiry)
    }
}
