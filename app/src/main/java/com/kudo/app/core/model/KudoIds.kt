package com.kudo.app.core.model

import java.util.concurrent.atomic.AtomicLong

object KudoIds {
    private val lastIssued = AtomicLong(0L)

    fun next(now: Long = System.currentTimeMillis()): Long {
        while (true) {
            val previous = lastIssued.get()
            val candidate = if (now > previous) now else previous + 1
            if (lastIssued.compareAndSet(previous, candidate)) {
                return candidate
            }
        }
    }
}
