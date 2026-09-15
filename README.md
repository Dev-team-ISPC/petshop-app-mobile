# 🐾 Petshop App Mobile 

Proyecto integrador de la Tecnicatura Superior en Desarrollo Web y Aplicaciones Digitales del ISPC, Modulo de Aplicaciones Digitales. La aplicación propone una libreta digital para mascotas que permite centralizar y consultar de forma portátil la información de cada animal, sus vacunas, turnos y registros relevantes, con acceso diferenciado para clientes, veterinarios y administradores.

## Equipo de desarrollo

| Integrante | GitHub |
|---|---|
| Claudio Nicolas Audicio | [NicolasAudicio](https://github.com/NicolasAudicio) |
| Francisco Junco | [FranJL075](https://github.com/FranJL075) |
| Lautaro Ancillotti | [lnancillotti](https://github.com/lnancillotti) |
| Maximiliano Fernandez | [lanusroots](https://github.com/lanusroots) |
| Carlos Ferri del Castillo | [Carlos-Ferri-Del-Castillo](https://github.com/Carlos-Ferri-Del-Castillo) |
| Cesar Ramiro Ruggieri | [subrami22](https://github.com/subrami22) |

## Objetivo

Facilitar el seguimiento y la trazabilidad de la salud y los cuidados de las mascotas mediante una aplicación accesible desde dispositivos móviles. El sistema busca reemplazar registros dispersos o en papel, mantener la información organizada y permitir que cada usuario acceda únicamente a las funciones y datos autorizados para su rol.

## Funcionalidades

- Registro e inicio de sesión de usuarios.
- Acceso diferenciado por roles: cliente, veterinario y administrador.
- Registro y consulta de mascotas.
- Consulta del carnet o libreta digital de cada mascota.
- Registro y seguimiento de vacunaciones y próximas dosis.
- Gestión de turnos veterinarios.
- Consulta de productos.
- Pantallas institucionales de inicio, contacto y quiénes somos.
- Administración de usuarios y productos.

## Tecnologías utilizadas

- **Frontend:** Angular 21, TypeScript, HTML y CSS.
- **Backend:** Django 6 y Django REST Framework.
- **Base de datos:** MySQL.
- **Seguridad de configuración:** variables de entorno mediante `python-dotenv`.
- **Gestión del proyecto:** Git, GitHub Issues, Milestones, Projects y Wiki.

## Estructura del proyecto

```text
petshop-app-mobile/
├── backend/    # API REST, modelos, autenticación y base de datos
└── frontend/   # Interfaz, rutas, componentes y servicios
```

## Requerimientos funcionales principales

1. Registrar usuarios con correo único y contraseña válida.
2. Iniciar y cerrar sesión.
3. Mostrar funciones diferentes según el rol.
4. Registrar y consultar mascotas.
5. Consultar la libreta digital de una mascota.
6. Consultar y registrar vacunaciones y próximas dosis.
7. Consultar y gestionar turnos veterinarios.
8. Consultar productos.
9. Enviar mensajes desde Contacto.
10. Gestionar usuarios y productos como administrador.

## Requerimientos no funcionales principales

- Compatibilidad con Android 5 o superior cuando se empaquete como aplicación móvil.
- Secretos en variables de entorno y fuera del repositorio.
- Contraseñas almacenadas mediante hash.
- Autenticación y autorización en endpoints privados.
- Validación de propiedad de recursos y roles en el backend.
- CORS restringido a entornos autorizados.
- Navegación con regreso desde pantallas hijas.
- Interfaz adaptable desde 320 px.

## Instalación y ejecución

### Backend

```bash
cd backend
python -m venv .venv
```

Luego, activar el entorno virtual y ejecutar:

```bash
pip install -r requirements.txt
cp .env_modelo .env
python manage.py migrate
python manage.py runserver
```

### Frontend

```bash
cd frontend
npm install
npm start
```

## Organización Scrum

El trabajo se organiza mediante Product Backlog, Sprint Backlog, Issues, Milestones y un tablero Kanban con los estados `Product Backlog`, `To Do`, `In Progress`, `Testing (QA)` y `Done`. Las ceremonias Planning, Daily Scrum, Review y Retrospective se documentan en la Wiki de cada sprint.

## Repositorios relacionados

- [AplicacionMovil](https://github.com/Dev-team-ISPC/AplicacionMovil): actividades y prácticas de Aplicaciones Móviles.
- [petshop-app](https://github.com/Dev-team-ISPC/petshop-app): proyecto del módulo Programador Web del primer cuatrimestre.
- [petshop-app-mobile](https://github.com/Dev-team-ISPC/petshop-app-mobile): repositorio principal de Proyecto Integrador II.

## Documentación

La Wiki reúne la documentación de Sprint 0 y Sprint 1, las ceremonias Scrum, el Plan de Pruebas, los Test Cases, el Plan de Seguridad y el enlace al documento IEEE 830.

## Estado del proyecto

El proyecto se encuentra en desarrollo incremental. La navegación, los modelos principales y parte de las funcionalidades ya están presentes. La migración de la autenticación propia a JWT, el refuerzo de la autorización por propietario y rol y las restantes medidas del Plan de Seguridad se encuentran planificados para las siguientes iteraciones.
