package com.devteamispc.petshop.data.model;

/** Lo que devuelve POST /auth/login/: los dos tokens y el usuario. */
public class LoginResponse {

    private String access;
    private String refresh;
    private Usuario usuario;

    public String getAccess() { return access; }
    public String getRefresh() { return refresh; }
    public Usuario getUsuario() { return usuario; }
}
