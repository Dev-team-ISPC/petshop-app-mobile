package com.devteamispc.petshop.data.api;

import androidx.annotation.Nullable;

import com.devteamispc.petshop.data.local.SessionManager;
import com.devteamispc.petshop.data.model.RefreshRequest;
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

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, Response response) throws IOException {
        SessionManager sesion = SessionManager.getSinContexto();
        if (sesion == null || sesion.getRefresh() == null) {
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

            retrofit2.Response<RefreshResponse> r = ApiClient.getApiSinAuth()
                    .refresh(new RefreshRequest(sesion.getRefresh()))
                    .execute();

            if (!r.isSuccessful() || r.body() == null) {
                sesion.cerrarSesion();
                return null;
            }

            sesion.actualizarTokens(r.body().getAccess(), r.body().getRefresh());

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + r.body().getAccess())
                    .build();
        }
    }
}
