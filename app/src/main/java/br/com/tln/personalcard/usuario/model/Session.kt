package br.com.tln.personalcard.usuario.model

import br.com.tln.personalcard.usuario.entity.Device
import br.com.tln.personalcard.usuario.entity.InitializationData

data class Session(
    val initializationData: InitializationData?,
    val account: Account?,
    val device: Device?
)