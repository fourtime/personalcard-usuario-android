package br.com.tln.personalcard.usuario.di

import android.app.Application
import androidx.room.Room
import br.com.tln.personalcard.usuario.SESSION_DB
import br.com.tln.personalcard.usuario.db.AccessTokenDao
import br.com.tln.personalcard.usuario.db.AccountDataDao
import br.com.tln.personalcard.usuario.db.CardDao
import br.com.tln.personalcard.usuario.db.DeviceDao
import br.com.tln.personalcard.usuario.db.InitializationDataDao
import br.com.tln.personalcard.usuario.db.ProfileDao
import br.com.tln.personalcard.usuario.db.SessionDb
import br.com.tln.personalcard.usuario.db.SessionDbMigrations
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Suppress("unused")
@Module
class DbModule {

    @Provides
    @Singleton
    fun provideSessionDb(application: Application): SessionDb {
        return Room.databaseBuilder(application, SessionDb::class.java, SESSION_DB)
            .addMigrations(*SessionDbMigrations.MIGRATIONS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAccessTokenDao(sessionDb: SessionDb): AccessTokenDao {
        return sessionDb.accessTokenDao()
    }

    @Provides
    @Singleton
    fun provideInitializationDataDao(sessionDb: SessionDb): InitializationDataDao {
        return sessionDb.initializedDataDao()
    }

    @Provides
    @Singleton
    fun provideAccountDataDao(sessionDb: SessionDb): AccountDataDao {
        return sessionDb.accountDataDao()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(sessionDb: SessionDb): DeviceDao {
        return sessionDb.deviceDao()
    }

    @Provides
    @Singleton
    fun provideProfileDao(sessionDb: SessionDb): ProfileDao {
        return sessionDb.profileDao()
    }

    @Provides
    @Singleton
    fun provideCardDao(sessionDb: SessionDb): CardDao {
        return sessionDb.cardDao()
    }
}