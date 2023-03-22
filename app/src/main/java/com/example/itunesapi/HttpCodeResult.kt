package com.example.itunesapi


enum class HttpCodeResult(val code: Int) {
    SUCCESS(200),
    ERROR(500),
    EMPTY(204)
}
