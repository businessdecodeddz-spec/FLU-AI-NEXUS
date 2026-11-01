package com.example.data.model

/**
 * Fundamental Action Lifecycle States required by NEXUS:
 * PROPOSED -> AUTHORIZED -> EXECUTED -> VERIFIED / FAILED / CANCELLED
 */
enum class ActionState {
    PROPOSED,
    AUTHORIZED,
    EXECUTED,
    VERIFIED,
    FAILED,
    CANCELLED
}
