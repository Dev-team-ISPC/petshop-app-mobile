"""
Tests del backend.

    $env:DJANGO_SETTINGS_MODULE = "config.settings_pruebas"
    python manage.py test api

- RegistrosDeSeguridadTest (TK-S2-15): registros de seguridad y 409 al borrar
  en uso. Cada test verifica la respuesta HTTP y la línea que queda en el log
  "petshop.seguridad", y que en el log no aparezcan la contraseña, los tokens
  ni el email en claro.
- RecursosAjenosTest (TK-S2-18): IDOR. Un cliente no puede ver, modificar ni
  borrar recursos de otro cliente cambiando el id.
- VeterinariosTest (TK-S2-18): minimización de datos en el selector.

Todos siguen el patrón AAA.
"""
import json
from datetime import date, timedelta

from django.core.cache import cache
from django.utils import timezone
from rest_framework.test import APITestCase

from .models import Mascota, Servicio, Turno, Usuario, Vacuna, Vacunacion

PASSWORD = 'Petshop2026!'


def eventos(capturados):
    """Convierte las líneas capturadas por assertLogs en diccionarios."""
    return [json.loads(r.getMessage()) for r in capturados.records]


def buscar(capturados, nombre):
    return [e for e in eventos(capturados) if e['evento'] == nombre]


class RegistrosDeSeguridadTest(APITestCase):

    def setUp(self):
        cache.clear()  # reinicia los contadores de throttling entre tests
        self.admin = Usuario.objects.create_user(
            'admin@petshop.test', PASSWORD, nombre='Admin', rol=Usuario.Rol.ADMIN)
        self.cliente = Usuario.objects.create_user(
            'cliente@petshop.test', PASSWORD, nombre='Juan Pérez')

    # --------------------------------------------------------- autenticación

    def test_loginConPasswordIncorrecta_registraLoginFallidoSinDatosSensibles(self):
        # Arrange
        datos = {'email': 'cliente@petshop.test', 'password': 'Incorrecta1!'}

        # Act
        with self.assertLogs('petshop.seguridad', level='INFO') as log:
            respuesta = self.client.post('/api/auth/login/', datos, format='json')

        # Assert
        self.assertEqual(respuesta.status_code, 401)
        fallidos = buscar(log, 'login_fallido')
        self.assertEqual(len(fallidos), 1)
        self.assertEqual(fallidos[0]['resultado'], 'rechazado')
        self.assertEqual(fallidos[0]['detalle']['email_enmascarado'], 'c******@petshop.test')
        texto = ' '.join(log.output)
        self.assertNotIn('Incorrecta1!', texto)
        self.assertNotIn('cliente@petshop.test', texto)

    def test_loginCorrecto_registraLoginExitosoSinTokens(self):
        # Arrange
        datos = {'email': 'cliente@petshop.test', 'password': PASSWORD}

        # Act
        with self.assertLogs('petshop.seguridad', level='INFO') as log:
            respuesta = self.client.post('/api/auth/login/', datos, format='json')

        # Assert
        self.assertEqual(respuesta.status_code, 200)
        exitosos = buscar(log, 'login_exitoso')
        self.assertEqual(exitosos[0]['usuario_id'], self.cliente.id)
        self.assertEqual(exitosos[0]['rol'], 'cliente')
        texto = ' '.join(log.output)
        self.assertNotIn(respuesta.data['access'], texto)
        self.assertNotIn(respuesta.data['refresh'], texto)

    def test_reusarRefreshRotado_registraRefreshRechazado(self):
        # Arrange
        login = self.client.post('/api/auth/login/',
                                 {'email': 'cliente@petshop.test', 'password': PASSWORD},
                                 format='json')
        refresh_viejo = login.data['refresh']
        self.client.post('/api/auth/refresh/', {'refresh': refresh_viejo}, format='json')

        # Act
        with self.assertLogs('petshop.seguridad', level='WARNING') as log:
            respuesta = self.client.post('/api/auth/refresh/', {'refresh': refresh_viejo},
                                         format='json')

        # Assert
        self.assertEqual(respuesta.status_code, 401)
        self.assertEqual(len(buscar(log, 'refresh_rechazado')), 1)

    # ------------------------------------------------------ accesos denegados

    def test_clienteEnListadoDeUsuarios_registraAccesoDenegado(self):
        # Arrange
        self.client.force_authenticate(self.cliente)

        # Act
        with self.assertLogs('petshop.seguridad', level='WARNING') as log:
            respuesta = self.client.get('/api/usuarios/')

        # Assert
        self.assertEqual(respuesta.status_code, 403)
        denegado = buscar(log, 'acceso_denegado')[0]
        self.assertEqual(denegado['usuario_id'], self.cliente.id)
        self.assertEqual(denegado['rol'], 'cliente')
        self.assertEqual(denegado['ruta'], '/api/usuarios/')

    # --------------------------------------------- acciones administrativas

    def test_adminCambiaElRol_registraRolCambiadoConAnteriorYNuevo(self):
        # Arrange
        self.client.force_authenticate(self.admin)

        # Act
        with self.assertLogs('petshop.seguridad', level='INFO') as log:
            respuesta = self.client.patch(f'/api/usuarios/{self.cliente.id}/',
                                          {'rol': 'veterinario'}, format='json')

        # Assert
        self.assertEqual(respuesta.status_code, 200)
        cambio = buscar(log, 'rol_cambiado')[0]
        self.assertEqual(cambio['usuario_id'], self.admin.id)
        self.assertEqual(cambio['recurso'], f'usuario:{self.cliente.id}')
        self.assertEqual(cambio['detalle'], {'rol_anterior': 'cliente', 'rol_nuevo': 'veterinario'})

    def test_adminRestablecePassword_registraElEventoSinLaPassword(self):
        # Arrange
        self.client.force_authenticate(self.admin)
        nueva = 'NuevaClave2026#'

        # Act
        with self.assertLogs('petshop.seguridad', level='INFO') as log:
            self.client.patch(f'/api/usuarios/{self.cliente.id}/', {'password': nueva},
                              format='json')

        # Assert
        self.assertEqual(len(buscar(log, 'password_restablecida')), 1)
        self.assertNotIn(nueva, ' '.join(log.output))

    # ------------------------------------------------- borrado de algo en uso

    def test_borrarServicioConTurnos_devuelve409YLoRegistra(self):
        # Arrange
        servicio = Servicio.objects.create(nombre='Consulta general')
        mascota = Mascota.objects.create(
            nombre='Rocco', especie='perro', raza='Labrador', peso='28.50',
            fecha_nacimiento='2021-03-10', dueno=self.cliente)
        Turno.objects.create(mascota=mascota, servicio=servicio,
                             fecha=timezone.now() + timedelta(days=3))
        self.client.force_authenticate(self.admin)

        # Act
        with self.assertLogs('petshop.seguridad', level='WARNING') as log:
            respuesta = self.client.delete(f'/api/servicios/{servicio.id}/')

        # Assert
        self.assertEqual(respuesta.status_code, 409)
        self.assertTrue(Servicio.objects.filter(id=servicio.id).exists())
        self.assertEqual(buscar(log, 'eliminacion_rechazada')[0]['resultado'], 'conflicto')

    def test_borrarServicioSinTurnos_loEliminaYLoRegistra(self):
        # Arrange
        servicio = Servicio.objects.create(nombre='Baño')
        self.client.force_authenticate(self.admin)

        # Act
        with self.assertLogs('petshop.seguridad', level='WARNING') as log:
            respuesta = self.client.delete(f'/api/servicios/{servicio.id}/')

        # Assert
        self.assertEqual(respuesta.status_code, 204)
        self.assertEqual(buscar(log, 'servicio_eliminado')[0]['recurso'], f'servicio:{servicio.id}')


class RecursosAjenosTest(APITestCase):
    """
    IDOR: Ana (cliente2) intenta llegar a Rocco, la mascota de Juan, cambiando el id.

    La API responde 404 y no 403 a propósito: para Ana ese recurso no existe,
    así no se puede usar la respuesta para averiguar qué ids están ocupados.
    El filtro por dueño está en get_queryset de cada ViewSet.
    """

    def setUp(self):
        cache.clear()
        self.veterinario = Usuario.objects.create_user(
            'vet@petshop.test', PASSWORD, nombre='Dra. Gómez', rol=Usuario.Rol.VETERINARIO)
        self.juan = Usuario.objects.create_user(
            'cliente@petshop.test', PASSWORD, nombre='Juan Pérez')
        self.ana = Usuario.objects.create_user(
            'cliente2@petshop.test', PASSWORD, nombre='Ana López')
        self.rocco = Mascota.objects.create(
            nombre='Rocco', especie='perro', raza='Labrador', peso='28.50',
            fecha_nacimiento='2021-03-10', dueno=self.juan)
        self.toby = Mascota.objects.create(
            nombre='Toby', especie='perro', raza='Beagle', peso='12.00',
            fecha_nacimiento='2022-06-01', dueno=self.ana)
        self.servicio = Servicio.objects.create(nombre='Consulta general')
        self.vacunacion = Vacunacion.objects.create(
            mascota=self.rocco, vacuna=Vacuna.objects.create(nombre='Quíntuple', frecuencia='anual'),
            fecha_aplicacion=date(2026, 3, 15), veterinario=self.veterinario)
        self.turno = Turno.objects.create(
            mascota=self.rocco, servicio=self.servicio,
            fecha=timezone.now() + timedelta(days=3))
        self.client.force_authenticate(self.ana)

    def test_clienteAbreMascotaAjenaPorId_devuelve404(self):
        # Act
        respuesta = self.client.get(f'/api/mascotas/{self.rocco.id}/')

        # Assert
        self.assertEqual(respuesta.status_code, 404)
        self.assertNotIn('Rocco', json.dumps(respuesta.data))

    def test_clienteAbreCarnetDeMascotaAjena_devuelve404EnVacunasYTurnos(self):
        # Act
        vacunas = self.client.get(f'/api/mascotas/{self.rocco.id}/vacunaciones/')
        turnos = self.client.get(f'/api/mascotas/{self.rocco.id}/turnos/')

        # Assert
        self.assertEqual(vacunas.status_code, 404)
        self.assertEqual(turnos.status_code, 404)

    def test_clienteBorraMascotaAjena_devuelve404YNoLaBorra(self):
        # Act
        respuesta = self.client.delete(f'/api/mascotas/{self.rocco.id}/')

        # Assert
        self.assertEqual(respuesta.status_code, 404)
        self.assertTrue(Mascota.objects.filter(id=self.rocco.id).exists())

    def test_clienteAbreVacunacionAjenaPorId_devuelve404(self):
        # Act
        respuesta = self.client.get(f'/api/vacunaciones/{self.vacunacion.id}/')

        # Assert
        self.assertEqual(respuesta.status_code, 404)

    def test_clienteCancelaTurnoAjeno_devuelve404YElTurnoSigueIgual(self):
        # Act
        respuesta = self.client.patch(f'/api/turnos/{self.turno.id}/',
                                      {'estado': Turno.Estado.CANCELADO}, format='json')

        # Assert
        self.assertEqual(respuesta.status_code, 404)
        self.turno.refresh_from_db()
        self.assertEqual(self.turno.estado, Turno.Estado.PENDIENTE)

    def test_clientePideTurnoParaMascotaAjena_devuelve400YNoLoCrea(self):
        # Arrange
        datos = {
            'mascota': self.rocco.id,
            'servicio': self.servicio.id,
            'fecha': (timezone.now() + timedelta(days=5)).isoformat(),
        }

        # Act
        respuesta = self.client.post('/api/turnos/', datos, format='json')

        # Assert
        self.assertEqual(respuesta.status_code, 400)
        self.assertIn('mascota', respuesta.data)
        self.assertEqual(Turno.objects.filter(mascota=self.rocco).count(), 1)

    def test_clienteListaTurnos_soloVeLosDeSusMascotas(self):
        # Arrange
        Turno.objects.create(mascota=self.toby, servicio=self.servicio,
                             fecha=timezone.now() + timedelta(days=4))

        # Act
        respuesta = self.client.get('/api/turnos/')

        # Assert
        self.assertEqual(respuesta.status_code, 200)
        mascotas = {t['mascota'] for t in respuesta.data['results']}
        self.assertEqual(mascotas, {self.toby.id})


class VeterinariosTest(APITestCase):
    """Minimización de datos: para elegir un profesional alcanza con el id y el nombre."""

    def test_clienteListaVeterinarios_recibeSoloIdYNombre(self):
        # Arrange
        Usuario.objects.create_user(
            'vet@petshop.test', PASSWORD, nombre='Dra. Gómez', rol=Usuario.Rol.VETERINARIO,
            telefono='3511234567', direccion='Av. Colón 1234')
        self.client.force_authenticate(
            Usuario.objects.create_user('cliente@petshop.test', PASSWORD, nombre='Juan Pérez'))

        # Act
        respuesta = self.client.get('/api/usuarios/veterinarios/')

        # Assert
        self.assertEqual(respuesta.status_code, 200)
        veterinario = respuesta.data['results'][0]
        self.assertEqual(set(veterinario), {'id', 'nombre'})
        self.assertEqual(veterinario['nombre'], 'Dra. Gómez')
