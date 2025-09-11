package com.example.notepad.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Parcel
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.notepad.db.entity.Note
import org.xml.sax.XMLReader

object AppUtil {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupKeyboardHiderForAllViews(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val focusedView = v.rootView.findFocus()
                    if (focusedView is EditText) {
                        focusedView.clearFocus()
                        hideKeyboard(focusedView)
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupKeyboardHiderForAllViews(view.getChildAt(i))
            }
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    fun readTextFileFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                it.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportNote(context: Context, note: Note, treeUri: Uri) {
        val resolver = context.contentResolver

        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )

        val fileName = (note.title.ifBlank { "Untitled" } + ".txt")
            .replace("[\\\\/:*?\"<>|]".toRegex(), "_")

        try {
            val fileUri = DocumentsContract.createDocument(
                resolver,
                docUri,
                "text/plain",
                fileName
            )

            if (fileUri != null) {
                resolver.openOutputStream(fileUri)?.use { outputStream ->
                    val content = note.content ?: ""
                    outputStream.write(content.toByteArray())
                    outputStream.flush()
                }
            }
            false
        } catch (e: Exception) {
        }
    }

    fun exportMultipleNotes(context: Context, notes: List<Note>, treeUri: Uri){
        for (note in notes) {
            exportNote(context, note, treeUri)
        }
    }

}

fun Spannable.toBase64(): String {
    val parcel = Parcel.obtain()
    TextUtils.writeToParcel(this, parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun String.toSpannable(): Spannable {
    val bytes = Base64.decode(this, Base64.DEFAULT)
    val parcel = Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)
    val spanned = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel)
    parcel.recycle()
    return spanned as? Spannable ?: SpannableString(this)
}

enum class SortType {
    BY_DATE, BY_TITLE
}

data class TextStyle(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val bgColor: Int? = null,
    val textColor: Int? = null,
    val size: Int = 20
)