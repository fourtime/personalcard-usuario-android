package br.com.tln.personalcard.usuario.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import br.com.tln.personalcard.usuario.di.Injectable
import br.com.tln.personalcard.usuario.extensions.observe
import br.com.tln.personalcard.usuario.extensions.observeEvent

abstract class SessionRequiredBaseFragment<DB : ViewDataBinding, NV : SessionRequiredNavigator, VM : SessionRequiredBaseViewModel<in NV>>(
    @LayoutRes layoutRes: Int,
    viewModelClass: Class<VM>,
    retainViewInstance: Boolean = false
) : BaseFragment<DB, NV, VM>(
    layoutRes = layoutRes,
    viewModelClass = viewModelClass,
    retainViewInstance = retainViewInstance
), Injectable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.sessionMonitorLiveData
            .observe(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        viewModel.authenticationErrorLiveData.observeEvent(viewLifecycleOwner) {
            viewModel.lockSession()
        }

        return view
    }
}