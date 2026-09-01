package com.jueqiao.jianghu.ui.screens.settings

import android.content.Context

/** Lightweight offline draft storage for the existing creation flow. */
class CreationDraftStore(context: Context) {
    private val appContext = context.applicationContext
    private val drafts = appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val settings = SettingsPreferences(appContext)

    fun read(key: String): String = drafts.getString(key, "").orEmpty()

    fun saveIfEnabled(key: String, value: String) {
        if (!settings.getBoolean(SettingsPreferences.Keys.AutoSave, true)) return
        drafts.edit().putString(key, value).apply()
    }

    fun clear(key: String) {
        drafts.edit().remove(key).apply()
    }

    object Keys {
        const val ImagePrompt = "image_prompt"
        const val PicturePrompt = "picture_prompt"
        const val ElementPrompt = "element_prompt"
    }

    private companion object {
        const val FILE_NAME = "jianghu_creation_drafts"
    }
}
