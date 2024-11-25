package com.twnel.android_components.login

import android.annotation.SuppressLint
import android.view.ViewTreeObserver
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlin.math.min


@Composable
fun PhoneNumberInput(
    phoneText: MutableState<TextFieldValue>,
    selectedOption: String,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    setSelectedOption: (String) -> Unit,
    setCountryCode: (String) -> Unit,
    countryCode: String,
    setFullPhoneNumber: (String) -> Unit
) {
    Row {
        OutlinedTextField(value = phoneText.value,
            onValueChange = {
                val phoneNumber = "$countryCode${it.text}"
                val newValue = getTextFormat(countryCode, it.text)
                phoneText.value = TextFieldValue(
                    text = newValue, selection = TextRange(newValue.length)
                )
                setFullPhoneNumber(phoneNumber)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(4.dp),
            placeholder = {
                Text(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    text = getExampleNumber(selectedOption)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done
            ),
            prefix = {
                Box(modifier = Modifier.clickable { setExpanded(!expanded) }) {
                    val country = countryOptions.find { it.first == selectedOption }
                    Text("${country?.third} ${country?.second}")
                    DropdownMenu(expanded = expanded, onDismissRequest = { setExpanded(false) }) {
                        countryOptions.forEach { option ->
                            DropdownMenuItem(modifier = Modifier.fillMaxWidth(), onClick = {
                                setSelectedOption(option.first)
                                setCountryCode(option.second)
                                setExpanded(false)
                            }, text = {
                                Text(text = "${option.second}  ${option.third} ${countryNames[option.first]}")
                            })
                        }
                    }
                }
            })
    }
}

@Composable
fun ResponsiveLogo(
    modifier: Modifier = Modifier, maxLogoSize: Int = 150, drawableLogoId: Int
) {
    val keyboardHeight = keyboardAsState().value

    BoxWithConstraints(
        modifier = modifier
    ) {
        val screenWidth = constraints.maxWidth
        val screenHeight = constraints.maxHeight - keyboardHeight

        val smallestDimension = min(screenWidth, screenHeight)
        val targetLogoSize = (smallestDimension * 0.3f).toInt().coerceAtMost(maxLogoSize)

        val animatedLogoSize by animateDpAsState(targetValue = targetLogoSize.dp, label = "")

        Image(
            painter = painterResource(id = drawableLogoId),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(animatedLogoSize)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun keyboardAsState(): State<Int> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }
    var imeHeight by remember { mutableIntStateOf(0) }

    DisposableEffect(LocalWindowInfo.current) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            val insets = ViewCompat.getRootWindowInsets(view)
            isImeVisible = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
            if (insets != null) {
                imeHeight = if (isImeVisible) {
                    insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                } else {
                    0
                }
            }
            true
        }

        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(imeHeight)
}

private fun getTextFormat(countryCode: String, phone: String): String {
    try {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val phoneNumber = phoneUtil.parse("$countryCode$phone", "")
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
    } catch (e: Exception) {
        return phone
    }
}

private fun getExampleNumber(countryCode: String): String {
    try {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val exampleNumber = phoneUtil.getExampleNumber(countryCode)
        return phoneUtil.format(exampleNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
    } catch (e: Exception) {
        return ""
    }
}

private val countryOptions = arrayOf(
    Triple("US", "+1", "\uD83C\uDDFA\uD83C\uDDF8"),
    Triple("AR", "+54", "\uD83C\uDDE6\uD83C\uDDF7"),
    Triple("BR", "+55", "\uD83C\uDDE7\uD83C\uDDF7"),
    Triple("CA", "+1", "\uD83C\uDDE8\uD83C\uDDE6"),
    Triple("CO", "+57", "\uD83C\uDDE8\uD83C\uDDF4"),
    Triple("MX", "+52", "\uD83C\uDDF2\uD83C\uDDFD"),
    Triple("IN", "+91", "\uD83C\uDDEE\uD83C\uDDF3"),
)

private val countryNames = hashMapOf(
    "US" to "United States",
    "AR" to "Argentina",
    "BR" to "Brazil",
    "CA" to "Canada",
    "CO" to "Colombia",
    "MX" to "Mexico",
    "IN" to "India",
)

@Composable
fun PinView(
    pinText: String,
    onPinTextChange: (String) -> Unit,
    digitCount: Int = 4,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    BasicTextField(
        value = pinText,
        onValueChange = onPinTextChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier.fillMaxWidth()
            ) {
                repeat(digitCount) { index ->
                    DigitView(index, pinText)
                }
            }
        },
        modifier = modifier
            .padding(16.dp)
            .focusRequester(focusRequester)
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun DigitView(
    index: Int,
    pinText: String,
) {
    val modifier = Modifier
        .width(50.dp)
        .border(
            width = 1.dp, color = Color.DarkGray, shape = MaterialTheme.shapes.small
        )
        .padding(12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (index >= pinText.length) "" else pinText[index].toString(),
            textAlign = TextAlign.Center,
            modifier = modifier
        )

    }
}
