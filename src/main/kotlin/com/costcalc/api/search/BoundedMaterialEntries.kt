package com.costcalc.api.search

import com.costcalc.api.model.UserId
import java.time.Duration
import java.util.UUID

internal class BoundedMaterialEntries<T>(
    private val ttl: Duration,
    private val capacity: Int,
    private val nanoTime: () -> Long = System::nanoTime,
) {
    private data class Entry<T>(val owner: UserId, val value: T, val issuedAt: Long)

    private val entries = linkedMapOf<UUID, Entry<T>>()

    @Synchronized
    fun issue(owner: UserId, value: T): UUID {
        val now = nanoTime()
        entries.values.removeIf { now - it.issuedAt >= ttl.toNanos() }
        if (entries.size >= capacity) entries.remove(entries.keys.first())
        return UUID.randomUUID().also { entries[it] = Entry(owner, value, now) }
    }

    @Synchronized
    fun find(owner: UserId, id: UUID): T? = entries[id]
        ?.takeIf { it.owner == owner && nanoTime() - it.issuedAt < ttl.toNanos() }
        ?.value

    @Synchronized
    fun remove(id: UUID) {
        entries.remove(id)
    }
}
