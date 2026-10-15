package com.devteamispc.petshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.Agenda;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Resumen;
import com.devteamispc.petshop.data.model.Turno;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.databinding.ActivityHomeBinding;
import com.devteamispc.petshop.databinding.ItemFilaMenuBinding;
import com.devteamispc.petshop.databinding.ItemNumeroBinding;
import com.devteamispc.petshop.databinding.ItemTarjetaAccesoBinding;
import com.devteamispc.petshop.databinding.ItemTurnoPendienteBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.admin.ConsultasActivity;
import com.devteamispc.petshop.ui.admin.ServiciosActivity;
import com.devteamispc.petshop.ui.admin.UsuariosActivity;
import com.devteamispc.petshop.ui.agenda.AgendaActivity;
import com.devteamispc.petshop.ui.carnet.CarnetActivity;
import com.devteamispc.petshop.ui.mascota.MascotasActivity;
import com.devteamispc.petshop.ui.perfil.PerfilActivity;
import com.devteamispc.petshop.ui.publico.ContactoActivity;
import com.devteamispc.petshop.ui.turno.TurnoFormActivity;
import com.devteamispc.petshop.ui.vacunacion.VacunasActivity;
import com.devteamispc.petshop.util.Badge;
import com.devteamispc.petshop.util.CierreSesion;
import com.devteamispc.petshop.util.Estados;
import com.devteamispc.petshop.util.Fechas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 3 · HomeActivity — panel diferenciado por rol (RF08), con el diseño del mockup.
 *
 * Endpoints: GET /resumen/ · GET /turnos/?estado=pendiente (veterinario)
 *
 * UNA sola Activity para los tres roles: mismo código, tres resultados.
 *   cliente     -> "Hola, nombre", próxima dosis destacada y tarjetas: Mis
 *                  mascotas, Agenda, Pedir turno, Mi perfil, Contacto
 *   veterinario -> nombre y rol, "Turnos por confirmar" y tarjetas: Registrar
 *                  vacuna, Turnos, Mascotas, Vacunas, Mi perfil
 *   admin       -> "Administración", tres números y un menú en lista
 *                  (Usuarios, Servicios, Turnos, Consultas, Mascotas, Mi perfil)
 *
 * Arriba a la derecha, el botón de salir (US08), con confirmación.
 */
public class HomeActivity extends BaseActivity {

    private static final int PENDIENTES_EN_INICIO = 2;

    private ActivityHomeBinding vista;
    private Resumen resumen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        pintarEncabezado(sesion.getUsuario());
        ViewCompat.setAccessibilityHeading(vista.textoNombre, true);
        ViewCompat.setAccessibilityHeading(vista.tituloPendientes, true);

        vista.botonSalir.setOnClickListener(v -> confirmarCierre());
        vista.linkVerTodos.setOnClickListener(v -> abrir(AgendaActivity.class));
        vista.botonReintentar.setOnClickListener(v -> cargar());
        vista.refrescar.setOnRefreshListener(this::cargar);

        pintarAccesos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de otra pantalla, los números se actualizan.
        cargar();
    }

    // ------------------------------------------------------------ encabezado

    private void pintarEncabezado(Usuario yo) {
        String nombre = yo == null || yo.getNombre() == null ? "" : yo.getNombre();
        if (sesion.esAdmin()) {
            vista.textoHola.setVisibility(View.GONE);
            vista.textoNombre.setText(R.string.home_administracion);
            mostrarRol(R.string.rol_admin);
        } else if (sesion.esVeterinario()) {
            vista.textoHola.setVisibility(View.GONE);
            vista.textoNombre.setText(nombre);
            mostrarRol(R.string.rol_veterinario);
        } else {
            vista.textoHola.setVisibility(View.VISIBLE);
            vista.textoNombre.setText(nombre);
            vista.chipRol.setVisibility(View.GONE);
        }
    }

    private void mostrarRol(int texto) {
        vista.chipRol.setVisibility(View.VISIBLE);
        vista.chipRol.setText(texto);
    }

    private void confirmarCierre() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_cerrar_sesion)
                .setMessage(R.string.msg_cerrar_sesion)
                .setPositiveButton(R.string.accion_cerrar_sesion, (d, b) -> CierreSesion.cerrar(this))
                .setNegativeButton(R.string.accion_quedarme, null)
                .show();
    }

    // ----------------------------------------------------------------- carga

    private void cargar() {
        if (!vista.refrescar.isRefreshing() && resumen == null) {
            vista.progreso.setVisibility(View.VISIBLE);
        }
        vista.bloqueError.setVisibility(View.GONE);

        ApiClient.getApi().resumen().enqueue(new Callback<Resumen>() {
            @Override
            public void onResponse(@NonNull Call<Resumen> call, @NonNull Response<Resumen> respuesta) {
                terminarCarga();
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    resumen = respuesta.body();
                    pintarResumen();
                } else {
                    mostrarError(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Resumen> call, @NonNull Throwable t) {
                terminarCarga();
                mostrarError(ApiError.sinConexion());
            }
        });

        if (sesion.esVeterinario()) cargarPendientes();
    }

    private void terminarCarga() {
        vista.progreso.setVisibility(View.GONE);
        vista.refrescar.setRefreshing(false);
    }

    private void mostrarError(String mensaje) {
        vista.textoError.setText(mensaje);
        vista.bloqueError.setVisibility(View.VISIBLE);
    }

    /** Con los números del resumen se actualizan los textos de las tarjetas. */
    private void pintarResumen() {
        if (resumen.getNombre() != null && !sesion.esAdmin()) {
            vista.textoNombre.setText(resumen.getNombre());
        }
        if (sesion.esCliente()) pintarProximaDosis(resumen.getProximaDosis());
        if (sesion.esAdmin()) pintarNumeros();
        pintarAccesos();
    }

    // ------------------------------------------------------- cliente: dosis

    private void pintarProximaDosis(Agenda.ProximaDosis dosis) {
        if (dosis == null) {
            vista.tarjetaDosis.setVisibility(View.GONE);
            return;
        }
        vista.tarjetaDosis.setVisibility(View.VISIBLE);
        vista.dosisTitulo.setText(dosis.getVacunaNombre() + " · " + dosis.getMascotaNombre());
        vista.dosisFecha.setText(Fechas.aVisible(dosis.getFecha()));
        Badge.pintarDias(vista.cajaDias, vista.diasNumero, vista.diasEtiqueta, dosis.getDiasRestantes());
        vista.tarjetaDosis.setOnClickListener(v -> {
            Intent i = new Intent(this, CarnetActivity.class);
            i.putExtra(CarnetActivity.EXTRA_MASCOTA_ID, dosis.getMascotaId());
            i.putExtra(CarnetActivity.EXTRA_MASCOTA_NOMBRE, dosis.getMascotaNombre());
            startActivity(i);
        });
    }

    // ------------------------------------------------ veterinario: pendientes

    private void cargarPendientes() {
        vista.seccionPendientes.setVisibility(View.VISIBLE);
        Map<String, String> filtros = new HashMap<>();
        filtros.put("estado", Turno.PENDIENTE);
        filtros.put("futuros", "1");
        filtros.put("ordering", "fecha");
        filtros.put("page_size", String.valueOf(PENDIENTES_EN_INICIO));
        ApiClient.getApi().turnos(filtros).enqueue(new Callback<Pagina<Turno>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Turno>> call, @NonNull Response<Pagina<Turno>> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    pintarPendientes(respuesta.body().getResults());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Turno>> call, @NonNull Throwable t) {
                // El error de conexión ya lo muestra el resumen.
            }
        });
    }

    private void pintarPendientes(List<Turno> turnos) {
        vista.contenedorPendientes.removeAllViews();
        vista.textoSinPendientes.setVisibility(turnos.isEmpty() ? View.VISIBLE : View.GONE);
        for (Turno t : turnos) {
            ItemTurnoPendienteBinding f = ItemTurnoPendienteBinding.inflate(
                    LayoutInflater.from(this), vista.contenedorPendientes, false);
            f.servicio.setText(t.getServicioNombre());
            f.mascota.setText(t.getMascotaNombre() + (t.getDuenoNombre() != null ? " · " + t.getDuenoNombre() : ""));
            f.fecha.setText(Fechas.fechaHoraAVisible(t.getFecha()));
            f.estado.setText(t.getEstadoDisplay() != null ? t.getEstadoDisplay() : Estados.etiquetaEstado(t.getEstado()));
            Badge.pintar(f.estado, Estados.nivelTurno(t.getEstado()));
            f.getRoot().setOnClickListener(v -> abrir(AgendaActivity.class));
            vista.contenedorPendientes.addView(f.getRoot());
        }
    }

    // ------------------------------------------------------ admin: números

    private void pintarNumeros() {
        vista.contenedorNumeros.setVisibility(View.VISIBLE);
        vista.contenedorNumeros.removeAllViews();
        agregarNumero(resumen.getTotalUsuarios(), R.string.ficha_usuarios, UsuariosActivity.class);
        agregarNumero(resumen.getTotalMascotas(), R.string.ficha_mascotas, MascotasActivity.class);
        agregarNumero(resumen.getTotalTurnos(), R.string.ficha_turnos_corto, AgendaActivity.class);
    }

    private void agregarNumero(Integer numero, int etiqueta, Class<?> destino) {
        ItemNumeroBinding n = ItemNumeroBinding.inflate(LayoutInflater.from(this), vista.contenedorNumeros, false);
        String valor = numero == null ? "–" : String.valueOf(numero);
        n.numero.setText(valor);
        n.etiqueta.setText(etiqueta);
        n.getRoot().setContentDescription(valor + " " + getString(etiqueta));
        n.getRoot().setOnClickListener(v -> abrir(destino));
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) n.getRoot().getLayoutParams();
        if (vista.contenedorNumeros.getChildCount() > 0) {
            p.setMarginStart(getResources().getDimensionPixelSize(R.dimen.espacio_fichas));
        }
        vista.contenedorNumeros.addView(n.getRoot(), p);
    }

    // -------------------------------------------------------------- accesos

    private void pintarAccesos() {
        vista.contenedorAccesos.removeAllViews();
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(sesion.getRol());

        if (sesion.esAdmin()) {
            // Admin: menú en lista, como el mockup.
            for (ReglasHome.Acceso a : accesos) agregarFila(a);
            return;
        }
        // Cliente y veterinario: tarjetas de a dos.
        View pendiente = null;
        for (ReglasHome.Acceso a : accesos) {
            View tarjeta = tarjeta(a);
            if (pendiente == null) {
                pendiente = tarjeta;
            } else {
                agregarFila(pendiente, tarjeta);
                pendiente = null;
            }
        }
        if (pendiente != null) agregarFila(pendiente, null);
    }

    private View tarjeta(ReglasHome.Acceso a) {
        ItemTarjetaAccesoBinding t = ItemTarjetaAccesoBinding.inflate(LayoutInflater.from(this), vista.contenedorAccesos, false);
        t.icono.setImageResource(icono(a));
        t.titulo.setText(titulo(a));
        t.detalle.setText(detalle(a));
        t.getRoot().setOnClickListener(v -> ir(a));
        return t.getRoot();
    }

    private void agregarFila(ReglasHome.Acceso a) {
        ItemFilaMenuBinding f = ItemFilaMenuBinding.inflate(LayoutInflater.from(this), vista.contenedorAccesos, false);
        f.icono.setImageResource(icono(a));
        f.titulo.setText(titulo(a));
        f.detalle.setText(detalle(a));
        f.getRoot().setOnClickListener(v -> ir(a));
        vista.contenedorAccesos.addView(f.getRoot());
    }

    /** Dos tarjetas del mismo ancho; si la segunda falta, un hueco de alto 0. */
    private void agregarFila(View izquierda, View derecha) {
        int espacio = getResources().getDimensionPixelSize(R.dimen.espacio_fichas);
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams pIzq = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        pIzq.setMarginEnd(espacio / 2);
        pIzq.bottomMargin = espacio;
        fila.addView(izquierda, pIzq);

        if (derecha != null) {
            LinearLayout.LayoutParams pDer = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            pDer.setMarginStart(espacio / 2);
            pDer.bottomMargin = espacio;
            fila.addView(derecha, pDer);
        } else {
            // Alto 0: un View común con WRAP_CONTENT ocuparía todo el alto disponible.
            fila.addView(new Space(this), new LinearLayout.LayoutParams(0, 0, 1f));
        }
        vista.contenedorAccesos.addView(fila);
    }

    private void ir(ReglasHome.Acceso a) {
        switch (a) {
            case MASCOTAS:
            case REGISTRAR_VACUNA:   // se elige la mascota y desde su carnet se registra
                abrir(MascotasActivity.class);
                break;
            case AGENDA:
                abrir(AgendaActivity.class);
                break;
            case PEDIR_TURNO:
                abrir(TurnoFormActivity.class);
                break;
            case USUARIOS:
                abrir(UsuariosActivity.class);
                break;
            case SERVICIOS:
                abrir(ServiciosActivity.class);
                break;
            case CONSULTAS:
                abrir(ConsultasActivity.class);
                break;
            case VACUNAS:
                abrir(VacunasActivity.class);
                break;
            case CONTACTO:
                abrir(ContactoActivity.class);
                break;
            case PERFIL:
                abrir(PerfilActivity.class);
                break;
        }
    }

    private void abrir(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    @DrawableRes
    private int icono(ReglasHome.Acceso a) {
        switch (a) {
            case MASCOTAS: return R.drawable.ic_huella;
            case AGENDA: return sesion.esCliente() ? R.drawable.ic_calendario : R.drawable.ic_calendario_ok;
            case PEDIR_TURNO: return R.drawable.ic_reloj;
            case REGISTRAR_VACUNA: return R.drawable.ic_salud;
            case USUARIOS: return R.drawable.ic_grupo;
            case SERVICIOS: return R.drawable.ic_lista;
            case CONSULTAS: return R.drawable.ic_consultas;
            case VACUNAS: return R.drawable.ic_catalogo;
            case CONTACTO: return R.drawable.ic_telefono;
            default: return R.drawable.ic_persona;
        }
    }

    private String titulo(ReglasHome.Acceso a) {
        switch (a) {
            case MASCOTAS: return getString(sesion.esCliente() ? R.string.titulo_mascotas : R.string.titulo_mascotas_vet);
            case AGENDA: return getString(sesion.esCliente() ? R.string.titulo_agenda : R.string.titulo_turnos);
            case PEDIR_TURNO: return getString(R.string.home_pedir_turno);
            case REGISTRAR_VACUNA: return getString(R.string.home_registrar_vacuna);
            case USUARIOS: return getString(R.string.titulo_usuarios);
            case SERVICIOS: return getString(R.string.titulo_servicios);
            case CONSULTAS: return getString(R.string.titulo_consultas);
            case VACUNAS: return getString(R.string.titulo_vacunas);
            case CONTACTO: return getString(R.string.titulo_contacto);
            default: return getString(R.string.titulo_perfil);
        }
    }

    /** Detalle de cada acceso; los que dependen de números se completan con el resumen. */
    private String detalle(ReglasHome.Acceso a) {
        Resumen r = resumen;
        switch (a) {
            case MASCOTAS:
                if (sesion.esCliente()) {
                    return r == null || r.getTotalMascotas() == null ? "" : getResources().getQuantityString(
                            R.plurals.home_mascotas_registradas, r.getTotalMascotas(), r.getTotalMascotas());
                }
                return getString(sesion.esAdmin() ? R.string.home_detalle_mascotas_admin : R.string.home_detalle_mascotas_vet);
            case AGENDA:
                if (sesion.esCliente()) {
                    return r == null || r.getTurnosPendientes() == null ? "" : getResources().getQuantityString(
                            R.plurals.home_turnos_pendientes, r.getTurnosPendientes(), r.getTurnosPendientes());
                }
                if (sesion.esVeterinario()) {
                    return r == null || r.getTurnosHoy() == null ? "" : getResources().getQuantityString(
                            R.plurals.home_turnos_hoy, r.getTurnosHoy(), r.getTurnosHoy());
                }
                return getString(R.string.home_detalle_agenda_admin);
            case PEDIR_TURNO: return getString(R.string.home_detalle_pedir_turno);
            case REGISTRAR_VACUNA: return getString(R.string.home_detalle_registrar_vacuna);
            case USUARIOS: return getString(R.string.home_detalle_usuarios);
            case SERVICIOS:
                return r == null || r.getTotalServicios() == null ? getString(R.string.home_detalle_servicios)
                        : getResources().getQuantityString(R.plurals.home_servicios_activos, r.getTotalServicios(), r.getTotalServicios());
            case CONSULTAS:
                return r == null || r.getConsultasSinLeer() == null ? "" : getResources().getQuantityString(
                        R.plurals.home_consultas_sin_leer, r.getConsultasSinLeer(), r.getConsultasSinLeer());
            case VACUNAS: return getString(R.string.home_detalle_vacunas);
            case CONTACTO: return getString(R.string.home_detalle_contacto);
            default: return getString(R.string.home_detalle_perfil);
        }
    }
}
