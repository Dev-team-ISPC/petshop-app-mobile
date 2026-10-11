package com.devteamispc.petshop.ui.publico;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.Consulta;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.databinding.ActivityContactoBinding;
import com.devteamispc.petshop.databinding.DialogoMensajeEnviadoBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.util.Validaciones;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 11 · ContactoActivity — RF06
 *
 * Endpoint: POST /contacto/ (público: no necesita token)
 *
 *   - Datos de la veterinaria: dirección, teléfono y email, tocables (abren el
 *     mapa, el marcador y el correo).
 *   - Acceso a Quiénes somos.
 *   - Formulario con los tres campos obligatorios. Con sesión, nombre y email
 *     vienen precargados. El mensaje va de 10 a 500 caracteres, con contador.
 *   - Al enviar, un diálogo confirma y ofrece volver o ir al inicio.
 */
public class ContactoActivity extends BaseActivity {

    private ActivityContactoBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pantalla pública: no exige sesión.

        vista = ActivityContactoBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        Usuario yo = sesion.getUsuario();
        if (yo != null) {
            vista.campoNombre.setText(yo.getNombre());
            vista.campoEmail.setText(yo.getEmail());
        }

        vista.tarjetaQuienesSomos.setOnClickListener(v ->
                startActivity(new Intent(this, QuienesSomosActivity.class)));
        vista.filaDireccion.setOnClickListener(v ->
                abrir(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(getString(R.string.institucional_direccion))))));
        vista.filaTelefono.setOnClickListener(v ->
                abrir(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + getString(R.string.institucional_telefono).replace(" ", "")))));
        vista.filaEmail.setOnClickListener(v ->
                abrir(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.institucional_email)))));

        vista.campoMensaje.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable s) {
                vista.contador.setText(getString(R.string.contador_mensaje, s.length(), Validaciones.MENSAJE_MAXIMO));
            }
        });
        vista.contador.setText(getString(R.string.contador_mensaje, 0, Validaciones.MENSAJE_MAXIMO));

        vista.botonEnviar.setOnClickListener(v -> enviar());
    }

    /** Si el teléfono no tiene una app para abrirlo, se avisa en vez de cerrarse. */
    private void abrir(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            aviso(getString(R.string.msg_sin_app));
        }
    }

    private void enviar() {
        String nombre = vista.campoNombre.getText().toString().trim();
        String email = vista.campoEmail.getText().toString().trim();
        String mensaje = vista.campoMensaje.getText().toString().trim();

        boolean ok = true;
        vista.campoNombre.setError(null);
        vista.campoEmail.setError(null);
        vista.campoMensaje.setError(null);
        if (!Validaciones.obligatorio(nombre)) {
            vista.campoNombre.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        if (!Validaciones.emailValido(email)) {
            vista.campoEmail.setError(getString(R.string.error_email));
            ok = false;
        }
        if (!Validaciones.mensajeValido(mensaje)) {
            vista.campoMensaje.setError(getString(R.string.error_mensaje,
                    Validaciones.MENSAJE_MINIMO, Validaciones.MENSAJE_MAXIMO));
            ok = false;
        }
        if (!ok) return;

        cargando(true);
        ApiClient.getApi().enviarConsulta(new Consulta(nombre, email, mensaje)).enqueue(new Callback<Consulta>() {
            @Override
            public void onResponse(@NonNull Call<Consulta> call, @NonNull Response<Consulta> respuesta) {
                cargando(false);
                if (respuesta.isSuccessful()) {
                    mostrarEnviado();
                } else if (respuesta.code() == 429) {
                    // Throttling del backend: 10 mensajes por hora.
                    aviso(getString(R.string.msg_demasiados_mensajes));
                } else {
                    ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                    if (errores.campo("nombre") != null) vista.campoNombre.setError(errores.campo("nombre"));
                    if (errores.campo("email") != null) vista.campoEmail.setError(errores.campo("email"));
                    if (errores.campo("mensaje") != null) vista.campoMensaje.setError(errores.campo("mensaje"));
                    if (errores.general() != null) aviso(errores.general());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Consulta> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void mostrarEnviado() {
        DialogoMensajeEnviadoBinding d = DialogoMensajeEnviadoBinding.inflate(getLayoutInflater());
        new AlertDialog.Builder(this)
                .setView(d.getRoot())
                .setCancelable(false)
                .setPositiveButton(R.string.accion_ir_al_inicio, (x, b) -> finish())
                .setNegativeButton(R.string.accion_volver_a_contacto, (x, b) -> vista.campoMensaje.setText(""))
                .show();
    }

    private void cargando(boolean si) {
        vista.progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        vista.botonEnviar.setEnabled(!si);
    }
}
