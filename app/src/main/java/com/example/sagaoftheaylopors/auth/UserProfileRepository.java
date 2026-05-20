package com.example.sagaoftheaylopors.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages users/{uid} documents in Firestore.
 * Firebase Auth holds credentials; Firestore holds game/profile metadata.
 */
public class UserProfileRepository {

    private static final String COLLECTION_USERS = "users";
    private static final int PRIVACY_POLICY_VERSION = 1;

    private final FirebaseFirestore firestore;

    public UserProfileRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public DocumentReference userDocument(@NonNull String uid) {
        return firestore.collection(COLLECTION_USERS).document(uid);
    }

    /**
     * Creates the full user profile on first registration.
     * Schema: uid, email, displayName, age, role, privacyPolicyAccepted,
     *         privacyPolicyAcceptedAt, privacyPolicyVersion, createdAt,
     *         lastLoginAt, accountStatus.
     */
    public Task<Void> createOrUpdateProfile(
            @NonNull FirebaseUser user,
            @NonNull String displayName,
            @Nullable String phoneNumber,
            int age
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("email", user.getEmail());
        data.put("displayName", displayName.trim());
        data.put("age", age);
        data.put("role", "standard");
        data.put("privacyPolicyAccepted", true);
        data.put("privacyPolicyAcceptedAt", FieldValue.serverTimestamp());
        data.put("privacyPolicyVersion", PRIVACY_POLICY_VERSION);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("lastLoginAt", FieldValue.serverTimestamp());
        data.put("accountStatus", "active");
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            data.put("phoneNumber", phoneNumber);
        }
        return userDocument(user.getUid()).set(data, SetOptions.merge());
    }

    /**
     * Updates lastLoginAt on every successful sign-in.
     */
    public Task<Void> updateLastLogin(@NonNull String uid) {
        Map<String, Object> data = new HashMap<>();
        data.put("lastLoginAt", FieldValue.serverTimestamp());
        return userDocument(uid).update(data);
    }

}
