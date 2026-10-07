# Reparto de las Activities

Las 14 Activities ya están creadas: clase, layout y declaración en el manifest.
Dos están terminadas y sirven de referencia —**Login** y **Mascotas**—; las otras
doce tienen el andamio armado y un `// TODO` adentro.

Cada archivo abre con un comentario que dice qué endpoints consume, qué recibe
por Intent y qué cambia según el rol. Nadie depende de nadie para empezar.

## Propuesta

| Persona | Activities | Archivos que toca |
| --- | --- | --- |
| 1 | RegistroActivity | `ui/auth/RegistroActivity.java` · `activity_registro.xml` |
| 2 | HomeActivity (las tres variantes por rol) | `ui/home/HomeActivity.java` · `activity_home.xml` |
| 3 | MascotaFormActivity | `ui/mascota/MascotaFormActivity.java` · `activity_mascota_form.xml` |
| 4 | CarnetActivity (dos pestañas) | `ui/carnet/CarnetActivity.java` · `activity_carnet.xml` |
| 5 | AgendaActivity + TurnoFormActivity | `ui/agenda/` · `ui/turno/` |
| 6 | PerfilActivity + ContactoActivity + QuienesSomosActivity | `ui/perfil/` · `ui/publico/` |

**Al final, entre quienes terminen primero:** VacunacionFormActivity,
UsuariosActivity y ServiciosActivity. Son las menos riesgosas: si algo se cae,
se cae lo administrativo y no el recorrido principal.

## Pantallas del diseño que todavía no están en el proyecto

El mockup contempla además: SplashActivity, UsuarioFormActivity,
ServicioFormActivity, ConsultasActivity, VacunasActivity y VacunaFormActivity.
Los dos formularios de administración completan el CRUD de usuarios y servicios,
que la consigna exige. Definir en la planning si entran en este sprint.

## Orden sugerido

1. **HomeActivity primero.** Es el centro del recorrido: hasta que no esté, las
   demás pantallas se prueban entrando a mano.
2. **Carnet y MascotaForm después**, porque completan el flujo del cliente.
3. **Agenda y turnos** al final del bloque cliente.
4. **Lo administrativo**, que es lo que menos se ve en la demo.

## Qué mirar antes de arrancar la tuya

- El comentario que está arriba de tu Activity: dice qué endpoints consume y
  qué tiene de particular.
- El diseño de tu pantalla en el mockup del equipo.
- `LoginActivity` si tu pantalla es un formulario.
- `MascotasActivity` si tu pantalla es un listado.
- El `README.md`, sección "Cosas que muerden".

## Regla para no pisarse

Cada uno toca **su Activity y su layout**. Si necesitás cambiar algo compartido
—un color, un string, el manifest, la capa de red— avisá en el grupo antes.
No es burocracia: es que esos archivos los abren seis personas y el merge duele.

Si te falta un endpoint o un campo que la API no devuelve, tampoco lo resuelvas
del lado de Android: avisá, porque probablemente haya que agregarlo al backend.