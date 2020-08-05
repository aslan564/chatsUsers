package com.aslanovaslan.firemesseageapp.model

data class ChatChannel(
    val userIds: MutableList<String>
) {
    constructor() : this(mutableListOf())
}