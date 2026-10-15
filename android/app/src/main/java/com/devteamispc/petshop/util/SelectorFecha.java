package com.devteamispc.petshop.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Diálogos de fecha y hora de Android, con límites. Devuelven la fecha en el
 * formato de la API (yyyy-MM-dd) y en el visible (dd/MM/yyyy), para no repetir
 * la conversión en cada formulario.
 */
public final class SelectorFecha {

    public interface AlElegirFecha {
        void fecha(String fechaApi, String fechaVisible);
    }

    public interface AlElegirHora {
        void hora(int hora, int minuto);
    }

    private SelectorFecha() { }

    /**
     * @param actualApi fecha inicial (yyyy-MM-dd) o null para hoy
     * @param minimo    milisegundos mínimos permitidos, o null
     * @param maximo    milisegundos máximos permitidos, o null
     */
    public static void fecha(Context contexto, String actualApi, Long minimo, Long maximo, AlElegirFecha alElegir) {
        Calendar c = Calendar.getInstance();
        if (actualApi != null && actualApi.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] p = actualApi.split("-");
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
        }
        DatePickerDialog dialogo = new DatePickerDialog(contexto, (vista, anio, mes, dia) -> {
            String api = String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia);
            alElegir.fecha(api, Fechas.aVisible(api));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        if (minimo != null) dialogo.getDatePicker().setMinDate(minimo);
        if (maximo != null) dialogo.getDatePicker().setMaxDate(maximo);
        dialogo.show();
    }

    public static void hora(Context contexto, int hora, int minuto, AlElegirHora alElegir) {
        new TimePickerDialog(contexto, (vista, h, m) -> alElegir.hora(h, m),
                hora, minuto, DateFormat.is24HourFormat(contexto)).show();
    }

    /** Hoy en formato de la API. */
    public static String hoyApi() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }
}
