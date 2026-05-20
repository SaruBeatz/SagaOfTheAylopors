package com.example.sagaoftheaylopors.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sagaoftheaylopors.R;
import com.example.sagaoftheaylopors.auth.AuthFlowHelper;
import com.example.sagaoftheaylopors.auth.AuthRepository;
import com.example.sagaoftheaylopors.auth.ConsentManager;

public class ConsentActivity extends AppCompatActivity {

    private ConsentManager consentManager;
    private int successDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        consentManager = new ConsentManager(this);
        successDestination = getIntent().getIntExtra(
                AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION,
                AuthFlowHelper.DEST_CHARACTER_SELECT
        );

        CheckBox checkBox = findViewById(R.id.consentCheckBox);
        Button continueButton = findViewById(R.id.consentContinueButton);
        Button declineButton = findViewById(R.id.consentDeclineButton);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                continueButton.setEnabled(isChecked));

        continueButton.setOnClickListener(v -> {
            consentManager.recordConsent();
            AuthRepository authRepository = new AuthRepository();
            if (authRepository.isLoggedIn()) {
                AuthFlowHelper.onAuthSuccess(
                        this,
                        successDestination,
                        new com.example.sagaoftheaylopors.auth.SessionManager(this)
                );
            } else {
                Intent intent = new Intent(this, RegisterActivity.class);
                intent.putExtra(AuthFlowHelper.EXTRA_AUTH_SUCCESS_DESTINATION, successDestination);
                startActivity(intent);
                finish();
            }
        });

        declineButton.setOnClickListener(v -> finish());
    }
}
