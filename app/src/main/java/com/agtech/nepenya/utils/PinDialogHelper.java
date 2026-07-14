package com.agtech.nepenya.utils;

import android.app.Activity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

/**
 * Dialogos reutilizables para crear, confirmar y verificar el PIN de la app.
 */
public final class PinDialogHelper {

    private PinDialogHelper() {
    }

    public static void mostrarCrearPin(Activity activity,
                                      PrefsManager prefsManager,
                                      String titulo,
                                      String mensaje,
                                      Runnable onPinGuardado) {
        mostrarCrearPin(activity, prefsManager, titulo, mensaje, onPinGuardado, null);
    }

    public static void mostrarCrearPin(Activity activity,
                                      PrefsManager prefsManager,
                                      String titulo,
                                      String mensaje,
                                      Runnable onPinGuardado,
                                      Runnable onCancelado) {
        EditText input = crearInputPin(activity);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setView(input)
                .setPositiveButton("Siguiente", null)
                .setNegativeButton("Cancelar", (d, which) -> {
                    if (onCancelado != null) {
                        onCancelado.run();
                    }
                })
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pin = input.getText().toString().trim();
            if (!esPinValido(pin)) {
                Toast.makeText(activity, "El PIN debe tener exactamente 4 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            mostrarConfirmarPin(activity, prefsManager, pin, onPinGuardado, onCancelado);
        }));

        dialog.show();
    }

    public static void mostrarVerificarPin(Activity activity,
                                          PrefsManager prefsManager,
                                          String titulo,
                                          String mensaje,
                                          Runnable onPinCorrecto) {
        mostrarVerificarPin(activity, prefsManager, titulo, mensaje, onPinCorrecto, true);
    }

    public static void mostrarVerificarPin(Activity activity,
                                          PrefsManager prefsManager,
                                          String titulo,
                                          String mensaje,
                                          Runnable onPinCorrecto,
                                          boolean cancelable) {
        EditText input = crearInputPin(activity);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setView(input)
                .setPositiveButton("Aceptar", null)
                .create();

        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pin = input.getText().toString().trim();
            if (prefsManager.verificarAdminPin(pin)) {
                dialog.dismiss();
                onPinCorrecto.run();
            } else {
                input.setText("");
                Toast.makeText(activity, "PIN incorrecto", Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.show();
    }

    private static void mostrarConfirmarPin(Activity activity,
                                           PrefsManager prefsManager,
                                           String primerPin,
                                           Runnable onPinGuardado,
                                           Runnable onCancelado) {
        EditText input = crearInputPin(activity);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Confirmar PIN")
                .setMessage("Reingrese el PIN de 4 dígitos para confirmar:")
                .setView(input)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", (d, which) -> {
                    if (onCancelado != null) {
                        onCancelado.run();
                    }
                })
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String confirmPin = input.getText().toString().trim();
            if (!primerPin.equals(confirmPin)) {
                input.setText("");
                Toast.makeText(activity, "Los PINs no coinciden. Intente de nuevo.", Toast.LENGTH_SHORT).show();
                return;
            }

            prefsManager.setAdminPin(confirmPin);
            dialog.dismiss();
            onPinGuardado.run();
        }));

        dialog.show();
    }

    private static EditText crearInputPin(Activity activity) {
        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        input.setGravity(Gravity.CENTER);
        return input;
    }

    private static boolean esPinValido(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }
}
