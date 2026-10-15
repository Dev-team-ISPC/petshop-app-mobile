# 🐾 Petshop App Mobile

[![Pruebas](https://github.com/Dev-team-ISPC/petshop-app-mobile/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Dev-team-ISPC/petshop-app-mobile/actions/workflows/ci.yml)

**Libreta sanitaria digital para mascotas.** Aplicación Android nativa que centraliza los datos de cada mascota, sus vacunas, próximas dosis y turnos, con acceso diferenciado para clientes, veterinarios y administradores. Consume una API REST en Django desplegada en Render.

Proyecto Integrador II · Tecnicatura Superior en Desarrollo Web y Aplicaciones Digitales · ISPC · 2026

## 🎯 Objetivo

Facilitar el seguimiento y la trazabilidad de la salud y los cuidados de las mascotas mediante una aplicación accesible desde dispositivos móviles. El sistema busca reemplazar registros dispersos o en papel, mantener la información organizada y permitir que cada usuario acceda únicamente a las funciones y datos autorizados para su rol.

---

## 📲 Descargar y ver

| | |
|---|---|
| **APK** | [Última versión en Releases](https://github.com/Dev-team-ISPC/petshop-app-mobile/releases/latest) |
| **Video de demostración** | *(enlace)* |
| **Video de autenticación y autorización** | *(enlace)* |
| **API desplegada** | `https://petshop-api-sk23.onrender.com/api/` |

> El servidor usa el plan gratuito de Render: si estuvo 15 minutos sin uso, la primera respuesta tarda hasta un minuto. La pantalla de inicio de la app lo despierta antes de mostrar el login.

**Instalación:** descargar el `.apk` en un teléfono con Android 5.0 o superior, abrirlo y permitir la instalación desde esa fuente cuando el sistema lo pida.

---

## ✨ Funcionalidades

### Todos los usuarios
- Registro con política de contraseñas visible mientras se escribe y aceptación de los términos y la política de privacidad.
- Inicio de sesión con JWT; la sesión se mantiene al reabrir la app y se renueva sola.
- Panel de inicio distinto según el rol.
- Contacto: dirección, teléfono, email y formulario de consulta.
- Quiénes somos, con imagen institucional.
- Mi perfil: consultar y editar los datos propios, cerrar sesión y **eliminar la cuenta** (botón de arrepentimiento).

### Cliente
- **Mis mascotas:** alta, edición, eliminación y consulta.
- **Carnet digital** de cada mascota, con pestañas de vacunas y turnos.
- **Turnos:** solicitar y cancelar.
- **Agenda:** próximas dosis y turnos futuros, ordenados por fecha.

### Veterinario
- Consultar todas las mascotas y corregir su peso.
- **Registrar vacunaciones**, con la próxima dosis sugerida según la frecuencia de la vacuna.
- Gestionar el **catálogo de vacunas**.
- **Confirmar, completar y cancelar turnos**. Al confirmar, el turno queda asignado a quien lo confirma.

### Administrador
- **CRUD de usuarios**, con filtros por rol y búsqueda.
- **CRUD de servicios**. Un servicio con turnos no se borra: se desactiva.
- Leer las consultas recibidas.
- Agenda general y consulta de la historia clínica, sin modificarla.

---

## 🧱 Tecnologías

| Capa | Tecnología |
|---|---|
| App | Java · Android SDK 34 (mínimo Android 5.0, API 21) · Material Components · View Binding |
| Red | Retrofit 2 · OkHttp 4 · Gson |
| Backend | Django 6 · Django REST Framework · SimpleJWT · django-cors-headers · whitenoise · gunicorn |
| Base de datos | MySQL (administrada, con SSL, en producción) |
| Pruebas | JUnit 4 · MockWebServer · Espresso · Accessibility Test Framework · Postman + Newman |
| CI | GitHub Actions |

## 🗂️ Estructura del repositorio

```
petshop-app-mobile/
├── android/                 App Android (proyecto de Android Studio)
│   └── app/src/
│       ├── main/            Código y recursos de la app
│       ├── test/            Tests unitarios y de red (JVM)
│       └── androidTest/     Flujos de Espresso y Screen Objects
├── backend/                 API REST en Django
├── tests/api/               Colección de Postman y entornos (Newman)
└── .github/workflows/       Pipeline de pruebas
```

---

## 🔐 Seguridad

- **JWT:** access de 60 minutos y refresh de 7 días con **rotación y revocación**; el cierre de sesión invalida el refresh en el servidor.
- **Autorización por rol y por propietario** resuelta en el backend: un recurso ajeno pedido por ID responde 404.
- **Contraseñas robustas** validadas en el servidor (más de 8 caracteres, mayúsculas, minúsculas, números y un carácter especial).
- **Límite de intentos** en login, registro y contacto.
- **Registros de seguridad**: logins fallidos, accesos denegados, cambios de rol y acciones administrativas, sin contraseñas ni tokens.
- HTTPS forzado con HSTS, secretos en variables de entorno, `DEBUG` desactivado por defecto y CORS cerrado.

Políticas, responsables y casos de prueba en la página [Ciberseguridad](https://github.com/Dev-team-ISPC/petshop-app-mobile/wiki/Ciberseguridad) de la Wiki.

---

## 🛠️ Ejecutar en local

### App

1. Abrir la carpeta `android/` con Android Studio.
2. Elegir un emulador o un teléfono y ejecutar **Run 'app'**.

Por defecto, la versión debug usa la API de Render. Para usar un backend local, agregar en `android/local.properties` (no se versiona):

```properties
API_URL=http://10.0.2.2:8000/api/
```

`10.0.2.2` es la PC vista desde el emulador; desde un teléfono físico se usa la IP de la PC en la red local.

### Backend

Requisitos: Python 3.12 y MySQL (por ejemplo, en Docker).

```powershell
cd backend
python -m venv venv
.\venv\Scripts\Activate.ps1
pip install -r requirements.txt
copy .env.example .env          # completar SECRET_KEY y los datos de la base
python manage.py migrate
python manage.py seed           # datos y cuentas de prueba
python manage.py runserver
```

Sin MySQL, con SQLite: `$env:DJANGO_SETTINGS_MODULE = "config.settings_pruebas"` antes de `migrate`.

### Cuentas de prueba (las crea `seed` en el entorno local)

| Email | Rol |
|---|---|
| `admin@petshop.test` | Administrador |
| `vet@petshop.test` | Veterinario |
| `cliente@petshop.test` | Cliente, con 2 mascotas |
| `cliente2@petshop.test` | Cliente, con 1 mascota |

Contraseña de todas: `Petshop2026!`

---

## 🧪 Pruebas

| Suite | Cantidad | Comando |
|---|---|---|
| Unitarios y de red (app) | 107 | `cd android` y `.\gradlew.bat testDebugUnitTest` |
| Flujos de Espresso + accesibilidad | 7 | `.\gradlew.bat connectedDebugAndroidTest` (con emulador) |
| Backend | 8 | `cd backend` y `python manage.py test api` (con `config.settings_pruebas`) |
| API (Postman + Newman) | 53 pedidos, 247 verificaciones | Ver [`tests/api/README.md`](tests/api/README.md) |

El pipeline corre los tests unitarios y la colección de Postman en cada push y en cada Pull Request a `main`, `develop` y `release`. Estrategia, resultados y trazabilidad en la página [Testing](https://github.com/Dev-team-ISPC/petshop-app-mobile/wiki/Testing) de la Wiki.

---

## 📚 Documentación

| Documento | Dónde |
|---|---|
| Especificación de requisitos (IEEE 830) | [Wiki → Documentación](https://github.com/Dev-team-ISPC/petshop-app-mobile/wiki/Documentación) |
| Plan de Pruebas, Test Cases y trazabilidad | [Wiki → Testing](https://github.com/Dev-team-ISPC/petshop-app-mobile/wiki/Testing) |
| Plan de seguridad | [Wiki → Ciberseguridad](https://github.com/Dev-team-ISPC/petshop-app-mobile/wiki/Ciberseguridad) |
| Informe de accesibilidad | [Wiki → Accesibilidad](https://github.com/Dev-team-ISPC/petshop-app-mobile/wiki/Accesibilidad) |
| Sprints y ceremonias | [Wiki](https://github.com/Dev-team-ISPC/petshop-app-mobile/wiki) |
| Tablero Kanban | Pestaña **Projects** del repositorio |

---

## 🗓️ Organización Scrum

- El trabajo se organiza con Product Backlog, Sprint Backlog, Issues, Milestones por sprint y un tablero Kanban con las columnas `Product Backlog`, `To Do`, `In Progress`, `Testing (QA)` y `Done`.
- Las ceremonias (Planning, Daily, Review y Retrospective) se documentan en la página de cada sprint en la Wiki.
- **Ramas:** `main` (versión estable de cada sprint, la que se corrige) ← `release` ← `develop` ← `feature/<integrante>/<tarea>`.
- Cada tarea entra a `develop` por un Pull Request con al menos una aprobación, y se mergea con **merge commit** (nunca squash) para conservar la autoría de cada integrante.
- Una tarea está en **Done** cuando está integrada, verificada y con evidencia; un test, además, con el pipeline en verde.

## 🔗 Repositorios relacionados

| Repositorio | Contenido |
|---|---|
| [petshop-app](https://github.com/Dev-team-ISPC/petshop-app) | Cliente web (Angular) del módulo Programador Web |
| [AplicacionMovil](https://github.com/Dev-team-ISPC/AplicacionMovil) | Actividades de la materia Aplicaciones Móviles |

---

## ⚠️ Limitaciones conocidas

Lo que todavía no está resuelto, para que nada de este README prometa algo que la app no hace:

- **Turnos:** la app no le ofrece al administrador confirmar o completar turnos, pero la API todavía se lo permite. Se corrige en el Sprint 3.
- **Lista de veterinarios:** el endpoint devuelve más datos de los necesarios (email, teléfono). Se reduce a id y nombre en el Sprint 3.
- **Sesión en el teléfono:** se guarda en almacenamiento privado de la app, sin cifrar, porque el cifrado nativo requiere Android 6 y la app soporta desde Android 5.
- **Pruebas:** no hay pruebas E2E con Appium ni cobertura medida; los flujos de Espresso corren en local, no en el pipeline.
- **Servidor:** la primera respuesta después de un rato sin uso puede tardar hasta un minuto (plan gratuito de Render).

---

## 👥 Equipo — Dev Team ISPC

| Integrante | GitHub |
|---|---|
| Maximiliano Fernandez | [@lanusroots](https://github.com/lanusroots) |
| Francisco Junco | [@FranJL075](https://github.com/FranJL075) |
| Claudio Nicolás Audicio | [@NicolasAudicio](https://github.com/NicolasAudicio) |
| Lautaro Ancillotti | [@lnancillotti](https://github.com/lnancillotti) |
| Carlos Ferri del Castillo | [@Carlos-Ferri-Del-Castillo](https://github.com/Carlos-Ferri-Del-Castillo) |
| Cesar Ramiro Ruggieri | [@subrami22](https://github.com/subrami22) |
