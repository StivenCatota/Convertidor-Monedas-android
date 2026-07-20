package com.example.catotaerick.convertidormoneda

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.catotaerick.convertidormoneda.databinding.ActivityLoginBinding
import com.example.catotaerick.convertidormoneda.ui.SettingsFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.core.widget.doOnTextChanged

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── IMPORTANTE: Aplicar el tema guardado ANTES de setContentView ──
        SettingsFragment.applyTheme(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            navegarAMain()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.etEmail.doOnTextChanged { _, _, _, _ -> binding.tilEmail.error = null }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> binding.tilPassword.error = null }
        binding.btnLogin.setOnClickListener { iniciarSesion() }
        binding.btnGoogle.setOnClickListener { iniciarSesionGoogle() }
        binding.tvRegister.setOnClickListener { registrar() }
        binding.tvForgotPassword.setOnClickListener { recuperarContrasena() }
    }

    private fun iniciarSesion() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = if (email.isEmpty()) "Ingresa tu correo" else null
        binding.tilPassword.error = if (password.isEmpty()) "Ingresa tu contraseña" else null

        if (email.isEmpty() || password.isEmpty()) {
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navegarAMain()
                } else {
                    mostrarMensaje("Error: ${task.exception?.message}")
                }
            }
    }

    private fun registrar() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = if (email.isEmpty()) "Ingresa tu correo" else null
        binding.tilPassword.error = when {
            password.isEmpty() -> "Ingresa tu contraseña"
            password.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }

        if (email.isEmpty() || password.isEmpty() || password.length < 6) {
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mostrarMensaje("Cuenta creada exitosamente")
                    navegarAMain()
                } else {
                    mostrarMensaje("Error: ${task.exception?.message}")
                }
            }
    }

    private fun iniciarSesionGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 123)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                    // El usuario cerró el selector sin elegir cuenta, no es un error real
                } else {
                    mostrarMensaje("Error: ${e.message}")
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navegarAMain()
                } else {
                    mostrarMensaje("Error: ${task.exception?.message}")
                }
            }
    }

    private fun recuperarContrasena() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            mostrarMensaje("Ingresa tu correo para recuperar la contraseña")
            return
        }

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mostrarMensaje("Correo de recuperación enviado")
                } else {
                    mostrarMensaje("Error: ${task.exception?.message}")
                }
            }
    }

    private fun navegarAMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
    }
}
