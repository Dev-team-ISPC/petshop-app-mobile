package com.devteamispc.petshop.data.api;

import com.devteamispc.petshop.data.local.SessionManager;

/**
 * Almacén de tokens de la app: delega en SessionManager, que se inicializa
 * con la primera Activity. Si todavía no existe, se comporta como sin sesión.
 */
final class TokensDeSesion implements AlmacenTokens {

    private SessionManager sesion() {
        return SessionManager.getSinContexto();
    }

    @Override public String getAccess() {
        SessionManager s = sesion();
        return s == null ? null : s.getAccess();
    }

    @Override public String getRefresh() {
        SessionManager s = sesion();
        return s == null ? null : s.getRefresh();
    }

    @Override public void actualizarTokens(String access, String refresh) {
        SessionManager s = sesion();
        if (s != null) s.actualizarTokens(access, refresh);
    }

    @Override public void cerrarSesion() {
        SessionManager s = sesion();
        if (s != null) s.cerrarSesion();
    }
}
