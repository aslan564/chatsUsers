package com.aslanovaslan.firemesseageapp.model

import java.util.*

data class TextMessage(
    val text: String,
    override val time: Date,
    override val gonderenId: String,
    override val qebuledenId: String,
    override val gonderenAdi: String,
    override val mesajinTipi: String = MessageType.TEXT
) : Message {
    constructor() : this("", Date(0), "","", "")
}