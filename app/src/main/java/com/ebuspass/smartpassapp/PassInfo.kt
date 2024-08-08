package com.ebuspass.smartpassapp
import java.text.SimpleDateFormat
import java.util.Date
data class PassInfo(
    val name: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val age: String? = null,
    val selectedPass: String? = null,
    val imageUrl: String? = null,
    val adhaarCardNumber: String? = null,
    val createTimeMillis: Long? = null,
) {
    fun getFormattedTime(): String {
        return createTimeMillis?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")
            val date = Date(it)
            sdf.format(date)
        } ?: ""
    }
}
