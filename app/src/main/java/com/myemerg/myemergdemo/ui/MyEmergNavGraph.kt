package com.myemerg.myemergdemo.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.myemerg.myemergdemo.navigate
import com.myemerg.myemergdemo.ui.imageeditor.ImageEditorScreen
import com.myemerg.myemergdemo.ui.imagepreview.ImagePreviewScreen
import com.myemerg.myemergdemo.ui.pdfpreview.PdfPreviewScreen
import com.myemerg.myemergdemo.ui.preparevisit.PrepareVisitScreen
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.URLDecoder
import java.net.URLEncoder

enum class MyEmergDestinations {
    PrepareVisit,
    ImageEditor,
    ImagePreview,
    PdfPreview;

    companion object {
        fun fromRoute(route: String): MyEmergDestinations {
            val argStartIndex = route.indexOf("/")
            val destinationName = if (argStartIndex != -1) route.substring(0, argStartIndex)
            else route
            return MyEmergDestinations.valueOf(destinationName)
        }
    }
}

const val RETURNED_FROM_EDIT_KEY = "RETURNED_FROM_EDIT_KEY"

@Composable
fun MyEmergNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = MyEmergDestinations.PrepareVisit.name,
        modifier = modifier
    ) {
        composable(route = MyEmergDestinations.PrepareVisit.name) {
            val returnedFromEdit by (navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow<Uri?>(RETURNED_FROM_EDIT_KEY, null) ?: MutableStateFlow(null))
                .collectAsState()

            PrepareVisitScreen(
                onImageEditRequested = {
                    navController.navigate(MyEmergDestinations.ImageEditor.name, encodeUri(it))
                },
                onImagePreviewRequested = {
                    navController.navigate(MyEmergDestinations.ImagePreview.name, encodeUri(it))
                },
                onPdfPreviewRequested = {
                    navController.navigate(MyEmergDestinations.PdfPreview.name, encodeString(it))
                },
                returnedFromEdit = returnedFromEdit
            )

            navController.currentBackStackEntry?.savedStateHandle?.remove<Uri?>(RETURNED_FROM_EDIT_KEY)
        }
        composable(
            route = MyEmergDestinations.ImageEditor.name + "/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) {
            BackHandler(true) {}
            ImageEditorScreen(
                imageUri = decodeUri(it.arguments?.getString("uri")!!),
                onResult = {uri ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(RETURNED_FROM_EDIT_KEY, uri)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = MyEmergDestinations.ImagePreview.name + "/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) {
            ImagePreviewScreen(imageUri = decodeUri(it.arguments?.getString("uri")!!))
        }
        composable(
            route = MyEmergDestinations.PdfPreview.name + "/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) {
            PdfPreviewScreen(pdfPath = decodeString(it.arguments?.getString("path")!!))
        }
    }
}

private fun encodeUri(uri: Uri): String = encodeString(uri.toString())
private fun encodeString(str: String): String = URLEncoder.encode(str, "UTF-8")

private fun decodeUri(uri: String): Uri = Uri.parse(decodeString(uri))
private fun decodeString(str: String): String = URLDecoder.decode(str, "UTF-8")