package com.agtech.nepenya.accessibility;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Helper para control de comandos de voz con SpeechRecognizer y TextToSpeech.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class VoiceCommandHelper {

    private final Context context;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private final CommandCallback callback;

    /**
     * Interfaz para recibir resultados del asistente.
     */
    public interface CommandCallback {
        void onCommandRecognized(String command, String cleanText);
        void onCommandNotRecognized(String originalText);
        void onError(String errorMsg);
    }

    public VoiceCommandHelper(Context context, CommandCallback callback) {
        this.context = context;
        this.callback = callback;
        initTTS();
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("es", "PE"));
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsInitialized = true;
                }
            }
        });
    }

    /**
     * Hace hablar al dispositivo.
     */
    public void speak(String text) {
        if (ttsInitialized && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    /**
     * Inicia el reconocimiento de voz.
     */
    public void startListening() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            callback.onError("NO_PERMISSION");
            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError("RECOGNIZER_UNAVAILABLE");
            return;
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(context, "Escuchando...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                callback.onError("SPEECH_ERROR_" + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    String command = reconocerComando(recognizedText);
                    if (command != null) {
                        callback.onCommandRecognized(command, recognizedText);
                    } else {
                        callback.onCommandNotRecognized(recognizedText);
                    }
                } else {
                    callback.onCommandNotRecognized("");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("es", "PE").toString());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    /**
     * Cancela la escucha activa.
     */
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    /**
     * Libera los recursos.
     */
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    /**
     * Reconoce si el texto contiene alguna de las 6 palabras clave.
     */
    public static String reconocerComando(String recognizedText) {
        if (recognizedText == null) return null;
        String normalizado = normalizar(recognizedText.toLowerCase());

        if (normalizado.contains("gasto")) return "GASTO";
        if (normalizado.contains("ingreso")) return "INGRESO";
        if (normalizado.contains("parcela")) return "PARCELAS";
        if (normalizado.contains("historial")) return "HISTORIAL";
        if (normalizado.contains("reporte")) return "REPORTES";
        if (normalizado.contains("inventario")) return "INVENTARIO";

        return null;
    }

    private static String normalizar(String src) {
        if (src == null) return "";
        return src.replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ü", "u");
    }
}
