package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.controller.BienvenidaController;
import com.google.android.material.textfield.TextInputEditText;

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

    private BienvenidaController controller;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DISTRITOS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrito.setAdapter(adapter);

        controller = new BienvenidaController(this);

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
        controller.crearUsuario(nombre, telefono, distrito, this);
    }

    @Override
    public void onUsuarioCreado(int userId) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        btnComenzar.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.destroy();
        }
    }
}
