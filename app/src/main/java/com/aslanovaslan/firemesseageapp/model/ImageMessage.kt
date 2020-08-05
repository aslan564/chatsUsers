package com.aslanovaslan.firemesseageapp.model

import java.util.*

class ImageMessage(
    val imagePath:String,
    override val time: Date,
    override val gonderenId: String,
    override val qebuledenId: String,
    override val gonderenAdi: String,
    override val mesajinTipi: String = MessageType.IMAGE
):Message {
    constructor() : this("", Date(0), "", "","", "")
}