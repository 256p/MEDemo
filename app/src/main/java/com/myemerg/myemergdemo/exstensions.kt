package com.myemerg.myemergdemo

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavHostController
import com.myemerg.myemergdemo.ui.preparevisit.Gender
import kotlin.math.roundToInt

fun Dp.roundToPx(): Int = (value * Resources.getSystem().displayMetrics.density).roundToInt()

fun Float.between(val1: Float, val2: Float): Boolean = this in if (val1 > val2) val2..val1 else val1..val2

fun AndroidViewModel.stringResource(@StringRes res: Int): String = getApplication<Application>().getString(res)

fun NavHostController.navigate(route: String, vararg arguments: String) = navigate(route + "/" + arguments.joinToString("/"))