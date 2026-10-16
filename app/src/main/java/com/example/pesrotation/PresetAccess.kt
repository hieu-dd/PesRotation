package com.example.pesrotation

const val PresetLongPressMillis = 1_000L
const val PresetAccessDurationMillis = 3 * 60 * 60 * 1_000L

fun presetAccessExpiry(now: Long): Long = now + PresetAccessDurationMillis

fun isPresetAccessActive(expiresAt: Long, now: Long = System.currentTimeMillis()): Boolean =
    expiresAt > now
