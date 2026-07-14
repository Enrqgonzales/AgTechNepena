package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.AgTechApp;
import com.agtech.nepenya.controller.BienvenidaController;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.utils.PinDialogHelper;
import com.agtech.nepenya.utils.PrefsManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Activity de Bienvenida mostrada la primera vez que se abre la app.
 * Solicita nombre, telefono y distrito del agricultor.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class BienvenidaActivity extends AppCompatActivity implements
        BienvenidaController.BienvenidaCallback {

    private TextInputEditText etNombre;
    private TextInputEditText etTelefono;
    private Spinner spinnerDistrito;
    private Button btnComenzar;
    private com.google.android.gms.common.SignInButton btnGoogleSignIn;

    private BienvenidaController controller;
    private PrefsManager prefsManager;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private String currentFirebaseUid = null;

    private static final String[] DISTRITOS = {
            "Seleccione su distrito",
            "Nepeña",
            "Moro",
            "Samanco",
            "Chimbote",
            "Nuevo Chimbote",
            "Coishco",
            "Santa",
            "Otro"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        etNombre = findViewById(R.id.et_nombre);
        etTelefono = findViewById(R.id.et_telefono);
        spinnerDistrito = findViewById(R.id.spinner_distrito);
        btnComenzar = findViewById(R.id.btn_comenzar);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DISTRITOS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrito.setAdapter(adapter);

        controller = new BienvenidaController(this);
        prefsManager = new PrefsManager(this);

        // Firebase & Google Init
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnComenzar.setOnClickListener(v -> guardarUsuario());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
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
                            currentFirebaseUid = user.getUid();
                            etNombre.setText(user.getDisplayName());
                            // Escenario A: Buscar si ya existe
                            verificarUsuarioExistente(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Error de Firebase Auth", Toast.LENGTH_SHORT).show();
                        btnGoogleSignIn.setEnabled(true);
                    }
                });
    }

    private void verificarUsuarioExistente(String uid) {
        // Primero localmente
        controller.buscarUsuarioLocal(uid, new BienvenidaController.BienvenidaCallback() {
            @Override
            public void onUsuarioCreado(int userId) {}

            @Override
            public void onUsuarioRecuperado(Usuario usuario, String distrito) {
                runOnUiThread(() -> {
                    Toast.makeText(BienvenidaActivity.this, "Bienvenido de nuevo, " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
                    irAlDashboard();
                });
            }

            @Override
            public void onError(String mensaje) {
                // No está local, buscar en la nube (Escenario A vs B)
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
                    Toast.makeText(BienvenidaActivity.this, "Datos recuperados de la nube.", Toast.LENGTH_SHORT).show();
                    irAlDashboard();
                });
            }

            @Override
            public void onError(String mensaje) {
                // Escenario B: No hay datos o error
                runOnUiThread(() -> {
                    Toast.makeText(BienvenidaActivity.this, "Usuario nuevo. Complete su registro.", Toast.LENGTH_SHORT).show();
                    btnGoogleSignIn.setVisibility(android.view.View.GONE);
                    btnGoogleSignIn.setEnabled(true);
                });
            }
        });
    }

    private void guardarUsuario() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
        int distritoPos = spinnerDistrito.getSelectedItemPosition();
        String distrito = DISTRITOS[distritoPos];

        String error = controller.validarDatos(nombre, distritoPos);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            if (nombre.isEmpty()) {
                etNombre.requestFocus();
            }
            return;
        }

        btnComenzar.setEnabled(false);
        controller.crearUsuario(nombre, telefono, distrito, currentFirebaseUid, this);
    }

    @Override
    public void onUsuarioCreado(int userId) {
        runOnUiThread(this::mostrarOpcionPinInicial);
    }

    @Override
    public void onUsuarioRecuperado(Usuario usuario, String distrito) {
        runOnUiThread(this::irAlDashboard);
    }

    private void mostrarOpcionPinInicial() {
        android.widget.LinearLayout contenido = new android.widget.LinearLayout(this);
        contenido.setOrientation(android.widget.LinearLayout.VERTICAL);
        contenido.setPadding(32, 8, 32, 0);

        android.widget.Button btnCrearPin = new android.widget.Button(this);
        btnCrearPin.setText("Crear PIN");
        contenido.addView(btnCrearPin, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        android.widget.Button btnOmitir = new android.widget.Button(this);
        btnOmitir.setText("Omitir por ahora");
        contenido.addView(btnOmitir, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        android.widget.TextView tvNota = new android.widget.TextView(this);
        tvNota.setText("Podrás activarlo más tarde desde Ajustes → Seguridad.");
        tvNota.setTextSize(12);
        tvNota.setTextColor(getColor(android.R.color.darker_gray));
        tvNota.setPadding(0, 12, 0, 0);
        contenido.addView(tvNota);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Quieres proteger tu app con un PIN?")
                .setView(contenido)
                .setCancelable(false)
                .create();

        btnCrearPin.setOnClickListener(v -> {
            dialog.dismiss();
            PinDialogHelper.mostrarCrearPin(
                        this,
                        prefsManager,
                        "Crear PIN de la app",
                        "Ingrese un PIN numérico de 4 dígitos para proteger el acceso a la app:",
                        this::irAlDashboard,
                        this::irAlDashboard
                );
        });
        btnOmitir.setOnClickListener(v -> {
            dialog.dismiss();
            irAlDashboard();
        });

        dialog.show();
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
        runOnUiThread(() -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            btnComenzar.setEnabled(true);
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
