package com.devteamispc.petshop.util;

import java.util.Calendar;
import java.util.Locale;

/**
 * Sugerencia de la próxima dosis a partir de la frecuencia de la vacuna.
 * Sin dependencias de Android: se prueba con tests unitarios.
 *
 * La frecuencia viene del catálogo como texto libre ("anual", "cada 6 meses").
 * Si no se entiende, no se sugiere nada y el veterinario la carga a mano.
 */
public final class ReglasVacunacion {

    private ReglasVacunacion() { }

    /** Cuántos meses representa la frecuencia, o null si no se reconoce. */
    public static Integer mesesDeFrecuencia(String frecuencia) {
        if (frecuencia == null) return null;
        String f = frecuencia.trim().toLowerCase(Locale.ROOT);
        if (f.isEmpty()) return null;
        if (f.contains("anual") && !f.contains("bi")) return 12;
        if (f.contains("semestral")) return 6;
        if (f.contains("trimestral")) return 3;
        if (f.contains("mensual")) return 1;

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*(mes|año|ano)").matcher(f);
        if (m.find()) {
            int n = Integer.parseInt(m.group(1));
            if (n <= 0) return null;
            return m.group(2).startsWith("mes") ? n : n * 12;
        }
        return null;
    }

    /**
     * Suma meses a una fecha de la API (yyyy-MM-dd) y devuelve otra en el mismo
     * formato. Si el día no existe en el mes destino (31 de enero + 1 mes), queda
     * el último día de ese mes, como hace Calendar.
     */
    public static String sumarMeses(String fechaApi, int meses) {
        String[] p = fechaApi.split("-");
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
        c.add(Calendar.MONTH, meses);
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Texto de frecuencia para guardar en el catálogo a partir de meses, en un
     * formato que mesesDeFrecuencia() vuelve a entender: 12 -> "anual",
     * 1 -> "mensual", otros -> "cada N meses".
     */
    public static String frecuenciaDesdeMeses(int meses) {
        if (meses == 12) return "anual";
        if (meses == 1) return "mensual";
        return "cada " + meses + " meses";
    }

    /** Próxima dosis sugerida, o null si la frecuencia no se entiende. */
    public static String sugerirProximaDosis(String fechaAplicacionApi, String frecuencia) {
        Integer meses = mesesDeFrecuencia(frecuencia);
        if (meses == null || fechaAplicacionApi == null) return null;
        return sumarMeses(fechaAplicacionApi, meses);
    }
}
