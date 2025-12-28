package com.example.theseus.di

import com.example.theseus.data.local.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory(androidContext()) }
}