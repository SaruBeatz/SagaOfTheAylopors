package com.example.sagaoftheaylopors.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.sagaoftheaylopors.CharacterSelectActivity;
import com.example.sagaoftheaylopors.auth.ui.ConsentActivity;
import com.example.sagaoftheaylopors.auth.ui.LoginActivity;
import com.example.sagaoftheaylopors.auth.ui.RegisterActivity;

/**
 * Central navigation hub: New Game → Consent → Auth → Character Select.
 */
public final class AuthFlowHelper {

    public static final String EXTRA_AUTH_SUCCESS_DESTINATION = "auth_success_destination";
    public static final int DEST_CHARACTER_SELECT = 1;
    public static final int DEST_MAP_CONTINUE = 2;

    private AuthFlowHelper() {
    }

    public static void startNewGameFlow(Activity activity) {
        ConsentManager consentManager = new ConsentManager(activity);
        AuthRepository authRepository = new AuthRepository();

        if (!consentManager.hasValidConsent()) {
            Intent intent = new Intent(activity, ConsentActivity.class);
            intent.putExtra(EXTRA_AUTH_SUCCESS_DESTINATION, DEST_CHARACTER_SELECT);
            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

        if (!authRepository.isLoggedIn()) {
            Intent intent = new Intent(activity, RegisterActivity.class);
            intent.putExtra(EXTRA_AUTH_SUCCESS_DESTINATION, DEST_CHARACTER_SELECT);
            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

        SessionManager sessionManager = new SessionManager(activity);
        sessionManager.markGameStarted();
        activity.startActivity(new Intent(activity, CharacterSelectActivity.class));
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Called from RegisterActivity / LoginActivity after successful auth.
     * Marks session state and navigates to the success destination.
     */
    public static void onAuthSuccess(Activity activity, int destination, SessionManager sessionManager) {
        sessionManager.markGameStarted();
        sessionManager.markJustRegistered();

        if (destination == DEST_CHARACTER_SELECT) {
            activity.startActivity(new Intent(activity, CharacterSelectActivity.class));
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        activity.finish();
    }

    public static void openLogin(Context context, int successDestination) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(EXTRA_AUTH_SUCCESS_DESTINATION, successDestination);
        context.startActivity(intent);
    }
}
