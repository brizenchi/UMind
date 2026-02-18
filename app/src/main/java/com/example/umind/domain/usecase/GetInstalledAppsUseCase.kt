package com.example.umind.domain.usecase

import com.example.umind.domain.model.AppInfo
import com.example.umind.domain.model.Result
import com.example.umind.domain.repository.FocusRepository
import javax.inject.Inject

/**
 * Use case for getting installed apps
 */
class GetInstalledAppsUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(): Result<List<AppInfo>> {
        return repository.getInstalledApps()
    }
}
