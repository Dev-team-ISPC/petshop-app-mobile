package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/** Usuario de la API. El rol decide qué ve y qué puede hacer. */
public class Usuario {

    public static final String ROL_CLIENTE = "cliente";
    public static final String ROL_VETERINARIO = "veterinario";
    public static final String ROL_ADMIN = "admin";

    private int id;
    private String email;
    private String nombre;
    private String telefono;
    private String direccion;
    private String rol;

    @SerializedName("rol_display")
    private String rolDisplay;

    @SerializedName("is_active")
    private Boolean activo;

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getRol() { return rol; }
    public String getRolDisplay() { return rolDisplay; }
    public Boolean getActivo() { return activo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public boolean esCliente() { return ROL_CLIENTE.equals(rol); }
    public boolean esVeterinario() { return ROL_VETERINARIO.equals(rol); }
    public boolean esAdmin() { return ROL_ADMIN.equals(rol); }
}
