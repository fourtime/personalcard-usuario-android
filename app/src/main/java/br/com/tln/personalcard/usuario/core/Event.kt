package br.com.tln.personalcard.usuario.core

class Event<T>(val data: T, var handled: Boolean = false)