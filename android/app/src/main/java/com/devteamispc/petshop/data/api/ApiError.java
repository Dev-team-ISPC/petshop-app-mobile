package com.devteamispc.petshop.data.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

import retrofit2.Response;

/**
 * Traduce la respuesta de error de DRF a un mensaje mostrable.
 *
 * El backend devuelve dos formas:
 *     {"detail": "No tiene permiso para realizar esta acción."}
 *     {"password": ["La contraseña debe incluir al menos un número."]}
 *
 * La segunda es la interesante: dice exactamente qué campo falló y por qué,
 * así que conviene mostrarla tal cual en vez de un "error" genérico.
 */
public final class ApiError {

    private ApiError() { }

    public static String mensaje(Response<?> respuesta) {
        String cuerpo = leerCuerpo(respuesta);

        if (cuerpo != null && !cuerpo.isEmpty()) {
            String parseado = parsear(cuerpo);
            if (parseado != null) {
                return parseado;
            }
        }
        return porCodigo(respuesta.code());
    }

    /** Devuelve el mensaje de un campo puntual, para mostrarlo bajo el input. */
    public static String mensajeDeCampo(Response<?> respuesta, String campo) {
        String cuerpo = leerCuerpo(respuesta);
        if (cuerpo == null) return null;
        try {
            JsonObject json = JsonParser.parseString(cuerpo).getAsJsonObject();
            if (!json.has(campo)) return null;
            return textoDe(json.get(campo));
        } catch (Exception e) {
            return null;
        }
    }

    private static String leerCuerpo(Response<?> respuesta) {
        try {
            return respuesta.errorBody() == null ? null : respuesta.errorBody().string();
        } catch (Exception e) {
            return null;
        }
    }

    private static String parsear(String cuerpo) {
        try {
            JsonObject json = JsonParser.parseString(cuerpo).getAsJsonObject();

            if (json.has("detail")) {
                return json.get("detail").getAsString();
            }

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, JsonElement> campo : json.entrySet()) {
                String texto = textoDe(campo.getValue());
                if (texto == null) continue;
                if (sb.length() > 0) sb.append('\n');
                sb.append(texto);
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String textoDe(JsonElement valor) {
        if (valor.isJsonArray()) {
            JsonArray lista = valor.getAsJsonArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lista.size(); i++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(lista.get(i).getAsString());
            }
            return sb.toString();
        }
        if (valor.isJsonPrimitive()) {
            return valor.getAsString();
        }
        return null;
    }

    private static String porCodigo(int codigo) {
        switch (codigo) {
            case 400: return "Revisá los datos ingresados.";
            case 401: return "Tu sesión expiró. Volvé a iniciar sesión.";
            case 403: return "No tenés permiso para hacer esto.";
            case 404: return "No se encontró lo que buscabas.";
            case 429: return "Demasiados intentos. Probá de nuevo en un rato.";
            case 500: return "Error del servidor. Intentá más tarde.";
            default:  return "Ocurrió un error (" + codigo + ").";
        }
    }

    /** Mensaje para cuando ni siquiera hubo respuesta: no hay red o el server está caído. */
    public static String sinConexion() {
        return "No se pudo conectar con el servidor. Revisá que esté corriendo.";
    }
}
