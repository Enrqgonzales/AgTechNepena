package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.AgTechApp;
import com.agtech.nepenya.controller.BienvenidaController;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.utils.PrefsManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Activity de Bienvenida mostrada la primera vez que se abre la app.
 * Gestiona el inicio de sesión con Google.
 *
 * @author AgTech Nepeña Team
 * @version 1.1
 */
public class BienvenidaActivity extends AppCompatActivity implements
        BienvenidaController.BienvenidaCallback {

    private com.google.android.gms.common.SignInButton btnGoogleSignIn;

    private BienvenidaController controller;
    private PrefsManager prefsManager;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private boolean isVerifyingExistingUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        btnGoogleSignIn = findViewById(R.id.btn_google_signin);

        controller = new BienvenidaController(this);
        prefsManager = new PrefsManager(this);

        // Firebase & Google Init
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // Verificación automática si ya está autenticado en Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            verificarUsuarioExistente(currentUser.getUid());
        }
    }

    private void signInWithGoogle() {
        btnGoogleSignIn.setEnabled(false);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            com.google.android.gms.tasks.Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error de Google Sign-In: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnGoogleSignIn.setEnabled(true);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        btnGoogleSignIn.setEnabled(false);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            verificarUsuarioExistente(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Error de Firebase Auth", Toast.LENGTH_SHORT).show();
                        btnGoogleSignIn.setEnabled(true);
                    }
                });
    }

    private void verificarUsuarioExistente(String uid) {
        if (isVerifyingExistingUser) {
            return;
        }
        isVerifyingExistingUser = true;

        // Primero localmente
        controller.buscarUsuarioLocal(uid, new BienvenidaController.BienvenidaCallback() {
            @Override
            public void onUsuarioCreado(int userId) {}

            @Override
            public void onUsuarioRecuperado(Usuario usuario, String distrito) {
                runOnUiThread(() -> {
                    isVerifyingExistingUser = false;
                    Toast.makeText(BienvenidaActivity.this, "Bienvenido de nuevo, " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
                    irAlDashboard();
                });
            }

            @Override
            public void onError(String mensaje) {
                // No está local, buscar en la nube
                buscarEnNube(uid);
            }
        });
    }

    private void buscarEnNube(String uid) {
        controller.buscarUsuarioNube(uid, new BienvenidaController.BienvenidaCallback() {
            @Override
            public void onUsuarioCreado(int userId) {}

            @Override
            public void onUsuarioRecuperado(Usuario usuario, String distrito) {
                runOnUiThread(() -> {
                    isVerifyingExistingUser = false;
                    Toast.makeText(BienvenidaActivity.this, "Datos recuperados de la nube.", Toast.LENGTH_SHORT).show();
                    irAlDashboard();
                });
            }

            @Override
            public void onError(String mensaje) {
                // No hay datos -> Ir a Completar Perfil
                runOnUiThread(() -> {
                    isVerifyingExistingUser = false;
                    irACompletarPerfil();
                });
            }
        });
    }

    private void irACompletarPerfil() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, CompletarPerfilActivity.class);
            intent.putExtra("FIREBASE_UID", user.getUid());
            intent.putExtra("DISPLAY_NAME", user.getDisplayName());
            intent.putExtra("EMAIL", user.getEmail());
            startActivity(intent);
            finish();
        } else {
            btnGoogleSignIn.setEnabled(true);
        }
    }

    @Override
    public void onUsuarioCreado(int userId) {
        // No se usa en esta Activity
    }

    @Override
    public void onUsuarioRecuperado(Usuario usuario, String distrito) {
        isVerifyingExistingUser = false;
        runOnUiThread(this::irAlDashboard);
    }

    private void irAlDashboard() {
        AgTechApp.markAppUnlocked();
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(String mensaje) {
        isVerifyingExistingUser = false;
        runOnUiThread(() -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            btnGoogleSignIn.setEnabled(true);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.destroy();
        }
    }
}
