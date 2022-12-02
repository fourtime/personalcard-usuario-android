package br.com.tln.personalcard.usuario.ui

import android.os.Bundle
import androidx.navigation.findNavController
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()
}