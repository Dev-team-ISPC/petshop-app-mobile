package com.devteamispc.petshop.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.databinding.ActivityPerfilBinding;
import com.devteamispc.petshop.databinding.DialogoEliminarCuentaBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.auth.LoginActivity;
import com.devteamispc.petshop.util.CierreSesion;
import com.devteamispc.petshop.util.Validaciones;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 10 · PerfilActivity — RF02, RF16, US08
 *
 * Endpoints: GET /auth/me/ · PATCH /auth/me/ · DELETE /auth/me/
 *
 *   - El email no se puede editar: se muestra deshabilitado.
 *   - Cerrar sesión usa CierreSesion (blacklist del refresh en el servidor).
 *   - Eliminar mi cuenta es el "botón de arrepentimiento" que pide
 *     ciberseguridad (ley de protección de datos personales): va aparte, en
 *     rojo, y para confirmar hay que escribir ELIMINAR.
 */
public class PerfilActivity extends BaseActivity {

    private static final String PALABRA_CONFIRMACION = "ELIMINAR";

    private ActivityPerfilBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // Mientras llega la respuesta, se muestra lo que ya hay guardado.
        Usuario guardado = sesion.getUsuario();
        if (guardado != null) completar(guardado);

        vista.botonGuardar.setOnClickListener(v -> guardar());
        vista.botonCerrarSesion.setOnClickListener(v -> confirmarCierre());
        vista.botonEliminar.setOnClickListener(v -> confirmarEliminar());

        cargar();
    }

    private void cargar() {
        ApiClient.getApi().miPerfil().enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    sesion.guardarUsuario(respuesta.body());
                    completar(respuesta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                // Queda lo guardado en el teléfono; al guardar se avisa si no hay conexión.
            }
        });
    }

    private void completar(Usuario u) {
        vista.campoEmail.setText(u.getEmail());
        vista.campoNombre.setText(u.getNombre());
        vista.campoTelefono.setText(u.getTelefono());
        vista.campoDireccion.setText(u.getDireccion());
    }

    // -------------------------------------------------------------- guardar

    private void guardar() {
        String nombre = vista.campoNombre.getText().toString().trim();
        vista.campoNombre.setError(null);
        if (!Validaciones.obligatorio(nombre)) {
            vista.campoNombre.setError(getString(R.string.error_obligatorio));
            return;
        }

        Usuario cambios = new Usuario();
        cambios.setNombre(nombre);
        cambios.setTelefono(vista.campoTelefono.getText().toString().trim());
        cambios.setDireccion(vista.campoDireccion.getText().toString().trim());

        cargando(true);
        ApiClient.getApi().editarMiPerfil(cambios).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    sesion.guardarUsuario(respuesta.body());
                    aviso(getString(R.string.msg_cambios_guardados));
                } else {
                    ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                    if (errores.campo("nombre") != null) vista.campoNombre.setError(errores.campo("nombre"));
                    if (errores.campo("telefono") != null) vista.campoTelefono.setError(errores.campo("telefono"));
                    if (errores.general() != null) aviso(errores.general());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    // -------------------------------------------------------- cerrar sesión

    private void confirmarCierre() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_cerrar_sesion)
                .setMessage(R.string.msg_cerrar_sesion)
                .setPositiveButton(R.string.accion_cerrar_sesion, (d, b) -> CierreSesion.cerrar(this))
                .setNegativeButton(R.string.accion_quedarme, null)
                .show();
    }

    // ------------------------------------------------------ eliminar cuenta

    /** El botón Eliminar recién se habilita cuando se escribe ELIMINAR. */
    private void confirmarEliminar() {
        DialogoEliminarCuentaBinding d = DialogoEliminarCuentaBinding.inflate(getLayoutInflater());
        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar_cuenta)
                .setView(d.getRoot())
                .setPositiveButton(R.string.accion_eliminar, (x, b) -> eliminar())
                .setNegativeButton(R.string.accion_volver, null)
                .create();
        dialogo.show();

        Button eliminar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
        eliminar.setEnabled(false);
        d.campoConfirmacion.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable s) {
                eliminar.setEnabled(PALABRA_CONFIRMACION.equals(s.toString().trim()));
            }
        });
    }

    private void eliminar() {
        cargando(true);
        ApiClient.getApi().eliminarMiCuenta().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> respuesta) {
                cargando(false);
                if (respuesta.isSuccessful()) {
                    // La cuenta ya no existe: se borra la sesión local y se vuelve al Login.
                    sesion.cerrarSesion();
                    aviso(getString(R.string.msg_cuenta_eliminada));
                    Intent i = new Intent(PerfilActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else if (!manejarErrorComun(respuesta)) {
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void cargando(boolean si) {
        vista.progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        vista.botonGuardar.setEnabled(!si);
        vista.botonEliminar.setEnabled(!si);
    }
}
