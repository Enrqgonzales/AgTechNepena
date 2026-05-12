package com.agtech.nepenya.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.controller.AccesibilidadController;
import com.agtech.nepenya.utils.PrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity de Ajustes de Vista/Accesibilidad.
 * Gestiona fuente, brillo, tema y asistente de voz.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class AccesibilidadActivity extends AppCompatActivity implements
        AccesibilidadController.AccesibilidadCallback,
        AccesibilidadController.ValoresCallback {

    private AccesibilidadController controller;

    // UI Elements
    private SeekBar seekBarFuente;
    private TextView tvPreviewFuente;
    private SeekBar seekBarBrillo;
    private Button btnDia;
    private Button btnNoche;
    private SwitchCompat switchVoz;

    private int currentFontSize = 16;
    private float currentBrightness = 0.5f;
    private String currentTheme = "DIA";
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessibilityPrefs.applyAll(this);
        setContentView(R.layout.activity_accesibilidad);

        prefsManager = new PrefsManager(this);
        initController();
        initViews();
        initListeners();

        // Read current theme synchronously and apply button state immediately
        currentTheme = prefsManager.getThemeMode();
        updateThemeButtons();

        // Cargar valores actuales (async, will also call updateThemeButtons)
        controller.obtenerValoresActuales(this);
    }

    private void initController() {
        controller = new AccesibilidadController(this, prefsManager);
    }

    private void initViews() {
        seekBarFuente = findViewById(R.id.seekbar_fuente);
        tvPreviewFuente = findViewById(R.id.tv_preview_fuente);
        seekBarBrillo = findViewById(R.id.seekbar_brillo);
        btnDia = findViewById(R.id.btn_dia);
        btnNoche = findViewById(R.id.btn_noche);
        switchVoz = findViewById(R.id.switch_voz);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_ajustes);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ajustes)
                return true;
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_registro) {
                startActivity(new Intent(this, RegistroActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reportes) {
                startActivity(new Intent(this, ReportesActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void initListeners() {
        // SeekBar de fuente (12sp - 32sp, pasos de 2sp)
        seekBarFuente.setMax(10); // (32 - 12) / 2 = 10 pasos
        seekBarFuente.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFontSize = 12 + (progress * 2); // 12, 14, 16... hasta 32
                tvPreviewFuente.setTextSize(currentFontSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                controller.guardarFuente(currentFontSize, new AccesibilidadController.AccesibilidadCallback() {
                    @Override
                    public void onGuardadoExitoso() {
                        AccessibilityPrefs.applyFontScale(AccesibilidadActivity.this, currentFontSize);
                        recreate();
                    }

                    @Override
                    public void onError(String mensaje) {
                        Toast.makeText(AccesibilidadActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // SeekBar de brillo (0-100%)
        seekBarBrillo.setMax(100);
        seekBarBrillo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentBrightness = progress / 100f;
                // Aplicar brillo inmediatamente para preview
                controller.guardarBrillo(currentBrightness, new AccesibilidadController.AccesibilidadCallback() {
                    @Override
                    public void onGuardadoExitoso() {
                    }

                    @Override
                    public void onError(String mensaje) {
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Botones de tema
        btnDia.setOnClickListener(v -> {
            currentTheme = "DIA";
            updateThemeButtons();
            controller.guardarTema("DIA", this);
        });

        btnNoche.setOnClickListener(v -> {
            currentTheme = "NOCHE";
            updateThemeButtons();
            controller.guardarTema("NOCHE", this);
        });

        // Switch de voz
        switchVoz.setOnCheckedChangeListener((buttonView, isChecked) -> controller.guardarVoz(isChecked, this));

        // Bottom navigation
        findViewById(R.id.nav_inicio).setOnClickListener(v -> {
            finish();
            startActivity(new android.content.Intent(this, DashboardActivity.class));
        });

        findViewById(R.id.nav_registro).setOnClickListener(v -> {
            finish();
            startActivity(new android.content.Intent(this, RegistroActivity.class));
        });

        findViewById(R.id.nav_historial).setOnClickListener(v -> {
            finish();
            startActivity(new android.content.Intent(this, HistorialActivity.class));
        });

        findViewById(R.id.nav_reportes).setOnClickListener(v -> {
            finish();
            startActivity(new android.content.Intent(this, ReportesActivity.class));
        });
    }

    private void updateThemeButtons() {
        if ("DIA".equals(currentTheme)) {
            btnDia.setBackgroundColor(Color.parseColor("#2E7D32"));
            btnDia.setTextColor(Color.WHITE);
            btnNoche.setBackgroundColor(Color.TRANSPARENT);
            btnNoche.setTextColor(getColor(android.R.color.darker_gray));
        } else {
            btnDia.setBackgroundColor(Color.TRANSPARENT);
            btnDia.setTextColor(getColor(android.R.color.darker_gray));
            btnNoche.setBackgroundColor(Color.parseColor("#2E7D32"));
            btnNoche.setTextColor(Color.WHITE);
        }
    }

    // Callbacks del Controller

    @Override
    public void onValores(int fontSize, float brightness, String themeMode, boolean voiceEnabled) {
        currentFontSize = fontSize;
        currentBrightness = brightness;
        currentTheme = themeMode;

        // Aplicar valores a la UI (convertir tamaño a progreso: (size - 12) / 2)
        int fontProgress = Math.max(0, Math.min(10, (fontSize - 12) / 2));
        seekBarFuente.setProgress(fontProgress);
        tvPreviewFuente.setTextSize(fontSize);
        seekBarBrillo.setProgress((int) (brightness * 100));
        switchVoz.setChecked(voiceEnabled);

        updateThemeButtons();
    }

    @Override
    public void onGuardadoExitoso() {
        // Los cambios se aplican inmediatamente, no es necesario mostrar toast
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.shutdown();
        }
    }
}
