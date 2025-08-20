package com.app.seedlockapp.data.model

import java.util.UUID

/**
 * Merepresentasikan sebuah seed phrase yang disimpan oleh pengguna.
 * Model ini berfungsi sebagai representasi data di level aplikasi untuk setiap
 * seed yang dikelola, yang ditampilkan di UI utama.
 *
 * @property id ID unik untuk setiap seed, biasanya dihasilkan menggunakan [UUID].
 * Ini digunakan sebagai kunci utama untuk mengambil, memperbarui, atau menghapus data terkait.
 * @property name Alias atau nama yang diberikan pengguna untuk mengidentifikasi seed ini dengan mudah.
 * Nama ini ditampilkan kepada pengguna di daftar seed.
 */
data class Seed(
    val id: String,
    val name: String
)