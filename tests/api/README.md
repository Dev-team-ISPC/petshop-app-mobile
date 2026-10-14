# Pruebas de API (AUT-API) — Postman + Newman

Suite de caja negra de la API: **53 pedidos y 247 verificaciones**, en orden,
cubriendo todos los recursos. Se corre completa con Newman, sin intervención.

## Qué verifica cada pedido

**Siempre** (script a nivel colección):
- que no haya errores del servidor (5xx);
- que el tiempo de respuesta esté por debajo del límite (`sla_ms`, 800 ms);
- que la respuesta sea JSON cuando tiene cuerpo.

Login, registro y alta de usuarios tienen un límite propio (`sla_hash_ms`,
2000 ms): calculan el hash de la contraseña con PBKDF2, que es lento **a
propósito** para frenar ataques de fuerza bruta.

**Además, cada pedido** verifica su código de estado y su contrato (campos y
valores). Los nombres indican el tipo:

| Prefijo | Tipo | Ejemplo |
| --- | --- | --- |
| (ninguno) | Caso feliz | Crear mascota → 201 y el dueño lo asigna el servidor |
| `NEG ·` | Caso negativo | Peso negativo → 400 |
| `SEG ·` | Seguridad | Mascota ajena por id → 404 (IDOR); reusar un refresh rotado → 401 |

## Carpetas

| # | Carpeta | Qué cubre |
| --- | --- | --- |
| 00 | Preparación | Login de las cuatro cuentas del `seed` |
| 01 | Autenticación | Perfil, refresh con rotación, login inválido, registro inválido, sin token |
| 02 | Mascotas | Alta, detalle, edición, aislamiento entre clientes |
| 03 | Servicios (CRUD 2) | CRUD completo y permiso de escritura |
| 04 | Turnos | Ciclo pendiente → confirmado → completado y transiciones inválidas |
| 05 | Vacunas y vacunaciones | Registro por el veterinario, permisos del admin y del cliente |
| 06 | Usuarios (CRUD 1) | CRUD completo y acceso exclusivo del admin |
| 07 | Contacto | Envío público, validación, lectura del admin |
| 08 | Inicio por rol | `/resumen/` distinto según el rol |
| 09 | Limpieza | Borra lo creado, para poder repetir la suite |

## Cómo correrla

Requisitos: Node.js y la API con los datos del `seed`.

```powershell
npm install -g newman newman-reporter-htmlextra
cd tests\api
newman run Petshop-API.postman_collection.json -e local.postman_environment.json -r cli,htmlextra --reporter-htmlextra-export reportes\api.html
```

- `local.postman_environment.json` → `http://localhost:8000/api`
- `produccion.postman_environment.json` → la API en Render. **Despertar el
  servidor antes**: la primera respuesta tras 15 minutos sin uso tarda hasta un
  minuto y hace fallar el límite de tiempo.

La API tiene límites de uso (throttling): login 20 por hora, registro y contacto
10 por hora. Correr la suite muchas veces seguidas termina en respuestas 429.

### Sin MySQL

El backend trae `config/settings_pruebas.py`, que usa SQLite:

```powershell
cd backend
$env:DJANGO_SETTINGS_MODULE = "config.settings_pruebas"
python manage.py migrate
python manage.py seed
python manage.py runserver
```

## En el pipeline

`.github/workflows/ci.yml` hace lo mismo en cada push o PR a `main`, `develop`
y `release`, y guarda el reporte HTML como artefacto de la ejecución.

## Resultado de referencia

Corrida local contra el backend en modo producción (`DEBUG=False`), SQLite:
**53 pedidos, 247 verificaciones, 0 fallas**. Tiempo medio 130 ms; máximo
902 ms (login, con hash).
