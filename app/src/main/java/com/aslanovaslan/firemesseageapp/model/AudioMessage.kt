package com.aslanovaslan.firemesseageapp.model

import java.util.*

class AudioMessage(
    val audioPath: String,
    override val time: Date,
    override val gonderenId: String,
    override val qebuledenId: String,
    override val gonderenAdi: String,
    override val mesajinTipi: String = MessageType.AUDIO
) : Message {
    constructor() : this("", Date(0), "", "", "", "")
}