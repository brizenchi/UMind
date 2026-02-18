package com.example.umind.di

import com.example.umind.data.repository.FocusRepositoryImpl
import com.example.umind.domain.repository.FocusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFocusRepository(
        focusRepositoryImpl: FocusRepositoryImpl
    ): FocusRepository
}
