# Android Google Sign-in Example
This repository is an example of Sign In with Google and using the Jetpack Libraries' CredentialManager.

## How to use
Create credentials on the Google APIs Console.
Paste the client ID of web application into the GOOGLE_WEB_CLIENT_ID attribute in MainActivity.kt.

```
private const val GOOGLE_WEB_CLIENT_ID = "<INPUT-YOUR-CLIENT-ID>"
```

## Notice
On the credentials page, need to create client ID both Android and Web application.

## Reference
- [Get started with One Tap sign-in and sign-up](https://developers.google.com/identity/one-tap/android/get-started)
- [Integrate Credential Manager with Sign in with Google](https://developer.android.com/training/sign-in/credential-manager)