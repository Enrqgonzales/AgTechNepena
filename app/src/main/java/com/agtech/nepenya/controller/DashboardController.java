package com.agtech.nepenya.controller;

import android.app.Activity;

import com.agtech.nepenya.model.repository.RegistroRepository;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Controller para la pantalla de Dashboard.
 * Gestiona logica de clima, tipo de cambio y estado de sincronizacion.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class DashboardController {

    private final Activity activity;
    private final UsuarioRepository usuarioRepository;
    private final ParcelaRepository parcelaRepository;
    private final RegistroRepository registroRepository;
    private final ExecutorService executorService;

    private final Retrofit retrofitOpenMeteo;
    private final Retrofit retrofitExchangeRate;

    /**
     * Callback para resultado de clima.
     */
    public interface ClimaCallback {
        void onClimaSuccess(double temperatura, int weatherCode, String descripcion);
        void onClimaError(String mensaje);
    }

    /**
     * Callback para resultado de tipo de cambio.
     */
    public interface TipoCambioCallback {
        void onTipoCambioSuccess(double tipoCambio);
        void onTipoCambioError(String mensaje);
    }

    /**
     * Callback para estado de sincronizacion.
     */
    public interface SyncCallback {
        void onSyncStatus(String estado);
    }

    /**
     * Interfaz Retrofit para Open-Meteo API.
     */
    private interface OpenMeteoService {
        @GET("v1/forecast")
        Call<OpenMeteoResponse> getCurrentWeather(
                @Query("latitude") double latitude,
                @Query("longitude") double longitude,
                @Query("current_weather") boolean currentWeather
        );
    }

    /**
     * Interfaz Retrofit para ExchangeRate API.
     */
    private interface ExchangeRateService {
        @GET("v4/latest/USD")
        Call<ExchangeRateResponse> getLatestRates();
    }

    /**
     * Clase de respuesta Open-Meteo.
     */
    private static class OpenMeteoResponse {
        CurrentWeather current_weather;
    }

    private static class CurrentWeather {
        double temperature;
        int weathercode;
    }

    /**
     * Clase de respuesta ExchangeRate.
     */
    private static class ExchangeRateResponse {
        java.util.Map<String, Double> rates;
    }

    /**
     * Constructor con inyeccion de dependencias.
     */
    public DashboardController(Activity activity,
                               UsuarioRepository usuarioRepository,
                               ParcelaRepository parcelaRepository,
                               RegistroRepository registroRepository) {
        this.activity = activity;
        this.usuarioRepository = usuarioRepository;
        this.parcelaRepository = parcelaRepository;
        this.registroRepository = registroRepository;
        this.executorService = Executors.newSingleThreadExecutor();

        this.retrofitOpenMeteo = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.retrofitExchangeRate = new Retrofit.Builder()
                .baseUrl("https://api.exchangerate-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Obtiene datos climaticos desde Open-Meteo.
     *
     * @param lat      Latitud
     * @param lon      Longitud
     * @param callback Callback para resultado
     */
    public void fetchClima(double lat, double lon, ClimaCallback callback) {
        OpenMeteoService service = retrofitOpenMeteo.create(OpenMeteoService.class);
        Call<OpenMeteoResponse> call = service.getCurrentWeather(lat, lon, true);

        call.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoResponse> call, Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrentWeather weather = response.body().current_weather;
                    String descripcion = interpretarWeatherCode(weather.weathercode);
                    activity.runOnUiThread(() ->
                            callback.onClimaSuccess(weather.temperature, weather.weathercode, descripcion));
                } else {
                    activity.runOnUiThread(() ->
                            callback.onClimaError("Error al obtener clima"));
                }
            }

            @Override
            public void onFailure(Call<OpenMeteoResponse> call, Throwable t) {
                activity.runOnUiThread(() ->
                        callback.onClimaError("Sin conexion al servicio de clima"));
            }
        });
    }

    /**
     * Obtiene tipo de cambio USD a PEN.
     *
     * @param callback Callback para resultado
     */
    public void fetchTipoCambio(TipoCambioCallback callback) {
        ExchangeRateService service = retrofitExchangeRate.create(ExchangeRateService.class);
        Call<ExchangeRateResponse> call = service.getLatestRates();

        call.enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Double penRate = response.body().rates.get("PEN");
                    if (penRate != null) {
                        activity.runOnUiThread(() -> callback.onTipoCambioSuccess(penRate));
                    } else {
                        activity.runOnUiThread(() ->
                                callback.onTipoCambioError("Tipo de cambio no disponible"));
                    }
                } else {
                    activity.runOnUiThread(() ->
                            callback.onTipoCambioError("Error al obtener tipo de cambio"));
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                activity.runOnUiThread(() ->
                        callback.onTipoCambioError("Sin conexion al servicio"));
            }
        });
    }

    /**
     * Verifica estado de sincronizacion.
     *
     * @param callback Callback con estado SYNCED o PENDING
     */
    public void checkSyncStatus(SyncCallback callback) {
        executorService.execute(() -> {
            int usuariosPendientes = usuarioRepository.contarPendientes();
            int parcelasPendientes = parcelaRepository.contarPendientes();
            int registrosPendientes = registroRepository.contarPendientes();

            int totalPendientes = usuariosPendientes + parcelasPendientes + registrosPendientes;
            String estado = totalPendientes > 0 ? "PENDING" : "SYNCED";

            activity.runOnUiThread(() -> callback.onSyncStatus(estado));
        });
    }

    /**
     * Interpreta codigo WMO Weather.
     *
     * @param code Codigo de clima
     * @return Descripcion en español
     */
    private String interpretarWeatherCode(int code) {
        if (code == 0) return "Despejado";
        if (code >= 1 && code <= 3) return "Parcialmente nublado";
        if (code >= 45 && code <= 48) return "Niebla";
        if (code >= 51 && code <= 55) return "Llovizna";
        if (code >= 61 && code <= 65) return "Lluvia";
        if (code >= 71 && code <= 77) return "Nieve";
        if (code >= 80 && code <= 82) return "Lluvias fuertes";
        if (code >= 95 && code <= 99) return "Tormenta";
        return "Soleado";
    }
}
