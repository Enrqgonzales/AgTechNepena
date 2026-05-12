package com.agtech.nepenya.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.accessibility.VoiceCommandManager;
import com.agtech.nepenya.controller.DashboardController;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.model.repository.RegistroRepository;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.utils.PrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity principal del Dashboard.
 * Muestra clima real, tipo de cambio real y navegacion principal.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class DashboardActivity extends AppCompatActivity implements
        DashboardController.ClimaCallback,
        DashboardController.MultiTipoCambioCallback,
        DashboardController.SyncCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long LOCATION_UPDATE_INTERVAL = 60000;

    private DashboardController controller;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private VoiceCommandManager voiceCommandManager;
    private PrefsManager prefsManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // UI Elements
    private TextView tvSaludo;
    private TextView tvNombre;
    private TextView tvSyncStatus;
    private View dotSyncStatus;
    private TextView tvClima;
    private TextView tvLocalidad;
    private TextView tvTipoCambio;
    private LinearLayout cardRegistrar;
    private LinearLayout cardParcelas;
    private LinearLayout cardReportes;
    private LinearLayout cardInventario;
    private FloatingActionButton fabVoice;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);

        prefsManager = new PrefsManager(this);

        // Si no hay usuario registrado, ir a Bienvenida
        if (prefsManager.getUserId() == -1) {
            startActivity(new Intent(this, BienvenidaActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_dashboard);

        initController();
        initViews();
        initListeners();
        initLocation();
        initVoiceCommand();

        String baseCurrency = prefsManager.getCurrencyBase();
        controller.fetchTipoCambio(baseCurrency, this);
        controller.checkSyncStatus(this);
        mostrarCacheInicial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefsManager != null) {
            controller.checkSyncStatus(this);
            actualizarSaludo();
        }
    }

    private void initController() {
        AppDatabase db = AppDatabase.getInstance(this);
        UsuarioRepository usuarioRepo = new UsuarioRepository(db.usuarioDao());
        ParcelaRepository parcelaRepo = new ParcelaRepository(db.parcelaDao());
        RegistroRepository registroRepo = new RegistroRepository(db.registroDao());
        controller = new DashboardController(this, usuarioRepo, parcelaRepo, registroRepo);
    }

    private void initViews() {
        tvSaludo = findViewById(R.id.tv_saludo);
        tvNombre = findViewById(R.id.tv_nombre);
        tvSyncStatus = findViewById(R.id.tv_sync_status);
        dotSyncStatus = findViewById(R.id.dot_sync_status);
        tvClima = findViewById(R.id.tv_clima);
        tvLocalidad = findViewById(R.id.tv_localidad);
        tvTipoCambio = findViewById(R.id.tv_tipo_cambio);
        cardRegistrar = findViewById(R.id.card_registrar);
        cardParcelas = findViewById(R.id.card_parcelas);
        cardReportes = findViewById(R.id.card_reportes);
        cardInventario = findViewById(R.id.card_inventario);
        fabVoice = findViewById(R.id.fab_voice);
        bottomNav = findViewById(R.id.bottom_nav);

        // Nombre real desde SharedPreferences
        String nombre = prefsManager.getUserName();
        tvNombre.setText(nombre.isEmpty() ? "Agricultor" : nombre);

        // Saludo dinámico
        actualizarSaludo();

        // Marcar ítem activo
        bottomNav.setSelectedItemId(R.id.nav_inicio);
    }

    private void actualizarSaludo() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hora >= 5 && hora < 12) {
            tvSaludo.setText(getString(R.string.saludo_dia));
        } else if (hora >= 12 && hora < 19) {
            tvSaludo.setText(getString(R.string.saludo_tarde));
        } else {
            tvSaludo.setText(getString(R.string.saludo_noche));
        }
    }

    private void mostrarCacheInicial() {
        // Mostrar caché mientras cargan los datos reales
        String climaCache = prefsManager.getClimaCache();
        if (!climaCache.isEmpty()) {
            tvClima.setText(climaCache);
        } else {
            tvClima.setText(getString(R.string.cargando));
        }

        String cambioCache = prefsManager.getCambioCache();
        if (!cambioCache.isEmpty()) {
            tvTipoCambio.setText(cambioCache);
        } else {
            tvTipoCambio.setText(getString(R.string.cargando));
        }
    }

    private void initListeners() {
        cardRegistrar.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));

        cardParcelas.setOnClickListener(v -> startActivity(new Intent(this, MisParcelasActivity.class)));

        cardReportes.setOnClickListener(v -> startActivity(new Intent(this, ReportesActivity.class)));

        cardInventario.setOnClickListener(v -> startActivity(new Intent(this, InventarioActivity.class)));

        fabVoice.setOnClickListener(v -> {
            if (voiceCommandManager != null) {
                voiceCommandManager.startListening(this, this::procesarComandoVoz);
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                return true;
            } else if (id == R.id.nav_registro) {
                startActivity(new Intent(this, RegistroActivity.class));
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                return true;
            } else if (id == R.id.nav_reportes) {
                startActivity(new Intent(this, ReportesActivity.class));
                return true;
            } else if (id == R.id.nav_ajustes) {
                startActivity(new Intent(this, AccesibilidadActivity.class));
                return true;
            }
            return false;
        });
    }

    private void initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (result.getLastLocation() != null) {
                    double lat = result.getLastLocation().getLatitude();
                    double lon = result.getLastLocation().getLongitude();
                    controller.fetchClima(lat, lon, DashboardActivity.this);
                    obtenerNombreLocalidad(lat, lon);
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        };

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                controller.fetchClima(location.getLatitude(), location.getLongitude(), this);
                obtenerNombreLocalidad(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void obtenerNombreLocalidad(double lat, double lon) {
        executorService.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("es", "PE"));
                List<android.location.Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address addr = addresses.get(0);
                    String localidad = addr.getSubAdminArea() != null ? addr.getSubAdminArea()
                            : addr.getAdminArea() != null ? addr.getAdminArea() : "";
                    runOnUiThread(() -> {
                        if (tvLocalidad != null && !localidad.isEmpty()) {
                            tvLocalidad.setText(localidad);
                            tvLocalidad.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (IOException e) {
                // Usar distrito guardado como fallback
                String distrito = prefsManager.getDistrito();
                if (!distrito.isEmpty()) {
                    runOnUiThread(() -> {
                        if (tvLocalidad != null) {
                            tvLocalidad.setText(distrito);
                            tvLocalidad.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private void initVoiceCommand() {
        voiceCommandManager = new VoiceCommandManager();
    }

    private void procesarComandoVoz(String comando) {
        comando = comando.toLowerCase();
        if (comando.contains("registrar") || comando.contains("gasto") || comando.contains("ingreso")) {
            startActivity(new Intent(this, RegistroActivity.class));
        } else if (comando.contains("historial")) {
            startActivity(new Intent(this, HistorialActivity.class));
        } else if (comando.contains("reporte")) {
            startActivity(new Intent(this, ReportesActivity.class));
        } else if (comando.contains("parcela")) {
            startActivity(new Intent(this, MisParcelasActivity.class));
        } else if (comando.contains("ajuste")) {
            startActivity(new Intent(this, AccesibilidadActivity.class));
        } else if (comando.contains("inventario") || comando.contains("almacen")) {
            startActivity(new Intent(this, InventarioActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Sin permiso: usar clima del distrito guardado como referencia
                String distrito = prefsManager.getDistrito();
                String climaCache = prefsManager.getClimaCache();
                if (!climaCache.isEmpty()) {
                    tvClima.setText(climaCache);
                } else {
                    tvClima.setText(getString(R.string.clima_fallback));
                }
                if (!distrito.isEmpty() && tvLocalidad != null) {
                    tvLocalidad.setText(distrito);
                    tvLocalidad.setVisibility(View.VISIBLE);
                }
            }
        } else if (requestCode == 2001) { // VoiceCommandManager PERMISSION_REQUEST_CODE
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, reiniciar escucha si se presionó el FAB
                if (voiceCommandManager != null) {
                    voiceCommandManager.startListening(this, this::procesarComandoVoz);
                }
            } else {
                Toast.makeText(this, "Permiso de audio denegado. Comandos de voz no disponibles.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Callbacks del Controller

    @Override
    public void onClimaSuccess(double temperatura, int weatherCode, String descripcion) {
        String texto = String.format(Locale.getDefault(), "%.0f°C %s", temperatura, descripcion);
        tvClima.setText(texto);
        prefsManager.setClimaCache(texto);
    }

    @Override
    public void onClimaError(String mensaje) {
        String cache = prefsManager.getClimaCache();
        tvClima.setText(cache.isEmpty() ? getString(R.string.clima_fallback) : cache);
    }

    @Override
    public void onTipoCambioSuccess(String base, Double pen, Double usd, Double eur, Double gbp, Double jpy) {
        // Mostrar todas las tasas principales
        StringBuilder texto = new StringBuilder();

        if ("USD".equals(base)) {
            texto.append(String.format(Locale.getDefault(), "1 USD = S/ %.2f (PEN)", pen));
            if (eur != null)
                texto.append(String.format(Locale.getDefault(), " | €%.2f", eur));
        } else if ("PEN".equals(base)) {
            texto.append(String.format(Locale.getDefault(), "S/ 1 = $%.2f (USD)", usd));
            if (eur != null)
                texto.append(String.format(Locale.getDefault(), " | €%.2f", eur));
        } else if ("EUR".equals(base)) {
            texto.append(String.format(Locale.getDefault(), "€1 = S/ %.2f (PEN)", pen));
            if (usd != null)
                texto.append(String.format(Locale.getDefault(), " | $%.2f", usd));
        }

        String resultado = texto.toString();
        tvTipoCambio.setText(resultado);
        prefsManager.setCambioCache(resultado);

        // Guardar tasas para uso en otras partes de la app
        String ratesJson = String.format(Locale.getDefault(),
                "{\"base\":\"%s\",\"PEN\":%.4f,\"USD\":%.4f,\"EUR\":%.4f}",
                base, pen != null ? pen : 0, usd != null ? usd : 0, eur != null ? eur : 0);
        prefsManager.setCurrencyRates(ratesJson);
    }

    @Override
    public void onTipoCambioError(String mensaje) {
        String cache = prefsManager.getCambioCache();
        tvTipoCambio.setText(cache.isEmpty() ? getString(R.string.cambio_fallback) : cache);
    }

    @Override
    public void onSyncStatus(String estado) {
        if ("SYNCED".equals(estado)) {
            dotSyncStatus.setBackgroundResource(R.drawable.dot_green);
            tvSyncStatus.setText(getString(R.string.sincronizado));
        } else {
            dotSyncStatus.setBackgroundResource(R.drawable.dot_red);
            tvSyncStatus.setText(getString(R.string.sin_conexion));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (voiceCommandManager != null) {
            voiceCommandManager.destroy();
        }
        executorService.shutdown();
    }
}
