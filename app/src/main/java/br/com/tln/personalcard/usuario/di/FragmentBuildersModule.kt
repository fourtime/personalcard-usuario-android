package br.com.tln.personalcard.usuario.di

import br.com.tln.personalcard.usuario.ui.accreditednetwork.AccreditedNetworkFilterFragment
import br.com.tln.personalcard.usuario.ui.accreditednetwork.AccreditedNetworkFilterResultsFragment
import br.com.tln.personalcard.usuario.ui.accreditednetwork.AccreditedNetworkMapFragment
import br.com.tln.personalcard.usuario.ui.changepassword.ChangePasswordFragment
import br.com.tln.personalcard.usuario.ui.editprofile.EditProfileFragment
import br.com.tln.personalcard.usuario.ui.home.HomeCardFragment
import br.com.tln.personalcard.usuario.ui.home.HomeFragment
import br.com.tln.personalcard.usuario.ui.initialization.cardlastdigits.InitializationCardLastDigitsFragment
import br.com.tln.personalcard.usuario.ui.initialization.cpf.InitializationCpfFragment
import br.com.tln.personalcard.usuario.ui.initialization.createpassword.InitializationCreatePasswordFragment
import br.com.tln.personalcard.usuario.ui.initialization.email.InitializationEmailFragment
import br.com.tln.personalcard.usuario.ui.initialization.forgotpassword.InitializationForgotPasswordFragment
import br.com.tln.personalcard.usuario.ui.initialization.forgotpassword.RecoveryPasswordSuccessFragment
import br.com.tln.personalcard.usuario.ui.initialization.password.InitializationPasswordFragment
import br.com.tln.personalcard.usuario.ui.initialization.welcome.InitializationWelcomeFragment
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodeCardPasswordFragment
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodePaymentDataFragment
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodeReaderFragment
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodeSuccessFragment
import br.com.tln.personalcard.usuario.ui.prelogin.PreLoginFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributePreLoginFragment(): PreLoginFragment

    @ContributesAndroidInjector
    abstract fun contributeInitializationCpfFragment(): InitializationCpfFragment

    @ContributesAndroidInjector
    abstract fun contributeInitializationCardLastDigitsFragment(): InitializationCardLastDigitsFragment

    @ContributesAndroidInjector
    abstract fun contributeInitializationPasswordFragment(): InitializationPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeInitializationForgotPasswordFragment(): InitializationForgotPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeInitializationCreatePasswordFragment(): InitializationCreatePasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeInitializationEmailFragment(): InitializationEmailFragment

    @ContributesAndroidInjector
    abstract fun contributeInitializationWelcomeFragment(): InitializationWelcomeFragment

    @ContributesAndroidInjector
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeHomeCardFragment(): HomeCardFragment

    @ContributesAndroidInjector
    abstract fun contributeEditProfileFragment(): EditProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeAccreditedNetworkMapFragment(): AccreditedNetworkMapFragment

    @ContributesAndroidInjector
    abstract fun contributePayWithQrCodeReaderFragment(): PayWithQrCodeReaderFragment

    @ContributesAndroidInjector
    abstract fun contributePayWithQrCodeQrPaymentDataFragment(): PayWithQrCodePaymentDataFragment

    @ContributesAndroidInjector
    abstract fun contributePayWithQrCodeCardPasswordFragment(): PayWithQrCodeCardPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributePayWithQrCodeSuccessFragment(): PayWithQrCodeSuccessFragment

    @ContributesAndroidInjector
    abstract fun contributeAccreditedNetworkFilterFragment(): AccreditedNetworkFilterFragment

    @ContributesAndroidInjector
    abstract fun contributeRecoveryPasswordSuccessFragment(): RecoveryPasswordSuccessFragment

    @ContributesAndroidInjector
    abstract fun contributeAccreditedNetworkFilterResultsFragment(): AccreditedNetworkFilterResultsFragment
}