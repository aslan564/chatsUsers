package com.aslanovaslan.firemesseageapp.model

class UserModel(
    val name: String,
    val bio: String,
    val profilePicturePath: String?,
    val registrationTokens:String,
    val oneSignalId:String?=null
) {
    constructor() : this("", "", null,"")
}