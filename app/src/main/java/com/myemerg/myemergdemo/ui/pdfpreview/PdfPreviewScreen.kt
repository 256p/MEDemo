package com.myemerg.myemergdemo.ui.pdfpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.rajat.pdfviewer.PdfQuality
import com.rajat.pdfviewer.PdfRendererView
import java.io.File

@Composable
fun PdfPreviewScreen(pdfPath: String) {

    AndroidView(
        modifier = Modifier.fillMaxSize().background(Color(0xFFA8A8A8)),
        factory = { PdfRendererView(it) },
        update = { it.initWithFile(File(pdfPath), PdfQuality.NORMAL) }
    )

}