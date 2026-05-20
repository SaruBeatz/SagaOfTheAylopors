package com.example.sagaoftheaylopors.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Thin wrapper around Firebase Auth for registration, login, and password reset.
 * UI layers should observe Task results and map error codes to user-facing messages.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public Task<AuthResult> signIn(@NonNull String email, @NonNull String password) {
        return firebaseAuth.signInWithEmailAndPassword(email.trim(), password);
    }

    public Task<AuthResult> register(@NonNull String email, @NonNull String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email.trim(), password);
    }

    public Task<Void> sendPasswordReset(@NonNull String email) {
        return firebaseAuth.sendPasswordResetEmail(email.trim());
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
}
