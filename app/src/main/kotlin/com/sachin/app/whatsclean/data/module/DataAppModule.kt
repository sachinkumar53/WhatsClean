package com.sachin.app.whatsclean.data.module

import android.content.Context
import androidx.room.Room
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.data.database.AppDatabase
import com.sachin.app.whatsclean.data.repositories.DashboardRepository
import com.sachin.app.whatsclean.data.repositories.MediaRepository
import com.sachin.app.whatsclean.data.source.MediaDatabaseBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataAppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun providesApplicationScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Singleton
    @Provides
    fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        ).build()
    }

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
