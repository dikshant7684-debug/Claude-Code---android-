package com.example.ui.formatting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

object ClipboardHelper {

  fun copyToClipboard(context: Context, label: String, content: String): Boolean {
    return try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return false
      // Exact text preservation without modifying internal whitespace
      val clip = ClipData.newPlainText(label, content)
      clipboard.setPrimaryClip(clip)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun getPrimaryClipText(context: Context): String? {
    return try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      val item = clipboard?.primaryClip?.getItemAt(0)
      item?.text?.toString()
    } catch (_: Exception) {
      null
    }
  }
}
