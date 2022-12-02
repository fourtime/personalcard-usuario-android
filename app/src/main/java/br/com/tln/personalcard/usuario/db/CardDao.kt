package br.com.tln.personalcard.usuario.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tln.personalcard.usuario.entity.Card

@Dao
abstract class CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(card: Card)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(card: List<Card>)

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract fun update(card: Card)

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract fun update(card: List<Card>)

    @Query("SELECT * FROM card WHERE id = :cardId")
    abstract  fun getCard(cardId: String): LiveData<Card>

    @Query("DELETE FROM card")
    abstract fun deleteAll()
}