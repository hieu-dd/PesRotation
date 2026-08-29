package com.example.pesrotation

const val TrickLongPressMillis = 1_000L
const val TrickAccessDurationMillis = 3 * 60 * 60 * 1_000L

fun trickAccessExpiry(now: Long): Long = now + TrickAccessDurationMillis

fun isTrickAccessActive(expiresAt: Long, now: Long = System.currentTimeMillis()): Boolean =
    expiresAt > now
