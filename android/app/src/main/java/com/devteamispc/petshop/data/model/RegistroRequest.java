package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /auth/registro/. La cuenta se crea siempre con rol cliente.
 * La contraseña debe tener más de 8 caracteres, con mayúscula, minúscula,
 * número y carácter especial; si no, el backend devuelve 400 con el detalle.
 */
public class RegistroRequest {

    private final String nombre;
    private final String email;
    private final String password;
    private final String telefono;
    private final String direccion;

    @SerializedName("acepta_terminos")
    private final boolean aceptaTerminos;

    public RegistroRequest(String nombre, String email, String password,
                           String telefono, String direccion, boolean aceptaTerminos) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.direccion = direccion;
        this.aceptaTerminos = aceptaTerminos;
    }
}
