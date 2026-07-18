package com.example.fuelmate.di

import androidx.room.Room
import com.example.fuelmate.data.local.AppDatabase
import com.example.fuelmate.data.local.dao.FuelEntryDao
import com.example.fuelmate.data.local.dao.VehicleDao
import com.example.fuelmate.data.repository.FuelRepository
import com.example.fuelmate.data.repository.VehicleRepository
import com.example.fuelmate.data.repository.impl.FuelRepositoryImpl
import com.example.fuelmate.data.repository.impl.VehicleRepositoryImpl
import com.example.fuelmate.ui.viewmodel.AddFuelEntryViewModel
import com.example.fuelmate.ui.viewmodel.VehicleDetailViewModel
import com.example.fuelmate.ui.viewmodel.VehicleListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    // Room database
    // With exportSchema = true + declared AutoMigrations, Room handles schema changes
    // safely. fallbackToDestructiveMigrationOnDowngrade keeps downgrades from crashing.
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigrationOnDowngrade(dropAllTables = false).build()
    }

    single<VehicleDao> { get<AppDatabase>().vehicleDao() }
    single<FuelEntryDao> { get<AppDatabase>().fuelEntryDao() }

    // Repositories
    single<VehicleRepository> { VehicleRepositoryImpl(get()) }
    single<FuelRepository> { FuelRepositoryImpl(get()) }

    // ViewModels — modern Koin DSL (no manual parametersOf; constructor args injected).
    viewModelOf(::VehicleListViewModel)
    viewModel { (vehicleId: Long) ->
        VehicleDetailViewModel(get(), get(), vehicleId = vehicleId)
    }
    viewModel { (vehicleId: Long, entryId: Long?) ->
        AddFuelEntryViewModel(get(), vehicleId = vehicleId, entryId = entryId)
    }
}