package com.example.umind.data.repository

import com.example.umind.data.local.dao.TemporaryUsageDao
import com.example.umind.data.local.entity.TemporaryUsageEntity
import com.example.umind.domain.model.TemporaryUsage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemporaryUsageRepository @Inject constructor(
    private val temporaryUsageDao: TemporaryUsageDao
) {
    /**
     * Request temporary usage for a package
     */
    suspend fun requestTemporaryUsage(
        packageName: String,
        appName: String,
        reason: String,
        durationMinutes: Int
    ): TemporaryUsage {
        val now = System.currentTimeMillis()
        val endTime = now + (durationMinutes * 60 * 1000L)

        val temporaryUsage = TemporaryUsage(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            appName = appName,
            reason = reason,
            requestedDurationMinutes = durationMinutes,
            startTime = now,
            endTime = endTime,
            isActive = true,
            createdAt = now
        )

        temporaryUsageDao.insertTemporaryUsage(TemporaryUsageEntity.fromDomainModel(temporaryUsage))
        return temporaryUsage
    }

    /**
     * Get active temporary usage for a package
     */
    suspend fun getActiveTemporaryUsage(packageName: String): TemporaryUsage? {
        val now = System.currentTimeMillis()
        return temporaryUsageDao.getActiveTemporaryUsage(packageName, now)?.toDomainModel()
    }

    /**
     * Get active temporary usage flow for a package
     */
    fun getActiveTemporaryUsageFlow(packageName: String): Flow<TemporaryUsage?> {
        val now = System.currentTimeMillis()
        return temporaryUsageDao.getActiveTemporaryUsageFlow(packageName, now)
            .map { it?.toDomainModel() }
    }

    /**
     * Check if package has active temporary usage
     */
    suspend fun hasActiveTemporaryUsage(packageName: String): Boolean {
        return getActiveTemporaryUsage(packageName) != null
    }

    /**
     * Deactivate temporary usage
     */
    suspend fun deactivateTemporaryUsage(id: String) {
        temporaryUsageDao.deactivateTemporaryUsage(id)
    }

    /**
     * Deactivate all expired temporary usages
     */
    suspend fun deactivateExpiredUsages() {
        val now = System.currentTimeMillis()
        temporaryUsageDao.deactivateExpiredUsages(now)
    }

    /**
     * Get all temporary usages
     */
    suspend fun getAllTemporaryUsages(): List<TemporaryUsage> {
        return temporaryUsageDao.getAllTemporaryUsages().map { it.toDomainModel() }
    }

    /**
     * Get temporary usages for a specific package
     */
    suspend fun getTemporaryUsagesForPackage(packageName: String): List<TemporaryUsage> {
        return temporaryUsageDao.getTemporaryUsagesForPackage(packageName).map { it.toDomainModel() }
    }

    /**
     * Clean up old records (older than 90 days)
     */
    suspend fun cleanupOldRecords() {
        val cutoffTime = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L)
        temporaryUsageDao.deleteOldRecords(cutoffTime)
    }
}
