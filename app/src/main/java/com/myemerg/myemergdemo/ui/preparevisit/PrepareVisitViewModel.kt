package com.myemerg.myemergdemo.ui.preparevisit

import android.app.Application
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myemerg.myemergdemo.ImageUtils
import com.myemerg.myemergdemo.MyEmergFileProvider
import com.myemerg.myemergdemo.R
import com.myemerg.myemergdemo.roundToPx
import com.myemerg.myemergdemo.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.Locale

enum class Gender {
    MALE, FEMALE, OTHER;

    override fun toString(): String = when (this) {
        MALE -> stringResource(R.string.male)
        FEMALE -> stringResource(R.string.female)
        OTHER -> stringResource(R.string.other)
    }
}

enum class YesNo {
    YES, NO;

    override fun toString(): String = when (this) {
        YES -> stringResource(R.string.yes)
        NO -> stringResource(R.string.no)
    }
}

data class AttachedDocument(
    var uri: Uri,
    var image: ImageBitmap,
    var description: String
)

data class PrepareVisitUiState(
    val consultationForOtherPerson: Boolean = false,
    val attachedDocuments: List<AttachedDocument> = listOf(),
    val agreedToPrivacyPolicy: Boolean = false,
    val question: String = "",
    val isQuestionError: Boolean = false,
    val name: String = "",
    val isNameError: Boolean = false,
    val surname: String = "",
    val isSurnameError: Boolean = false,
    val personalCode: String = "",
    val formattedPersonalCode: String = "",
    val isPersonalCodeError: Boolean = false,
    val gender: Gender? = null,
    val isGenderError: Boolean = false,
    val birthDate: Long? = null,
    val formattedBirthDate: String = "",
    val isBirthDateError: Boolean = false,
    val weight: Int? = null,
    val isWeightError: Boolean = false,
    val height: Int? = null,
    val isHeightError: Boolean = false,
    val smoker: YesNo? = null,
    val isSmokerError: Boolean = false,
    val otherInformation: String = "",
    val showPdf: File? = null
)

class PrepareVisitViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PrepareVisitUiState())
    val uiState: StateFlow<PrepareVisitUiState> = _uiState.asStateFlow()

    private val imageUris = mutableListOf<Uri>()

    fun updateTargetPatient(consultationForOtherPerson: Boolean) {
        _uiState.update {
            it.copy(consultationForOtherPerson = consultationForOtherPerson)
        }
    }

    fun updateSelectedImages(uri: Uri?) {
        uri ?: return
        if (imageUris.contains(uri)) return
        imageUris.add(uri)

        viewModelScope.launch {
            val bitmap = ImageUtils.uriToImageBitmap(getApplication(), uri, 50.dp.roundToPx(), 50.dp.roundToPx()) ?: return@launch
            _uiState.update {
                it.copy(attachedDocuments = it.attachedDocuments + AttachedDocument(uri, bitmap, ""))
            }
        }
    }

    fun updateAgreeToPrivacyPolicy(agreed: Boolean) {
        _uiState.update {
            it.copy(agreedToPrivacyPolicy = agreed)
        }
    }

    fun updateQuestion(question: String) {
        if (question.length > 500) return
        _uiState.update {
            it.copy(question = question, isQuestionError = false)
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(name = name, isNameError = false)
        }
    }

    fun updateSurname(surname: String) {
        _uiState.update {
            it.copy(surname = surname, isSurnameError = false)
        }
    }

    fun updatePersonalCode(personalCode: String) {
        if (personalCode.length > 11 || !personalCode.isDigitsOnly()) return
        _uiState.update {
            it.copy(
                personalCode = personalCode,
                formattedPersonalCode = formatPersonalCode(personalCode),
                isPersonalCodeError = false,
            )
        }
    }

    fun updateGender(gender: Gender) {
        _uiState.update {
            it.copy(gender = gender, isGenderError = false)
        }
    }

    fun updateBirthDate(birthDate: Long) {
        _uiState.update {
            it.copy(
                birthDate = birthDate,
                formattedBirthDate = formatDate(birthDate),
                isBirthDateError = false
            )
        }
    }

    fun updateWeight(weight: String) {
        if (!weight.isDigitsOnly()) return
        _uiState.update {
            it.copy(weight = weight.toIntOrNull(), isWeightError = false)
        }
    }

    fun updateHeight(height: String) {
        if (!height.isDigitsOnly()) return
        _uiState.update {
            it.copy(height = height.toIntOrNull(), isHeightError = false)
        }
    }

    fun updateIsSmoker(isSmoker: YesNo) {
        _uiState.update {
            it.copy(smoker = isSmoker, isSmokerError = false)
        }
    }

    fun updateOtherInformation(otherInformation: String) {
        _uiState.update {
            it.copy(otherInformation = otherInformation)
        }
    }

    fun updateAttachedDocumentDescription(attachedDocument: AttachedDocument, description: String) {
        _uiState.update {
            val newList = it.attachedDocuments.toMutableList().apply {
                replaceAll { doc ->
                    if (doc == attachedDocument) attachedDocument.copy(description = description)
                    else doc
                }
            }
            it.copy(attachedDocuments = newList)
        }
    }

    fun deleteAttachedImage(attachedDocument: AttachedDocument) {
        imageUris.remove(attachedDocument.uri)
        File(attachedDocument.uri.toString()).delete()
        _uiState.update {
            val newList = it.attachedDocuments.toMutableList().apply { remove(attachedDocument) }
            it.copy(attachedDocuments = newList)
        }
    }

    fun showPdf() {
        if (areFieldsValid(uiState.value)) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(showPdf = createPdf(it))
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    isQuestionError = it.question.isBlank(),
                    isNameError = it.name.isBlank(),
                    isSurnameError = it.surname.isBlank(),
                    isPersonalCodeError = it.personalCode.isBlank(),
                    isGenderError = it.gender == null,
                    isBirthDateError = it.birthDate == null,
                    isWeightError = it.weight == null,
                    isHeightError = it.height == null,
                    isSmokerError = it.smoker == null,
                )
            }
        }
    }

    fun pdfShown() {
        _uiState.update { it.copy(showPdf = null) }
    }

    private fun formatDate(millis: Long?): String =
        millis?.let { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(it)) } ?: ""

    private fun formatPersonalCode(code: String): String =
        if (code.length > 6) "${code.substring(0, 6)}-${code.substring(6)}"
        else code

    private fun areFieldsValid(state: PrepareVisitUiState) =
        state.question.isNotBlank() &&
        state.name.isNotBlank() &&
        state.surname.isNotBlank() &&
        state.personalCode.isNotBlank() &&
        state.gender != null &&
        state.birthDate != null &&
        state.weight != null &&
        state.height != null &&
        state.smoker != null

    private suspend fun createPdf(state: PrepareVisitUiState): File {
        return withContext(Dispatchers.IO) {
            val file = MyEmergFileProvider.getFile(getApplication(), "pdfs", ".pdf")

            val doc = PdfDocument()
            //A4
            val pageInfo = PageInfo.Builder(595, 842, 1).create()
            val page = doc.startPage(pageInfo)
            page.canvas.apply {
                val docMargins = 2f.cmToPt()
                val textSize = 14f //pt
                val lineSpacing = textSize * 1.2f

                val boldTextPaint = Paint().apply {
                    color = Color.BLACK
                    typeface = Typeface.DEFAULT_BOLD
                    this.textSize = textSize
                }

                val normalTextPaint = Paint().apply {
                    color = Color.BLACK
                    this.textSize = textSize
                }

                val textBounds = Rect()
                var text = stringResource(R.string.name) + ":"
                var offsetX = docMargins
                var offsetY = docMargins

                drawTextList(
                    listOf(
                        stringResource(R.string.name) to state.name,
                        stringResource(R.string.surname) to state.surname,
                        stringResource(R.string.personal_code) to state.formattedPersonalCode,
                        stringResource(R.string.gender) to state.gender.toString(),
                        stringResource(R.string.birth_date) to state.formattedBirthDate,
                        stringResource(R.string.weight_kg) to state.weight.toString(),
                        stringResource(R.string.height_cm) to state.height.toString(),
                        stringResource(R.string.smoker) to state.smoker.toString()
                    ),
                    boldTextPaint,
                    normalTextPaint,
                    lineSpacing,
                    docMargins,
                    docMargins
                )
            }
            doc.finishPage(page)

            file.apply {
                outputStream().use {
                    doc.writeTo(it)
                    doc.close()
                }
            }
        }
    }

    private fun Canvas.drawTextList(
        texts: List<Pair<String, String>>,
        startTextPaint: Paint,
        endTextPaint: Paint,
        lineSpacing: Float,
        x: Float,
        y: Float
    ) {
        var offsetY = y
        texts.forEach {
            offsetY += drawTextLine(it, startTextPaint, endTextPaint, x, offsetY) + lineSpacing
        }
    }

    private fun Canvas.drawTextLine(
        text: Pair<String, String>,
        startTextPaint: Paint,
        endTextPaint: Paint,
        x: Float,
        y: Float
    ): Int {
        val startText = text.first + ":"
        drawText(startText, x, y, startTextPaint)
        val bounds = Rect()
        startTextPaint.getTextBounds(startText, bounds)
        drawText(" " + text.second, x + bounds.width(), y, endTextPaint)
        return bounds.height()
    }

    private fun Paint.getTextBounds(text: String, bounds: Rect) = getTextBounds(text, 0, text.length, bounds)

    private fun Float.cmToPt(): Float = this / 2.54f * 72f

    private fun Gender.asString() = when (this) {
        Gender.MALE -> stringResource(R.string.male)
        Gender.FEMALE -> stringResource(R.string.female)
        Gender.OTHER -> stringResource(R.string.other)
    }

}