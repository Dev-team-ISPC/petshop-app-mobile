package com.devteamispc.petshop.data.model;

/**
 * POST /auth/refresh/. Como el backend rota los refresh, puede venir uno nuevo:
 * si llega, hay que guardarlo, porque el anterior queda en la blacklist.
 */
public class RefreshResponse {

    private String access;
    private String refresh;

    public String getAccess() { return access; }
    public String getRefresh() { return refresh; }
}
