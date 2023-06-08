package com.myemerg.myemergdemo.geometry

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isUnspecified
import com.myemerg.myemergdemo.between

class Line(
    val start: Offset,
    val end: Offset
) {
    val isUnspecified = start.isUnspecified || end.isUnspecified

    val a = (end.y - start.y) / (end.x - start.x)

    val b = (end.x * start.y - start.x * end.y) / (end.x - start.x)

    val isVertical = start.x == end.x

    val x0 = if (isVertical) start.x else Float.NaN

    fun getY(x: Float) = a * x + b

    fun contains(offset: Offset): Boolean =
        if (isUnspecified || offset.isUnspecified) false
        else if (isVertical) offset.x == x0 && offset.y.between(start.y, end.y)
        else
            offset.x.between(start.x, end.x) &&
            offset.y.between(start.y, end.y) &&
            getY(offset.x) == offset.y

    fun intersection(line: Line): Offset? {
        if (isUnspecified || line.isUnspecified) return null
        if (isVertical && line.isVertical) return null

        val x: Float
        val y: Float

        if (isVertical) {
            x = x0
            y = line.getY(x)
        } else if (line.isVertical) {
            x = line.x0
            y = getY(x)
        } else {
            x = (b - line.b) / (line.a - a)
            y = (b * line.a - line.b * a) / (line.a - a)
        }
        val offset = Offset(x, y)
        return if (contains(offset) && line.contains(offset)) offset
        else null
    }

    fun intersection(rect: Rect): Offset? {
        if (isUnspecified) return null

        val leftLine = Line(Offset(rect.left, rect.bottom), Offset(rect.left, rect.top))
        intersection(leftLine)?.let { return it }
        val topLine = Line(Offset(rect.left, rect.top), Offset(rect.right, rect.top))
        intersection(topLine)?.let { return it }
        val rightLine = Line(Offset(rect.right, rect.bottom), Offset(rect.right, rect.top))
        intersection(rightLine)?.let { return it }
        val bottomLine = Line(Offset(rect.left, rect.bottom), Offset(rect.right, rect.bottom))
        intersection(bottomLine)?.let { return it }
        return null
    }

}