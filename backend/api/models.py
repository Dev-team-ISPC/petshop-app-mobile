from decimal import Decimal

from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.core.validators import MinValueValidator
from django.db import models
from django.utils import timezone


class UsuarioManager(BaseUserManager):
    """Manager del modelo de usuario propio (el login es por email, no por username)."""

    use_in_migrations = True

    def _create_user(self, email, password, **extra):
        if not email:
            raise ValueError('El email es obligatorio.')
        email = self.normalize_email(email)
        user = self.model(email=email, **extra)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_user(self, email, password=None, **extra):
        extra.setdefault('rol', Usuario.Rol.CLIENTE)
        extra.setdefault('is_staff', False)
        extra.setdefault('is_superuser', False)
        return self._create_user(email, password, **extra)

    def create_superuser(self, email, password=None, **extra):
        extra.setdefault('rol', Usuario.Rol.ADMIN)
        extra.setdefault('is_staff', True)
        extra.setdefault('is_superuser', True)
        if extra.get('is_staff') is not True:
            raise ValueError('Un superusuario debe tener is_staff=True.')
        if extra.get('is_superuser') is not True:
            raise ValueError('Un superusuario debe tener is_superuser=True.')
        return self._create_user(email, password, **extra)


class Usuario(AbstractBaseUser, PermissionsMixin):
    class Rol(models.TextChoices):
        CLIENTE = 'cliente', 'Cliente'
        VETERINARIO = 'veterinario', 'Veterinario'
        ADMIN = 'admin', 'Administrador'

    email = models.EmailField('email', unique=True)
    nombre = models.CharField('nombre', max_length=100)
    telefono = models.CharField('teléfono', max_length=20, blank=True)
    direccion = models.CharField('dirección', max_length=150, blank=True)
    rol = models.CharField('rol', max_length=15, choices=Rol.choices, default=Rol.CLIENTE)

    is_active = models.BooleanField('activo', default=True)
    is_staff = models.BooleanField('acceso al admin', default=False)
    date_joined = models.DateTimeField('fecha de alta', default=timezone.now)

    objects = UsuarioManager()

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['nombre']

    class Meta:
        verbose_name = 'usuario'
        verbose_name_plural = 'usuarios'
        ordering = ['nombre']

    def __str__(self):
        return f'{self.nombre} <{self.email}>'

    @property
    def es_admin(self):
        return self.rol == self.Rol.ADMIN

    @property
    def es_veterinario(self):
        return self.rol == self.Rol.VETERINARIO

    @property
    def es_cliente(self):
        return self.rol == self.Rol.CLIENTE


class Mascota(models.Model):
    class Especie(models.TextChoices):
        PERRO = 'perro', 'Perro'
        GATO = 'gato', 'Gato'
        AVE = 'ave', 'Ave'
        CONEJO = 'conejo', 'Conejo'
        REPTIL = 'reptil', 'Reptil'
        OTRO = 'otro', 'Otro'

    nombre = models.CharField(max_length=100)
    especie = models.CharField(max_length=20, choices=Especie.choices)
    raza = models.CharField(max_length=50)
    peso = models.DecimalField(
        'peso en kg', max_digits=5, decimal_places=2,
        validators=[MinValueValidator(Decimal('0.01'))],
    )
    fecha_nacimiento = models.DateField()
    dueno = models.ForeignKey(
        Usuario, on_delete=models.CASCADE, related_name='mascotas',
        verbose_name='dueño',
    )
    creado_en = models.DateTimeField(auto_now_add=True)
    actualizado_en = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = 'mascota'
        verbose_name_plural = 'mascotas'
        ordering = ['nombre']

    def __str__(self):
        return f'{self.nombre} ({self.get_especie_display()})'


class Vacuna(models.Model):
    """Catálogo de vacunas disponibles."""

    nombre = models.CharField(max_length=100, unique=True)
    descripcion = models.TextField(blank=True)
    frecuencia = models.CharField(max_length=50, help_text='Ej.: anual, cada 6 meses')

    class Meta:
        verbose_name = 'vacuna'
        verbose_name_plural = 'vacunas'
        ordering = ['nombre']

    def __str__(self):
        return self.nombre


class Vacunacion(models.Model):
    """Aplicación concreta de una vacuna a una mascota."""

    mascota = models.ForeignKey(Mascota, on_delete=models.CASCADE, related_name='vacunaciones')
    vacuna = models.ForeignKey(Vacuna, on_delete=models.PROTECT, related_name='aplicaciones')
    fecha_aplicacion = models.DateField()
    proxima_dosis = models.DateField(null=True, blank=True)
    veterinario = models.ForeignKey(
        Usuario, on_delete=models.SET_NULL, null=True, blank=True,
        related_name='vacunaciones_aplicadas',
    )
    creado_en = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'vacunación'
        verbose_name_plural = 'vacunaciones'
        ordering = ['-fecha_aplicacion']

    def __str__(self):
        return f'{self.vacuna} — {self.mascota} ({self.fecha_aplicacion})'


class Servicio(models.Model):
    """Servicios que presta la veterinaria. Un turno se pide para un servicio."""

    nombre = models.CharField(max_length=100, unique=True)
    descripcion = models.TextField(blank=True)
    duracion_minutos = models.PositiveIntegerField(default=30)
    activo = models.BooleanField(default=True)

    class Meta:
        verbose_name = 'servicio'
        verbose_name_plural = 'servicios'
        ordering = ['nombre']

    def __str__(self):
        return self.nombre


class Turno(models.Model):
    class Estado(models.TextChoices):
        PENDIENTE = 'pendiente', 'Pendiente'
        CONFIRMADO = 'confirmado', 'Confirmado'
        CANCELADO = 'cancelado', 'Cancelado'
        COMPLETADO = 'completado', 'Completado'

    mascota = models.ForeignKey(Mascota, on_delete=models.CASCADE, related_name='turnos')
    servicio = models.ForeignKey(Servicio, on_delete=models.PROTECT, related_name='turnos')
    veterinario = models.ForeignKey(
        Usuario, on_delete=models.SET_NULL, null=True, blank=True,
        related_name='turnos_asignados',
    )
    fecha = models.DateTimeField()
    estado = models.CharField(max_length=15, choices=Estado.choices, default=Estado.PENDIENTE)
    observaciones = models.TextField(blank=True)
    creado_en = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'turno'
        verbose_name_plural = 'turnos'
        ordering = ['fecha']

    def __str__(self):
        return f'{self.servicio} — {self.mascota} ({self.fecha:%d/%m/%Y %H:%M})'


class Consulta(models.Model):
    """Mensaje enviado desde la pantalla de contacto."""

    nombre = models.CharField(max_length=100)
    email = models.EmailField()
    mensaje = models.TextField(max_length=500)
    leida = models.BooleanField(default=False)
    creado_en = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'consulta'
        verbose_name_plural = 'consultas'
        ordering = ['-creado_en']

    def __str__(self):
        return f'{self.nombre} — {self.creado_en:%d/%m/%Y}'


# ---------------------------------------------------------------------------
# Entidades heredadas del módulo Programador Web.
# El frontend Angular las sigue usando. La aplicación móvil no las consume.
# ---------------------------------------------------------------------------

class Categoria(models.Model):
    nombre = models.CharField(max_length=100)

    class Meta:
        verbose_name = 'categoría'
        verbose_name_plural = 'categorías'
        ordering = ['nombre']

    def __str__(self):
        return self.nombre


class Producto(models.Model):
    nombre = models.CharField(max_length=100)
    descripcion = models.TextField(blank=True)
    precio = models.DecimalField(max_digits=10, decimal_places=2)
    stock = models.PositiveIntegerField(default=0)
    categoria = models.ForeignKey(Categoria, on_delete=models.PROTECT, related_name='productos')
    imagen = models.URLField(blank=True)

    class Meta:
        verbose_name = 'producto'
        verbose_name_plural = 'productos'
        ordering = ['nombre']

    def __str__(self):
        return self.nombre
