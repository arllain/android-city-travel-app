package br.com.arllain.citytravelapp.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.arllain.citytravelapp.OUTPUT_UNZIP_PATH
import br.com.arllain.citytravelapp.databinding.ItemPacoteBinding
import br.com.arllain.citytravelapp.model.Pacote
import java.io.File

class PacoteAdapter(applicationContext: Context) : ListAdapter<Pacote, PacoteAdapter.MyViewHolder>(MyDiff()) {

    val imagesDirectory = File(applicationContext.filesDir, OUTPUT_UNZIP_PATH)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPacoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pacoteToBind = getItem(position)
        holder.bind(pacoteToBind)
    }

    inner class MyViewHolder(private val binding: ItemPacoteBinding ): RecyclerView.ViewHolder(binding.root){
        fun bind(pacote: Pacote) {
            binding.apply {
                ivPacoteImagem.setImageBitmap(BitmapFactory.decodeFile(imagesDirectory.absolutePath + "/${pacote.imagem}"));
                tvLocal.text = pacote.local
                tvPacoteDias.text = pacote.dias
                tvPacotePreco.text = pacote.preco
            }
        }
    }

    private class MyDiff: DiffUtil.ItemCallback<Pacote>() {
        override fun areItemsTheSame(oldItem: Pacote, newItem: Pacote) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Pacote, newItem: Pacote) = oldItem.local == newItem.local
    }
}