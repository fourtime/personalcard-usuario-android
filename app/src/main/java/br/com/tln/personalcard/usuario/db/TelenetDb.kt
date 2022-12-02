package br.com.tln.personalcard.usuario.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.entity.AccountData
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.entity.Device
import br.com.tln.personalcard.usuario.entity.InitializationData
import br.com.tln.personalcard.usuario.entity.Profile

@Database(
    entities = [AccessToken::class, InitializationData::class, AccountData::class, Profile::class, Card::class, Device::class],
    version = 1,
    exportSchema = false
)
abstract class SessionDb : RoomDatabase() {
    abstract fun accessTokenDao(): AccessTokenDao
    abstract fun initializedDataDao(): InitializationDataDao
    abstract fun accountDataDao(): AccountDataDao
    abstract fun profileDao(): ProfileDao
    abstract fun cardDao(): CardDao
    abstract fun deviceDao(): DeviceDao
}