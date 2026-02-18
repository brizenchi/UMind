package com.example.umind.domain.usecase

import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import com.example.umind.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all focus strategies
 */
class GetFocusStrategiesUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    operator fun invoke(): Flow<List<FocusStrategy>> {
        return repository.getFocusStrategiesFlow()
    }

    suspend fun execute(): Result<List<FocusStrategy>> {
        return repository.getFocusStrategies()
    }
}
