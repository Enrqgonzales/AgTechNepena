// Usado solo para dictado de campos de formulario en RegistroActivity, NO confundir con VoiceCommandHelper del Dashboard.
package com.agtech.nepenya.accessibility;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Manager para comandos de voz.
 * Utiliza SpeechRecognizer con locale es-PE.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class VoiceCommandManager implements RecognitionListener {

    private SpeechRecognizer speechRecognizer;
    private Consumer<String> onResultCallback;
    private Activity activity;

    /**
     * Constructor.
     */
    public VoiceCommandManager() {
    }

    private static final int PERMISSION_REQUEST_CODE = 2001;

    /**
     * Inicia el reconocimiento de voz.
     *
     * @param activity Activity origen
     * @param onResult Callback con el texto reconocido
     */
    public void startListening(Activity activity, Consumer<String> onResult) {
        this.activity = activity;
        this.onResultCallback = onResult;

        // Verificar permiso de audio
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[] { Manifest.permission.RECORD_AUDIO }, PERMISSION_REQUEST_CODE);
            Toast.makeText(activity, "Se requiere permiso de audio para comandos de voz", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar disponibilidad
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            Toast.makeText(activity, "Reconocimiento de voz no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inicializar recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizer.setRecognitionListener(this);

        // Configurar intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.forLanguageTag("es-PE").toString());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.startListening(intent);
    }

    /**
     * Detiene el reconocimiento.
     */
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    /**
     * Libera recursos.
     */
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    // RecognitionListener implementation

    @Override
    public void onReadyForSpeech(Bundle params) {
        if (activity != null) {
            com.google.android.material.snackbar.Snackbar.make(activity.findViewById(android.R.id.content), "Escuchando...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int error) {
        String mensaje;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                mensaje = "Error de audio";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                mensaje = "Error del cliente";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                mensaje = "Permisos insuficientes";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                mensaje = "Error de red";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                mensaje = "Timeout de red";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                mensaje = "No se reconocio el audio";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                mensaje = "Reconocedor ocupado";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                mensaje = "Error del servidor";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                mensaje = "No se detecto voz";
                break;
            default:
                mensaje = "Error desconocido";
        }

        if (activity != null) {
            com.google.android.material.snackbar.Snackbar.make(activity.findViewById(android.R.id.content), mensaje, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String text = matches.get(0);
            if (onResultCallback != null) {
                onResultCallback.accept(text);
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }
}
