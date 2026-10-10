package com.devteamispc.petshop.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversión entre lo que muestra la app y lo que espera la API.
 *
 *   API  -> fechas: yyyy-MM-dd   |  fecha y hora: ISO 8601
 *   App  -> fechas: dd/MM/yyyy   |  hora: HH:mm
 *
 * La API devuelve las fechas con hora en ISO 8601 con zona horaria, y Django
 * la escribe según su configuración: "2026-09-23T15:30:00-03:00", a veces con
 * microsegundos ("…:00.123456-03:00") y a veces en UTC ("…:00Z"). Android 5
 * no tiene un formato que entienda todas esas variantes (el patrón "X" es de
 * Android 7), así que se interpretan a mano en parsearIso().
 */
public final class Fechas {

    private static final Locale AR = new Locale("es", "AR");
    private static final String API_FECHA = "yyyy-MM-dd";
    private static final String API_FECHA_HORA = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String VISIBLE_FECHA = "dd/MM/yyyy";
    private static final String VISIBLE_FECHA_HORA = "dd/MM/yyyy · HH:mm";

    private static final Pattern ISO = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2})(?::(\\d{2})(?:\\.\\d+)?)?(Z|[+-]\\d{2}:?\\d{2})?$");

    private Fechas() { }

    /** "2026-10-13" -> "13/10/2026" */
    public static String aVisible(String fechaApi) {
        if (fechaApi == null) return "";
        try {
            Date d = new SimpleDateFormat(API_FECHA, AR).parse(fechaApi);
            return new SimpleDateFormat(VISIBLE_FECHA, AR).format(d);
        } catch (ParseException e) {
            return fechaApi;
        }
    }

    /** "13/10/2026" -> "2026-10-13" */
    public static String aApi(String fechaVisible) {
        if (fechaVisible == null) return null;
        try {
            Date d = new SimpleDateFormat(VISIBLE_FECHA, AR).parse(fechaVisible);
            return new SimpleDateFormat(API_FECHA, AR).format(d);
        } catch (ParseException e) {
            return fechaVisible;
        }
    }

    /**
     * Instante (milisegundos desde 1970, UTC) de una fecha y hora ISO 8601 de la
     * API, o null si no tiene ese formato. Sin zona horaria se toma como UTC.
     */
    public static Long parsearIso(String iso) {
        if (iso == null) return null;
        Matcher m = ISO.matcher(iso.trim());
        if (!m.matches()) return null;

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)) - 1, Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)),
                m.group(6) == null ? 0 : Integer.parseInt(m.group(6)));
        long millis = c.getTimeInMillis();

        String zona = m.group(7);
        if (zona != null && !"Z".equals(zona)) {
            String z = zona.replace(":", "");
            int signo = z.charAt(0) == '-' ? -1 : 1;
            int horas = Integer.parseInt(z.substring(1, 3));
            int minutos = Integer.parseInt(z.substring(3, 5));
            // "15:30-03:00" es las 18:30 en UTC: se resta el desfasaje.
            millis -= signo * (horas * 60L + minutos) * 60_000L;
        }
        return millis;
    }

    /** "2026-10-13T15:30:00-03:00" -> "13/10/2026 · 15:30" en la hora del teléfono. */
    public static String fechaHoraAVisible(String fechaHoraApi) {
        return fechaHoraAVisible(fechaHoraApi, TimeZone.getDefault());
    }

    /** Igual, con la zona explícita (para los tests). */
    public static String fechaHoraAVisible(String fechaHoraApi, TimeZone zona) {
        if (fechaHoraApi == null) return "";
        Long millis = parsearIso(fechaHoraApi);
        if (millis == null) return fechaHoraApi;
        SimpleDateFormat salida = new SimpleDateFormat(VISIBLE_FECHA_HORA, AR);
        salida.setTimeZone(zona);
        return salida.format(new Date(millis));
    }

    /**
     * "Hoy 10:12", "Ayer 17:40" o "15/09 09:05", para listados como las consultas.
     */
    public static String fechaHoraRelativa(String fechaHoraApi, long ahora, TimeZone zona) {
        Long millis = parsearIso(fechaHoraApi);
        if (millis == null) return fechaHoraApi == null ? "" : fechaHoraApi;

        Calendar cuando = Calendar.getInstance(zona);
        cuando.setTimeInMillis(millis);
        Calendar hoy = Calendar.getInstance(zona);
        hoy.setTimeInMillis(ahora);
        Calendar ayer = (Calendar) hoy.clone();
        ayer.add(Calendar.DAY_OF_YEAR, -1);

        SimpleDateFormat hora = new SimpleDateFormat("HH:mm", AR);
        hora.setTimeZone(zona);
        if (mismoDia(cuando, hoy)) return "Hoy " + hora.format(new Date(millis));
        if (mismoDia(cuando, ayer)) return "Ayer " + hora.format(new Date(millis));
        SimpleDateFormat otro = new SimpleDateFormat("dd/MM HH:mm", AR);
        otro.setTimeZone(zona);
        return otro.format(new Date(millis));
    }

    public static String fechaHoraRelativa(String fechaHoraApi) {
        return fechaHoraRelativa(fechaHoraApi, System.currentTimeMillis(), TimeZone.getDefault());
    }

    private static boolean mismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    /** Arma el ISO 8601 UTC que espera la API a partir de los campos del formulario. */
    public static String aApiFechaHora(int anio, int mes, int dia, int hora, int minuto) {
        Calendar c = Calendar.getInstance();
        c.set(anio, mes, dia, hora, minuto, 0);
        c.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat salida = new SimpleDateFormat(API_FECHA_HORA, AR);
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
