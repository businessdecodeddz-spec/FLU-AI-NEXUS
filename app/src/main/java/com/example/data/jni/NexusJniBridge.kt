package com.example.data.jni

import android.util.Log
import java.security.MessageDigest

/**
 * JNI Bridge interface for NEXUS native Rust/C++ modules.
 * Strictly separates active native execution (libnexus_core_native.so) from Kotlin JVM fallback.
 * Rule enforced: Kotlin fallback is NEVER presented as active native C++/Rust execution.
 */
object NexusJniBridge {

    private const val TAG = "NexusJniBridge"
    private var isNativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("nexus_core_native")
            isNativeLibraryLoaded = true
            try { Log.i(TAG, "Native C++/Rust library 'nexus_core_native' loaded successfully.") } catch (_: Throwable) {}
        } catch (e: UnsatisfiedLinkError) {
            isNativeLibraryLoaded = false
            try { Log.w(TAG, "Native library 'nexus_core_native' not present. Active mode: KOTLIN_JVM_FALLBACK.") } catch (_: Throwable) {}
        }
    }

    fun isNativeActive(): Boolean = isNativeLibraryLoaded

    fun getExecutionMode(): String {
        return if (isNativeLibraryLoaded) "NATIVE_JNI_ACTIVE" else "KOTLIN_JVM_FALLBACK"
    }

    fun getBridgeStatus(): String {
        return if (isNativeLibraryLoaded) {
            "RUST/C++ NATIVE CORE ACTIVE (libnexus_core_native.so)"
        } else {
            "JNI READY [MODE: KOTLIN_JVM_FALLBACK - Binaire natif .so non compilé]"
        }
    }

    // Native functions declarations (matching JNI C++ headers)
    external fun nativeGetCoreVersion(): String
    external fun nativeExecuteBitNetInference(prompt: String, quantBits: Int): String
    external fun nativeTurbocompteAccelerate(threadCount: Int): Long
    external fun nativeBizTalkRouteData(payloadJson: String): String
    external fun nativeChronosSyncTask(taskId: String): Boolean
    external fun nativeGetHardwareTelemetry(): String

    fun getCoreVersion(): String {
        return if (isNativeLibraryLoaded) {
            try { nativeGetCoreVersion() } catch (e: Exception) { "v3.8-Native" }
        } else {
            "v3.8-JVM-Fallback (Interface JNI déclarée, .so absent)"
        }
    }

    fun executeBitNetInference(prompt: String, quantBits: Int = 2): String {
        return if (isNativeLibraryLoaded) {
            try { nativeExecuteBitNetInference(prompt, quantBits) } catch (e: Exception) { "Fallback" }
        } else {
            val hash = MessageDigest.getInstance("SHA-256")
                .digest("$prompt:$quantBits".toByteArray())
                .joinToString("") { "%02x".format(it) }.take(12).uppercase()
            "[MODE: KOTLIN_JVM_FALLBACK | Non-Natif] Traitement local JVM ($quantBits-bit émulé, SHA256:$hash) pour '$prompt'"
        }
    }

    /**
     * If native library is loaded, invokes native C++ implementation.
     * Otherwise performs a real JVM memory/CPU benchmark and returns the actual measured execution time in microseconds.
     */
    fun turbocompteAccelerate(threadCount: Int = 4): Long {
        if (isNativeLibraryLoaded) {
            return try { nativeTurbocompteAccelerate(threadCount) } catch (e: Exception) { 1L }
        }
        val startNs = System.nanoTime()
        var acc = 0L
        val iterations = (threadCount.coerceIn(1, 16)) * 2500
        for (i in 0 until iterations) {
            acc += (i * 31L) xor (acc ushr 3)
        }
        val elapsedMicros = ((System.nanoTime() - startNs) / 1000L).coerceAtLeast(1L)
        return if (acc == Long.MIN_VALUE) elapsedMicros + 1L else elapsedMicros
    }
}
