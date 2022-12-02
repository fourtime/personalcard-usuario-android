package br.com.tln.personalcard.usuario.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tln.personalcard.usuario.entity.Profile

@Dao
abstract class ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(profile: Profile)

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract fun update(profile: Profile)

    @Query("SELECT * FROM profile WHERE _id = ${Profile.ID}")
    abstract fun get(): Profile?

    @Query("SELECT * FROM profile WHERE _id = ${Profile.ID}")
    abstract fun getLiveData(): LiveData<Profile>

    @Query("DELETE FROM profile")
    abstract fun delete()
}