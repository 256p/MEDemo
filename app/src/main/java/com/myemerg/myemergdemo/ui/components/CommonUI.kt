@file:OptIn(ExperimentalMaterial3Api::class)

package com.myemerg.myemergdemo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import com.myemerg.myemergdemo.R
import com.myemerg.myemergdemo.geometry.nearestPointInside
import kotlinx.coroutines.launch

@Composable
fun myEmergTextFieldColors(
    focusedTextColor: Color = Color.Black,
    unfocusedTextColor: Color = Color.Black,
    disabledTextColor: Color = Color.Black,
    focusedLabelColor: Color = MaterialTheme.colorScheme.secondary,
    unfocusedLabelColor: Color = MaterialTheme.colorScheme.secondary,
    disabledLabelColor: Color = MaterialTheme.colorScheme.secondary,
    focusedIndicatorColor: Color = MaterialTheme.colorScheme.secondary,
    unfocusedIndicatorColor: Color = Color.Black,
    disabledIndicatorColor: Color = Color.Black,
    errorIndicatorColor: Color = MaterialTheme.colorScheme.error,
    focusedContainerColor: Color = Color.Transparent,
    unfocusedContainerColor: Color = Color.Transparent,
    disabledContainerColor: Color = Color.Transparent,
    errorContainerColor: Color = Color.Transparent,
): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedTextColor = focusedTextColor,
        unfocusedTextColor = unfocusedTextColor,
        disabledTextColor = disabledTextColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = disabledLabelColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor,
        disabledIndicatorColor = disabledIndicatorColor,
        errorIndicatorColor = errorIndicatorColor,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        errorContainerColor = errorContainerColor,
    )
}

@Composable
fun hideUnderlineColors(): TextFieldColors {
    return myEmergTextFieldColors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent
    )
}

@Composable
fun forceErrorColors(): TextFieldColors {
    return myEmergTextFieldColors(
        disabledIndicatorColor = MaterialTheme.colorScheme.error,
        disabledLabelColor = MaterialTheme.colorScheme.error
    )
}

@Composable
fun MyEmergTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    showLabel: Boolean = true,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    hideUnderline: Boolean = false,
    isError: Boolean = false,
    colors: TextFieldColors = if (hideUnderline) hideUnderlineColors() else myEmergTextFieldColors(),
) {
    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = if (showLabel) { { Text(label) } } else null,
        colors = colors,
        placeholder = {
            Text(
                color = MaterialTheme.colorScheme.onSurface,
                text = placeholder
            )
        },
        singleLine = singleLine,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        isError = isError
    )
}

@Composable
fun MyEmergDatePickerDialog(
    onDismissRequest: () -> Unit,
    onSelectedRequest: (selectedDateMillis: Long) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { onSelectedRequest(datePickerState.selectedDateMillis!!) },
                enabled = confirmEnabled
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun MyEmergTextFieldButton(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    MyEmergTextField(
        modifier = modifier.clickable { onClick() },
        value = value,
        onValueChange = {},
        colors = if (isError) forceErrorColors() else myEmergTextFieldColors(),
        label = label,
        readOnly = true,
        enabled = false,
        isError = isError
    )
}

@Composable
fun <T: Enum<T>> MyEmergDropdownMenu(
    modifier: Modifier = Modifier,
    selectedOption: T?,
    options: Array<T>,
    label: String,
    optionToTextTransformer: @Composable (option: T) -> String,
    onSelected: (option: T) -> Unit,
    isError: Boolean = false
) {
    data class OptionText(val option: T?, val text: String)

    val internalOptions = listOf(OptionText(null, "")) + options.map { OptionText(it, optionToTextTransformer(it)) }
    var expanded by remember { mutableStateOf(false) }
    var internalSelectedOption by remember { mutableStateOf(internalOptions.first { it.option == selectedOption }) }


    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        MyEmergTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = internalSelectedOption.text,
            onValueChange = {},
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            internalOptions.filter { it.option != null }.forEach { selected ->
                DropdownMenuItem(
                    text = { Text(selected.text) },
                    onClick = {
                        internalSelectedOption = selected
                        onSelected(selected.option!!)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}


@Composable
fun ZoomableLazyColumn(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    var containerWidth by remember { mutableStateOf(0) }
    var containerHeight by remember { mutableStateOf(0) }
    var scroll by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        userScrollEnabled = false,
        modifier = modifier
            .onGloballyPositioned {
                containerWidth = it.size.width
                containerHeight = it.size.height
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, gestureZoom, gestureRotate ->
                    val oldScale = zoom
                    val newScale = nearestInRange(1f..3f, zoom * gestureZoom)
                    val allowedWidth = containerWidth - containerWidth / newScale
                    val allowedHeight = containerHeight - containerHeight / newScale
                    val allowedOffsetRect = Rect(Offset.Zero, Size(allowedWidth, allowedHeight))
                    val newOffset =
                        offset + centroid / oldScale - (centroid / newScale + pan / oldScale)
                    offset = if (allowedOffsetRect.contains(newOffset)) newOffset
                    else allowedOffsetRect.nearestPointInside(newOffset)
                    scroll = newOffset.y - offset.y
//                    val scrollAnim = animateFloatAsState(targetValue = )
                    coroutineScope.launch {
//                        listState.animateScrollBy()
                        listState.scrollBy(scroll)
                    }
                    zoom = newScale

                }
            }
            .graphicsLayer {
                translationX = -offset.x * zoom
                translationY = -offset.y * zoom
                scaleX = zoom
                scaleY = zoom
                transformOrigin = TransformOrigin(0f, 0f)

            },
        content = content
    )
}

private fun nearestInRange(range: ClosedFloatingPointRange<Float>, targetVal: Float): Float =
    maxOf(range.start, minOf(range.endInclusive, targetVal))