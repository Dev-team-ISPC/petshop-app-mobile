package com.devteamispc.petshop.ui.mascota;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.Mascota;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.databinding.ActivityMascotasBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.carnet.CarnetActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PANTALLA DE REFERENCIA — patrón de GET paginado con lista y filtros.
 *
 * Si tu pantalla muestra un listado de la API, copiá la estructura de acá:
 * RecyclerView + adapter, mapa de filtros, estado vacío, estado de error,
 * y el paso de datos a la siguiente pantalla por extras del Intent.
 *
 * El mismo endpoint devuelve cosas distintas según el rol: al cliente sólo
 * sus mascotas, al veterinario todas. El filtrado lo hace el servidor.
 *
 * Endpoint: GET /mascotas/?search=&ordering=&page_size=
 */
public class MascotasActivity extends BaseActivity {

    private ActivityMascotasBinding vista;
    private MascotaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityMascotasBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // El veterinario y el admin ven todas, así que el título cambia.
        setTitle(sesion.esCliente()
                ? R.string.titulo_mascotas
                : R.string.titulo_mascotas_vet);

        configurarLista();
        configurarBuscador();

        // Sólo el cliente y el veterinario dan de alta mascotas.
        boolean puedeCrear = sesion.esCliente() || sesion.esVeterinario();
        vista.botonNueva.setVisibility(puedeCrear ? View.VISIBLE : View.GONE);
        vista.botonNueva.setOnClickListener(v ->
                startActivity(new Intent(this, MascotaFormActivity.class)));

        vista.refrescar.setOnRefreshListener(this::cargarMascotas);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver del formulario, la lista se actualiza sola.
        cargarMascotas();
    }

    private void configurarLista() {
        // El veterinario necesita ver de quién es cada mascota.
        adapter = new MascotaAdapter(!sesion.esCliente(), mascota -> {
            Intent i = new Intent(this, CarnetActivity.class);
            i.putExtra(CarnetActivity.EXTRA_MASCOTA_ID, mascota.getId());
            i.putExtra(CarnetActivity.EXTRA_MASCOTA_NOMBRE, mascota.getNombre());
            startActivity(i);
        });

        vista.lista.setLayoutManager(new LinearLayoutManager(this));
        vista.lista.setAdapter(adapter);
    }

    private void configurarBuscador() {
        vista.campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                cargarMascotas();
            }
        });
    }

    private void cargarMascotas() {
        mostrarEstado(Estado.CARGANDO);

        Map<String, String> filtros = new HashMap<>();
        String busqueda = vista.campoBuscar.getText().toString().trim();
        if (!busqueda.isEmpty()) {
            // El backend busca por nombre, raza y nombre del dueño.
            filtros.put("search", busqueda);
        }
        filtros.put("ordering", "nombre");

        ApiClient.getApi().mascotas(filtros).enqueue(new Callback<Pagina<Mascota>>() {

            @Override
            public void onResponse(@NonNull Call<Pagina<Mascota>> call,
                                   @NonNull Response<Pagina<Mascota>> respuesta) {
                vista.refrescar.setRefreshing(false);

                if (manejarErrorComun(respuesta)) return;

                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    adapter.actualizar(respuesta.body().getResults());
                    mostrarEstado(adapter.getItemCount() == 0 ? Estado.VACIO : Estado.CON_DATOS);
                } else {
                    mostrarError(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Mascota>> call, @NonNull Throwable t) {
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
        if (estado == Estado.VACIO) {
            vista.textoVacio.setText(R.string.vacio_mascotas);
        }
    }

    private void mostrarError(String mensaje) {
        vista.progreso.setVisibility(View.GONE);
        vista.lista.setVisibility(View.GONE);
        vista.textoVacio.setVisibility(View.VISIBLE);
        vista.textoVacio.setText(mensaje);
    }
}
