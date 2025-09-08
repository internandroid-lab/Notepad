package com.example.notepad.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.notepad.db.entity.Note

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

    fun exportNoteToUri(context: Context, note: Note, treeUri: Uri): Boolean {
        val resolver = context.contentResolver

        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )

        val fileName = (note.title.ifBlank { "Untitled" } + ".txt")
            .replace("[\\\\/:*?\"<>|]".toRegex(), "_")

        return try {
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
                    return true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportMultipleNotes(context: Context, notes: List<Note>, treeUri: Uri): Pair<List<String>, List<String>> {
        val successList = mutableListOf<String>()
        val failList = mutableListOf<String>()

        for (note in notes) {
            if (exportNoteToUri(context, note, treeUri)) {
                successList.add(note.title)
            } else {
                failList.add(note.title)
            }
        }

        return successList to failList
    }

}