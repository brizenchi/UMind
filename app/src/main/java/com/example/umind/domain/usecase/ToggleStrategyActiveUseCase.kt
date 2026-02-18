package com.example.umind.domain.usecase

import com.example.umind.domain.model.Result
import com.example.umind.domain.repository.FocusRepository
import javax.inject.Inject

/**
 * Use case for toggling strategy active state
 */
class ToggleStrategyActiveUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(id: String, isActive: Boolean): Result<Unit> {
        return repository.setStrategyActive(id, isActive)
    }
}
