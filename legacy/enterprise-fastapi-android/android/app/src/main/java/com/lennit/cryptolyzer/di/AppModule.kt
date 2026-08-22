package com.lennit.cryptolyzer.di

import com.lennit.cryptolyzer.data.local.SettingsDataStore
import com.lennit.cryptolyzer.data.remote.LennitApiService
import com.lennit.cryptolyzer.data.repository.MockRepository
import com.lennit.cryptolyzer.data.repository.RemoteRepository
import com.lennit.cryptolyzer.domain.repository.CryptolyzerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * MODULE 01: Hilt DI — provides network, repo, and security dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(dataStore: SettingsDataStore): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val authInterceptor = Interceptor { chain ->
            val config = runBlocking { dataStore.appConfig.first() }
            val req    = chain.request().newBuilder()
                .addHeader("X-API-Key", config.apiKey)
                .build()
            chain.proceed(req)
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, dataStore: SettingsDataStore): Retrofit {
        val baseUrl = runBlocking {
            dataStore.appConfig.first().apiEndpoint.ifBlank { "http://localhost:8000/" }
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): LennitApiService =
        retrofit.create(LennitApiService::class.java)

    @Provides
    @Singleton
    @Named("remote")
    fun provideRemoteRepository(impl: RemoteRepository): CryptolyzerRepository = impl

    @Provides
    @Singleton
    @Named("mock")
    fun provideMockRepository(impl: MockRepository): CryptolyzerRepository = impl
}
