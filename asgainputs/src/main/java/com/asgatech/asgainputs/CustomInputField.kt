package com.asgatech.asgainputs

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import com.asgatech.asgainputs.utils.ValidationUtil
import com.google.android.material.textfield.TextInputLayout

/**
 * @Author: Muhammad Noamany
 * @Date: 12/12/2021
 * @Email: muhammadnoamany@gmail.com
 */
class CustomInputField : TextInputLayout {
    private lateinit var inputEditText: AppCompatEditText
    private var errorColor: ColorStateList? = null
    private var inputHint: String = ""
    private var errorText: String = ""
    private var inputFieldType: Int = -1
    private var minLength: Int = 1
    private var maxLength: Int = 100
    private var hasDot: Int = -1
    private var hasUnderScore: Int = -1
    private var hasSpecialCharacter: Int = -1
    private var hasNumbers: Int = -1
    private var hasCapitalLetters:Int = -1

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedAttributeSet = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomInputField,
            0,
            0
        )
        try {
            readXmlAttr(typedAttributeSet)
            setBoxErrorColor(errorColor)
        } finally {
            typedAttributeSet.recycle()
        }

        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * Fetch attr from xml view or init with the default values
     * inputHint => the hint of the contained edit text
     * errorText => the text of the validation error
     * input field type => type of the field (username, password, email, egyPhone, ksaPhone, ksaNationalId, egyNationalId, Payment Cards(Visa, Master , Amex) and ipAddress )
     * errorColor => maximum length of username or password
     * minLength => minimum length of username or password
     * maxLength => maximum length of username or password
     * hasDot => the capability of having [.] for username or password
     * hasUnderScore => the capability of having [_]  for username or password
     * hasSpecialCharacter => the capability of having [!@#$]  for username or password
     * hasCapitalLetters => the capability of having [A-Z] for username or password
     * hasNumbers => the capability of having [0,1,2...9] for username or password
     */
    private fun readXmlAttr(typedAttributeSet: TypedArray) {
        inputHint =
            typedAttributeSet.getString(R.styleable.CustomInputField_hint) ?: ""
        errorText =
            typedAttributeSet.getString(R.styleable.CustomInputField_errorText)
                ?: "Please enter valid field data"
        inputFieldType =
            typedAttributeSet.getInt(R.styleable.CustomInputField_fieldType, -1)
        errorColor =
            typedAttributeSet.getColorStateList(R.styleable.CustomInputField_errorColor)
                ?: ColorStateList.valueOf(Color.RED)
        maxLength =
            typedAttributeSet.getInt(R.styleable.CustomInputField_maxLength, 100)
        minLength =
            typedAttributeSet.getInt(R.styleable.CustomInputField_minLength, 1)
        hasDot =
            typedAttributeSet.getInt(R.styleable.CustomInputField_includeDot, -1)
        hasUnderScore =
            typedAttributeSet.getInt(R.styleable.CustomInputField_includeUnderScore, -1)
        hasSpecialCharacter =
            typedAttributeSet.getInt(R.styleable.CustomInputField_includeSpecialCharacter, -1)
        hasNumbers =
            typedAttributeSet.getInt(R.styleable.CustomInputField_includeNumbers, -1)
        hasCapitalLetters =
            typedAttributeSet.getInt(R.styleable.CustomInputField_includeCapitalLetters, -1)
    }

    /**
     * Set the error color for box border, error text, hint label and error icon view
     */
    private fun setBoxErrorColor(errorColor: ColorStateList?) {
        boxStrokeErrorColor = errorColor
        setErrorIconTintList(errorColor)
        setErrorTextColor(errorColor)
    }

    /**
     * Init the whole view
     */
    private fun init() {
        addTextInputField()
        setInputTextValidationListener()
    }

    /**
     * Adding and input EditText to it
     * setting the hint of EditText according to the xml value or with default value ""
     */
    private fun addTextInputField() {
        inputEditText = AppCompatEditText(context)
        inputEditText.hint = inputHint
        setInputType()
        addView(inputEditText)
    }

    /**
     * Set input type according to the filed type
     */
    private fun setInputType() {
        when (inputFieldType) {
            // email input field
            isEmailFieldType() -> inputEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            // password input field
            isPasswordFieldType() -> inputEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            // Check for the number input field
            isNumber(inputFieldType) -> inputEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_NUMBER
            // ip address input field
            isWebFieldType() -> inputEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
            // default input field (text)
            else -> inputEditText.inputType =
                InputType.TYPE_CLASS_TEXT
        }
    }

    /**
     * check if field should be number input
     */
    private fun isNumber(inputTypes: Int): Int {
        return when (inputTypes) {
            InputTypes.VISA.value -> InputTypes.VISA.value
            InputTypes.MASTER_CARD.value -> InputTypes.MASTER_CARD.value
            InputTypes.AMEX.value -> InputTypes.AMEX.value
            InputTypes.CVV.value -> InputTypes.CVV.value
            InputTypes.KSA_NATIONAL_ID.value -> InputTypes.KSA_NATIONAL_ID.value
            InputTypes.EGY_NATIONAL_ID.value -> InputTypes.EGY_NATIONAL_ID.value
            InputTypes.KSA_PHONE.value -> InputTypes.KSA_PHONE.value
            InputTypes.EGY_PHONE.value -> InputTypes.EGY_PHONE.value
            else -> -1
        }
    }

    /**
     * check if input field is an email field
     */
    private fun isEmailFieldType() = InputTypes.EMAIL.value

    /**
     * check if input field is an ip address field
     */
    private fun isWebFieldType() = InputTypes.IP_ADDRESS.value

    /**
     * check if input field is an password field
     */
    private fun isPasswordFieldType() = InputTypes.PASSWORD.value

    /**
     * Listing for EditText inputs and its validation process
     */
    private fun setInputTextValidationListener() {
        inputEditText.doOnTextChanged { text, _, _, _ ->
            Log.e("inputChange", "${text!!.isNotEmpty()}")
            validateVIew(text)
        }
    }

    /**
     * Validate the input value
     */
    private fun validateVIew(text: CharSequence) {
        error = when (inputFieldType) {
            /**
             * Validate input text as username
             */
            InputTypes.USERNAME.value -> if (ValidationUtil.isValidUserName(
                    text.toString(),
                    minLength,
                    maxLength,
                    hasDot,
                    hasUnderScore,
                    hasSpecialCharacter,
                    hasNumbers,
                    hasCapitalLetters
                )
            ) null else errorText
            /**
             * Validate input text as password
             */
            InputTypes.PASSWORD.value -> if (ValidationUtil.isValidUserPassword(
                    text.toString(),
                    minLength,
                    maxLength,
                    hasDot,
                    hasUnderScore,
                    hasSpecialCharacter,
                    hasNumbers,
                    hasCapitalLetters
                )
            ) null else errorText
            /**
             * Validate input text as email address
             */
            InputTypes.EMAIL.value -> if (ValidationUtil.isValidUserEmail(text.toString())) null else errorText
            /**
             * Validate input text as ksa phone number
             */
            InputTypes.KSA_PHONE.value -> if (ValidationUtil.isValidKsaPhoneNumber(text.toString())) null else errorText
            /**
             * Validate input text as ksa national Id
             */
            InputTypes.KSA_NATIONAL_ID.value -> if (ValidationUtil.isKsaCitizen(text.toString())) null else errorText
            /**
             * Validate input text as egyptian phone
             */
            InputTypes.EGY_PHONE.value -> if (ValidationUtil.isValidEgPhoneNumber(text.toString())) null else errorText
            /**
             * Validate input text as egyptian national Id
             */
            InputTypes.EGY_NATIONAL_ID.value -> if (ValidationUtil.isValidEgyptianNationalId(
                    text.toString()
                )
            ) null else errorText
            /**
             * Validate input text as ip address
             */
            InputTypes.IP_ADDRESS.value -> if (ValidationUtil.isValidIpAddress(text.toString())) null else errorText
            /**
             * Validate input text as visa card
             */
            InputTypes.VISA.value -> if (ValidationUtil.isVisa(text.toString())) null else errorText
            /**
             * Validate input text as master card
             */
            InputTypes.MASTER_CARD.value -> if (ValidationUtil.isMaster(text.toString())) null else errorText
            /**
             * Validate input text as americam express card
             */
            InputTypes.AMEX.value -> if (ValidationUtil.isAmex(text.toString())) null else errorText
            /**
             * Validate input text as cvv number
             */
            InputTypes.CVV.value -> if (ValidationUtil.isValidCvvNumber(text.toString())) null else errorText
            else -> null
        }
    }

    /**
     * enum to hold all types of input fields
     */
    private enum class InputTypes(val value: Int) {
        USERNAME(0), PASSWORD(1), EMAIL(2), EGY_PHONE(3),
        KSA_PHONE(4), KSA_NATIONAL_ID(5), EGY_NATIONAL_ID(6),
        VISA(7), MASTER_CARD(8), AMEX(9), CVV(10), IP_ADDRESS(11);

        companion object {
            fun from(findValue: Int): InputTypes =
                values().first { it.value == findValue }
        }
    }
    /**
     * @usage: this enum class identifies different types of regex
     */
    enum class RegexAdditionType(val value: Int) {
        MANDATORY(1), OPTIONAL(2), NON(-1);

        companion object {
            fun from(findValue: Int): RegexAdditionType =
                RegexAdditionType.values().first { it.value == findValue }
        }
    }

    /**
     * Run the field validation from out side the view class and return bool
     */
    fun isValidView(): Boolean {
        if (inputEditText.text.isNullOrEmpty()) error = errorText
        return error == null
    }
}