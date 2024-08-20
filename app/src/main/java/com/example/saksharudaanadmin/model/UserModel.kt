package com.example.saksharudaanadmin.model

data class UserModel(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val imageUri: String? = "",
    val profileHeadline: String? = null,
    val gender: String? = null,
    val phone: String? = null,
    val state: String? = null,
)
