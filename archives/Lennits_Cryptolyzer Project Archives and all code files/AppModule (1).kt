package com.lennit.cryptolyzer.di

import com.lennit.cryptolyzer.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAgentsRepository(): AgentsRepository = InMemoryAgentsRepository()

    @Provides
    @Singleton
    fun provideVaultRepository(): VaultRepository = InMemoryVaultRepository()

    @Provides
    @Singleton
    fun provideStrategiesRepository(): StrategiesRepository = InMemoryStrategiesRepository()

    @Provides
    @Singleton
    fun provideNotificationsRepository(): NotificationsRepository = InMemoryNotificationsRepository()

    @Provides
    @Singleton
    fun provideSystemRepository(): SystemRepository = InMemorySystemRepository()
}
