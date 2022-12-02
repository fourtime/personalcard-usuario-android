package br.com.tln.personalcard.usuario.entity

data class Address(
    val state: String,
    val city: String,
    val neighborhood: String,
    val street: String,
    val number: String,
    val complement: String,
    val postalCode: String
)