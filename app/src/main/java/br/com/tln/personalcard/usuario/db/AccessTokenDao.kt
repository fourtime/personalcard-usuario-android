package br.com.tln.personalcard.usuario.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tln.personalcard.usuario.entity.AccessToken

@Dao
abstract class AccessTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(accessToken: AccessToken)

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract fun update(accessToken: AccessToken)

    @Query("SELECT * FROM access_token WHERE _id = ${AccessToken.ID}")
    abstract fun get(): AccessToken?

    @Query("SELECT * FROM access_token WHERE _id = ${AccessToken.ID}")
    abstract fun getLiveData(): LiveData<AccessToken>

    @Query("DELETE FROM access_token")
    abstract fun delete()
}