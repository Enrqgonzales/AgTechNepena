package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.utils.PrefsManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity de Bienvenida mostrada la primera vez que se abre la app.
 * Solicita nombre, telefono y distrito del agricultor.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class BienvenidaActivity extends AppCompatActivity {

    private TextInputEditText etNombre;
    private TextInputEditText etTelefono;
    private Spinner spinnerDistrito;
    private Button btnComenzar;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        btnComenzar.setOnClickListener(v -> guardarUsuario());
    }

    private void guardarUsuario() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
        int distritoPos = spinnerDistrito.getSelectedItemPosition();
        String distrito = DISTRITOS[distritoPos];

        if (nombre.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_nombre_requerido), Toast.LENGTH_SHORT).show();
            etNombre.requestFocus();
            return;
        }

        if (distritoPos == 0) {
            Toast.makeText(this, getString(R.string.error_distrito_requerido), Toast.LENGTH_SHORT).show();
            return;
        }

        btnComenzar.setEnabled(false);

        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            UsuarioRepository usuarioRepo = new UsuarioRepository(db.usuarioDao());

            Usuario usuario = new Usuario(nombre, telefono);
            long userId = usuarioRepo.insertar(usuario);

            PrefsManager prefs = new PrefsManager(this);
            prefs.setUserId((int) userId);
            prefs.setUserName(nombre);
            prefs.setDistrito(distrito);

            runOnUiThread(() -> {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
