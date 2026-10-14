"""
Tests de los registros de seguridad (TK-S2-15) y del 409 al borrar en uso.

    $env:DJANGO_SETTINGS_MODULE = "config.settings_pruebas"
    python manage.py test api

Cada test sigue el patrón AAA y verifica dos cosas: la respuesta HTTP y la
línea que queda en el log "petshop.seguridad". También comprueba que en el
log no aparezcan la contraseña, los tokens ni el email en claro.
"""
import json
from datetime import timedelta

from django.core.cache import cache
from django.utils import timezone
from rest_framework.test import APITestCase

from .models import Mascota, Servicio, Turno, Usuario

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
