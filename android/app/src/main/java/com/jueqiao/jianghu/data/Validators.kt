package com.jueqiao.jianghu.data

/**
 * Validation utilities — mirrors utils/validators.ts.
 */
object Validators {
    private val phoneRegex = Regex("^1[3-9]\\d{9}$")
    private val codeRegex  = Regex("^\\d{6}$")

    fun isPhone(raw: String): Boolean = phoneRegex.matches(raw.trim())
    fun isCode(raw: String): Boolean  = codeRegex.matches(raw.trim())

    /** Passwords are passed through exactly as entered; never trim secret values. */
    fun isPassword(raw: String): Boolean = raw.length in 8..64
}
