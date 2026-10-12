package com.devteamispc.petshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Servicio;
import com.devteamispc.petshop.databinding.ActivityServiciosBinding;
import com.devteamispc.petshop.ui.BaseActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 14 · ServiciosActivity — CRUD 2 de la consigna (Read; el resto en ServicioFormActivity)
 *
 * Endpoint: GET /servicios/?activo=&ordering=&page_size=
 *
 * Catálogo de servicios que se ofrecen al pedir turno. Sólo administrador.
 * Un servicio inactivo deja de ofrecerse, pero los turnos ya registrados lo
 * conservan: por eso lo habitual es desactivarlo, no borrarlo.
 */
public class ServiciosActivity extends BaseActivity {

    private ActivityServiciosBinding vista;
    private ServicioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esAdmin()) {
            aviso(getString(R.string.msg_solo_admin));
            finish();
            return;
        }

        vista = ActivityServiciosBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        adapter = new ServicioAdapter(this::abrirEdicion);
        vista.lista.setLayoutManager(new LinearLayoutManager(this));
        vista.lista.setAdapter(adapter);

        vista.filtros.setOnCheckedStateChangeListener((grupo, ids) -> cargar());
        vista.refrescar.setOnRefreshListener(this::cargar);
        vista.botonNuevo.setOnClickListener(v ->
                startActivity(new Intent(this, ServicioFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    /**
     * No hay GET /servicios/{id}/ en el contrato de la API, así que los datos
     * del servicio viajan por extras del Intent.
     */
    private void abrirEdicion(Servicio s) {
        Intent i = new Intent(this, ServicioFormActivity.class);
        i.putExtra(ServicioFormActivity.EXTRA_SERVICIO_ID, s.getId());
        i.putExtra(ServicioFormActivity.EXTRA_NOMBRE, s.getNombre());
        i.putExtra(ServicioFormActivity.EXTRA_DESCRIPCION, s.getDescripcion());
        i.putExtra(ServicioFormActivity.EXTRA_DURACION, s.getDuracionMinutos());
        i.putExtra(ServicioFormActivity.EXTRA_ACTIVO, s.isActivo());
        startActivity(i);
    }

    private void cargar() {
        if (!vista.refrescar.isRefreshing() && adapter.getItemCount() == 0) {
            mostrarEstado(Estado.CARGANDO);
        }

        Map<String, String> filtros = new HashMap<>();
        int elegido = vista.filtros.getCheckedChipId();
        if (elegido == R.id.chipActivos) filtros.put("activo", "true");
        if (elegido == R.id.chipInactivos) filtros.put("activo", "false");
        filtros.put("ordering", "nombre");
        filtros.put("page_size", "100");

        ApiClient.getApi().servicios(filtros).enqueue(new Callback<Pagina<Servicio>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Servicio>> call,
                                   @NonNull Response<Pagina<Servicio>> respuesta) {
                vista.refrescar.setRefreshing(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    List<Servicio> servicios = respuesta.body().getResults();
                    adapter.actualizar(servicios);
                    mostrarEstado(servicios.isEmpty() ? Estado.VACIO : Estado.CON_DATOS);
                } else {
                    mostrarError(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Servicio>> call, @NonNull Throwable t) {
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
        if (estado == Estado.VACIO) vista.textoVacio.setText(R.string.vacio_servicios);
    }

    private void mostrarError(String mensaje) {
        vista.progreso.setVisibility(View.GONE);
        vista.lista.setVisibility(View.GONE);
        vista.textoVacio.setVisibility(View.VISIBLE);
        vista.textoVacio.setText(mensaje);
    }
}
