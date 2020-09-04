/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.stokerapps.chartmaker.data.*
import com.stokerapps.chartmaker.data.database.AppDatabase
import com.stokerapps.chartmaker.domain.ChartRepository
import com.stokerapps.chartmaker.domain.EditorRepository
import com.stokerapps.chartmaker.domain.FileManager
import com.stokerapps.chartmaker.domain.Resources
import com.stokerapps.chartmaker.ui.FragmentFactory
import com.stokerapps.chartmaker.ui.ViewModelFactory
import com.stokerapps.chartmaker.ui.common.AndroidFileManager
import com.stokerapps.chartmaker.ui.common.AndroidResources
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import com.stokerapps.chartmaker.common.AppDispatchers as Dispatchers

@Suppress("unused")
class ChartMakerApp : Application(), KoinComponent {

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        Timber.e(error)
    }

    private val module = module {
        // data sources
        single { Cache() }
        single { AppDatabase.getDatabase(this@ChartMakerApp) }
        single { get<AppDatabase>() as ChartDataSource }
        single { get<AppDatabase>() as EditorDataSource }
        single { AndroidFileManager(this@ChartMakerApp) as FileManager }
        single { AndroidResources(this@ChartMakerApp) as Resources }

        // application coroutine scope
        single { CoroutineScope(SupervisorJob() + Dispatchers.Default + errorHandler) }

        // repositories
        single { ChartRepositoryImpl(get(), get()) as ChartRepository }
        single { EditorRepositoryImpl(get(), get()) as EditorRepository }

        // view model factory
        single {
            ViewModelFactory(
                get(),
                get(),
                get(),
                get(),
                get()
            ) as ViewModelProvider.Factory
        }

        // fragment factory
        single { FragmentFactory(get(), get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            modules(module)
        }
    }
}