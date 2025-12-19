package com.example.theseus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform