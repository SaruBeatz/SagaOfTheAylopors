package com.example.sagaoftheaylopors.auth.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.sagaoftheaylopors.R;
import com.example.sagaoftheaylopors.auth.AuthErrorMapper;
import com.example.sagaoftheaylopors.auth.AuthFlowHelper;
import com.example.sagaoftheaylopors.auth.AuthRepository;
import com.example.sagaoftheaylopors.auth.SessionManager;
import com.example.sagaoftheaylopors.auth.UserProfileRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseAuthActivity {

    @Override
    protected int getFormLayoutRes() {
        return R.layout.include_auth_form_login;
    }

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private AuthRepository authRepository;
    private UserProfileRepository profileRepository;
    private SessionManager sessionManager;
    private int successDestination;

    @Override
    protected void bindViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
    }

    @Override
    protected void setupUi() {
        authRepository = new AuthRepository();
        profileRepository = new UserProfileRepository();
        sessionManager = new SessionManager(this);
        successDestination = getIntent().getIntExtra(
                AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION,
                AuthFlowHelper.DEST_CHARACTER_SELECT);

        titleText.setText(R.string.auth_login_title);
        subtitleText.setText(R.string.auth_login_subtitle);
        primaryButton.setText(R.string.auth_login_action);
        secondaryButton.setText(R.string.auth_go_to_register);

        TextView forgotLink = findViewById(R.id.forgotPasswordLink);
        forgotLink.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        primaryButton.setOnClickListener(v -> attemptLogin());
        secondaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION, successDestination);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        clearError();
        String email = textOf(emailInput);
        String password = textOf(passwordInput);

        if (TextUtils.isEmpty(email)) {
            showError(R.string.auth_error_invalid_email);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showError(R.string.auth_error_wrong_password);
            return;
        }

        setLoading(true);
        authRepository.signIn(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = authRepository.getCurrentUser();
                        if (user != null) {
                            // fire-and-forget: best-effort update
                            profileRepository.updateLastLogin(user.getUid());
                        }
                        AuthFlowHelper.onAuthSuccess(this, successDestination, sessionManager);
                    } else {
                        Exception e = task.getException();
                        showError(e != null
                                ? AuthErrorMapper.toMessage(this, e)
                                : getString(R.string.auth_error_generic));
                    }
                });
    }

    private static String textOf(TextInputEditText input) {
        return input != null && input.getText() != null
                ? input.getText().toString().trim() : "";
    }
}
