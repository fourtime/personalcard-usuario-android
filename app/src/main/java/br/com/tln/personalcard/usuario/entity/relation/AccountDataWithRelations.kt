package br.com.tln.personalcard.usuario.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.entity.AccountData
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.entity.Profile

class AccountDataWithRelations(
    @Embedded val accountData: AccountData
) {

    @Relation(parentColumn = "_id", entityColumn = "account_id")
    lateinit var accessTokenList: List<AccessToken>
    @Relation(parentColumn = "_id", entityColumn = "account_id")
    lateinit var profileList: List<Profile>
    @Relation(parentColumn = "_id", entityColumn = "account_id")
    lateinit var cards: List<Card>

    val accessToken: AccessToken
        get() = accessTokenList.first()

    val profile: Profile
        get() = profileList.first()
}