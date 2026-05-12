package com.agtech.nepenya.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilidades para formateo de moneda y conversión de divisas.
 * Centraliza toda la lógica de formato monetario de la aplicación.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class CurrencyUtils {

    private static final String DEFAULT_CURRENCY = "PEN";
    private static final String DEFAULT_SYMBOL = "S/";

    // Símbolos de moneda
    public static final String SYMBOL_PEN = "S/";
    public static final String SYMBOL_USD = "$";
    public static final String SYMBOL_EUR = "€";
    public static final String SYMBOL_GBP = "£";
    public static final String SYMBOL_JPY = "¥";

    /**
     * Formatea un monto con el símbolo de moneda predeterminado.
     *
     * @param monto Monto a formatear
     * @return String formateado (ej: "S/ 1,250.00")
     */
    public static String format(double monto) {
        return format(monto, DEFAULT_SYMBOL);
    }

    /**
     * Formatea un monto con símbolo de moneda específico.
     *
     * @param monto Monto a formatear
     * @param symbol Símbolo de moneda
     * @return String formateado (ej: "$ 1,250.00")
     */
    public static String format(double monto, String symbol) {
        return String.format(Locale.getDefault(), "%s %,.2f", symbol, monto);
    }

    /**
     * Formatea un monto con código de moneda específico.
     *
     * @param monto Monto a formatear
     * @param currencyCode Código de moneda (USD, EUR, PEN, etc.)
     * @return String formateado
     */
    public static String formatWithCode(double monto, String currencyCode) {
        String symbol = getSymbolForCurrency(currencyCode);
        return format(monto, symbol);
    }

    /**
     * Obtiene el símbolo para un código de moneda.
     *
     * @param currencyCode Código de moneda
     * @return Símbolo correspondiente
     */
    public static String getSymbolForCurrency(String currencyCode) {
        if (currencyCode == null) return DEFAULT_SYMBOL;

        switch (currencyCode.toUpperCase()) {
            case "USD": return SYMBOL_USD;
            case "EUR": return SYMBOL_EUR;
            case "GBP": return SYMBOL_GBP;
            case "JPY": return SYMBOL_JPY;
            case "PEN":
            default: return SYMBOL_PEN;
        }
    }

    /**
     * Convierte un monto de una moneda a otra usando tasas de cambio.
     *
     * @param amount Monto a convertir
     * @param fromCurrency Moneda origen (ej: "USD")
     * @param toCurrency Moneda destino (ej: "PEN")
     * @param rate Tasa de cambio (cuántas unidades de toCurrency equivale 1 unidad de fromCurrency)
     * @return Monto convertido
     */
    public static double convert(double amount, String fromCurrency, String toCurrency, double rate) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }
        return amount * rate;
    }

    /**
     * Formatea un monto con conversión automática según preferencias del usuario.
     * Obtiene la moneda base de PrefsManager y aplica conversión si es necesario.
     *
     * @param context Contexto para acceder a PrefsManager
     * @param montoMonto en moneda base del sistema (PEN)
     * @return String formateado en la moneda del usuario
     */
    public static String formatWithUserCurrency(android.content.Context context, double monto) {
        PrefsManager prefs = new PrefsManager(context);
        String baseCurrency = prefs.getCurrencyBase();

        // Si la moneda base es PEN, no convertir
        if ("PEN".equals(baseCurrency)) {
            return format(monto);
        }

        // Obtener tasa de conversión
        double rate = prefs.getCurrencyRate(baseCurrency);
        if (rate <= 0) rate = 1.0;

        // Convertir de PEN a la moneda base del usuario
        double convertedAmount = monto / rate;

        return format(convertedAmount, getSymbolForCurrency(baseCurrency));
    }

    /**
     * Formatea para mostrar valor total (con etiqueta).
     *
     * @param monto Monto total
     * @return String formateado (ej: "Total: S/ 1,250.00")
     */
    public static String formatTotal(double monto) {
        return String.format(Locale.getDefault(), "Total: %s %,.2f", DEFAULT_SYMBOL, monto);
    }

    /**
     * Formatea costo unitario.
     *
     * @param costo Costo unitario
     * @return String formateado (ej: "S/ 25.00/u")
     */
    public static String formatUnitCost(double costo) {
        return String.format(Locale.getDefault(), "%s %,.2f/u", DEFAULT_SYMBOL, costo);
    }

    /**
     * Parsea un monto desde string, manejando diferentes formatos.
     *
     * @param amountStr String con el monto
     * @return Valor numérico, o 0 si no se puede parsear
     */
    public static double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0;
        }
        try {
            // Remover símbolos de moneda y espacios
            String cleaned = amountStr.replaceAll("[S/$€£¥,\\s]", "").replace(",", ".");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
