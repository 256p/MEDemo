package com.myemerg.myemergdemo.ui.imageeditor

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.myemerg.myemergdemo.ImageUtils
import com.myemerg.myemergdemo.R
import com.smarttoolfactory.gesture.pointerMotionEvents
import com.smarttoolfactory.image.ImageWithConstraints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MotionEvent {
    Idle, Down, Move, Up
}

@Composable
fun ImageEditorScreen(
    imageUri: Uri,
    onResult: (Uri) -> Unit
) {

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val paths = remember { mutableStateListOf<Pair<Path, PathProperties>>() }
    var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }
    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    var previousPosition by remember { mutableStateOf(Offset.Unspecified) }
    var currentPath by remember { mutableStateOf(Path()) }
    var currentPathProperty by remember { mutableStateOf(PathProperties()) }
    var onDonePressed by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycle = LocalLifecycleOwner.current
    val density = LocalDensity.current
    var canvasWidth = 0
    var canvasHeight = 0

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        imageBitmap?.let { bitmap ->
            ImageWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                imageBitmap = bitmap
            ) {
                with(density) {
                    canvasWidth = imageWidth.roundToPx()
                    canvasHeight = imageHeight.roundToPx()
                }
                Canvas(
                    modifier = Modifier
                        .requiredWidth(imageWidth)
                        .requiredHeight(imageHeight)
                        .clipToBounds()
                        .pointerMotionEvents(
                            onDown = {
                                if (onDonePressed) return@pointerMotionEvents
                                motionEvent = MotionEvent.Down
                                currentPosition = it.position
                                it.consume()
                            },
                            onMove = {
                                if (onDonePressed) return@pointerMotionEvents
                                motionEvent = MotionEvent.Move
                                currentPosition = it.position
                                it.consume()
                            },
                            onUp = {
                                if (onDonePressed) return@pointerMotionEvents
                                motionEvent = MotionEvent.Up
                                it.consume()
                            }
                        )
                ) {
                    when (motionEvent) {
                        MotionEvent.Down -> {
                            if (currentPosition.isSpecified) {
                                Log.i("12345", "Down curr $currentPosition")
                                currentPath.moveTo(currentPosition.x, currentPosition.y)
                                previousPosition = currentPosition
                            }
                        }
                        MotionEvent.Move -> {
                            if (previousPosition.isSpecified && currentPosition.isSpecified) {
                                Log.i("12345", "Move curr $currentPosition")
                                Log.i("12345", "Move prev $previousPosition")
                                currentPath.quadraticBezierTo(
                                    previousPosition.x,
                                    previousPosition.y,
                                    (previousPosition.x + currentPosition.x) / 2,
                                    (previousPosition.y + currentPosition.y) / 2
                                )
                            } else if (currentPosition.isSpecified) {
                                currentPath.moveTo(currentPosition.x, currentPosition.y)
                            }
                            if (currentPosition.isSpecified) previousPosition = currentPosition
                        }
                        MotionEvent.Up -> {
                            if (currentPosition.isSpecified) {
                                Log.i("12345", "Up curr $currentPosition")
                                currentPath.lineTo(currentPosition.x, currentPosition.y)
                                paths.add(Pair(currentPath, currentPathProperty))

                                currentPath = Path()
                                currentPathProperty = currentPathProperty.copy()
                                currentPosition = Offset.Unspecified
                                previousPosition = Offset.Unspecified
                                motionEvent = MotionEvent.Idle
                            }
                        }
                        MotionEvent.Idle -> {}
                    }

                    paths.forEach {
                        val path = it.first
                        val property = it.second
                        drawPath(
                            color = property.color,
                            path = path,
                            style = Stroke(
                                width = property.strokeWidth,
                                cap = property.strokeCap,
                                join = property.strokeJoin
                            )
                        )
                    }

                    if (motionEvent != MotionEvent.Idle) {
                        drawPath(
                            color = currentPathProperty.color,
                            path = currentPath,
                            style = Stroke(
                                width = currentPathProperty.strokeWidth,
                                cap = currentPathProperty.strokeCap,
                                join = currentPathProperty.strokeJoin
                            )
                        )
                    }
                }
            }
        } ?: Spacer(
            Modifier
                .fillMaxSize()
                .weight(1f))

        ColorPickerRow(
            onSelectedColor = { currentPathProperty = currentPathProperty.copy(color = it) },
            currentColor = currentPathProperty.color
        )

        MenuRow(
            isUndoEnabled = !onDonePressed && paths.isNotEmpty(),
            onClearClick = { paths.clear() },
            onUndoClick = { paths.apply { remove(last()) } },
            onDoneClick = onDone@{
                if (onDonePressed || canvasWidth == 0 || canvasHeight == 0) return@onDone
                onDonePressed = true

                lifecycle.lifecycleScope.launch(Dispatchers.IO) {
                    val smallBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                    val smallCanvas = Canvas(smallBitmap)
                    paths.forEach {
                        val property = it.second
                        val paint = Paint().apply {
                            color = property.color.toArgb()
                            style = Paint.Style.STROKE
                            strokeWidth = property.strokeWidth
                            strokeCap =
                                Paint.Cap.valueOf(property.strokeCap.toString().uppercase())
                            strokeJoin =
                                Paint.Join.valueOf(property.strokeJoin.toString().uppercase())
                        }
                        smallCanvas.drawPath(it.first.asAndroidPath(), paint)
                    }

                    val originalBitmap = BitmapFactory.Options().run {
                        inMutable = true
                        //todo null error handling
                        context.contentResolver.openInputStream(imageUri)!!.use {
                            BitmapFactory.decodeStream(it, null, this)!!
                        }
                    }
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        smallBitmap,
                        originalBitmap.width,
                        originalBitmap.height,
                        false
                    )
                    smallBitmap.recycle()

                    val origCanvas = Canvas(originalBitmap)
                    origCanvas.drawBitmap(scaledBitmap, Matrix(), null)
                    context.contentResolver.openOutputStream(imageUri)?.use {
                        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    originalBitmap.recycle()
                    scaledBitmap.recycle()
                    withContext(Dispatchers.Main) {
                        onResult(imageUri)
                    }
                }
            }
        )
    }
    if (onDonePressed) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x77000000))
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun MenuRow(
    modifier: Modifier = Modifier,
    isUndoEnabled: Boolean,
    onClearClick: () -> Unit,
    onUndoClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        TextButton(
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            enabled = isUndoEnabled,
            onClick = onClearClick
        ) {
            Text(stringResource(R.string.clear))
        }

        IconButton(
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White
            ),
            onClick = onUndoClick,
            enabled = isUndoEnabled
        ) {
            Icon(
                Icons.Filled.Undo,
                contentDescription = stringResource(R.string.undo)
            )
        }

        OutlinedButton(onClick = onDoneClick) {
            Text(stringResource(R.string.done))
        }

    }
}

@Composable
fun ColorPickerRow(
    modifier: Modifier = Modifier,
    onSelectedColor: (Color) -> Unit,
    currentColor: Color
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ColorPickerButton(
            onPick = { onSelectedColor(it) },
            color = Color.Black,
            currentColor = currentColor
        )

        ColorPickerButton(
            onPick = { onSelectedColor(it) },
            color = Color.Red,
            currentColor = currentColor
        )

        ColorPickerButton(
            onPick = { onSelectedColor(it) },
            color = Color.Yellow,
            currentColor = currentColor
        )

        ColorPickerButton(
            onPick = { onSelectedColor(it) },
            color = Color.Green,
            currentColor = currentColor
        )

        ColorPickerButton(
            onPick = { onSelectedColor(it) },
            color = Color.Blue,
            currentColor = currentColor
        )

        ColorPickerButton(
            onPick = { onSelectedColor(it) },
            color = Color.Magenta,
            currentColor = currentColor
        )

        ColorPickerButton(
            onPick = { onSelectedColor(it) },
            color = Color.White,
            currentColor = currentColor
        )
    }
}

@Composable
fun ColorPickerButton(
    modifier: Modifier = Modifier,
    onPick: (Color) -> Unit,
    color: Color,
    currentColor: Color
) {
    val border = if (color == Color.Black) BorderStroke(1.dp, Color.White)
    else BorderStroke(0.dp, Color.Transparent)

    val size by animateDpAsState(
        animationSpec = tween(
            durationMillis = 100
        ),
        targetValue = if (currentColor == color) 30.dp else 20.dp,
    )
    Button(
        border = border,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(50),
        modifier = modifier.size(size),
        onClick = { onPick(color) }
    ) {}
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}