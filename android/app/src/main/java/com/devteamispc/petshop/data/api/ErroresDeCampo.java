package com.devteamispc.petshop.data.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

/**
 * Lee TODOS los errores de una respuesta 400 de una sola vez.
 *
 * Por qué hace falta: el cuerpo de error de OkHttp se puede leer una sola vez.
 * Si un formulario llama a ApiError.mensajeDeCampo() para "email" y después
 * para "password", la segunda llamada ya no encuentra nada. Acá se lee una vez
 * y se guarda en un mapa campo -> mensaje.
 */
public final class ErroresDeCampo {

    private final Map<String, String> campos = new HashMap<>();
    private String general;

    private ErroresDeCampo() { }

    public static ErroresDeCampo de(Response<?> respuesta) {
        ErroresDeCampo e = new ErroresDeCampo();
        String cuerpo = null;
        try {
            if (respuesta.errorBody() != null) cuerpo = respuesta.errorBody().string();
        } catch (Exception ignorado) {
            // Sin cuerpo legible: queda sólo el mensaje por código.
        }
        if (cuerpo != null && !cuerpo.isEmpty()) {
            try {
                JsonObject json = JsonParser.parseString(cuerpo).getAsJsonObject();
                for (Map.Entry<String, JsonElement> campo : json.entrySet()) {
                    String texto = texto(campo.getValue());
                    if (texto == null) continue;
                    if ("detail".equals(campo.getKey()) || "non_field_errors".equals(campo.getKey())) {
                        e.general = texto;
                    } else {
                        e.campos.put(campo.getKey(), texto);
                    }
                }
            } catch (Exception ignorado) {
                // No era JSON (por ejemplo, una página de error 500).
            }
        }
        if (e.general == null && e.campos.isEmpty()) {
            e.general = porCodigo(respuesta.code());
        }
        return e;
    }

    /** Mensaje del campo, o null si ese campo no tuvo error. */
    public String campo(String nombre) {
        return campos.get(nombre);
    }

    /** Mensaje que no corresponde a ningún campo del formulario. */
    public String general() {
        return general;
    }

    public boolean hayErroresDeCampo() {
        return !campos.isEmpty();
    }

    private static String texto(JsonElement valor) {
        if (valor.isJsonArray()) {
            JsonArray lista = valor.getAsJsonArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lista.size(); i++) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(lista.get(i).getAsString());
            }
            return sb.toString();
        }
        return valor.isJsonPrimitive() ? valor.getAsString() : null;
    }

    private static String porCodigo(int codigo) {
        switch (codigo) {
            case 400: return "Revisá los datos ingresados.";
            case 403: return "No tenés permiso para hacer esto.";
            case 404: return "No se encontró el registro.";
            case 409: return "La operación entra en conflicto con datos existentes.";
            default: return "Ocurrió un error en el servidor. Probá de nuevo.";
        }
    }
}
