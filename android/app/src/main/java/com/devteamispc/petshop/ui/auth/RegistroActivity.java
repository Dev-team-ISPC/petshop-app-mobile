package com.devteamispc.petshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.RegistroRequest;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.databinding.ActivityRegistroBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.util.Validaciones;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 2 · RegistroActivity — RF01, RF15
 *
 * Endpoint: POST /auth/registro/ (público)
 *
 * Pantalla pública: quien se registra todavía no tiene sesión, por eso NO
 * llama a exigirSesion().
 *
 *   - La lista de reglas de la contraseña se marca con ✓ o ✗ mientras se escribe.
 *     Usa las mismas reglas que el backend (Validaciones).
 *   - Aceptar los términos es obligatorio: sin eso la API rechaza el alta (RF15).
 *   - La cuenta se crea siempre con rol cliente: el backend no acepta otro rol.
 *   - Si el alta sale bien, vuelve al Login con el email ya escrito.
 */
public class RegistroActivity extends BaseActivity {

    private ActivityRegistroBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pantalla pública: quien se registra todavía no tiene sesión.

        vista = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        vista.campoPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable s) {
                pintarReglas(s.toString());
            }
        });
        pintarReglas("");

        vista.linkTerminos.setOnClickListener(v -> mostrarTerminos());
        vista.botonRegistrar.setOnClickListener(v -> registrar());
    }

    // ------------------------------------------------- reglas de contraseña

    private void pintarReglas(String password) {
        pintarRegla(vista.reglaLargo, Validaciones.tieneLargo(password), R.string.regla_largo);
        pintarRegla(vista.reglaMayusculas, Validaciones.tieneMayusculaYMinuscula(password), R.string.regla_mayusculas);
        pintarRegla(vista.reglaNumero, Validaciones.tieneNumero(password), R.string.regla_numero);
        pintarRegla(vista.reglaEspecial, Validaciones.tieneEspecial(password), R.string.regla_especial);
    }

    /** El estado se indica con símbolo y texto, no sólo con color (WCAG 1.4.1). */
    private void pintarRegla(TextView regla, boolean cumple, int texto) {
        String nombre = getString(texto);
        regla.setText((cumple ? "✓  " : "✗  ") + nombre);
        regla.setTextColor(ContextCompat.getColor(this, cumple ? R.color.confirmado_texto : R.color.texto_medio));
        regla.setContentDescription(getString(cumple ? R.string.regla_cumple : R.string.regla_falta, nombre));
    }

    // ---------------------------------------------------------- términos

    private void mostrarTerminos() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_terminos)
                .setMessage(R.string.texto_terminos)
                .setPositiveButton(R.string.accion_aceptar, (d, b) -> vista.campoTerminos.setChecked(true))
                .setNegativeButton(R.string.accion_cerrar, null)
                .show();
    }

    // ---------------------------------------------------------- registro

    private boolean validar(String nombre, String email, String password) {
        boolean ok = true;
        vista.campoNombre.setError(null);
        vista.campoEmail.setError(null);
        vista.campoPassword.setError(null);
        vista.textoError.setVisibility(View.GONE);

        if (!Validaciones.obligatorio(nombre)) {
            vista.campoNombre.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        if (!Validaciones.emailValido(email)) {
            vista.campoEmail.setError(getString(R.string.error_email));
            ok = false;
        }
        String errorPassword = Validaciones.errorPassword(password);
        if (errorPassword != null) {
            vista.campoPassword.setError(errorPassword);
            ok = false;
        }
        if (!vista.campoTerminos.isChecked()) {
            mostrarError(getString(R.string.error_terminos));
            ok = false;
        }
        return ok;
    }

    private void registrar() {
        String nombre = vista.campoNombre.getText().toString().trim();
        String email = vista.campoEmail.getText().toString().trim();
        String password = vista.campoPassword.getText().toString();
        if (!validar(nombre, email, password)) return;

        cargando(true);
        RegistroRequest pedido = new RegistroRequest(nombre, email, password, "", "", true);
        ApiClient.getApi().registro(pedido).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> respuesta) {
                cargando(false);
                if (respuesta.isSuccessful()) {
                    volverAlLogin(email);
                    return;
                }
                ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                if (errores.campo("nombre") != null) vista.campoNombre.setError(errores.campo("nombre"));
                if (errores.campo("email") != null) vista.campoEmail.setError(errores.campo("email"));
                if (errores.campo("password") != null) vista.campoPassword.setError(errores.campo("password"));
                if (errores.campo("acepta_terminos") != null) mostrarError(errores.campo("acepta_terminos"));
                if (errores.general() != null) mostrarError(errores.general());
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                cargando(false);
                mostrarError(ApiError.sinConexion());
            }
        });
    }

    /**
     * Con la cuenta creada, vuelve al Login con el email ya escrito: el usuario
     * confirma que recuerda su contraseña ingresándola una vez.
     * CLEAR_TOP + SINGLE_TOP reutiliza el Login que ya estaba abierto debajo.
     */
    private void volverAlLogin(String email) {
        aviso(getString(R.string.msg_cuenta_creada_ingresa));
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra(LoginActivity.EXTRA_EMAIL, email);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void cargando(boolean si) {
        vista.progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        vista.botonRegistrar.setEnabled(!si);
    }

    private void mostrarError(String mensaje) {
        vista.textoError.setText(mensaje);
        vista.textoError.setVisibility(View.VISIBLE);
    }
}
