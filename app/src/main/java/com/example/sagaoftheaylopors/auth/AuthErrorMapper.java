package com.example.sagaoftheaylopors.auth;

import android.content.Context;

import com.example.sagaoftheaylopors.R;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

/**
 * Maps Firebase Auth errors to localized user-facing messages.
 */
public final class AuthErrorMapper {

    private AuthErrorMapper() {
    }

    public static String toMessage(Context context, Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return context.getString(R.string.auth_error_weak_password);
        }
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return context.getString(R.string.auth_error_user_not_found);
        }
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return context.getString(R.string.auth_error_wrong_password);
        }
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return context.getString(R.string.auth_error_email_in_use);
        }
        if (exception instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) exception).getErrorCode();
            if ("ERROR_INVALID_EMAIL".equals(code)) {
                return context.getString(R.string.auth_error_invalid_email);
            }
        }
        return context.getString(R.string.auth_error_generic);
    }
}
