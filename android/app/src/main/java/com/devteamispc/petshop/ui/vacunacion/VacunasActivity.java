package com.devteamispc.petshop.ui.vacunacion;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Vacuna;
import com.devteamispc.petshop.databinding.ActivityVacunasBinding;
import com.devteamispc.petshop.ui.BaseActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 18 · VacunasActivity — catálogo de vacunas. Sólo veterinario.
 *
 * Endpoints: GET /vacunas/?search= · DELETE /vacunas/{id}/
 *
 * La frecuencia de cada vacuna es la que usa el formulario de vacunación para
 * sugerir la próxima dosis. Tocar una vacuna muestra su detalle y la opción de
 * eliminarla; una vacuna ya aplicada a alguna mascota no se puede borrar.
 */
public class VacunasActivity extends BaseActivity {

    private static final long PAUSA_BUSQUEDA_MS = 350;

    private ActivityVacunasBinding vista;
    private VacunaAdapter adapter;
    private final Handler hilo = new Handler(Looper.getMainLooper());
    private final Runnable buscar = this::cargar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esVeterinario()) {
            aviso(getString(R.string.msg_solo_veterinario_catalogo));
            finish();
            return;
        }

        vista = ActivityVacunasBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        adapter = new VacunaAdapter(this::mostrarDetalle);
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
        vista.refrescar.setOnRefreshListener(this::cargar);
        vista.botonNueva.setOnClickListener(v -> startActivity(new Intent(this, VacunaFormActivity.class)));
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

    private void cargar() {
        if (!vista.refrescar.isRefreshing() && adapter.getItemCount() == 0) mostrarEstado(Estado.CARGANDO);
        Map<String, String> filtros = new HashMap<>();
        String busqueda = vista.campoBuscar.getText().toString().trim();
        if (!busqueda.isEmpty()) filtros.put("search", busqueda);
        filtros.put("ordering", "nombre");
        filtros.put("page_size", "100");

        ApiClient.getApi().vacunas(filtros).enqueue(new Callback<Pagina<Vacuna>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Vacuna>> call, @NonNull Response<Pagina<Vacuna>> respuesta) {
                vista.refrescar.setRefreshing(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    List<Vacuna> vacunas = respuesta.body().getResults();
                    adapter.actualizar(vacunas);
                    mostrarEstado(vacunas.isEmpty() ? Estado.VACIO : Estado.CON_DATOS);
                } else {
                    mostrarError(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Vacuna>> call, @NonNull Throwable t) {
                vista.refrescar.setRefreshing(false);
                mostrarError(ApiError.sinConexion());
            }
        });
    }

    private void mostrarDetalle(Vacuna v) {
        String descripcion = v.getDescripcion() == null || v.getDescripcion().isEmpty()
                ? getString(R.string.vacuna_sin_descripcion) : v.getDescripcion();
        new AlertDialog.Builder(this)
                .setTitle(v.getNombre())
                .setMessage(getString(R.string.vacuna_detalle, v.getFrecuencia(), descripcion))
                .setPositiveButton(R.string.accion_cerrar, null)
                .setNegativeButton(R.string.accion_eliminar_vacuna, (d, b) -> confirmarEliminar(v))
                .show();
    }

    private void confirmarEliminar(Vacuna v) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar_vacuna)
                .setMessage(R.string.msg_eliminar_vacuna)
                .setPositiveButton(R.string.accion_eliminar, (d, b) -> eliminar(v))
                .setNegativeButton(R.string.accion_volver, null)
                .show();
    }

    private void eliminar(Vacuna v) {
        ApiClient.getApi().eliminarVacuna(v.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.msg_vacuna_eliminada));
                    cargar();
                } else if (respuesta.code() == 409 || respuesta.code() >= 500) {
                    // Ya aplicada a alguna mascota: la base protege el historial.
                    new AlertDialog.Builder(VacunasActivity.this)
                            .setTitle(R.string.titulo_no_se_puede_eliminar)
                            .setMessage(R.string.msg_vacuna_con_aplicaciones)
                            .setPositiveButton(R.string.accion_aceptar, null)
                            .show();
                } else {
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                aviso(ApiError.sinConexion());
            }
        });
    }

    private enum Estado { CARGANDO, CON_DATOS, VACIO }

    private void mostrarEstado(Estado estado) {
        vista.progreso.setVisibility(estado == Estado.CARGANDO ? View.VISIBLE : View.GONE);
        vista.lista.setVisibility(estado == Estado.CON_DATOS ? View.VISIBLE : View.GONE);
        vista.textoVacio.setVisibility(estado == Estado.VACIO ? View.VISIBLE : View.GONE);
        if (estado == Estado.VACIO) vista.textoVacio.setText(R.string.vacio_vacunas_catalogo);
    }

    private void mostrarError(String mensaje) {
        vista.progreso.setVisibility(View.GONE);
        vista.lista.setVisibility(View.GONE);
        vista.textoVacio.setVisibility(View.VISIBLE);
        vista.textoVacio.setText(mensaje);
    }
}
