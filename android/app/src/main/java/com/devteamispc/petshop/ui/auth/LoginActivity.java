package com.devteamispc.petshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;

import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.LoginRequest;
import com.devteamispc.petshop.data.model.LoginResponse;
import com.devteamispc.petshop.databinding.ActivityLoginBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.home.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PANTALLA DE REFERENCIA — patrón de POST con sesión.
 *
 * Si tu pantalla manda datos a la API, copiá la estructura de acá:
 * validar en el cliente, deshabilitar el botón, llamar, y en onResponse
 * distinguir entre éxito y error mostrando el mensaje que devuelve el backend.
 *
 * Endpoint: POST /auth/login/  ->  {access, refresh, usuario}
 */
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        // Si ya hay sesión guardada, saltamos directo al inicio.
        if (sesion.haySesion()) {
            irAlHome();
            return;
        }

        vista.botonIngresar.setOnClickListener(v -> intentarLogin());
        vista.linkRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void intentarLogin() {
        String email = vista.campoEmail.getText().toString().trim();
        String password = vista.campoPassword.getText().toString();

        if (!validar(email, password)) {
            return;
        }

        cargando(true);

        ApiClient.getApi().login(new LoginRequest(email, password))
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call,
                                           @NonNull Response<LoginResponse> respuesta) {
                        cargando(false);

                        if (respuesta.isSuccessful() && respuesta.body() != null) {
                            LoginResponse datos = respuesta.body();
                            sesion.guardarSesion(datos.getAccess(), datos.getRefresh(), datos.getUsuario());
                            irAlHome();
                            return;
                        }

                        // Mensaje genérico a propósito: no revelamos si falló
                        // el email o la contraseña.
                        if (respuesta.code() == 401) {
                            mostrarError("Email o contraseña incorrectos.");
                        } else {
                            mostrarError(ApiError.mensaje(respuesta));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        cargando(false);
                        mostrarError(ApiError.sinConexion());
                    }
                });
    }

    private boolean validar(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            vista.campoEmail.setError("Ingresá tu email");
            vista.campoEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            vista.campoEmail.setError("El email no es válido");
            vista.campoEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            vista.campoPassword.setError("Ingresá tu contraseña");
            vista.campoPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void cargando(boolean activo) {
        vista.progreso.setVisibility(activo ? View.VISIBLE : View.GONE);
        vista.botonIngresar.setEnabled(!activo);
        if (activo) {
            vista.textoError.setVisibility(View.GONE);
        }
    }

    private void mostrarError(String mensaje) {
        vista.textoError.setText(mensaje);
        vista.textoError.setVisibility(View.VISIBLE);
    }

    private void irAlHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
