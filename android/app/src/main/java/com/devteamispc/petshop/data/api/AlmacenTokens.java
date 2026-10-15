package com.devteamispc.petshop.data.api;

/**
 * Dónde viven los tokens. En la app es SessionManager (SharedPreferences);
 * en los tests de red, un almacén en memoria. Así el interceptor y el
 * authenticator se pueden probar sin Android.
 */
public interface AlmacenTokens {
    String getAccess();
    String getRefresh();
    void actualizarTokens(String access, String refresh);
    void cerrarSesion();
}
