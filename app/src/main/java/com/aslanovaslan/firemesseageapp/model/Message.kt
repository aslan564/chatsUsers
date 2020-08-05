package com.aslanovaslan.firemesseageapp.model

import java.util.*


object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val AUDIO = "AUDIO"
}

interface Message {
    val time: Date
    val gonderenId: String
    val qebuledenId: String
    val gonderenAdi:String
    val mesajinTipi: String
}