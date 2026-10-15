package com.devteamispc.petshop.data.api;

import androidx.annotation.Nullable;

import com.devteamispc.petshop.data.model.RefreshResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * El access token dura 60 minutos. Cuando vence, la API responde 401 y
 * OkHttp llama acá: pedimos uno nuevo con el refresh y reintentamos la
 * request original. El usuario no se entera.
 *
 * Si el refresh también falló (venció, o el logout lo puso en la blacklist),
 * devolvemos null: la request queda en 401 y BaseActivity manda al login.
 */
public class TokenAuthenticator implements Authenticator {

    /** Pide un access nuevo con el refresh. Devuelve null si el servidor lo rechaza. */
    public interface Renovador {
        RefreshResponse renovar(String refresh) throws IOException;
    }

    private final AlmacenTokens sesion;
    private final Renovador renovador;

    public TokenAuthenticator(AlmacenTokens tokens, Renovador renovador) {
        this.sesion = tokens;
        this.renovador = renovador;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, Response response) throws IOException {
        if (sesion.getRefresh() == null) {
            return null;
        }

        // Si ya reintentamos una vez, no insistimos: evita el bucle infinito.
        if (response.priorResponse() != null) {
            return null;
        }

        synchronized (this) {
            String tokenActual = sesion.getAccess();
            String tokenUsado = response.request().header("Authorization");

            // Otro hilo pudo haberlo renovado mientras esperábamos.
            if (tokenActual != null && tokenUsado != null
                    && !tokenUsado.endsWith(tokenActual)) {
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + tokenActual)
                        .build();
            }

            RefreshResponse nuevos = renovador.renovar(sesion.getRefresh());

            if (nuevos == null) {
                sesion.cerrarSesion();
                return null;
            }

            sesion.actualizarTokens(nuevos.getAccess(), nuevos.getRefresh());

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + nuevos.getAccess())
                    .build();
        }
    }
}
