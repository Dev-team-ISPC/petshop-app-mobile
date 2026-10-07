package com.devteamispc.petshop.data.api;

import com.devteamispc.petshop.data.model.*;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Contrato de la API. Está congelado: si hace falta cambiar una firma,
 * se avisa al equipo antes, porque hay pantallas que dependen de ella.
 *
 * Todos los listados devuelven Pagina&lt;T&gt; y aceptan search, ordering y page_size.
 */
public interface PetshopApi {

    // ------------------------------------------------------------------ auth

    @POST("auth/registro/")
    Call<Usuario> registro(@Body RegistroRequest body);

    @POST("auth/login/")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("auth/refresh/")
    Call<RefreshResponse> refresh(@Body RefreshRequest body);

    @POST("auth/logout/")
    Call<Void> logout(@Body RefreshRequest body);

    @GET("auth/me/")
    Call<Usuario> miPerfil();

    @PATCH("auth/me/")
    Call<Usuario> editarMiPerfil(@Body Usuario body);

    /** Botón de arrepentimiento: baja de la cuenta propia. */
    @DELETE("auth/me/")
    Call<Void> eliminarMiCuenta();

    // --------------------------------------------------------------- resumen

    /** Lo que muestra la pantalla de inicio, según el rol. */
    @GET("resumen/")
    Call<Resumen> resumen();

    @GET("agenda/")
    Call<Agenda> agenda();

    // -------------------------------------------------------------- usuarios

    @GET("usuarios/")
    Call<Pagina<Usuario>> usuarios(@QueryMap Map<String, String> filtros);

    @GET("usuarios/{id}/")
    Call<Usuario> usuario(@Path("id") int id);

    @POST("usuarios/")
    Call<Usuario> crearUsuario(@Body Map<String, Object> body);

    @PATCH("usuarios/{id}/")
    Call<Usuario> editarUsuario(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("usuarios/{id}/")
    Call<Void> eliminarUsuario(@Path("id") int id);

    /** Para los selectores que necesitan elegir veterinario. */
    @GET("usuarios/veterinarios/")
    Call<Pagina<Usuario>> veterinarios();

    // -------------------------------------------------------------- mascotas

    /** Filtros: search, dueno, especie, ordering, page, page_size. */
    @GET("mascotas/")
    Call<Pagina<Mascota>> mascotas(@QueryMap Map<String, String> filtros);

    @GET("mascotas/{id}/")
    Call<Mascota> mascota(@Path("id") int id);

    @POST("mascotas/")
    Call<Mascota> crearMascota(@Body Mascota body);

    @PUT("mascotas/{id}/")
    Call<Mascota> reemplazarMascota(@Path("id") int id, @Body Mascota body);

    @PATCH("mascotas/{id}/")
    Call<Mascota> editarMascota(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("mascotas/{id}/")
    Call<Void> eliminarMascota(@Path("id") int id);

    @GET("mascotas/{id}/vacunaciones/")
    Call<Pagina<Vacunacion>> vacunacionesDeMascota(@Path("id") int id);

    @GET("mascotas/{id}/turnos/")
    Call<Pagina<Turno>> turnosDeMascota(@Path("id") int id);

    // --------------------------------------------------------------- vacunas

    @GET("vacunas/")
    Call<Pagina<Vacuna>> vacunas(@QueryMap Map<String, String> filtros);

    @POST("vacunas/")
    Call<Vacuna> crearVacuna(@Body Map<String, Object> body);

    @DELETE("vacunas/{id}/")
    Call<Void> eliminarVacuna(@Path("id") int id);

    // ---------------------------------------------------------- vacunaciones

    /** Filtros: search, mascota, proximas=1, ordering. */
    @GET("vacunaciones/")
    Call<Pagina<Vacunacion>> vacunaciones(@QueryMap Map<String, String> filtros);

    @GET("vacunaciones/{id}/")
    Call<Vacunacion> vacunacion(@Path("id") int id);

    /** Sólo veterinario: al admin le devuelve 403. */
    @POST("vacunaciones/")
    Call<Vacunacion> crearVacunacion(@Body NuevaVacunacion body);

    @PATCH("vacunaciones/{id}/")
    Call<Vacunacion> editarVacunacion(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("vacunaciones/{id}/")
    Call<Void> eliminarVacunacion(@Path("id") int id);

    // ------------------------------------------------------------- servicios

    @GET("servicios/")
    Call<Pagina<Servicio>> servicios(@QueryMap Map<String, String> filtros);

    @POST("servicios/")
    Call<Servicio> crearServicio(@Body Map<String, Object> body);

    @PATCH("servicios/{id}/")
    Call<Servicio> editarServicio(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("servicios/{id}/")
    Call<Void> eliminarServicio(@Path("id") int id);

    // ---------------------------------------------------------------- turnos

    /** Filtros: estado (acepta coma), mascota, desde, hasta, futuros=1, search. */
    @GET("turnos/")
    Call<Pagina<Turno>> turnos(@QueryMap Map<String, String> filtros);

    @GET("turnos/{id}/")
    Call<Turno> turno(@Path("id") int id);

    @POST("turnos/")
    Call<Turno> crearTurno(@Body NuevoTurno body);

    /**
     * El cliente sólo puede mandar "cancelado". Confirmar y completar son
     * del veterinario, que además queda asignado al turno automáticamente.
     */
    @PATCH("turnos/{id}/")
    Call<Turno> cambiarEstadoTurno(@Path("id") int id, @Body CambioEstado body);

    @PATCH("turnos/{id}/")
    Call<Turno> editarTurno(@Path("id") int id, @Body Map<String, Object> body);

    /** Sólo admin: borrar destruye el histórico. */
    @DELETE("turnos/{id}/")
    Call<Void> eliminarTurno(@Path("id") int id);

    // -------------------------------------------------------------- contacto

    /** Envío público: no requiere sesión. */
    @POST("contacto/")
    Call<Consulta> enviarConsulta(@Body Consulta body);

    @GET("contacto/")
    Call<Pagina<Consulta>> consultas(@Query("leida") Boolean leida);

    @PATCH("contacto/{id}/")
    Call<Consulta> marcarConsultaLeida(@Path("id") int id, @Body Map<String, Object> body);
}
