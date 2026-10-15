package com.devteamispc.petshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.databinding.ActivityUsuariosBinding;
import com.devteamispc.petshop.ui.BaseActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 13 · UsuariosActivity — CRUD 1 de la consigna (Read; Create/Update/Delete en UsuarioFormActivity)
 *
 * Endpoint: GET /usuarios/?search=&rol=&ordering=&page_size=
 *
 * Sólo administrador: el backend devuelve 403 a cualquier otro rol, y la
 * pantalla además se cierra si la abre alguien que no es admin.
 * Búsqueda por nombre o email (con una pausa corta para no pedir en cada tecla)
 * y filtro por rol con chips.
 */
public class UsuariosActivity extends BaseActivity {

    private static final long PAUSA_BUSQUEDA_MS = 350;

    private ActivityUsuariosBinding vista;
    private UsuarioAdapter adapter;
    private final Handler hilo = new Handler(Looper.getMainLooper());
    private final Runnable buscar = this::cargar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esAdmin()) {
            aviso(getString(R.string.msg_solo_admin));
            finish();
            return;
        }

        vista = ActivityUsuariosBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        adapter = new UsuarioAdapter(usuario -> abrirFormulario(usuario.getId()));
        vista.lista.setLayoutManager(new LinearLayoutManager(this));
        vista.lista.setAdapter(adapter);

        vista.campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable s) {
                hilo.removeCallbacks(buscar);
                hilo.postDelayed(buscar, PAUSA_BUSQUEDA_MS);
            }
        });
        vista.filtros.setOnCheckedStateChangeListener((grupo, ids) -> cargar());
        vista.refrescar.setOnRefreshListener(this::cargar);
        vista.botonNuevo.setOnClickListener(v -> abrirFormulario(0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    @Override
    protected void onDestroy() {
        hilo.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void abrirFormulario(int usuarioId) {
        Intent i = new Intent(this, UsuarioFormActivity.class);
        if (usuarioId != 0) i.putExtra(UsuarioFormActivity.EXTRA_USUARIO_ID, usuarioId);
        startActivity(i);
    }

    /** Rol elegido en los chips, o null para "Todos". */
    private String rolFiltrado() {
        int elegido = vista.filtros.getCheckedChipId();
        if (elegido == R.id.chipClientes) return Usuario.ROL_CLIENTE;
        if (elegido == R.id.chipVeterinarios) return Usuario.ROL_VETERINARIO;
        if (elegido == R.id.chipAdmins) return Usuario.ROL_ADMIN;
        return null;
    }

    private void cargar() {
        if (!vista.refrescar.isRefreshing() && adapter.getItemCount() == 0) {
            mostrarEstado(Estado.CARGANDO);
        }

        Map<String, String> filtros = new HashMap<>();
        String busqueda = vista.campoBuscar.getText().toString().trim();
        if (!busqueda.isEmpty()) filtros.put("search", busqueda);
        String rol = rolFiltrado();
        if (rol != null) filtros.put("rol", rol);
        filtros.put("ordering", "nombre");
        filtros.put("page_size", "100");

        ApiClient.getApi().usuarios(filtros).enqueue(new Callback<Pagina<Usuario>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Usuario>> call,
                                   @NonNull Response<Pagina<Usuario>> respuesta) {
                vista.refrescar.setRefreshing(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    List<Usuario> usuarios = respuesta.body().getResults();
                    adapter.actualizar(usuarios);
                    mostrarEstado(usuarios.isEmpty() ? Estado.VACIO : Estado.CON_DATOS);
                } else {
                    mostrarError(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Usuario>> call, @NonNull Throwable t) {
                vista.refrescar.setRefreshing(false);
                mostrarError(ApiError.sinConexion());
            }
        });
    }

    private enum Estado { CARGANDO, CON_DATOS, VACIO }

    private void mostrarEstado(Estado estado) {
        vista.progreso.setVisibility(estado == Estado.CARGANDO ? View.VISIBLE : View.GONE);
        vista.lista.setVisibility(estado == Estado.CON_DATOS ? View.VISIBLE : View.GONE);
        vista.textoVacio.setVisibility(estado == Estado.VACIO ? View.VISIBLE : View.GONE);
        if (estado == Estado.VACIO) vista.textoVacio.setText(R.string.vacio_usuarios);
    }

    private void mostrarError(String mensaje) {
        vista.progreso.setVisibility(View.GONE);
        vista.lista.setVisibility(View.GONE);
        vista.textoVacio.setVisibility(View.VISIBLE);
        vista.textoVacio.setText(mensaje);
    }
}
