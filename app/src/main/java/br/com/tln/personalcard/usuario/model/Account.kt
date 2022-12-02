package br.com.tln.personalcard.usuario.model

import br.com.tln.personalcard.usuario.entity.AccessToken
import br.com.tln.personalcard.usuario.entity.AccountData
import br.com.tln.personalcard.usuario.entity.Card
import br.com.tln.personalcard.usuario.entity.Profile

data class Account(
    val accessToken: AccessToken,
    val accountData: AccountData,
    val profile: Profile,
    val cards: List<Card>
) {
    val id
        get() = accountData._id

    val username
        get() = accountData.username
}