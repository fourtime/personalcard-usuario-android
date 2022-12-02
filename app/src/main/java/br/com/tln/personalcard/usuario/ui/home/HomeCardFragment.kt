package br.com.tln.personalcard.usuario.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import br.com.tln.personalcard.usuario.R
import br.com.tln.personalcard.usuario.core.BaseFragment
import br.com.tln.personalcard.usuario.databinding.FragmentHomeCardBinding
import br.com.tln.personalcard.usuario.entity.Card

class HomeCardFragment :
    BaseFragment<FragmentHomeCardBinding, HomeCardNavigator, HomeCardViewModel>(
        R.layout.fragment_home_card,
        HomeCardViewModel::class.java,
        retainViewInstance = false
    ), HomeCardNavigator {

    interface OnCardClickListener {
        fun onCardClicked(cardIndex: Int)
    }

    lateinit var onCardClickListener: OnCardClickListener

    private var cardIndex: Int = -1
    private lateinit var cardId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardId = arguments?.getString(ARG_CARD_ID) ?: run {
            if (arguments == null) {
                throw IllegalStateException("Arguments must not be null")
            } else {
                throw IllegalStateException("CardId must not be null")
            }
        }

        cardIndex = arguments?.getInt(ARG_CARD_INDEX, 0) ?: run {
            if (arguments == null) {
                throw IllegalStateException("Arguments must not be null")
            } else {
                throw IllegalStateException("CardId must not be null")
            }
        }

        viewModel.setCard(cardId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireBinding().root.setOnClickListener {
            onCardClickListener.onCardClicked(cardIndex)
        }
    }

    companion object {

        const val ARG_CARD_ID: String = "cardid"
        const val ARG_CARD_INDEX: String = "cardindex"

        fun newInstance(index: Int, card: Card): HomeCardFragment {
            val args = bundleOf(
                ARG_CARD_ID to card.id,
                ARG_CARD_INDEX to index
            )

            val fragment = HomeCardFragment()
            fragment.arguments = args

            return fragment
        }
    }
}