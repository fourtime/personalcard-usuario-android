/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.tln.personalcard.usuario.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.tln.personalcard.usuario.ui.accreditednetwork.AccreditedNetworkFilterResultsViewModel
import br.com.tln.personalcard.usuario.ui.accreditednetwork.AccreditedNetworkFilterViewModel
import br.com.tln.personalcard.usuario.ui.accreditednetwork.AccreditedNetworkMapViewModel
import br.com.tln.personalcard.usuario.ui.changepassword.ChangePasswordViewModel
import br.com.tln.personalcard.usuario.ui.editprofile.EditProfileViewModel
import br.com.tln.personalcard.usuario.ui.home.HomeCardViewModel
import br.com.tln.personalcard.usuario.ui.home.HomeViewModel
import br.com.tln.personalcard.usuario.ui.initialization.cardlastdigits.InitializationCardLastDigitsViewModel
import br.com.tln.personalcard.usuario.ui.initialization.cpf.InitializationCpfViewModel
import br.com.tln.personalcard.usuario.ui.initialization.createpassword.InitializationCreatePasswordViewModel
import br.com.tln.personalcard.usuario.ui.initialization.email.InitializationEmailViewModel
import br.com.tln.personalcard.usuario.ui.initialization.forgotpassword.InitializationForgotPasswordViewModel
import br.com.tln.personalcard.usuario.ui.initialization.forgotpassword.RecoveryPasswordSuccessViewModel
import br.com.tln.personalcard.usuario.ui.initialization.password.InitializationPasswordViewModel
import br.com.tln.personalcard.usuario.ui.initialization.welcome.InitializationWelcomeViewModel
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodeCardPasswordViewModel
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodePaymentDataViewModel
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodeReaderViewModel
import br.com.tln.personalcard.usuario.ui.paywithqrcode.PayWithQrCodeSuccessViewModel
import br.com.tln.personalcard.usuario.ui.prelogin.PreLoginViewModel
import br.com.tln.personalcard.usuario.viewmodel.TelenetViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: TelenetViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(InitializationCpfViewModel::class)
    abstract fun bindInitializationCpfViewModel(viewModel: InitializationCpfViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InitializationCardLastDigitsViewModel::class)
    abstract fun bindInitializationCardLastDigitsViewModel(viewModel: InitializationCardLastDigitsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InitializationPasswordViewModel::class)
    abstract fun bindInitializationPasswordViewModel(viewModel: InitializationPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InitializationForgotPasswordViewModel::class)
    abstract fun bindInitializationForgotPasswordViewModel(viewModel: InitializationForgotPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InitializationCreatePasswordViewModel::class)
    abstract fun bindInitializationCreatePasswordViewModel(viewModel: InitializationCreatePasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InitializationEmailViewModel::class)
    abstract fun bindInitializationEmailViewModel(viewModel: InitializationEmailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InitializationWelcomeViewModel::class)
    abstract fun bindInitializationWelcomeViewModel(viewModel: InitializationWelcomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PreLoginViewModel::class)
    abstract fun bindPreLoginViewModel(viewModel: PreLoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangePasswordViewModel::class)
    abstract fun bindChangePasswordViewModel(viewModel: ChangePasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeCardViewModel::class)
    abstract fun bindHomeCardViewModel(viewModel: HomeCardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel::class)
    abstract fun bindEditProfileViewModel(viewModel: EditProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccreditedNetworkMapViewModel::class)
    abstract fun bindAccreditedNetworkMapViewModel(viewModel: AccreditedNetworkMapViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PayWithQrCodeReaderViewModel::class)
    abstract fun bindPayWithQrCodeReaderViewModel(viewModel: PayWithQrCodeReaderViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PayWithQrCodePaymentDataViewModel::class)
    abstract fun bindPayWithQrCodePaymentDataViewModel(viewModel: PayWithQrCodePaymentDataViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PayWithQrCodeCardPasswordViewModel::class)
    abstract fun bindPayWithQrCodeCardPasswordViewModel(viewModel: PayWithQrCodeCardPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PayWithQrCodeSuccessViewModel::class)
    abstract fun bindPayWithQrCodeSuccessViewModel(viewModel: PayWithQrCodeSuccessViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccreditedNetworkFilterViewModel::class)
    abstract fun bindAccreditedNetworkFilterViewModelViewModel(viewModel: AccreditedNetworkFilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecoveryPasswordSuccessViewModel::class)
    abstract fun bindRecoveryPasswordSuccessViewModel(viewModel: RecoveryPasswordSuccessViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccreditedNetworkFilterResultsViewModel::class)
    abstract fun bindAccreditedNetworkFilterResultsViewModel(viewModel: AccreditedNetworkFilterResultsViewModel): ViewModel
}