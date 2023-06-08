package com.myemerg.myemergdemo.ui.preparevisit

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.myemerg.myemergdemo.MyEmergFileProvider
import com.myemerg.myemergdemo.R
import com.myemerg.myemergdemo.ui.components.MyEmergDatePickerDialog
import com.myemerg.myemergdemo.ui.components.MyEmergDropdownMenu
import com.myemerg.myemergdemo.ui.components.MyEmergTextField
import com.myemerg.myemergdemo.ui.components.MyEmergTextFieldButton

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrepareVisitScreen(
    prepareVisitViewModel: PrepareVisitViewModel = viewModel(),
    onImageEditRequested: (Uri) -> Unit,
    onImagePreviewRequested: (Uri) -> Unit,
    onPdfPreviewRequested: (String) -> Unit,
    returnedFromEdit: Uri?
) {

    val uiState by prepareVisitViewModel.uiState.collectAsState()

    var openDatePicker by rememberSaveable { mutableStateOf(false) }
    var openAttachImageDialog by rememberSaveable { mutableStateOf(false) }
    var openCamera by rememberSaveable { mutableStateOf(false) }

    prepareVisitViewModel.updateSelectedImages(returnedFromEdit)

    val context = LocalContext.current

    uiState.showPdf?.let {
        onPdfPreviewRequested(it.absolutePath)
        prepareVisitViewModel.pdfShown()
    }

    if (openDatePicker) {
        MyEmergDatePickerDialog(
            onDismissRequest = { openDatePicker = false },
            onSelectedRequest = {
                openDatePicker = false
                prepareVisitViewModel.updateBirthDate(birthDate = it)
            }
        )
    }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            MyEmergFileProvider.getUri(context, "images",".jpg").also {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        out.write(input.readBytes())
                    }
                }
                onImageEditRequested(it)
            }
        }
    }

    val cameraImageUri = MyEmergFileProvider.getUri(context, "images", ".jpg")
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        openCamera = false
        if (it) onImageEditRequested(cameraImageUri)
    }

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    if (openCamera && permissionState.status.isGranted) {
        cameraLauncher.launch(cameraImageUri)
    }

    if (openAttachImageDialog) {
        AttachImageDialog(
            onDismissRequest = { openAttachImageDialog = false },
            onSelected = {
                openAttachImageDialog = false
                when (it) {
                    AttachImageSource.CAMERA -> {
                        permissionState.launchPermissionRequest()
                        openCamera = true
                    }
                    AttachImageSource.GALLERY -> {
                        pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 7.dp, vertical = 2.dp)
            .verticalScroll(rememberScrollState())
            .animateContentSize(),
    ) {
        HeaderItem(text = stringResource(R.string.patient))

        DataItem {
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary,
                    text = stringResource(R.string.the_consultation_will_be_for)
                )
                Switch(
                    checked = uiState.consultationForOtherPerson,
                    onCheckedChange = { prepareVisitViewModel.updateTargetPatient(it) },
                    enabled = true
                )
            }
        }

        HeaderItem(text = stringResource(R.string.question_problem_description))

        DataItem {
            MyEmergTextField(
                value = uiState.question,
                onValueChange = { prepareVisitViewModel.updateQuestion(it) },
                singleLine = false,
                label = stringResource(R.string.question) + " *",
                placeholder = stringResource(R.string.write_the_question),
                isError = uiState.isQuestionError
            )
        }

        HeaderItem(
            text = stringResource(R.string.attached_documents),
            canAttachDocument = true,
            onAttachClick = { openAttachImageDialog = true },
        )

        if (uiState.attachedDocuments.isEmpty()) {
            TextItem(text = stringResource(R.string.if_necessary_attach))
        } else {
            uiState.attachedDocuments.map {
                ImageItem(
                    attachedDocument = it,
                    onDescriptionChange = { newDescription ->
                        prepareVisitViewModel.updateAttachedDocumentDescription(it, newDescription)
                    },
                    onImageClicked = { doc -> onImagePreviewRequested(doc.uri) },
                    onDelete = { doc -> prepareVisitViewModel.deleteAttachedImage(doc) }
                )
            }
        }

        HeaderItem(text = stringResource(R.string.information_about_you))

        TextItem(text = stringResource(R.string.please_provide_information_about_yourself))

        DataItem {
            Column {
                MyEmergTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.name,
                    onValueChange = { prepareVisitViewModel.updateName(it) },
                    label = stringResource(R.string.name) + " *",
                    placeholder = stringResource(R.string.enter_your_name),
                    isError = uiState.isNameError
                )
                MyEmergTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.surname,
                    onValueChange = { prepareVisitViewModel.updateSurname(it) },
                    label = stringResource(R.string.surname) + " *",
                    placeholder = stringResource(R.string.enter_your_surname),
                    isError = uiState.isSurnameError
                )
                MyEmergTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.personalCode,
                    onValueChange = { prepareVisitViewModel.updatePersonalCode(it) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    visualTransformation = {
                        val newAnnotatedString = AnnotatedString(uiState.formattedPersonalCode)
                        val newOffset = object : OffsetMapping {
                            override fun originalToTransformed(offset: Int): Int {
                                return if (offset < 7) offset
                                else offset.inc()
                            }

                            override fun transformedToOriginal(offset: Int): Int {
                                return if (offset < 7) offset
                                else offset.dec()
                            }
                        }

                        TransformedText(
                            text = newAnnotatedString,
                            offsetMapping = newOffset
                        )
                    },
                    label = stringResource(R.string.personal_code) + " *",
                    placeholder = stringResource(R.string.enter_your_personal_code),
                    isError = uiState.isPersonalCodeError
                )
                MyEmergDropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    options = Gender.values(),
                    selectedOption = uiState.gender,
                    optionToTextTransformer = { it.toString() },
                    onSelected = { prepareVisitViewModel.updateGender(it) },
                    label = stringResource(R.string.gender) + " *",
                    isError = uiState.isGenderError
                )
                MyEmergTextFieldButton(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.formattedBirthDate,
                    label = stringResource(R.string.birth_date),
                    onClick = { openDatePicker = true },
                    isError = uiState.isBirthDateError
                )
                MyEmergTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.weight?.toString() ?: "",
                    onValueChange = { prepareVisitViewModel.updateWeight(it) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    label = stringResource(R.string.weight_kg) + " *",
                    placeholder = stringResource(R.string.enter_your_weight),
                    isError = uiState.isWeightError
                )
                MyEmergTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.height?.toString() ?: "",
                    onValueChange = { prepareVisitViewModel.updateHeight(it) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    label = stringResource(R.string.height_cm) + " *",
                    placeholder = stringResource(R.string.enter_your_height),
                    isError = uiState.isHeightError
                )
                MyEmergDropdownMenu(
                    selectedOption = uiState.smoker,
                    options = YesNo.values(),
                    label = stringResource(R.string.smoker) + " *",
                    optionToTextTransformer = { it.toString() },
                    onSelected = { prepareVisitViewModel.updateIsSmoker(it) },
                    isError = uiState.isSmokerError
                )
                MyEmergTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.otherInformation,
                    onValueChange = { prepareVisitViewModel.updateOtherInformation(it) },
                    singleLine = false,
                    label = stringResource(R.string.other_information_your_doctor_should_know),
                    placeholder = stringResource(R.string.for_example_running)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = uiState.agreedToPrivacyPolicy,
                onCheckedChange = { prepareVisitViewModel.updateAgreeToPrivacyPolicy(it) }
            )
            Spacer(modifier = Modifier.width(20.dp))
            PrivacyPolicyText()
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { prepareVisitViewModel.showPdf() },
            enabled = uiState.agreedToPrivacyPolicy,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color(0xff0073F6)
            )
        ) {
            Text(
                text = stringResource(R.string.show_a_pdf)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

}

private enum class AttachImageSource { CAMERA, GALLERY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachImageDialog(
    onDismissRequest: () -> Unit,
    onSelected: (source: AttachImageSource) -> Unit
) {
    AlertDialog(onDismissRequest = onDismissRequest,) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    text = stringResource(R.string.add_photo)
                )
                Divider()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(AttachImageSource.CAMERA) }
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    text = stringResource(R.string.camera)
                )
                Divider()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(AttachImageSource.GALLERY) }
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    text = stringResource(R.string.gallery)
                )
                TextButton(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 16.dp, end = 16.dp),
                    onClick = onDismissRequest
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}


@Composable
private fun PrivacyPolicyText(modifier: Modifier = Modifier) {
    val annotatedString = buildAnnotatedString {
        val str = stringResource(R.string.i_agree_to_the_privacy_policy)
        val startIndex = str.indexOf(stringResource(R.string.privacy_policy))
        val endIndex = startIndex + stringResource(R.string.privacy_policy).length
        append(str)
        addStyle(
            style = SpanStyle(
                color = Color(0xff0073F6),
                textDecoration = TextDecoration.Underline
            ), start = startIndex, end = endIndex
        )
        addStringAnnotation(
            tag = "URL",
            annotation = "https://myemerg.com",
            start = startIndex,
            end = endIndex
        )
    }
    val uriHandler = LocalUriHandler.current
    ClickableText(
        modifier = modifier,
        text = annotatedString,
        onClick = {
            annotatedString
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.let {annotation ->
                    uriHandler.openUri(annotation.item)
                }
        })
}

@Composable
private fun HeaderItem(
    modifier: Modifier = Modifier,
    text: String,
    canAttachDocument: Boolean = false,
    onAttachClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 3.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp)
                    .weight(1f),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                text = text
            )
            if (canAttachDocument) {
                IconButton(onClick = onAttachClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.attach_file)
                    )
                }
            }
        }
    }
}

@Composable
private fun DataItem(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = MaterialTheme.shapes.medium,
        color = color,
        shadowElevation = 3.dp,
    ) {
        Surface(
            modifier = Modifier.padding(10.dp),
            color = color
        ) {
            content()
        }
    }
}

@Composable
private fun TextItem(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
        text = text
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageItem(
    modifier: Modifier = Modifier,
    attachedDocument: AttachedDocument,
    onDescriptionChange: (String) -> Unit,
    onImageClicked: (AttachedDocument) -> Unit,
    onDelete: (AttachedDocument) -> Unit
) {
    val dismissState = DismissState(
        DismissValue.Default,
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart) {
                onDelete(attachedDocument)
            }
            true
        }
    )
    SwipeToDismiss(
        modifier = modifier,
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    modifier = Modifier.scale(scale),
                    contentDescription = stringResource(R.string.delete_icon),
                )
            }
        },
        dismissContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == DismissValue.Default)  MaterialTheme.colorScheme.surface
                else Color.Red
            )

            DataItem(color = color) {
                Row{
                    Image(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Top)
                            .clickable { onImageClicked(attachedDocument) },
                        bitmap = attachedDocument.image,
                        contentDescription = stringResource(R.string.image),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    MyEmergTextField(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        value = attachedDocument.description,
                        onValueChange = onDescriptionChange,
                        showLabel = false,
                        hideUnderline = true,
                        singleLine = false,
                        placeholder = stringResource(R.string.photo)
                    )
                }
            }
        }
    )

}