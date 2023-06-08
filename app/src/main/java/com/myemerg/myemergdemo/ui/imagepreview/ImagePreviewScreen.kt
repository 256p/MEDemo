package com.myemerg.myemergdemo.ui.imagepreview

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myemerg.myemergdemo.ImageUtils
import com.myemerg.myemergdemo.R
import com.smarttoolfactory.image.zoom.AnimatedZoomLayout

@Composable
fun ImagePreviewScreen(imageUri: Uri) {

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    Surface(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
    ) {
        if (imageBitmap == null) {
            LaunchedEffect(Unit) {
                with(density) {
                    imageBitmap = ImageUtils.uriToImageBitmap(
                        context,
                        imageUri,
                        configuration.screenWidthDp.dp.roundToPx(),
                        configuration.screenHeightDp.dp.roundToPx()
                    )
                }
            }
        }

        imageBitmap?.let { bitmap ->
            AnimatedZoomLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
               Image(
                   bitmap = bitmap,
                   contentDescription = stringResource(id = R.string.image)
               )
            }
        }
    }

}

