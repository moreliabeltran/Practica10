package mx.edu.itesca.practica10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class MainActivity : AppCompatActivity() {
    object Global {
        var preferencias_compartidas = "sharedpreferences"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        verificar_sesion_abierta()
        var correo: EditText = findViewById(R.id.etCorreo)
        var pass: EditText = findViewById(R.id.etPassword)
        //var btnLogin: Button = findViewById(R.id.btnLogin)
        //var btnCrear: Button = findViewById(R.id.btnCrear)

        var btnLoginGoogle: Button = findViewById(R.id.btnLoginGoogle)

        /*btnLoginGoogle.setOnClickListener{
            DialogCrearCuenta().show(supportFragmentManager,null)
        }*/

        btnLoginGoogle.setOnClickListener {
            setContent {
                Login_google()
            }
        }

        /*btnLogin.setOnClickListener {
            if (pass.text.toString() != "") {
                if (correo.text.toString() != "" && Patterns.EMAIL_ADDRESS.matcher(correo.text.toString())
                        .matches()
                ) {
                    loginFirebase(correo.text.toString(), pass.text.toString())
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Formato de correo incorrecto",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }*/
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun Login_google() {
        val context = LocalContext.current
        val coroutineScope: CoroutineScope = rememberCoroutineScope()
        val credentialManager = CredentialManager.create(context)
        /*val googleIdOption = GetGoogleIdOption.Builder()
            // Your server's client ID, not your Android client ID.
            .setServerClientId(getString(R.string.web_client))
            // Only show accounts previously used to sign in.
            .setFilterByAuthorizedAccounts(true)
            .build()

        // Create the Credential Manager request
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()*/
        val signInWithGoogleOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(getString(R.string.web_client))
                .setNonce("nonce")
                .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Toast.makeText(
                    applicationContext,
                    "Error al obtener la credencial " + e,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val credencial =
                            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                        /*GoogleIdTokenVerifier verifier = ... // see validation instructions
                        GoogleIdToken idToken = verifier.verify(idTokenString);
                        idToken.getPayload().getSubject()*/

                        FirebaseAuth.getInstance().signInWithCredential(credencial)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    var intent = Intent(applicationContext, Bienvenida::class.java);
                                    intent.putExtra("Correo", task.result.user?.email)
                                    intent.putExtra("Proveedor", "Google")
                                    startActivity(intent)
                                    guardar_sesion(task.result.user?.email.toString(), "Google")
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "Error en la autenticación con Firebase",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } catch (e: GoogleIdTokenParsingException) {
                        //Log.e(TAG, "Received an invalid google id token response", e)
                        Toast.makeText(
                            applicationContext,
                            "Usuario/Contraseña incorrecto(s)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    //Log.e(TAG, "Unexpected type of credential")
                    Toast.makeText(
                        applicationContext,
                        "Usuario/Contraseña incorrecto(s)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            else -> {
                //Log.e(TAG, "Unexpected type of credential")
                Toast.makeText(
                    applicationContext,
                    "Usuario/Contraseña incorrecto(s)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun login_firebase(correo: String, pass: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var intent = Intent(applicationContext, Bienvenida::class.java);
                    intent.putExtra("Correo", task.result.user?.email)
                    intent.putExtra("Proveedor", "Usuario/Contraseña")
                    startActivity(intent)
                    guardar_sesion(task.result.user?.email.toString(), "Usuario/Contraseña")
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Usuario/Contraseña incorrecto(s)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun verificar_sesion_abierta() {
        var sesion_abierta: SharedPreferences = this.getSharedPreferences(
            Global.preferencias_compartidas,
            Context.MODE_PRIVATE
        )
        var correo = sesion_abierta.getString("Correo", null)
        var proveedor = sesion_abierta.getString("Proveedor", null)
        if (correo != null && proveedor != null) {
            var intent = Intent(applicationContext, Bienvenida::class.java);
            intent.putExtra("Correo", correo)
            intent.putExtra("Proveedor", proveedor)
            startActivity(intent)
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun guardar_sesion(correo: String, proveedor: String) {
        var guardar_sesion: SharedPreferences.Editor = this.getSharedPreferences(
            Global.preferencias_compartidas,
            Context.MODE_PRIVATE
        ).edit()
        guardar_sesion.putString("Correo",correo);
        guardar_sesion.putString("Proveedor",proveedor);
        guardar_sesion.apply()
        guardar_sesion.commit()
    }
}