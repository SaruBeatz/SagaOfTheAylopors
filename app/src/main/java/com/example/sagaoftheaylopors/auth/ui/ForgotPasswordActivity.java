package com.example.sagaoftheaylopors.auth.ui;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.example.sagaoftheaylopors.R;
import com.example.sagaoftheaylopors.auth.AuthErrorMapper;
import com.example.sagaoftheaylopors.auth.AuthRepository;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends BaseAuthActivity {

    @Override
    protected int getFormLayoutRes() {
        return R.layout.include_auth_form_forgot;
    }

    private TextInputEditText emailInput;
    private AuthRepository authRepository;

    @Override
    protected void bindViews() {
        emailInput = findViewById(R.id.emailInput);
    }

    @Override
    protected void setupUi() {
        authRepository = new AuthRepository();

        titleText.setText(R.string.auth_forgot_title);
        subtitleText.setText(R.string.auth_forgot_subtitle);
        primaryButton.setText(R.string.auth_forgot_action);
        secondaryButton.setText(R.string.auth_back_to_login);

        primaryButton.setOnClickListener(v -> sendReset());
        secondaryButton.setOnClickListener(v -> finish());
    }

    private void sendReset() {
        clearError();
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(R.string.auth_error_invalid_email);
            return;
        }

        setLoading(true);
        authRepository.sendPasswordReset(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        subtitleText.setText(R.string.auth_forgot_sent);
                        primaryButton.setVisibility(View.GONE);
                    } else {
                        Exception e = task.getException();
                        showError(e != null
                                ? AuthErrorMapper.toMessage(this, e)
                                : getString(R.string.auth_error_generic));
                    }
                });
    }
}
