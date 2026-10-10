package com.devteamispc.petshop.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.Consulta;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.databinding.ActivityConsultasBinding;
import com.devteamispc.petshop.ui.BaseActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 17 · ConsultasActivity — mensajes del formulario de Contacto. Sólo administrador.
 *
 * Endpoints: GET /contacto/?leida= · PATCH /contacto/{id}/ {leida: true}
 *
 * Filtros Todas / Sin leer (con la cantidad). Las sin leer llevan un punto y
 * el botón "Marcar como leída". Tocar el email abre el correo para responder.
 */
public class ConsultasActivity extends BaseActivity {

    private ActivityConsultasBinding vista;
    private ConsultaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esAdmin()) {
            aviso(getString(R.string.msg_solo_admin));
            finish();
            return;
        }

        vista = ActivityConsultasBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        adapter = new ConsultaAdapter(new ConsultaAdapter.Acciones() {
            @Override public void marcarLeida(Consulta c) { ConsultasActivity.this.marcarLeida(c); }
            @Override public void responder(Consulta c) { ConsultasActivity.this.responder(c); }
        });
        vista.lista.setLayoutManager(new LinearLayoutManager(this));
        vista.lista.setAdapter(adapter);

        vista.filtros.setOnCheckedStateChangeListener((grupo, ids) -> cargar());
        vista.refrescar.setOnRefreshListener(this::cargar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        if (!vista.refrescar.isRefreshing() && adapter.getItemCount() == 0) mostrarEstado(Estado.CARGANDO);
        Boolean leida = vista.filtros.getCheckedChipId() == R.id.chipSinLeer ? Boolean.FALSE : null;

        ApiClient.getApi().consultas(leida).enqueue(new Callback<Pagina<Consulta>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Consulta>> call, @NonNull Response<Pagina<Consulta>> respuesta) {
                vista.refrescar.setRefreshing(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    List<Consulta> consultas = respuesta.body().getResults();
                    adapter.actualizar(consultas);
                    mostrarEstado(consultas.isEmpty() ? Estado.VACIO : Estado.CON_DATOS);
                } else {
                    mostrarError(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Consulta>> call, @NonNull Throwable t) {
                vista.refrescar.setRefreshing(false);
                mostrarError(ApiError.sinConexion());
            }
        });
        contarSinLeer();
    }

    /** La cantidad del chip "Sin leer (2)". */
    private void contarSinLeer() {
        ApiClient.getApi().consultas(Boolean.FALSE).enqueue(new Callback<Pagina<Consulta>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Consulta>> call, @NonNull Response<Pagina<Consulta>> respuesta) {
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    vista.chipSinLeer.setText(getString(R.string.filtro_sin_leer_n, respuesta.body().getCount()));
                }
            }

            @Override public void onFailure(@NonNull Call<Pagina<Consulta>> call, @NonNull Throwable t) { }
        });
    }

    private void marcarLeida(Consulta c) {
        adapter.marcarOcupada(c.getId());
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("leida", true);
        ApiClient.getApi().marcarConsultaLeida(c.getId(), cuerpo).enqueue(new Callback<Consulta>() {
            @Override
            public void onResponse(@NonNull Call<Consulta> call, @NonNull Response<Consulta> respuesta) {
                adapter.marcarOcupada(-1);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.msg_consulta_leida));
                    cargar();
                } else {
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Consulta> call, @NonNull Throwable t) {
                adapter.marcarOcupada(-1);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void responder(Consulta c) {
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + c.getEmail()));
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.asunto_respuesta_consulta));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            aviso(getString(R.string.msg_sin_app));
        }
    }

    private enum Estado { CARGANDO, CON_DATOS, VACIO }

    private void mostrarEstado(Estado estado) {
        vista.progreso.setVisibility(estado == Estado.CARGANDO ? View.VISIBLE : View.GONE);
        vista.lista.setVisibility(estado == Estado.CON_DATOS ? View.VISIBLE : View.GONE);
        vista.textoVacio.setVisibility(estado == Estado.VACIO ? View.VISIBLE : View.GONE);
        if (estado == Estado.VACIO) vista.textoVacio.setText(R.string.vacio_consultas);
    }

    private void mostrarError(String mensaje) {
        adapter.actualizar(Collections.<Consulta>emptyList());
        vista.progreso.setVisibility(View.GONE);
        vista.lista.setVisibility(View.GONE);
        vista.textoVacio.setVisibility(View.VISIBLE);
        vista.textoVacio.setText(mensaje);
    }
}
