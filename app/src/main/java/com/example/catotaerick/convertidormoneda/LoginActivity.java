package com.example.catotaerick.convertidormoneda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(this,
                                    "Error con Google: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Si ya hay sesión activa ir directo a MainActivity
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual != null) {
            navegarAMain();
            return;
        }

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Conectar vistas
        tilEmail     = findViewById(R.id.tilEmail);
        tilPassword  = findViewById(R.id.tilPassword);
        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        btnLogin     = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> iniciarSesion());
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> recuperarContrasena());
        findViewById(R.id.tvRegister).setOnClickListener(v -> iniciarSesionGoogle());
    }

    // ── LOGIN EMAIL/PASSWORD ───────────────────────────────────
    private void iniciarSesion() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (!validarCampos(email, password)) return;

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navegarAMain();
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("INICIAR SESIÓN");
                        tilPassword.setError("Correo o contraseña incorrectos");
                    }
                });
    }

    // ── REGISTRO EMAIL/PASSWORD ────────────────────────────────
    private void registrar() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (!validarCampos(email, password)) return;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Cuenta creada exitosamente",
                                Toast.LENGTH_SHORT).show();
                        navegarAMain();
                    } else {
                        tilEmail.setError("Correo ya registrado o inválido");
                    }
                });
    }

    // ── GOOGLE SIGN-IN ─────────────────────────────────────────
    private void iniciarSesionGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navegarAMain();
                    } else {
                        Toast.makeText(this,
                                "Error al autenticar con Google",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── RECUPERAR CONTRASEÑA ───────────────────────────────────
    private void recuperarContrasena() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            tilEmail.setError("Ingresa tu correo primero");
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Correo de recuperación enviado",
                                Toast.LENGTH_LONG).show();
                    } else {
                        tilEmail.setError("Correo no encontrado");
                    }
                });
    }

    // ── VALIDACIONES ───────────────────────────────────────────
    private boolean validarCampos(String email, String password) {
        boolean valido = true;
        tilEmail.setError(null);
        tilPassword.setError(null);
        if (email.isEmpty()) {
            tilEmail.setError("Ingresa tu correo");
            valido = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Ingresa tu contraseña");
            valido = false;
        }
        return valido;
    }

    // ── NAVEGACIÓN ─────────────────────────────────────────────
    private void navegarAMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}