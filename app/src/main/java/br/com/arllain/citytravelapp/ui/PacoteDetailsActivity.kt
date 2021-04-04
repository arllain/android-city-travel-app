package br.com.arllain.citytravelapp.ui

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.arllain.citytravelapp.OUTPUT_UNZIP_PATH
import br.com.arllain.citytravelapp.databinding.ActivityPacoteDetailsBinding
import br.com.arllain.citytravelapp.model.Pacote
import java.io.File

class PacoteDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPacoteDetailsBinding
    private var pacote: Pacote? = null

    companion object {
        const val PACOTE_DETAILS_ACTIVITY_EXTRA_ID = "pacote"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPacoteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUi()
    }

    private fun initUi() {
        pacote = intent.getParcelableExtra(MainActivity.MAIN_ACTIVITY_PACOTE_EXTRA_ID)
        supportActionBar?.title = pacote?.local
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.apply {
            ivPacoteImagem.setImageBitmap(BitmapFactory.decodeFile(File(applicationContext.filesDir, OUTPUT_UNZIP_PATH).absoluteFile.toString() + "/${pacote?.imagem}"));
            tvPacoteDias.text = pacote?.dias
            tvPacotePreco.text = pacote?.preco
        }


        binding.cardView.setOnClickListener {
            openMap()
        }

        binding.btMaps.setOnClickListener {
            openMap()
        }
    }

    private fun openMap() {
        val mapsItent = Intent(this, MapsActivity::class.java)
        mapsItent.putExtra(PACOTE_DETAILS_ACTIVITY_EXTRA_ID, pacote)
        startActivity(mapsItent)
    }
}