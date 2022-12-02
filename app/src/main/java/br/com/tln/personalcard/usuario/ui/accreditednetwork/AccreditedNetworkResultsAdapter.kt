package br.com.tln.personalcard.usuario.ui.accreditednetwork

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.com.tln.personalcard.usuario.databinding.ItemFilterResultsBinding
import br.com.tln.personalcard.usuario.model.AccreditedNetwork

class AccreditedNetworkResultsAdapter(
    private val clickListener: (AccreditedNetwork) -> Unit
) :
    PagedListAdapter<AccreditedNetwork, AccreditedNetworkResultsAdapter.ViewHolder>(
        DIFF_CONFIG
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFilterResultsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.configure(it) }
    }

    inner class ViewHolder(val binding: ItemFilterResultsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var item: AccreditedNetwork

        init {
            binding.root.setOnClickListener {
                clickListener(this.item)
            }
        }

        fun configure(accreditedNetwork: AccreditedNetwork) {
            this.item = accreditedNetwork
            binding.accreditedNetwork = accreditedNetwork

            binding.divider.visibility = if (adapterPosition == itemCount - 1) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    companion object {
        val DIFF_CONFIG = AsyncDifferConfig.Builder(
            object : DiffUtil.ItemCallback<AccreditedNetwork>() {
                override fun areItemsTheSame(
                    oldItem: AccreditedNetwork,
                    newItem: AccreditedNetwork
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: AccreditedNetwork,
                    newItem: AccreditedNetwork
                ): Boolean {
                    return oldItem == newItem
                }
            }
        ).build()
    }
}