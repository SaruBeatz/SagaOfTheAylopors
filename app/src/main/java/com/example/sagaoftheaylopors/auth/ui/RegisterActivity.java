package com.example.sagaoftheaylopors.auth.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;

import com.example.sagaoftheaylopors.R;
import com.example.sagaoftheaylopors.auth.AuthErrorMapper;
import com.example.sagaoftheaylopors.auth.AuthFlowHelper;
import com.example.sagaoftheaylopors.auth.AuthRepository;
import com.example.sagaoftheaylopors.auth.ConsentManager;
import com.example.sagaoftheaylopors.auth.SessionManager;
import com.example.sagaoftheaylopors.auth.UserProfileRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseAuthActivity {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_AGE = 5;
    private static final int MAX_AGE = 120;

    @Override
    protected int getFormLayoutRes() {
        return R.layout.include_auth_form_register;
    }

    private TextInputEditText displayNameInput;
    private TextInputEditText ageInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText passwordInput;
    private TextInputEditText passwordConfirmInput;
    private TextInputLayout displayNameLayout;
    private TextInputLayout ageLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout passwordConfirmLayout;

    private AuthRepository authRepository;
    private UserProfileRepository profileRepository;
    private ConsentManager consentManager;
    private SessionManager sessionManager;
    private int successDestination;
    private boolean phoneFormatting;

    @Override
    protected void bindViews() {
        displayNameInput = findViewById(R.id.displayNameInput);
        ageInput = findViewById(R.id.ageInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordConfirmInput = findViewById(R.id.passwordConfirmInput);
        displayNameLayout = findViewById(R.id.displayNameLayout);
        ageLayout = findViewById(R.id.ageLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        passwordConfirmLayout = findViewById(R.id.passwordConfirmLayout);
    }

    @Override
    protected void setupUi() {
        authRepository = new AuthRepository();
        profileRepository = new UserProfileRepository();
        consentManager = new ConsentManager(this);
        sessionManager = new SessionManager(this);

        if (!consentManager.hasValidConsent()) {
            Intent consentIntent = new Intent(this, ConsentActivity.class);
            consentIntent.putExtra(
                    AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION,
                    getIntent().getIntExtra(
                            AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION,
                            AuthFlowHelper.DEST_CHARACTER_SELECT));
            startActivity(consentIntent);
            finish();
            return;
        }

        successDestination = getIntent().getIntExtra(
                AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION,
                AuthFlowHelper.DEST_CHARACTER_SELECT);

        titleText.setText(R.string.auth_register_title);
        subtitleText.setText(R.string.auth_register_subtitle);
        primaryButton.setText(R.string.auth_register_action);
        secondaryButton.setText(R.string.auth_go_to_login);
        setupFieldValidation();
        setupPhoneMask();

        primaryButton.setOnClickListener(v -> attemptRegister());
        secondaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION, successDestination);
            startActivity(intent);
        });
    }

    private void attemptRegister() {
        clearError();

        String email = textOf(emailInput);
        String password = textOf(passwordInput);
        if (!validateForm(true)) {
            return;
        }

        final String finalDisplayName = textOf(displayNameInput);
        final int finalAge = Integer.parseInt(extractDigits(textOf(ageInput)));
        final String finalPhone = toE164OrNull(textOf(phoneInput));

        setLoading(true);
        authRepository.register(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false);
                        Exception e = task.getException();
                        showError(e != null
                                ? AuthErrorMapper.toMessage(this, e)
                                : getString(R.string.auth_error_generic));
                        return;
                    }
                    FirebaseUser user = authRepository.getCurrentUser();
                    if (user == null) {
                        setLoading(false);
                        showError(R.string.auth_error_generic);
                        return;
                    }
                    profileRepository.createOrUpdateProfile(user, finalDisplayName, finalPhone, finalAge)
                            .addOnCompleteListener(profileTask -> {
                                setLoading(false);
                                if (profileTask.isSuccessful()) {
                                    AuthFlowHelper.onAuthSuccess(this, successDestination, sessionManager);
                                } else {
                                    showError(R.string.auth_error_generic);
                                }
                            });
                });
    }

    private void setupFieldValidation() {
        configureErrorIcon(displayNameLayout);
        configureErrorIcon(ageLayout);
        configureErrorIcon(emailLayout);
        configureErrorIcon(phoneLayout);
        configureErrorIcon(passwordLayout);
        configureErrorIcon(passwordConfirmLayout);

        addValidationWatcher(displayNameInput, () -> validateDisplayName(true));
        addValidationWatcher(ageInput, () -> validateAge(true));
        addValidationWatcher(emailInput, () -> validateEmail(true));
        addValidationWatcher(phoneInput, () -> validatePhone(true));
        addValidationWatcher(passwordInput, () -> {
            validatePassword(true);
            validatePasswordConfirm(true);
        });
        addValidationWatcher(passwordConfirmInput, () -> validatePasswordConfirm(true));
    }

    private void setupPhoneMask() {
        if (TextUtils.isEmpty(textOf(phoneInput))) {
            phoneInput.setText("+7 ");
            phoneInput.setSelection(phoneInput.getText() != null ? phoneInput.getText().length() : 0);
        }

        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (phoneFormatting) return;
                phoneFormatting = true;
                String digits = extractDigits(s.toString());
                String formatted = formatPhone(digits);
                phoneInput.setText(formatted);
                phoneInput.setSelection(formatted.length());
                phoneFormatting = false;
            }
        });
    }

    private boolean validateForm(boolean showError) {
        boolean validDisplayName = validateDisplayName(showError);
        boolean validAge = validateAge(showError);
        boolean validEmail = validateEmail(showError);
        boolean validPhone = validatePhone(showError);
        boolean validPassword = validatePassword(showError);
        boolean validConfirm = validatePasswordConfirm(showError);
        return validDisplayName && validAge && validEmail && validPhone && validPassword && validConfirm;
    }

    private boolean validateDisplayName(boolean showError) {
        String value = textOf(displayNameInput);
        boolean valid = !TextUtils.isEmpty(value);
        setFieldError(displayNameLayout, showError && !valid, R.string.auth_error_display_name_required);
        return valid;
    }

    private boolean validateAge(boolean showError) {
        String digits = extractDigits(textOf(ageInput));
        boolean valid = !TextUtils.isEmpty(digits);
        if (valid) {
            int age = Integer.parseInt(digits);
            valid = age >= MIN_AGE && age <= MAX_AGE;
        }
        setFieldError(ageLayout, showError && !valid, R.string.auth_error_invalid_age);
        return valid;
    }

    private boolean validateEmail(boolean showError) {
        String email = textOf(emailInput);
        boolean valid = !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
        setFieldError(emailLayout, showError && !valid, R.string.auth_error_invalid_email);
        return valid;
    }

    private boolean validatePhone(boolean showError) {
        String value = textOf(phoneInput);
        String digits = extractDigits(value);
        boolean empty = TextUtils.isEmpty(value) || "+7".equals(value) || "+7 ".equals(value);
        boolean valid = empty || (digits.length() == 11 && digits.startsWith("7"));
        setFieldError(phoneLayout, showError && !valid, R.string.auth_error_phone_mask);
        return valid;
    }

    private boolean validatePassword(boolean showError) {
        String password = textOf(passwordInput);
        boolean valid = password.length() >= MIN_PASSWORD_LENGTH;
        setFieldError(passwordLayout, showError && !valid, R.string.auth_error_weak_password);
        return valid;
    }

    private boolean validatePasswordConfirm(boolean showError) {
        String password = textOf(passwordInput);
        String confirm = textOf(passwordConfirmInput);
        boolean valid = !TextUtils.isEmpty(confirm) && password.equals(confirm);
        setFieldError(passwordConfirmLayout, showError && !valid, R.string.auth_error_password_mismatch);
        return valid;
    }

    private void setFieldError(TextInputLayout layout, boolean hasError, int messageResId) {
        if (layout == null) return;
        layout.setError(hasError ? getString(messageResId) : null);
    }

    private void configureErrorIcon(TextInputLayout layout) {
        if (layout == null) return;
        layout.setErrorIconDrawable(android.R.drawable.stat_notify_error);
        layout.setErrorIconTintList(ColorStateList.valueOf(getColor(android.R.color.holo_red_dark)));
    }

    private void addValidationWatcher(TextInputEditText input, Runnable validator) {
        if (input == null) return;
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    validator.run();
                }
            }
        });
    }

    private static String extractDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D+", "");
    }

    private static String formatPhone(String rawDigits) {
        String digits = rawDigits;
        if (TextUtils.isEmpty(digits)) {
            return "+7 ";
        }
        if (!digits.startsWith("7")) {
            if (digits.startsWith("8")) {
                digits = "7" + digits.substring(1);
            } else {
                digits = "7" + digits;
            }
        }
        if (digits.length() > 11) {
            digits = digits.substring(0, 11);
        }

        String body = digits.substring(1);
        StringBuilder result = new StringBuilder("+7 ");
        for (int i = 0; i < body.length(); i++) {
            if (i == 3 || i == 6) result.append('-');
            result.append(body.charAt(i));
        }
        return result.toString();
    }

    private static String toE164OrNull(String maskedPhone) {
        String digits = extractDigits(maskedPhone);
        if (digits.length() == 11 && digits.startsWith("7")) {
            return "+" + digits;
        }
        return null;
    }

    private static String textOf(TextInputEditText input) {
        return input != null && input.getText() != null
                ? input.getText().toString().trim() : "";
    }
}
