package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.AgTechApp;
import com.agtech.nepenya.controller.BienvenidaController;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.utils.PinDialogHelper;
import com.agtech.nepenya.utils.PrefsManager;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity para completar el perfil del agricultor tras el primer login con Google.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class CompletarPerfilActivity extends AppCompatActivity implements
        BienvenidaController.BienvenidaCallback {

    private TextView tvEmailGoogle;
    private TextInputEditText etNombre;
    private TextInputEditText etTelefono;
    private Spinner spinnerDistrito;
    private Button btnComenzar;

    private BienvenidaController controller;
    private PrefsManager prefsManager;
    private String firebaseUid;

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
        setContentView(R.layout.activity_completar_perfil);

        tvEmailGoogle = findViewById(R.id.tv_email_google);
        etNombre = findViewById(R.id.et_nombre);
        etTelefono = findViewById(R.id.et_telefono);
        spinnerDistrito = findViewById(R.id.spinner_distrito);
        btnComenzar = findViewById(R.id.btn_comenzar);

        controller = new BienvenidaController(this);
        prefsManager = new PrefsManager(this);

        // Obtener datos del Intent
        if (getIntent() != null) {
            firebaseUid = getIntent().getStringExtra("FIREBASE_UID");
            String nombreGoogle = getIntent().getStringExtra("DISPLAY_NAME");
            String emailGoogle = getIntent().getStringExtra("EMAIL");

            if (nombreGoogle != null) etNombre.setText(nombreGoogle);
            if (emailGoogle != null) tvEmailGoogle.setText(emailGoogle);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DISTRITOS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrito.setAdapter(adapter);

        btnComenzar.setOnClickListener(v -> guardarUsuario());
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
        controller.crearUsuario(nombre, telefono, distrito, firebaseUid, this);
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
                    "Ingrese un PIN numérico de 4 dígitos:",
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
