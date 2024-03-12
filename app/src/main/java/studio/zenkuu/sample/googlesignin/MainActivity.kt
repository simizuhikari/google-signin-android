package studio.zenkuu.sample.googlesignin

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.util.UUID

private const val GOOGLE_WEB_CLIENT_ID = "<INPUT-YOUR-CLIENT-ID>"

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Activity.showErrorToast(e: Exception) {
    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
}

fun Activity.showSignInSuccessfulDialog(displayName: String?, id: String?) {
    AlertDialog.Builder(this)
        .setTitle("Sign In successful")
        .setMessage("Hello ~ $displayName\nYour ID: $id")
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, null)
        .show()
}

class MainActivity: AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                try {
                    val credential =
                        Identity.getSignInClient(this).getSignInCredentialFromIntent(data)
                    // Signed in successfully - show authenticated UI
                    showSignInSuccessfulDialog(credential.displayName, credential.id)

                } catch (e: ApiException) {
                    // The ApiException status code indicates the detailed failure reason.
                    showErrorToast(e)
                }
            } else {
                showToast("User Cancelled")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        credentialManager = CredentialManager.create(this)

        // button for sign in
        findViewById<View>(R.id.button_sign_in).setOnClickListener {
            signIn()
        }

        // button for sign in (one tap)
        findViewById<View>(R.id.button_sign_in_one_tap).setOnClickListener {
            signInOneTap()
        }

        // button for sign out
        findViewById<View>(R.id.button_sign_out).setOnClickListener {
            signOut()
        }
    }

    private fun signIn() {

        val request = GetSignInIntentRequest.Builder()
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            .build()

        Identity.getSignInClient(this)
            .getSignInIntent(request)
            .addOnSuccessListener{ result ->
                try {
                    val input = IntentSenderRequest.Builder(result.intentSender)
                    resultLauncher.launch(input.build())
                } catch (e: IntentSender.SendIntentException) {
                    showErrorToast(e)
                }
            }
            .addOnFailureListener{ e ->
                showErrorToast(e)
            }
    }

    private fun signInOneTap() {
        lifecycleScope.launch {
            try {
                val request = GetCredentialRequest()
                val response = credentialManager.getCredential(this@MainActivity, request)
                handleSignIn(response)
            } catch (e: NoCredentialException) {
                signIn()
            } catch (e: GetCredentialCustomException) {
                showErrorToast(e)
            } catch (e: GetCredentialCancellationException) {
                showToast("User Cancelled")
            }
        }
    }

    private fun signOut() {
        lifecycleScope.launch {
            try {
                val request = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(request)
                showToast("User Sign Out")
            } catch (e: ClearCredentialException) {
                showErrorToast(e)
            }
        }
    }

    private fun handleSignIn(response: GetCredentialResponse) {
        when (val credential = response.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        showSignInSuccessfulDialog(googleIdTokenCredential.displayName, googleIdTokenCredential.id)

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("MainActivity", "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    println("TODO")
                }
            }
            else -> {
                println("TODO")
            }
        }
    }

    private fun GetCredentialRequest(): GetCredentialRequest {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setNonce(UUID.randomUUID().toString())
            .setAutoSelectEnabled(true)
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            .build()
        return GetCredentialRequest(listOf(googleIdOption))
    }
}