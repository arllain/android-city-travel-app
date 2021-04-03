

@file:JvmName("Constants")

package br.com.arllain.citytravelapp

@JvmField val NOTIFICATION_CHANNEL_NAME: CharSequence =
        "WorkManager Notifications"
const val NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1

const val OUTPUT_PATH = "downloads"

const val OUTPUT_UNZIP_PATH = "downloads/cidades/"

const val DELAY_TIME_MILLIS: Long = 3000

const val FILE_URL_DOWNLOAD = "https://github.com/haldny/imagens/raw/main/cidades.zip"

const val JSON_URL_DOWNLOAD = "https://github.com/haldny/imagens/raw/main/pacotes.json"
