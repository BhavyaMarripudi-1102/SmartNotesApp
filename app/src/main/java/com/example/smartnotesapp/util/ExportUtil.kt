package com.example.smartnotesapp.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.smartnotesapp.data.Note
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.pdf.PdfDocument

fun exportNotesToTxt(context: Context, notes: List<Note>) {
    val fileName = "notes_export_${System.currentTimeMillis()}.txt"
    val fileContents = notes.joinToString("\n\n") { note ->
        "Title: ${note.title}\nContent: ${note.content}\n" +
                (note.reminderTime?.let {
                    "Reminder: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it))}\n"
                } ?: "")
    }
    val file = File(context.cacheDir, fileName)
    file.writeText(fileContents)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Notes"))
}

fun exportNotesToPdf(context: Context, notes: List<Note>) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    var y = 50f
    val paint = android.graphics.Paint()
    paint.textSize = 14f

    notes.forEach { note ->
        canvas.drawText("Title: ${note.title}", 30f, y, paint)
        y += 18f
        canvas.drawText("Content: ${note.content}", 30f, y, paint)
        y += 18f
        note.reminderTime?.let {
            canvas.drawText(
                "Reminder: ${
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it))
                }",
                30f,
                y,
                paint
            )
            y += 18f
        }
        y += 24f
    }

    document.finishPage(page)

    val fileName = "notes_export_${System.currentTimeMillis()}.pdf"
    val file = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(file)
    document.writeTo(outputStream)
    document.close()
    outputStream.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Notes as PDF"))
}