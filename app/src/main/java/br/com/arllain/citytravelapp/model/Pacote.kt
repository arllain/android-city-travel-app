package br.com.arllain.citytravelapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pacote(
    val local: String,
    val imagem: String,
    val dias: String,
    val preco: String) : Parcelable


