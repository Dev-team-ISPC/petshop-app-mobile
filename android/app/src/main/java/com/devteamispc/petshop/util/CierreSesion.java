package com.devteamispc.petshop.util;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.local.SessionManager;
import com.devteamispc.petshop.data.model.RefreshRequest;
import com.devteamispc.petshop.ui.auth.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Cierre de sesión completo (US08), reutilizable desde el Home y el Perfil.
 *
 *   1. POST /auth/logout/ con el refresh: el backend lo pone en la blacklist.
 *      Si sólo se borrara del teléfono, ese refresh seguiría sirviendo en el
 *      servidor durante 7 días.
 *   2. Se borra la sesión del teléfono.
 *   3. Se vuelve al Login limpiando la pila: con "atrás" no se vuelve a entrar.
 *
 * Si el pedido al servidor falla (sin internet), igual se cierra la sesión local:
 * el usuario pidió salir y no debe quedar logueado por un problema de red.
 */
public final class CierreSesion {

    private CierreSesion() { }

    public static void cerrar(@NonNull Activity activity) {
        SessionManager sesion = SessionManager.get(activity);
        String refresh = sesion.getRefresh();

        if (refresh == null) {
            terminar(activity, sesion);
            return;
        }

        ApiClient.getApi().logout(new RefreshRequest(refresh)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> respuesta) {
                terminar(activity, sesion);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                terminar(activity, sesion);
            }
        });
    }

    private static void terminar(Activity activity, SessionManager sesion) {
        sesion.cerrarSesion();
        Intent i = new Intent(activity, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}
