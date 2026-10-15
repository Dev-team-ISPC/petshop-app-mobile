package com.devteamispc.petshop.data.api;

import androidx.annotation.NonNull;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Agrega el header Authorization a cada request. Así ninguna pantalla
 * tiene que acordarse de mandarlo.
 */
public class AuthInterceptor implements Interceptor {

    private final AlmacenTokens tokens;

    public AuthInterceptor(AlmacenTokens tokens) {
        this.tokens = tokens;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        String token = tokens.getAccess();

        // Login, registro, refresh y el envío de contacto son públicos.
        if (token == null || esPublico(original)) {
            return chain.proceed(original);
        }

        Request conToken = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(conToken);
    }

    private boolean esPublico(Request request) {
        String ruta = request.url().encodedPath();
        return ruta.endsWith("/auth/login/")
                || ruta.endsWith("/auth/registro/")
                || ruta.endsWith("/auth/refresh/")
                || (ruta.endsWith("/contacto/") && "POST".equals(request.method()));
    }
}
