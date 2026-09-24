# Petshop App Mobile — Android

Aplicación Android nativa en Java que consume la API REST de `backend/`.
Libreta digital de mascotas: vacunas, próximas dosis y turnos, con acceso
diferenciado por rol.

## Antes de escribir una línea

**Tocá sólo dos archivos: tu Activity y tu layout.**

Todo lo demás ya está hecho y es compartido. Si seis personas editan el
manifest o los colores, cada merge es un conflicto y se pierde medio día.

| Archivo | Quién lo toca |
| --- | --- |
| `AndroidManifest.xml` | Nadie. Las 14 Activities ya están declaradas |
| `res/values/colors.xml`, `themes.xml`, `strings.xml` | Nadie sin avisar |
| `app/build.gradle` | Nadie. Las dependencias ya están |
| `data/` (api, model, local) | Nadie sin avisar al equipo |
| `ui/BaseActivity.java` | Nadie sin avisar |
| `ui/<tu paquete>/TuActivity.java` | **Vos** |
| `res/layout/activity_tu_pantalla.xml` | **Vos** |

## Puesta en marcha

1. Abrí este proyecto en Android Studio y esperá el Gradle Sync.
2. Corré la app en el emulador o en tu teléfono.

No hace falta levantar nada: **la app apunta al backend desplegado**. La URL sale
de `BuildConfig.API_URL` y por omisión es la de producción.

Si preferís trabajar contra un backend local, agregalo a `local.properties`, que
no se versiona:

```properties
API_URL=http://10.0.2.2:8000/api/       # emulador
API_URL=http://192.168.0.XX:8000/api/   # telefono en la misma red WiFi
```

Con backend local hay que levantarlo con `python manage.py runserver 0.0.0.0:8000`
para que sea alcanzable desde el teléfono, y agregar esa IP a `ALLOWED_HOSTS`.

### Cuentas de prueba

Todas con la contraseña `Petshop2026!`

| Email | Rol |
| --- | --- |
| `cliente@petshop.test` | cliente, 2 mascotas |
| `vet@petshop.test` | veterinario |
| `admin@petshop.test` | administrador |

## Cómo está organizado

```
data/
  api/     ApiClient, PetshopApi, AuthInterceptor, TokenAuthenticator, ApiError
  model/   los POJOs y Pagina<T>
  local/   SessionManager (token y usuario en SharedPreferences)
ui/
  BaseActivity      flecha de retorno, manejo del 401, acceso a la sesion
  auth/             Login, Registro
  home/             Home (las tres variantes por rol)
  mascota/          Mascotas, MascotaForm, MascotaAdapter
  carnet/           Carnet
  turno/            TurnoForm
  vacunacion/       VacunacionForm
  agenda/           Agenda
  perfil/           Perfil
  publico/          Contacto, QuienesSomos
  admin/            Usuarios, Servicios
util/
  Fechas            conversion entre lo que muestra la app y lo que espera la API
```

Las doce Activities pendientes están creadas con su andamio: la clase, el layout,
la declaración en el manifest y un comentario de cabecera que dice qué endpoints
consume y qué tiene de particular. El cuerpo es un `// TODO`.

## Las dos pantallas de referencia

Están hechas de punta a punta. Antes de arrancar la tuya, leelas:

- **`ui/auth/LoginActivity.java`** — formulario con POST, validación, estado de carga y manejo de error.
- **`ui/mascota/MascotasActivity.java`** — listado con RecyclerView, buscador, filtros, estado vacío y paso de datos a la pantalla siguiente.

## Cómo llamar a la API

```java
Map<String, String> filtros = new HashMap<>();
filtros.put("estado", "pendiente");

ApiClient.getApi().turnos(filtros).enqueue(new Callback<Pagina<Turno>>() {

    @Override
    public void onResponse(Call<Pagina<Turno>> call, Response<Pagina<Turno>> respuesta) {
        if (manejarErrorComun(respuesta)) return;   // se ocupa del 401

        if (respuesta.isSuccessful() && respuesta.body() != null) {
            adapter.actualizar(respuesta.body().getResults());
        } else {
            aviso(ApiError.mensaje(respuesta));     // el motivo real, no un generico
        }
    }

    @Override
    public void onFailure(Call<Pagina<Turno>> call, Throwable t) {
        aviso(ApiError.sinConexion());
    }
});
```

Tres cosas que ya están resueltas y no tenés que repetir:

- **El header de autorización** lo agrega `AuthInterceptor` solo.
- **El token vencido** lo renueva `TokenAuthenticator` sin que el usuario se entere.
- **Los mensajes de error** los traduce `ApiError`: si el backend dice *"La contraseña debe incluir al menos un número"*, mostrá eso, no un "error genérico".

## Importante a tener en cuenta

**La primera consulta puede tardar hasta un minuto.** El backend está en un plan
gratuito que se apaga por inactividad y tarda en despertar. No es un error de tu
pantalla: si hace una llamada al abrir, mostrá un indicador de carga.

**Los listados vienen paginados.** Todo es `Pagina<T>`, no `List<T>`. Los datos
están en `.getResults()`. La excepción es `/agenda/`, que devuelve
`{proximas_dosis, turnos}`.

**El peso viaja como String** (`"28.50"`), porque así serializa los decimales el backend.

**Los Spinner de catálogo muestran una cosa y envían otra.** La especie se muestra
como "Perro" pero se manda `perro`. El `especie_display` es sólo para mostrar.

**Las fechas tienen dos formatos.** `yyyy-MM-dd` para fechas sueltas, ISO 8601 UTC
para fecha y hora. Usá `util/Fechas` en los dos sentidos y no armes strings a mano.

**Los días restantes ya vienen calculados** en `dias_restantes` y
`dias_para_proxima_dosis`. Vienen negativos si está vencido. No los recalcules.

**El rol decide qué se muestra**, pero el backend igual lo valida. Si tu pantalla
deja hacer algo que el rol no puede, vas a ver un 403 — y está bien que así sea.

## Convenciones

- Una rama por persona desde `develop`.
- Commits en castellano, en presente: *"agrega listado de turnos"*.
- Antes de pushear: que compile y que la app arranque.
- Todo ícono sin texto lleva `contentDescription` y 48 dp de área táctil.