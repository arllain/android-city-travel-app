package br.com.arllain.citytravelapp.model

import com.google.gson.annotations.SerializedName

data class Travel(
        @SerializedName("pacotes")
        var pacoteList: List<Pacote>,
        )


