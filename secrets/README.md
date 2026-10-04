# Local secrets (not committed to Git)

Create these files locally (both are gitignored):

- `secrets/.env`
- `secrets/secrets.properties`

Required keys:

```properties
FIREBASE_API_KEY=
FIREBASE_PROJECT_ID=
FIREBASE_PROJECT_NUMBER=
FIREBASE_STORAGE_BUCKET=
FIREBASE_MOBILE_SDK_APP_ID=
ANDROID_APPLICATION_ID=
```

Gradle reads `.env` first, then `secrets.properties` (properties override `.env`).

`app/google-services.json` is generated automatically on sync/build. Do not commit it.

## CI

Write `secrets/.env` or `secrets/secrets.properties` from GitHub Actions secrets before `./gradlew assembleRelease`.
