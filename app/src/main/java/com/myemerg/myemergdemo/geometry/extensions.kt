package com.myemerg.myemergdemo.geometry

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun Offset.length(offset: Offset): Float = sqrt(abs(x - offset.x).pow(2) + abs(y - offset.y).pow(2))

fun Rect.nearestPointInside(offset: Offset): Offset {
    listOf(
        (bottomLeft to topLeft),
        (topLeft to topRight),
        (topRight to bottomRight),
        (bottomRight to bottomLeft)
    )
        .mapNotNull { findNormalPoint(offset, it.first, it.second) }
        .minByOrNull { it.length(offset) }
        ?.let { return it }

    return listOf(topLeft, topRight, bottomRight, bottomLeft)
        .minByOrNull { it.length(offset) } ?: Offset.Unspecified
}

private fun findNormalPoint(offset: Offset, vertex1: Offset, vertex2: Offset): Offset? {
    val a = ((vertex2.x - vertex1.x) * (offset.x - vertex1.x) + (vertex2.y - vertex1.y) * (offset.y - vertex1.y)) /
            ((vertex2.x - vertex1.x).pow(2) + (vertex2.y - vertex1.y).pow(2))
    if (a !in 0f..1f) return null

    val x = vertex1.x + (vertex2.x - vertex1.x) * a
    val y = vertex1.y + (vertex2.y - vertex1.y) * a
    return Offset(x, y)
}