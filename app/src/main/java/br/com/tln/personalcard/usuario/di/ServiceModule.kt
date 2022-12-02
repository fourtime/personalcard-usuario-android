package br.com.tln.personalcard.usuario.di

import br.com.tln.personalcard.usuario.fcm.MessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeMessagingService(): MessagingService
}