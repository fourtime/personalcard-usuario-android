package br.com.tln.personalcard.usuario.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tln.personalcard.usuario.entity.InitializationData

@Dao
abstract class InitializationDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(initializationData: InitializationData)

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract fun update(initializationData: InitializationData)

    @Query("SELECT * FROM initialization_data WHERE _id = ${InitializationData.ID}")
    abstract fun get(): InitializationData?

    @Query("SELECT * FROM initialization_data WHERE _id = ${InitializationData.ID}")
    abstract fun getLiveData(): LiveData<InitializationData>
}