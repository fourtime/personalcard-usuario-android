package br.com.tln.personalcard.usuario.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.com.tln.personalcard.usuario.entity.AccountData
import br.com.tln.personalcard.usuario.entity.relation.AccountDataWithRelations

@Dao
abstract class AccountDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(accountData: AccountData)

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract fun update(accountData: AccountData)

    @Query("SELECT * FROM account WHERE _id = ${AccountData.ID}")
    abstract fun get(): AccountData?

    @Query("SELECT * FROM account WHERE _id = ${AccountData.ID}")
    abstract fun getLiveData(): LiveData<AccountData>

    @Transaction
    @Query("SELECT * FROM account WHERE _id = ${AccountData.ID}")
    abstract fun getWithRelations(): AccountDataWithRelations?

    @Transaction
    @Query("SELECT * FROM account WHERE _id = ${AccountData.ID}")
    abstract fun getWithRelationsLiveData(): LiveData<AccountDataWithRelations>

    @Query("DELETE FROM account")
    abstract fun delete()
}