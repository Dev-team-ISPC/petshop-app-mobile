package com.devteamispc.petshop.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.databinding.ActivityUsuarioFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.util.Validaciones;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * UsuarioFormActivity — CRUD 1: Create, Update y Delete de usuarios.
 *
 * Endpoints: GET /usuarios/{id}/ · POST /usuarios/ · PATCH /usuarios/{id}/ · DELETE /usuarios/{id}/
 *
 * Sin EXTRA_USUARIO_ID es un alta; con él, una edición.
 *   - En el alta la contraseña es obligatoria; en la edición, si queda vacía
 *     no se cambia.
 *   - Desactivar (is_active) impide que el usuario entre sin borrar su historial:
 *     es la opción recomendada frente a eliminar.
 *   - Eliminar borra también sus mascotas, vacunas y turnos, por eso pide
 *     confirmación. El administrador no puede eliminarse a sí mismo desde acá.
 *
 * Las validaciones locales (Validaciones) replican las del backend; los
 * errores que igual devuelva el servidor se muestran en su campo.
 */
public class UsuarioFormActivity extends BaseActivity {

    public static final String EXTRA_USUARIO_ID = "usuario_id";

    /** Lo que se envía a la API, en el mismo orden que ROLES_TEXTO. */
    private static final String[] ROLES_VALOR = {
            Usuario.ROL_CLIENTE, Usuario.ROL_VETERINARIO, Usuario.ROL_ADMIN
    };
    private static final String[] ROLES_TEXTO = { "Cliente", "Veterinario", "Administrador" };

    private ActivityUsuarioFormBinding vista;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esAdmin()) {
            aviso(getString(R.string.msg_solo_admin));
            finish();
            return;
        }

        vista = ActivityUsuarioFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        usuarioId = getIntent().getIntExtra(EXTRA_USUARIO_ID, 0);
        boolean esAlta = usuarioId == 0;
        setTitle(esAlta ? R.string.titulo_usuario_nuevo : R.string.titulo_usuario_editar);

        ArrayAdapter<String> roles = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ROLES_TEXTO);
        roles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vista.campoRol.setAdapter(roles);

        vista.ayudaPassword.setText(esAlta ? R.string.password_reglas : R.string.ayuda_password_edicion);
        vista.campoActivo.setChecked(true);
        vista.botonGuardar.setOnClickListener(v -> guardar());
        vista.botonEliminar.setOnClickListener(v -> confirmarEliminar());

        // El admin no se elimina a sí mismo: perdería el acceso en el acto.
        Usuario yo = sesion.getUsuario();
        boolean soyYo = yo != null && yo.getId() == usuarioId;
        vista.bloqueEliminar.setVisibility(esAlta || soyYo ? View.GONE : View.VISIBLE);
        if (soyYo) vista.campoActivo.setEnabled(false);

        if (!esAlta) cargar();
    }

    // ---------------------------------------------------------------- carga

    private void cargar() {
        cargando(true);
        ApiClient.getApi().usuario(usuarioId).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    completar(respuesta.body());
                } else {
                    aviso(ApiError.mensaje(respuesta));
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
                finish();
            }
        });
    }

    private void completar(Usuario u) {
        vista.campoNombre.setText(u.getNombre());
        vista.campoEmail.setText(u.getEmail());
        vista.campoTelefono.setText(u.getTelefono());
        vista.campoDireccion.setText(u.getDireccion());
        for (int i = 0; i < ROLES_VALOR.length; i++) {
            if (ROLES_VALOR[i].equals(u.getRol())) vista.campoRol.setSelection(i);
        }
        vista.campoActivo.setChecked(u.getActivo() == null || u.getActivo());
    }

    // -------------------------------------------------------------- guardar

    /** Valida en el cliente. Devuelve true si se puede enviar. */
    private boolean validar(String nombre, String email, String password) {
        boolean ok = true;
        vista.campoNombre.setError(null);
        vista.campoEmail.setError(null);
        vista.campoPassword.setError(null);

        if (!Validaciones.obligatorio(nombre)) {
            vista.campoNombre.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        if (!Validaciones.emailValido(email)) {
            vista.campoEmail.setError(getString(R.string.error_email));
            ok = false;
        }
        // En el alta es obligatoria; en la edición sólo se valida si la escribió.
        if (usuarioId == 0 || !password.isEmpty()) {
            String error = Validaciones.errorPassword(password);
            if (error != null) {
                vista.campoPassword.setError(error);
                ok = false;
            }
        }
        return ok;
    }

    private void guardar() {
        String nombre = vista.campoNombre.getText().toString().trim();
        String email = vista.campoEmail.getText().toString().trim();
        String password = vista.campoPassword.getText().toString();
        if (!validar(nombre, email, password)) return;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("email", email);
        cuerpo.put("telefono", vista.campoTelefono.getText().toString().trim());
        cuerpo.put("direccion", vista.campoDireccion.getText().toString().trim());
        cuerpo.put("rol", ROLES_VALOR[vista.campoRol.getSelectedItemPosition()]);
        cuerpo.put("is_active", vista.campoActivo.isChecked());
        if (!password.isEmpty()) cuerpo.put("password", password);

        Call<Usuario> llamada = usuarioId == 0
                ? ApiClient.getApi().crearUsuario(cuerpo)
                : ApiClient.getApi().editarUsuario(usuarioId, cuerpo);

        cargando(true);
        llamada.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(usuarioId == 0 ? R.string.msg_usuario_creado : R.string.msg_cambios_guardados));
                    finish();
                } else {
                    mostrarErrores(ErroresDeCampo.de(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    /** Cada error del servidor va debajo de su campo; el resto, en el aviso general. */
    private void mostrarErrores(ErroresDeCampo errores) {
        if (errores.campo("nombre") != null) vista.campoNombre.setError(errores.campo("nombre"));
        if (errores.campo("email") != null) vista.campoEmail.setError(errores.campo("email"));
        if (errores.campo("password") != null) vista.campoPassword.setError(errores.campo("password"));
        if (errores.campo("telefono") != null) vista.campoTelefono.setError(errores.campo("telefono"));
        if (errores.general() != null) {
            vista.textoError.setText(errores.general());
            vista.textoError.setVisibility(View.VISIBLE);
        } else {
            vista.textoError.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------------------- eliminar

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar_usuario)
                .setMessage(R.string.msg_eliminar_usuario)
                .setPositiveButton(R.string.accion_eliminar, (d, b) -> eliminar())
                .setNegativeButton(R.string.accion_volver, null)
                .show();
    }

    private void eliminar() {
        cargando(true);
        ApiClient.getApi().eliminarUsuario(usuarioId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.msg_usuario_eliminado));
                    finish();
                } else {
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
