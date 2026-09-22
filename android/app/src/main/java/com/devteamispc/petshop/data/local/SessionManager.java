package com.devteamispc.petshop.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.devteamispc.petshop.data.model.Usuario;
import com.google.gson.Gson;

/**
 * Guarda la sesión en SharedPreferences: los dos tokens y el usuario.
 * Es un singleton porque el interceptor de red también necesita el token.
 */
public class SessionManager {

    private static final String PREFS = "petshop_session";
    private static final String KEY_ACCESS = "access";
    private static final String KEY_REFRESH = "refresh";
    private static final String KEY_USUARIO = "usuario";

    private static SessionManager instancia;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager get(Context context) {
        if (instancia == null) {
            instancia = new SessionManager(context);
        }
        return instancia;
    }

    /** Sólo para el interceptor, que no tiene Context a mano. */
    public static SessionManager getSinContexto() {
        return instancia;
    }

    public void guardarSesion(String access, String refresh, Usuario usuario) {
        prefs.edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .putString(KEY_USUARIO, gson.toJson(usuario))
                .apply();
    }

    public void actualizarTokens(String access, String refresh) {
        SharedPreferences.Editor editor = prefs.edit().putString(KEY_ACCESS, access);
        // El backend rota los refresh: si viene uno nuevo, el anterior ya no sirve.
        if (refresh != null && !refresh.isEmpty()) {
            editor.putString(KEY_REFRESH, refresh);
        }
        editor.apply();
    }

    public void guardarUsuario(Usuario usuario) {
        prefs.edit().putString(KEY_USUARIO, gson.toJson(usuario)).apply();
    }

    public String getAccess() { return prefs.getString(KEY_ACCESS, null); }

    public String getRefresh() { return prefs.getString(KEY_REFRESH, null); }

    public Usuario getUsuario() {
        String json = prefs.getString(KEY_USUARIO, null);
        return json == null ? null : gson.fromJson(json, Usuario.class);
    }

    public boolean haySesion() { return getAccess() != null; }

    public String getRol() {
        Usuario u = getUsuario();
        return u == null ? null : u.getRol();
    }

    public boolean esCliente() {
        Usuario u = getUsuario();
        return u != null && u.esCliente();
    }

    public boolean esVeterinario() {
        Usuario u = getUsuario();
        return u != null && u.esVeterinario();
    }

    public boolean esAdmin() {
        Usuario u = getUsuario();
        return u != null && u.esAdmin();
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}
