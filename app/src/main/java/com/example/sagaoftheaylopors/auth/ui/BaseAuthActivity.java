package com.example.sagaoftheaylopors.auth.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sagaoftheaylopors.R;

/**
 * Shared shell for Login / Register / Forgot password screens.
 * Handles keyboard-safe scrolling via WindowInsets + NestedScrollView.
 */
public abstract class BaseAuthActivity extends AppCompatActivity {

    @LayoutRes
    protected abstract int getFormLayoutRes();

    protected TextView titleText;
    protected TextView subtitleText;
    protected FrameLayout formSlot;
    protected ProgressBar progressBar;
    protected TextView errorText;
    protected Button primaryButton;
    protected Button secondaryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateAuthShell(getFormLayoutRes());
    }

    private void inflateAuthShell(@LayoutRes int formLayoutResId) {
        // Allow content to draw behind system bars (edge-to-edge background image)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        super.setContentView(R.layout.activity_auth_shell);

        titleText = findViewById(R.id.authTitleText);
        subtitleText = findViewById(R.id.authSubtitleText);
        formSlot = findViewById(R.id.authFormSlot);
        progressBar = findViewById(R.id.authProgressBar);
        errorText = findViewById(R.id.authErrorText);
        primaryButton = findViewById(R.id.authPrimaryButton);
        secondaryButton = findViewById(R.id.authSecondaryButton);

        getLayoutInflater().inflate(formLayoutResId, formSlot, true);

        // Apply system bar insets as padding on the scroll view so content
        // is never hidden behind status/nav bar and moves up with keyboard.
        View scrollView = findViewById(R.id.authScrollView);
        if (scrollView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
                androidx.core.graphics.Insets bars =
                        insets.getInsets(WindowInsetsCompat.Type.systemBars()
                                | WindowInsetsCompat.Type.ime());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        bindViews();
        setupUi();
    }

    protected abstract void bindViews();

    protected abstract void setupUi();

    protected void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        primaryButton.setEnabled(!loading);
        secondaryButton.setEnabled(!loading);
    }

    protected void showError(@StringRes int messageRes) {
        errorText.setText(messageRes);
        errorText.setVisibility(View.VISIBLE);
    }

    protected void showError(CharSequence message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    protected void clearError() {
        errorText.setVisibility(View.GONE);
    }
}
