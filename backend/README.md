# Backend — Petshop App Mobile

API REST que consume la aplicación Android. Django 6 + Django REST Framework + MySQL, con autenticación JWT.

## Puesta en marcha

```bash
cd backend
python -m venv venv
venv\Scripts\activate
```

```bash
pip install -r requirements.txt
```

Crear el archivo `.env` a partir del modelo y completarlo:

```bash
copy .env.example .env
```

Generar una `SECRET_KEY` propia:

```bash
python -c "from django.core.management.utils import get_random_secret_key; print(get_random_secret_key())"
```

Crear la base en MySQL:

```sql
CREATE DATABASE petshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Migrar y cargar datos de prueba:

```bash
python manage.py makemigrations api
python manage.py migrate
python manage.py seed
```

```bash
python manage.py runserver
```

Para probar desde el emulador de Android, la API queda en `http://10.0.2.2:8000/api/`.

## Cuentas de prueba

Todas usan la contraseña `Petshop2026!`.

| Email | Rol | Datos |
| --- | --- | --- |
| `admin@petshop.test` | administrador | acceso completo y al admin de Django |
| `vet@petshop.test` | veterinario | ve todas las mascotas, confirma turnos |
| `cliente@petshop.test` | cliente | 2 mascotas (Rocco y Mía), turnos y vacunaciones |
| `cliente2@petshop.test` | cliente | 1 mascota (Toby), sirve para probar aislamiento entre clientes |

`python manage.py seed --reset` borra y recarga.

## Endpoints

Base: `/api/`. Todos los listados vienen paginados de a 20 en `{count, next, previous, results}`, salvo `/agenda/`.

### Autenticación

| Método | Ruta | Permiso |
| --- | --- | --- |
| POST | `/auth/registro/` | público |
| POST | `/auth/login/` | público |
| POST | `/auth/refresh/` | público |
| POST | `/auth/logout/` | autenticado |
| GET · PATCH · DELETE | `/auth/me/` | autenticado |

El login devuelve `access`, `refresh` y `usuario`. El header es `Authorization: Bearer <access>`.

### Recursos

| Ruta | Lectura | Escritura |
| --- | --- | --- |
| `/usuarios/` | admin | admin |
| `/usuarios/veterinarios/` | autenticado | — |
| `/mascotas/` | filtrado por rol | dueño y admin |
| `/mascotas/{id}/vacunaciones/` | dueño, vet, admin | — |
| `/mascotas/{id}/turnos/` | dueño, vet, admin | — |
| `/vacunas/` | autenticado | sólo veterinario |
| `/vacunaciones/` | filtrado por rol | sólo veterinario |
| `/servicios/` | autenticado | admin |
| `/turnos/` | filtrado por rol | cliente crea y cancela, vet confirma, admin borra |
| `/resumen/` | autenticado | — |
| `/agenda/` | autenticado | — |
| `/contacto/` | admin | envío público |
| `/productos/` · `/categorias/` | heredados del módulo web | admin |

### Filtros, búsqueda y orden

Todos los listados aceptan `?search=`, `?ordering=` y `?page_size=`. Además:

| Endpoint | Parámetros |
| --- | --- |
| `/turnos/` | `estado` (acepta varios separados por coma), `mascota`, `desde`, `hasta`, `futuros=1` |
| `/vacunaciones/` | `mascota`, `proximas=1` |
| `/mascotas/` | `dueno`, `especie` |
| `/usuarios/` | `rol`, `activo` |
| `/servicios/` | `activo` |
| `/contacto/` | `leida` |

`GET /api/resumen/` devuelve, en una sola llamada, lo que muestra la pantalla de inicio de cada rol: el cliente recibe sus totales y la próxima dosis destacada; el veterinario, los turnos pendientes y los de hoy; el administrador, los contadores generales y las consultas sin leer.

### Campos derivados

Vienen calculados desde el servidor para que la app no repita lógica:

| Campo | Dónde | Para qué |
| --- | --- | --- |
| `edad_anios` | mascota | cabecera del carnet |
| `dias_para_proxima_dosis` | vacunación | badge del carnet (negativo si está vencida) |
| `dias_restantes` | turno | badge de la agenda (negativo si ya pasó) |
| `dueno` y `dueno_nombre` | turno | lista de turnos del veterinario |
| `*_display` | especie, rol, estado | etiqueta legible sin traducir en la app |

### Filtrado por rol

Se resuelve en el `get_queryset()` del servidor, nunca en el cliente.

| Endpoint | cliente | veterinario | admin |
| --- | --- | --- | --- |
| `/mascotas/` | sólo las suyas | todas | todas |
| `/vacunaciones/` | de sus mascotas | todas | todas |
| `/turnos/` | de sus mascotas | todos | todos |
| `/agenda/` | lo suyo | lo asignado o sin asignar | todo |
| `/usuarios/` | 403 | 403 | todos |

## Decisiones de seguridad

- **JWT con SimpleJWT.** Access de 60 minutos, refresh de 7 días, con rotación y blacklist. El logout invalida el refresh en el servidor.
- **Contraseñas.** Más de 8 caracteres, con mayúscula, minúscula, número y carácter especial. Validado en `api/validators.py` y aplicado en el registro y en el alta desde el panel de administración.
- **Autorización.** Clases de permiso por rol en `api/permissions.py`, más `EsDuenoOStaff` a nivel de objeto, que cubre el caso de cambiar el id en la URL.
- **Separación entre lo administrativo y lo clínico.** El administrador gestiona usuarios, servicios y turnos, y lee la historia clínica, pero **no la escribe**: registrar una vacunación y mantener el catálogo de vacunas son exclusivos del veterinario. Es el principio de mínimo privilegio aplicado al rol más poderoso.
- **Cancelar no es borrar.** El cliente puede pasar su turno a cancelado, pero no eliminarlo: el histórico se conserva. Tampoco puede confirmarlo, completarlo, reabrirlo ni moverle la fecha. El borrado queda reservado al administrador.
- **El veterinario queda asignado al turno que confirma**, sin tener que elegirse a sí mismo.
- **El veterinario abre la libreta.** Puede dar de alta una mascota asignando dueño y corregir datos clínicos como el peso medido en la consulta, pero no eliminar mascotas.
- **El dueño lo asigna el servidor.** Si un cliente manda `dueno` en el alta de una mascota, se ignora.
- **`DEBUG` por omisión `False`.** Un descuido de configuración falla del lado seguro.
- **`SECRET_KEY` sin valor por omisión.** Si falta en el `.env`, el proyecto no arranca.
- **CORS con lista explícita** de orígenes, tomada del `.env`.
- **Throttling** en contacto (10/hora), login (20/hora) y registro (10/hora).
- **HTTPS forzado** cuando `DEBUG=False`, junto con HSTS y cookies seguras.

## Cambios respecto del backend del módulo web

| Antes | Ahora |
| --- | --- |
| Token propio sin expiración, en texto plano en la tabla | JWT con expiración, rotación y blacklist |
| `Usuario` como `models.Model` con `is_authenticated` fijo en `True` | `AbstractBaseUser` + `PermissionsMixin`, login por email |
| Claves primarias mezcladas (`id_turno`, `id`, `id_producto`) | Todas `id` |
| `name`, `role` | `nombre`, `rol` |
| `Vacunacion.nombre_vacuna` y `veterinario` como texto libre | Claves foráneas a `Vacuna` y `Usuario` |
| `Turno.motivo` como texto libre | Clave foránea a `Servicio` |
| `Turno.fecha` sólo fecha | Fecha y hora |
| `Mascota.peso` entero | Decimal(5,2) |
| `especie` texto libre | Choices |
| `APIView` a mano con `try/except` repetido | `ModelViewSet` con router |
| Sin paginación | 20 por página |
| Sin permisos por rol | Clases de permiso por rol y por propiedad |
| `CORS_ALLOW_ALL_ORIGINS = True` | Lista explícita |

Las entidades `Producto` y `Categoria` se conservan porque el frontend Angular todavía las usa. La aplicación móvil no las consume.

## Verificación

El proyecto se probó con tres recorridos sobre los endpoints reales, 55 comprobaciones en total.

`smoke.py` (15): login, filtrado por rol, acceso a recursos ajenos por id, permisos por rol, agenda, asignación de dueño, política de contraseñas, contacto público y blacklist del refresh token.

`smoke2.py` (19): el cliente cancela pero no confirma, no completa, no reabre, no mueve la fecha y no borra; el administrador lee la historia clínica pero no la escribe; el veterinario registra y queda firmado, da de alta mascotas y corrige el peso, pero no elimina; y el catálogo de vacunas lo mantiene el veterinario, no el administrador.

`smoke3.py` (21): el resumen de cada rol, los filtros de turnos por estado y fecha, los buscadores de mascotas y usuarios, los campos derivados, el orden, el tamaño de página y la asignación automática del veterinario al confirmar un turno.
