package ru.sem.soilultrasound.di

import ru.sem.soilultrasound.navigator.MainNavigator
import ru.sem.soilultrasound.navigator.NavigatorV2
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sem.soilultrasound.data.settings.SettingsRepository
import ru.sem.soilultrasound.data.settings.sharedprefs.SharedprefSettingsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class BindDataModule {

//    @Binds
//    abstract fun bindSessionRepository(sharedPreferences: SharedprefSessionRepository): SessionRepository

    @Binds
    abstract fun bindDictionaryRepository(sharedPreferences: SharedprefSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindNavigatorV2(mainNavigator: MainNavigator): NavigatorV2

}