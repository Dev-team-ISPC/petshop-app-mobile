package com.devteamispc.petshop.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validaciones de formularios, sin dependencias de Android: se prueban con
 * tests unitarios (ValidacionesTest).
 *
 * Replican las reglas del backend para avisar antes de enviar. El backend
 * vuelve a validar todo: esto mejora la experiencia, no reemplaza al servidor.
 */
public final class Validaciones {

    /** Largo mínimo de la contraseña: "más de 8 caracteres" (RNF15). */
    public static final int PASSWORD_MINIMO = 9;
    public static final int DURACION_MINIMA = 5;
    public static final int MENSAJE_MINIMO = 10;
    public static final int MENSAJE_MAXIMO = 500;
    /** El backend guarda el peso con 5 dígitos y 2 decimales: hasta 999,99 kg. */
    public static final double PESO_MAXIMO = 999.99;
    public static final int DURACION_MAXIMA = 480;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern MAYUSCULA = Pattern.compile("[A-ZÁÉÍÓÚÑ]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-záéíóúñ]");
    private static final Pattern NUMERO = Pattern.compile("\\d");
    private static final Pattern ESPECIAL = Pattern.compile("[!@#$%^&*()\\-_=+\\[\\]{};:,.<>/?\\\\|`~\"']");

    private Validaciones() { }

    public static boolean obligatorio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean emailValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    // Reglas de la contraseña, una por una: el Registro las muestra como lista
    // que se va marcando mientras el usuario escribe.

    public static boolean tieneLargo(String p) {
        return p != null && p.length() >= PASSWORD_MINIMO;
    }

    public static boolean tieneMayusculaYMinuscula(String p) {
        return p != null && MAYUSCULA.matcher(p).find() && MINUSCULA.matcher(p).find();
    }

    public static boolean tieneNumero(String p) {
        return p != null && NUMERO.matcher(p).find();
    }

    public static boolean tieneEspecial(String p) {
        return p != null && ESPECIAL.matcher(p).find();
    }

    /**
     * Qué le falta a la contraseña para cumplir la política. Lista vacía si
     * cumple. Mismas reglas que api/validators.py del backend.
     */
    public static List<String> faltantesPassword(String password) {
        List<String> faltan = new ArrayList<>();
        String p = password == null ? "" : password;
        if (!tieneLargo(p)) faltan.add("más de 8 caracteres");
        if (!MAYUSCULA.matcher(p).find()) faltan.add("una mayúscula");
        if (!MINUSCULA.matcher(p).find()) faltan.add("una minúscula");
        if (!tieneNumero(p)) faltan.add("un número");
        if (!tieneEspecial(p)) faltan.add("un carácter especial");
        return faltan;
    }

    /** Texto para mostrar bajo el campo, o null si la contraseña es válida. */
    public static String errorPassword(String password) {
        List<String> faltan = faltantesPassword(password);
        if (faltan.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("Falta: ");
        for (int i = 0; i < faltan.size(); i++) {
            if (i > 0) sb.append(i == faltan.size() - 1 ? " y " : ", ");
            sb.append(faltan.get(i));
        }
        return sb.append('.').toString();
    }

    /** Mensaje de contacto: entre 10 y 500 caracteres, sin contar espacios de los bordes. */
    public static boolean mensajeValido(String mensaje) {
        if (mensaje == null) return false;
        int largo = mensaje.trim().length();
        return largo >= MENSAJE_MINIMO && largo <= MENSAJE_MAXIMO;
    }

    /**
     * Peso de una mascota. Acepta coma o punto ("28,5" o "28.50") y devuelve
     * el texto que espera la API ("28.50"), o null si no es válido
     * (vacío, cero, negativo, más de 2 decimales o mayor a 999,99).
     */
    public static String pesoParaApi(String texto) {
        if (texto == null) return null;
        String t = texto.trim().replace(',', '.');
        if (!t.matches("\\d{1,3}(\\.\\d{1,2})?")) return null;
        double valor = Double.parseDouble(t);
        if (valor <= 0 || valor > PESO_MAXIMO) return null;
        return String.format(java.util.Locale.US, "%.2f", valor);
    }

    /**
     * Compara dos fechas de la API (yyyy-MM-dd). Como el formato va de mayor a
     * menor (año, mes, día), el orden alfabético coincide con el cronológico.
     */
    public static boolean esPosterior(String fechaApi, String otraFechaApi) {
        return fechaApi != null && otraFechaApi != null && fechaApi.compareTo(otraFechaApi) > 0;
    }

    /** Duración de un servicio, en minutos. Null si el texto no es un número. */
    public static Integer duracion(String texto) {
        if (texto == null) return null;
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean duracionValida(Integer minutos) {
        return minutos != null && minutos >= DURACION_MINIMA && minutos <= DURACION_MAXIMA;
    }
}
