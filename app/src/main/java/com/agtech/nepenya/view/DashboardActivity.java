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
import com.agtech.nepenya.AgTechApp;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.accessibility.VoiceCommandHelper;
import com.agtech.nepenya.controller.DashboardController;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.model.repository.RegistroRepository;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.utils.PinDialogHelper;
import com.agtech.nepenya.utils.PrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

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
    private VoiceCommandHelper voiceCommandHelper;
    private PrefsManager prefsManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Fields for sync observation
    private ConnectivityManager.NetworkCallback networkCallback;
    private final MutableLiveData<Boolean> isOnlineLiveData = new MutableLiveData<>();
    private final MediatorLiveData<Integer> totalPendientesLiveData = new MediatorLiveData<>();
    private final int[] syncCounts = new int[5]; // usuarios, parcelas, registros, inventario, movimientos
    private boolean isSyncingPeriodic = false;
    private boolean isSyncingImmediate = false;
    private boolean wasSyncing = false;
    private AlphaAnimation blinkingAnimation;
    private boolean isPinDialogShowing = false;
    private boolean isDashboardInicializado = false;

    // UI Elements
    private TextView tvSaludo;
    private TextView tvNombre;
    private View layoutSyncStatus;
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
        super.onCreate(savedInstanceState);
        AccessibilityPrefs.applyAll(this);

        prefsManager = new PrefsManager(this);

        // Si no hay usuario registrado, ir a Bienvenida
        if (prefsManager.getUserId() == -1) {
            startActivity(new Intent(this, BienvenidaActivity.class));
            finish();
            return;
        }

        // Siempre inflamos el layout e inicializamos las vistas
        setContentView(R.layout.activity_dashboard);
        initController();
        initViews();
        initListeners();

        boolean needsUnlock = prefsManager.getAdminPin() != null && AgTechApp.shouldRequireAppUnlock();
        if (needsUnlock) {
            // Ocultamos el contenido inmediatamente para evitar parpadeos
            findViewById(android.R.id.content).setVisibility(View.INVISIBLE);
        } else {
            // Ya está desbloqueado o no tiene PIN
            AgTechApp.markAppUnlocked();
            comenzarServiciosYDatos();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefsManager != null) {
            boolean needsUnlock = prefsManager.getAdminPin() != null && AgTechApp.shouldRequireAppUnlock();
            if (needsUnlock) {
                findViewById(android.R.id.content).setVisibility(View.INVISIBLE);
                if (!isPinDialogShowing) {
                    isPinDialogShowing = true;
                    PinDialogHelper.mostrarVerificarPin(
                            this,
                            prefsManager,
                            "PIN de la app",
                            "Ingrese su PIN de 4 dígitos para continuar:",
                            () -> {
                                isPinDialogShowing = false;
                                AgTechApp.markAppUnlocked();
                                findViewById(android.R.id.content).setVisibility(View.VISIBLE);
                                comenzarServiciosYDatos();
                                actualizarSaludo();
                                actualizarVoiceFab();
                            },
                            false
                    );
                }
            } else {
                findViewById(android.R.id.content).setVisibility(View.VISIBLE);
                comenzarServiciosYDatos();
                actualizarSaludo();
                actualizarVoiceFab();
            }
        }
    }

    private void comenzarServiciosYDatos() {
        if (isDashboardInicializado) return;
        isDashboardInicializado = true;

        initLocation();
        initVoiceCommand();

        String baseCurrency = prefsManager.getCurrencyBase();
        controller.fetchTipoCambio(baseCurrency, this);
        initSyncObserver();
        mostrarCacheInicial();
    }

    private void actualizarVoiceFab() {
        if (fabVoice != null) {
            if (prefsManager.isVoiceEnabled()) {
                fabVoice.setVisibility(View.VISIBLE);
            } else {
                fabVoice.setVisibility(View.GONE);
            }
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
        layoutSyncStatus = findViewById(R.id.layout_sync_status);
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
        layoutSyncStatus.setOnClickListener(v -> {
            boolean online = isOnlineLiveData.getValue() != null && isOnlineLiveData.getValue();
            if (online) {
                Toast.makeText(this, R.string.sincronizando, Toast.LENGTH_SHORT).show();
                com.agtech.nepenya.sync.SyncManager.syncNow(this);
            } else {
                Toast.makeText(this, R.string.sin_conexion, Toast.LENGTH_SHORT).show();
            }
        });

        cardRegistrar.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));

        cardParcelas.setOnClickListener(v -> startActivity(new Intent(this, MisParcelasActivity.class)));

        cardReportes.setOnClickListener(v -> startActivity(new Intent(this, ReportesActivity.class)));

        cardInventario.setOnClickListener(v -> startActivity(new Intent(this, InventarioActivity.class)));

        fabVoice.setOnClickListener(v -> {
            if (voiceCommandHelper != null) {
                voiceCommandHelper.startListening();
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
        if (executorService.isShutdown()) return;

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
        voiceCommandHelper = new VoiceCommandHelper(this, new VoiceCommandHelper.CommandCallback() {
            @Override
            public void onCommandRecognized(String command, String cleanText) {
                String speakText = "";
                Intent intent = null;
                switch (command) {
                    case "REGISTRO":
                        speakText = "Abriendo registro";
                        intent = new Intent(DashboardActivity.this, RegistroActivity.class);
                        break;
                    case "GASTO":
                        speakText = "Abriendo registro de gasto";
                        intent = new Intent(DashboardActivity.this, RegistroActivity.class);
                        intent.putExtra("TIPO", "GASTO");
                        break;
                    case "INGRESO":
                        speakText = "Abriendo registro de ingreso";
                        intent = new Intent(DashboardActivity.this, RegistroActivity.class);
                        intent.putExtra("TIPO", "INGRESO");
                        break;
                    case "PARCELAS":
                        speakText = "Abriendo parcelas";
                        intent = new Intent(DashboardActivity.this, MisParcelasActivity.class);
                        break;
                    case "HISTORIAL":
                        speakText = "Abriendo historial";
                        intent = new Intent(DashboardActivity.this, HistorialActivity.class);
                        break;
                    case "REPORTES":
                        speakText = "Abriendo reportes";
                        intent = new Intent(DashboardActivity.this, ReportesActivity.class);
                        break;
                    case "INVENTARIO":
                        speakText = "Abriendo inventario";
                        intent = new Intent(DashboardActivity.this, InventarioActivity.class);
                        break;
                    case "AJUSTES":
                        speakText = "Abriendo ajustes";
                        intent = new Intent(DashboardActivity.this, AccesibilidadActivity.class);
                        break;
                }
                if (intent != null) {
                    voiceCommandHelper.speak(speakText);
                    final Intent finalIntent = intent;
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(finalIntent);
                    }, 1200);
                }
            }

            @Override
            public void onCommandNotRecognized(String originalText) {
                voiceCommandHelper.speak("No entendí, intenta de nuevo");
            }

            @Override
            public void onError(String errorMsg) {
                if ("NO_PERMISSION".equals(errorMsg)) {
                    ActivityCompat.requestPermissions(DashboardActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, 2001);
                } else if ("RECOGNIZER_UNAVAILABLE".equals(errorMsg)) {
                    Toast.makeText(DashboardActivity.this, "El reconocimiento de voz no está disponible", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "Error de voz: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        } else if (requestCode == 2001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (voiceCommandHelper != null) {
                    voiceCommandHelper.startListening();
                }
            } else {
                Toast.makeText(this, "Permiso de audio denegado. El asistente de voz no puede funcionar.",
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
    public void onTipoCambioSuccess(String base, Double pen, Double usd, Double eur, Double gbp, Double jpy,
            Double cny) {
        // Store all rates in prefs (base is always USD)
        String ratesJson = String.format(Locale.US,
                "{\"base\":\"USD\",\"PEN\":%.4f,\"EUR\":%.4f,\"GBP\":%.4f,\"JPY\":%.4f,\"CNY\":%.4f}",
                pen != null ? pen : 0,
                eur != null ? eur : 0,
                gbp != null ? gbp : 0,
                jpy != null ? jpy : 0,
                cny != null ? cny : 0);
        prefsManager.setCurrencyRates(ratesJson);

        // Display current index
        mostrarTasaActual();

        // Make widget tappable to cycle through currencies
        tvTipoCambio.setClickable(true);
        tvTipoCambio.setOnClickListener(v -> {
            int next = (prefsManager.getCurrencyIndex() + 1) % 5;
            prefsManager.setCurrencyIndex(next);
            mostrarTasaActual();
        });
    }

    private void mostrarTasaActual() {
        int index = prefsManager.getCurrencyIndex();
        String[] codes = { "PEN", "EUR", "GBP", "JPY", "CNY" };
        String[] labels = { "S/", "\u20AC", "\u00A3", "\u00A5", "\u00A5 CNY" };
        boolean[] noDecimal = { false, false, false, true, false };

        String code = codes[index];
        double rate = prefsManager.getCurrencyRate(code);
        String label = labels[index];

        String texto;
        if (noDecimal[index]) {
            texto = String.format(Locale.US, "1 USD = %s %.0f", label, rate);
        } else {
            texto = String.format(Locale.US, "1 USD = %s %.2f", label, rate);
        }

        tvTipoCambio.setText(texto);
        prefsManager.setCambioCache(texto);
    }

    @Override
    public void onTipoCambioError(String mensaje) {
        String cache = prefsManager.getCambioCache();
        tvTipoCambio.setText(cache.isEmpty() ? getString(R.string.cambio_fallback) : cache);
    }

    @Override
    public void onSyncStatus(String estado) {
        // Obsoleto: Usamos initSyncObserver() con LiveData reactivo en su lugar
    }

    private void initSyncObserver() {
        AppDatabase db = AppDatabase.getInstance(this);

        // 1. Monitor network changes
        isOnlineLiveData.setValue(com.agtech.nepenya.utils.NetworkUtils.isConnected(this));
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    isOnlineLiveData.postValue(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    isOnlineLiveData.postValue(false);
                }
            };
            try {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. Observe pending counts from Room DAOs
        totalPendientesLiveData.addSource(db.usuarioDao().contarPendientesLiveData(), val -> {
            syncCounts[0] = val != null ? val : 0;
            actualizarTotalPendientes();
        });
        totalPendientesLiveData.addSource(db.parcelaDao().contarPendientesLiveData(), val -> {
            syncCounts[1] = val != null ? val : 0;
            actualizarTotalPendientes();
        });
        totalPendientesLiveData.addSource(db.registroDao().contarPendientesLiveData(), val -> {
            syncCounts[2] = val != null ? val : 0;
            actualizarTotalPendientes();
        });
        totalPendientesLiveData.addSource(db.inventarioDao().contarPendientesLiveData(), val -> {
            syncCounts[3] = val != null ? val : 0;
            actualizarTotalPendientes();
        });
        totalPendientesLiveData.addSource(db.inventarioMovimientoDao().contarPendientesLiveData(), val -> {
            syncCounts[4] = val != null ? val : 0;
            actualizarTotalPendientes();
        });

        // 3. Observe WorkManager sync tasks
        WorkManager.getInstance(this).getWorkInfosByTagLiveData("agtech_sync_work")
                .observe(this, workInfos -> {
                    isSyncingPeriodic = checkAnyWorkRunning(workInfos);
                    combinarYActualizarEstadoSincronizacion();
                });

        WorkManager.getInstance(this).getWorkInfosByTagLiveData("agtech_sync_work_immediate")
                .observe(this, workInfos -> {
                    isSyncingImmediate = checkAnyWorkRunning(workInfos);
                    combinarYActualizarEstadoSincronizacion();
                });

        // Observe online and pending changes
        isOnlineLiveData.observe(this, online -> combinarYActualizarEstadoSincronizacion());
        totalPendientesLiveData.observe(this, count -> combinarYActualizarEstadoSincronizacion());
    }

    private void actualizarTotalPendientes() {
        int sum = 0;
        for (int count : syncCounts) {
            sum += count;
        }
        totalPendientesLiveData.setValue(sum);
    }

    private boolean checkAnyWorkRunning(List<WorkInfo> workInfos) {
        if (workInfos != null) {
            for (WorkInfo info : workInfos) {
                if (info.getState() == WorkInfo.State.RUNNING) {
                    return true;
                }
            }
        }
        return false;
    }

    private void combinarYActualizarEstadoSincronizacion() {
        boolean online = isOnlineLiveData.getValue() != null && isOnlineLiveData.getValue();
        int pendientes = totalPendientesLiveData.getValue() != null ? totalPendientesLiveData.getValue() : 0;
        boolean syncing = isSyncingPeriodic || isSyncingImmediate;

        // Feedback al terminar sincronización
        if (wasSyncing && !syncing && online) {
            Toast.makeText(this, R.string.sincronizacion_completa, Toast.LENGTH_SHORT).show();
        }
        wasSyncing = syncing;

        // Si hay conexión y hay cambios pendientes pero no se está ejecutando el worker,
        // forzar una sincronización inmediata
        if (online && pendientes > 0 && !syncing) {
            com.agtech.nepenya.sync.SyncManager.syncNow(this);
            syncing = true;
        }

        actualizarUIEstadoSync(online, pendientes, syncing);
    }

    private void actualizarUIEstadoSync(boolean online, int pendientes, boolean syncing) {
        if (syncing) {
            dotSyncStatus.setBackgroundResource(R.drawable.dot_yellow);
            tvSyncStatus.setText("Sincronizando...");
            tvSyncStatus.setTextColor(ContextCompat.getColor(this, R.color.sync_yellow));
            startBlinking(tvSyncStatus);
        } else if (!online && pendientes > 0) {
            dotSyncStatus.setBackgroundResource(R.drawable.dot_red);
            tvSyncStatus.setText(getString(R.string.sin_conexion));
            tvSyncStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_red));
            stopBlinking(tvSyncStatus);
        } else {
            // Sincronizado
            dotSyncStatus.setBackgroundResource(R.drawable.dot_green);
            tvSyncStatus.setText(getString(R.string.sincronizado));
            tvSyncStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green));
            stopBlinking(tvSyncStatus);
        }
    }

    private void startBlinking(View view) {
        if (blinkingAnimation == null) {
            blinkingAnimation = new AlphaAnimation(1.0f, 0.3f);
            blinkingAnimation.setDuration(800);
            blinkingAnimation.setRepeatMode(Animation.REVERSE);
            blinkingAnimation.setRepeatCount(Animation.INFINITE);
        }
        if (view.getAnimation() == null) {
            view.startAnimation(blinkingAnimation);
        }
    }

    private void stopBlinking(View view) {
        view.clearAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (voiceCommandHelper != null) {
            voiceCommandHelper.destroy();
        }
        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
        }
        executorService.shutdown();
    }
}
