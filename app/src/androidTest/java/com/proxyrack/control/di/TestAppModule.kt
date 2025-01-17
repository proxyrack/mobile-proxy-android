package com.proxyrack.control.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.proxyrack.control.BuildConfig
import com.proxyrack.control.data.network.GithubReleasesRetrofit
import com.proxyrack.control.data.network.GithubReleasesService
import com.proxyrack.control.data.network.IpInfoApiService
import com.proxyrack.control.data.repository.AnalyticsStatusNotifier
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.data.repository.DataAccessorImpl
import com.proxyrack.control.data.repository.GithubReleasesRepoImpl
import com.proxyrack.control.data.repository.IpInfoRepository
import com.proxyrack.control.data.repository.SettingsRepoImpl
import com.proxyrack.control.domain.AirplaneMode
import com.proxyrack.control.domain.AirplaneModeImpl
import com.proxyrack.control.domain.ConnectionServiceLauncher
import com.proxyrack.control.domain.ConnectionServiceLauncherImpl
import com.proxyrack.control.domain.IPRotator
import com.proxyrack.control.domain.IPRotatorImpl
import com.proxyrack.control.domain.proxy_manager.ProxyManagerProvider
import com.proxyrack.control.domain.proxy_manager.ProxyManagerProviderImpl
import com.proxyrack.control.domain.repository.DataAccessor
import com.proxyrack.control.domain.repository.GithubReleasesRepo
import com.proxyrack.control.domain.repository.SettingsRepo
import com.proxyrack.control.domain.updates.APKInstallerImpl
import com.proxyrack.control.domain.updates.UpdateManager
import com.proxyrack.control.domain.updates.UpdateManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton
import io.github.z4kn4fein.semver.toVersion
import kotlinx.coroutines.flow.flowOf
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideProxyManagerProvider(): ProxyManagerProvider {
        return ProxyManagerProviderImpl()
    }

    @Provides
    @Singleton
    fun provideIPInfoRepo(): IpInfoRepository {
        return IpInfoRepository()
    }

    @Provides
    @Singleton
    fun provideSettingsRepo(
        @Named("deviceID") deviceIDAccessor: DataAccessor,
        @Named("username") usernameAccessor: DataAccessor,
        @Named("initialized") initializedAccessor: DataAccessor,
        @Named("analytics") analyticsAccessor: DataAccessor,
        @Named("ipRotationInterval") ipRotationIntervalAccessor: DataAccessor,
        @Named("bandwidthSharingEnabled") bandwidthSharingAccessor: DataAccessor,
    ): SettingsRepo {
        return SettingsRepoImpl(deviceIDAccessor, usernameAccessor, initializedAccessor,
            analyticsAccessor, ipRotationIntervalAccessor, bandwidthSharingAccessor)
    }

    @Provides
    @Singleton
    fun provideAnalyticsStatusNotifier(): AnalyticsStatusNotifier {
        return AnalyticsStatusNotifier()
    }

    // https://stackoverflow.com/a/66603090/6716264
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        val mockDataStore = mock<DataStore<Preferences>>()
        `when`(mockDataStore.data).thenReturn(flowOf(emptyPreferences()))
        return mockDataStore
    }

    @Provides
    @Singleton
    @Named("deviceID")
    fun provideDeviceIDAccessor(datastore: DataStore<Preferences>): DataAccessor {
        return DataAccessorImpl(datastore, "deviceID")
    }

    @Provides
    @Singleton
    @Named("username")
    fun provideUsernameAccessor(datastore: DataStore<Preferences>): DataAccessor {
        return DataAccessorImpl(datastore, "username")
    }

    @Provides
    @Singleton
    @Named("initialized")
    fun provideInitializedAccessor(datastore: DataStore<Preferences>): DataAccessor {
        return DataAccessorImpl(datastore, "initialized")
    }

    @Provides
    @Singleton
    @Named("analytics")
    fun provideAnalyticsAccessor(datastore: DataStore<Preferences>): DataAccessor {
        return DataAccessorImpl(datastore, "analytics")
    }

    @Provides
    @Singleton
    @Named("bandwidthSharingEnabled")
    fun provideBandwidthSharingAccessor(datastore: DataStore<Preferences>): DataAccessor {
        return DataAccessorImpl(datastore, "bandwidthSharingEnabled")
    }

    @Provides
    @Singleton
    @Named("ipRotationInterval")
    fun provideIPRotationIntervalAccessor(datastore: DataStore<Preferences>): DataAccessor {
        return DataAccessorImpl(datastore, "ipRotationInterval")
    }

    @Provides
    @Singleton
    fun provideConnectionRepo(): ConnectionRepo {
        return ConnectionRepo()
    }

    @Provides
    @Singleton
    fun provideAirplaneMode(): AirplaneMode {
        return AirplaneModeImpl()
    }

    @Provides
    @Singleton
    fun provideConnServiceLauncher(@ApplicationContext context: Context): ConnectionServiceLauncher {
        return ConnectionServiceLauncherImpl(context)
    }

    @Provides
    @Singleton
    fun provideIPRotator(ap: AirplaneMode, connRepo: ConnectionRepo, connLauncher: ConnectionServiceLauncher): IPRotator {
        return IPRotatorImpl(connRepo, ap, connLauncher, CoroutineScope(Dispatchers.IO))
    }

    @Provides
    @Singleton
    fun provideUpdateManager(@ApplicationContext context: Context): UpdateManager {
        val repo = GithubReleasesRepoImpl(GithubReleasesRetrofit.retrofit.create(GithubReleasesService::class.java))

        return UpdateManagerImpl(
            BuildConfig.VERSION_NAME.toVersion(strict = false),
            repo,
            APKInstallerImpl(context),
            context.cacheDir,
        )
    }
}