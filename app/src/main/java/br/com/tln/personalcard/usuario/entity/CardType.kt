package br.com.tln.personalcard.usuario.entity

enum class CardType(val id: Int) {
    POST_PAID(0),
    PRE_PAID(1),
    FLEET(2);

    companion object {
        fun fromId(id: Int?): CardType? {
            return values().firstOrNull {
                it.id == id
            }
        }
    }
}