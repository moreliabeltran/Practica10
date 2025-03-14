package mx.edu.itesca.practica10

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mx.edu.itesca.practica10.MainActivity.Global

class Bienvenida : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)

        var correo: TextView = findViewById(R.id.evCorreo)
        var proveedor: TextView = findViewById(R.id.evProveedor)

        var btnSalir: Button = findViewById(R.id.btnSalir)
        var intent = getIntent()
        correo.text = "Correo: " + intent.getStringExtra("Correo")
        proveedor.text = "Proveedor: " + intent.getStringExtra("Proveedor")

        btnSalir.setOnClickListener {
            var intent: Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            setContent {
                borrar_sesion()
            }
        }
    }

    @Composable
    @SuppressLint("CommitPrefEdits", "CoroutineCreationDuringComposition")
    fun borrar_sesion() {
        var borrar_sesion: SharedPreferences.Editor = this.getSharedPreferences(
            Global.preferencias_compartidas,
            Context.MODE_PRIVATE
        ).edit()
        borrar_sesion.clear()
        borrar_sesion.apply()
        borrar_sesion.commit()

        //Firebase.auth.signOut()

        val context = LocalContext.current
        val coroutineScope: CoroutineScope = rememberCoroutineScope()
        val credentialManager = CredentialManager.create(context)

        coroutineScope.launch {
            val crearRequest= ClearCredentialStateRequest(ClearCredentialStateRequest.TYPE_CLEAR_RESTORE_CREDENTIAL)
            credentialManager.clearCredentialState(crearRequest)
        }
    }
}