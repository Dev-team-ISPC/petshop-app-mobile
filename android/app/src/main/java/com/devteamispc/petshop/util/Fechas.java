package com.devteamispc.petshop.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Conversión entre lo que muestra la app y lo que espera la API.
 *
 *   API  -> fechas: yyyy-MM-dd   |  fecha y hora: ISO 8601 UTC
 *   App  -> fechas: dd/MM/yyyy   |  hora: HH:mm
 */
public final class Fechas {

    private static final String API_FECHA = "yyyy-MM-dd";
    private static final String API_FECHA_HORA = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String VISIBLE_FECHA = "dd/MM/yyyy";
    private static final String VISIBLE_FECHA_HORA = "dd/MM/yyyy · HH:mm";

    private Fechas() { }

    /** "2026-10-13" -> "13/10/2026" */
    public static String aVisible(String fechaApi) {
        if (fechaApi == null) return "";
        try {
            Date d = new SimpleDateFormat(API_FECHA, new Locale("es", "AR")).parse(fechaApi);
            return new SimpleDateFormat(VISIBLE_FECHA, new Locale("es", "AR")).format(d);
        } catch (ParseException e) {
            return fechaApi;
        }
    }

    /** "13/10/2026" -> "2026-10-13" */
    public static String aApi(String fechaVisible) {
        if (fechaVisible == null) return null;
        try {
            Date d = new SimpleDateFormat(VISIBLE_FECHA, new Locale("es", "AR")).parse(fechaVisible);
            return new SimpleDateFormat(API_FECHA, new Locale("es", "AR")).format(d);
        } catch (ParseException e) {
            return fechaVisible;
        }
    }

    /** "2026-10-13T15:30:00Z" -> "13/10/2026 · 12:30" en hora local */
    public static String fechaHoraAVisible(String fechaHoraApi) {
        if (fechaHoraApi == null) return "";
        try {
            SimpleDateFormat entrada = new SimpleDateFormat(API_FECHA_HORA, new Locale("es", "AR"));
            entrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = entrada.parse(fechaHoraApi);
            return new SimpleDateFormat(VISIBLE_FECHA_HORA, new Locale("es", "AR")).format(d);
        } catch (ParseException e) {
            return fechaHoraApi;
        }
    }

    /** Arma el ISO 8601 UTC que espera la API a partir de los campos del formulario. */
    public static String aApiFechaHora(int anio, int mes, int dia, int hora, int minuto) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(anio, mes, dia, hora, minuto, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        SimpleDateFormat salida = new SimpleDateFormat(API_FECHA_HORA, new Locale("es", "AR"));
        salida.setTimeZone(TimeZone.getTimeZone("UTC"));
        return salida.format(c.getTime());
    }

    /** Texto del badge: "En 25 días", "Hoy", "Vencida hace 3 días". */
    public static String textoDias(Integer dias) {
        if (dias == null) return "";
        if (dias == 0) return "Hoy";
        if (dias == 1) return "Mañana";
        if (dias > 0) return "En " + dias + " días";
        int vencidos = -dias;
        return vencidos == 1 ? "Venció ayer" : "Vencida hace " + vencidos + " días";
    }
}
