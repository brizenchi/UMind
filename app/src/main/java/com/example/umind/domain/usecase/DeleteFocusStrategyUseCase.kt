package com.example.umind.domain.usecase

import com.example.umind.domain.model.Result
import com.example.umind.domain.repository.FocusRepository
import javax.inject.Inject

/**
 * Use case for deleting a focus strategy
 */
class DeleteFocusStrategyUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteFocusStrategy(id)
    }
}
