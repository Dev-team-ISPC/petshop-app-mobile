from datetime import timedelta

from django.core.management.base import BaseCommand
from django.db import transaction
from django.utils import timezone

from api.models import (
    Categoria, Consulta, Mascota, Producto, Servicio, Turno, Usuario, Vacuna, Vacunacion,
)

PASSWORD = 'Petshop2026!'


class Command(BaseCommand):
    help = 'Carga datos de prueba para desarrollo y testing.'

    def add_arguments(self, parser):
        parser.add_argument(
            '--reset', action='store_true',
            help='Borra los datos existentes antes de cargar.',
        )

    @transaction.atomic
    def handle(self, *args, **options):
        if options['reset']:
            self.stdout.write('Borrando datos existentes...')
            Turno.objects.all().delete()
            Vacunacion.objects.all().delete()
            Mascota.objects.all().delete()
            Consulta.objects.all().delete()
            Servicio.objects.all().delete()
            Vacuna.objects.all().delete()
            Producto.objects.all().delete()
            Categoria.objects.all().delete()
            Usuario.objects.filter(is_superuser=False).delete()

        hoy = timezone.localdate()
        ahora = timezone.now()

        # --- Usuarios ---------------------------------------------------
        admin, _ = Usuario.objects.get_or_create(
            email='admin@petshop.test',
            defaults={
                'nombre': 'Administrador',
                'rol': Usuario.Rol.ADMIN,
                'is_staff': True,
                'is_superuser': True,
                'telefono': '3510000000',
            },
        )
        admin.set_password(PASSWORD)
        admin.save()

        vet, _ = Usuario.objects.get_or_create(
            email='vet@petshop.test',
            defaults={
                'nombre': 'Dra. Gómez',
                'rol': Usuario.Rol.VETERINARIO,
                'telefono': '3511111111',
            },
        )
        vet.set_password(PASSWORD)
        vet.save()

        cliente, _ = Usuario.objects.get_or_create(
            email='cliente@petshop.test',
            defaults={
                'nombre': 'Juan Pérez',
                'rol': Usuario.Rol.CLIENTE,
                'telefono': '3512222222',
                'direccion': 'Av. Siempreviva 742',
            },
        )
        cliente.set_password(PASSWORD)
        cliente.save()

        otro_cliente, _ = Usuario.objects.get_or_create(
            email='cliente2@petshop.test',
            defaults={
                'nombre': 'Ana López',
                'rol': Usuario.Rol.CLIENTE,
                'telefono': '3513333333',
            },
        )
        otro_cliente.set_password(PASSWORD)
        otro_cliente.save()

        # --- Catálogo de vacunas ---------------------------------------
        vacunas = {}
        for nombre, descripcion, frecuencia in [
            ('Antirrábica', 'Prevención de la rabia.', 'anual'),
            ('Quíntuple', 'Moquillo, hepatitis, parvovirus, parainfluenza y leptospirosis.', 'anual'),
            ('Triple felina', 'Rinotraqueitis, calicivirus y panleucopenia.', 'anual'),
            ('Antiparasitaria', 'Desparasitación interna.', 'cada 6 meses'),
        ]:
            vacunas[nombre], _ = Vacuna.objects.get_or_create(
                nombre=nombre,
                defaults={'descripcion': descripcion, 'frecuencia': frecuencia},
            )

        # --- Servicios --------------------------------------------------
        servicios = {}
        for nombre, descripcion, duracion in [
            ('Consulta general', 'Revisión clínica general.', 30),
            ('Vacunación', 'Aplicación de vacunas del plan sanitario.', 20),
            ('Baño y peluquería', 'Higiene y corte.', 60),
            ('Control post operatorio', 'Seguimiento tras una cirugía.', 30),
        ]:
            servicios[nombre], _ = Servicio.objects.get_or_create(
                nombre=nombre,
                defaults={'descripcion': descripcion, 'duracion_minutos': duracion},
            )

        # --- Mascotas ---------------------------------------------------
        rocco, _ = Mascota.objects.get_or_create(
            nombre='Rocco', dueno=cliente,
            defaults={
                'especie': Mascota.Especie.PERRO,
                'raza': 'Labrador',
                'peso': '28.50',
                'fecha_nacimiento': hoy - timedelta(days=1800),
            },
        )
        mia, _ = Mascota.objects.get_or_create(
            nombre='Mía', dueno=cliente,
            defaults={
                'especie': Mascota.Especie.GATO,
                'raza': 'Siamés',
                'peso': '4.20',
                'fecha_nacimiento': hoy - timedelta(days=900),
            },
        )
        # Mascota de otro dueño: sirve para probar que no se filtra en los listados.
        toby, _ = Mascota.objects.get_or_create(
            nombre='Toby', dueno=otro_cliente,
            defaults={
                'especie': Mascota.Especie.PERRO,
                'raza': 'Caniche',
                'peso': '7.80',
                'fecha_nacimiento': hoy - timedelta(days=1200),
            },
        )

        # --- Vacunaciones (con próxima dosis futura para poblar la agenda) ---
        Vacunacion.objects.get_or_create(
            mascota=rocco, vacuna=vacunas['Antirrábica'],
            defaults={
                'fecha_aplicacion': hoy - timedelta(days=180),
                'proxima_dosis': hoy + timedelta(days=185),
                'veterinario': vet,
            },
        )
        Vacunacion.objects.get_or_create(
            mascota=rocco, vacuna=vacunas['Quíntuple'],
            defaults={
                'fecha_aplicacion': hoy - timedelta(days=340),
                'proxima_dosis': hoy + timedelta(days=25),
                'veterinario': vet,
            },
        )
        Vacunacion.objects.get_or_create(
            mascota=mia, vacuna=vacunas['Triple felina'],
            defaults={
                'fecha_aplicacion': hoy - timedelta(days=100),
                'proxima_dosis': hoy + timedelta(days=265),
                'veterinario': vet,
            },
        )
        Vacunacion.objects.get_or_create(
            mascota=toby, vacuna=vacunas['Antirrábica'],
            defaults={
                'fecha_aplicacion': hoy - timedelta(days=60),
                'proxima_dosis': hoy + timedelta(days=305),
                'veterinario': vet,
            },
        )

        # --- Turnos en los cuatro estados -------------------------------
        Turno.objects.get_or_create(
            mascota=rocco, servicio=servicios['Consulta general'],
            fecha=ahora + timedelta(days=5, hours=3),
            defaults={'estado': Turno.Estado.PENDIENTE},
        )
        Turno.objects.get_or_create(
            mascota=rocco, servicio=servicios['Baño y peluquería'],
            fecha=ahora + timedelta(days=12),
            defaults={'estado': Turno.Estado.CONFIRMADO, 'veterinario': vet},
        )
        Turno.objects.get_or_create(
            mascota=mia, servicio=servicios['Vacunación'],
            fecha=ahora + timedelta(days=20),
            defaults={'estado': Turno.Estado.PENDIENTE},
        )
        Turno.objects.get_or_create(
            mascota=mia, servicio=servicios['Consulta general'],
            fecha=ahora - timedelta(days=15),
            defaults={
                'estado': Turno.Estado.COMPLETADO,
                'veterinario': vet,
                'observaciones': 'Control de rutina sin novedades.',
            },
        )
        Turno.objects.get_or_create(
            mascota=toby, servicio=servicios['Consulta general'],
            fecha=ahora + timedelta(days=8),
            defaults={'estado': Turno.Estado.CANCELADO},
        )

        # --- Contacto ---------------------------------------------------
        Consulta.objects.get_or_create(
            email='interesado@mail.test',
            defaults={
                'nombre': 'Laura Suárez',
                'mensaje': 'Quisiera saber si atienden animales exóticos los fines de semana.',
            },
        )

        # --- Heredados del módulo web -----------------------------------
        alimentos, _ = Categoria.objects.get_or_create(nombre='Alimentos')
        accesorios, _ = Categoria.objects.get_or_create(nombre='Accesorios')
        Producto.objects.get_or_create(
            nombre='Alimento balanceado adulto 15 kg',
            defaults={
                'descripcion': 'Alimento seco para perros adultos.',
                'precio': '24500.00', 'stock': 30, 'categoria': alimentos,
            },
        )
        Producto.objects.get_or_create(
            nombre='Collar reflectivo',
            defaults={
                'descripcion': 'Collar regulable con banda reflectiva.',
                'precio': '6800.00', 'stock': 45, 'categoria': accesorios,
            },
        )

        self.stdout.write(self.style.SUCCESS('\nDatos de prueba cargados.\n'))
        self.stdout.write(f'  admin@petshop.test    / {PASSWORD}   (administrador)')
        self.stdout.write(f'  vet@petshop.test      / {PASSWORD}   (veterinario)')
        self.stdout.write(f'  cliente@petshop.test  / {PASSWORD}   (cliente, 2 mascotas)')
        self.stdout.write(f'  cliente2@petshop.test / {PASSWORD}   (cliente, 1 mascota)\n')
