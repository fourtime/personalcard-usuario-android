package br.com.tln.personalcard.usuario.ui.home

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import br.com.tln.personalcard.usuario.entity.Card

class CardAdapter(
    fragmentManager: FragmentManager,
    val cards: List<Card>
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount() = cards.size

    override fun getItem(position: Int) = HomeCardFragment.newInstance(position, cards[position])
}