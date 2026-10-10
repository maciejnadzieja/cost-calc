package com.costcalc.api.service

import com.costcalc.api.dto.AdminUser
import com.costcalc.api.dto.AdminUserPage
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AdminUserService(private val directory: KeycloakDirectoryClient) {
    fun page(page: Int): AdminUserPage {
        require(page >= 0)
        val deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos()
        val token = directory.token()
        val skip = page.toLong() * PAGE_SIZE
        var humanCount = 0L
        var first = 0
        val result = mutableListOf<AdminUser>()
        while (true) {
            if (System.nanoTime() > deadline) throw DirectoryUnavailableException()
            val batch = directory.users(token, first, BATCH_SIZE)
            if (System.nanoTime() > deadline) throw DirectoryUnavailableException()
            for (entry in batch) {
                if (!entry.isObject) throw DirectoryUnavailableException()
                if (!entry.path("serviceAccountClientId").isNull && !entry.path("serviceAccountClientId").isMissingNode) continue
                if (humanCount++ < skip) continue
                if (result.size == PAGE_SIZE) return AdminUserPage(result, page, true)
                val username = entry.get("username")?.takeIf { it.isTextual && it.asText().isNotBlank() }?.asText()
                    ?: throw DirectoryUnavailableException()
                val enabled = entry.get("enabled")?.takeIf { it.isBoolean }?.asBoolean()
                    ?: throw DirectoryUnavailableException()
                val email = entry.get("email")?.takeIf { it.isTextual }?.asText()?.takeIf { it.isNotBlank() }
                result.add(AdminUser(username, email, enabled))
            }
            // Only the upstream batch length ends the scan, never the number of humans in it.
            if (batch.size() < BATCH_SIZE) return AdminUserPage(result, page, false)
            if (first > Int.MAX_VALUE - batch.size()) throw DirectoryUnavailableException()
            first += batch.size()
        }
    }

    companion object {
        const val PAGE_SIZE = 20
        const val BATCH_SIZE = 100
    }
}
