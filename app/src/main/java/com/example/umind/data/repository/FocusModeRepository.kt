package com.example.umind.data.repository

import com.example.umind.data.local.dao.FocusModeDao
import com.example.umind.data.local.entity.FocusModeEntity
import com.example.umind.domain.model.FocusMode
import com.example.umind.domain.model.FocusModeType
import com.example.umind.domain.model.TimeRestriction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Focus Mode
 */
@Singleton
class FocusModeRepository @Inject constructor(
    private val focusModeDao: FocusModeDao
) {
    /**
     * Get focus mode as Flow
     */
    fun getFocusMode(): Flow<FocusMode> {
        return focusModeDao.getFocusMode().map { entity ->
            entity?.toDomainModel() ?: FocusMode()
        }
    }

    /**
     * Get focus mode once (for accessibility service)
     */
    suspend fun getFocusModeOnce(): FocusMode {
        return focusModeDao.getFocusModeOnce()?.toDomainModel() ?: FocusMode()
    }

    /**
     * Toggle focus mode on/off (manual mode)
     */
    suspend fun toggleFocusMode(isEnabled: Boolean) {
        val existing = focusModeDao.getFocusModeOnce()
        if (existing != null) {
            focusModeDao.update(
                existing.copy(
                    isEnabled = isEnabled,
                    modeType = FocusModeType.MANUAL,
                    countdownEndTime = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            focusModeDao.insert(
                FocusModeEntity(
                    isEnabled = isEnabled,
                    modeType = FocusModeType.MANUAL
                )
            )
        }
    }

    /**
     * Start countdown mode
     */
    suspend fun startCountdown(durationMinutes: Int) {
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        val existing = focusModeDao.getFocusModeOnce()
        if (existing != null) {
            focusModeDao.update(
                existing.copy(
                    isEnabled = true,
                    modeType = FocusModeType.COUNTDOWN,
                    countdownEndTime = endTime,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            focusModeDao.insert(
                FocusModeEntity(
                    isEnabled = true,
                    modeType = FocusModeType.COUNTDOWN,
                    countdownEndTime = endTime
                )
            )
        }
    }

    /**
     * Set scheduled mode with time ranges
     */
    suspend fun setScheduledMode(timeRanges: List<TimeRestriction>) {
        val existing = focusModeDao.getFocusModeOnce()
        if (existing != null) {
            focusModeDao.update(
                existing.copy(
                    isEnabled = true,
                    modeType = FocusModeType.SCHEDULED,
                    scheduledTimeRanges = timeRanges,
                    countdownEndTime = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            focusModeDao.insert(
                FocusModeEntity(
                    isEnabled = true,
                    modeType = FocusModeType.SCHEDULED,
                    scheduledTimeRanges = timeRanges
                )
            )
        }
    }

    /**
     * Update scheduled time ranges without activating the mode
     */
    suspend fun updateScheduledTimeRanges(timeRanges: List<TimeRestriction>) {
        val existing = focusModeDao.getFocusModeOnce()
        if (existing != null) {
            focusModeDao.update(
                existing.copy(
                    scheduledTimeRanges = timeRanges,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            focusModeDao.insert(
                FocusModeEntity(
                    scheduledTimeRanges = timeRanges
                )
            )
        }
    }

    /**
     * Stop focus mode
     */
    suspend fun stopFocusMode() {
        val existing = focusModeDao.getFocusModeOnce()
        if (existing != null) {
            focusModeDao.update(
                existing.copy(
                    isEnabled = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Update whitelist
     */
    suspend fun updateWhitelist(whitelistedApps: Set<String>) {
        val existing = focusModeDao.getFocusModeOnce()
        if (existing != null) {
            focusModeDao.update(
                existing.copy(
                    whitelistedApps = whitelistedApps,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            focusModeDao.insert(
                FocusModeEntity(
                    whitelistedApps = whitelistedApps
                )
            )
        }
    }

    /**
     * Add app to whitelist
     */
    suspend fun addToWhitelist(packageName: String) {
        val existing = focusModeDao.getFocusModeOnce()
        val currentWhitelist = existing?.whitelistedApps ?: emptySet()
        updateWhitelist(currentWhitelist + packageName)
    }

    /**
     * Remove app from whitelist
     */
    suspend fun removeFromWhitelist(packageName: String) {
        val existing = focusModeDao.getFocusModeOnce()
        val currentWhitelist = existing?.whitelistedApps ?: emptySet()
        updateWhitelist(currentWhitelist - packageName)
    }

    private fun FocusModeEntity.toDomainModel(): FocusMode {
        return FocusMode(
            id = id,
            isEnabled = isEnabled,
            whitelistedApps = whitelistedApps,
            modeType = modeType,
            countdownEndTime = countdownEndTime,
            scheduledTimeRanges = scheduledTimeRanges,
            updatedAt = updatedAt
        )
    }
}
