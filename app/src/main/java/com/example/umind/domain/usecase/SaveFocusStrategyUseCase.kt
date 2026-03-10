package com.example.umind.domain.usecase

import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import com.example.umind.domain.repository.FocusRepository
import javax.inject.Inject

/**
 * Use case for saving a focus strategy
 */
class SaveFocusStrategyUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(strategy: FocusStrategy): Result<Unit> {
        // Validate strategy before saving
        if (strategy.name.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Strategy name cannot be empty"),
                "请输入应用组名称"
            )
        }

        if (strategy.targetApps.isEmpty()) {
            return Result.Error(
                IllegalArgumentException("No apps selected"),
                "请至少选择一个要限制的应用"
            )
        }

        return repository.saveFocusStrategy(strategy)
    }
}
