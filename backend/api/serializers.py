from django.contrib.auth.password_validation import validate_password
from django.core.exceptions import ValidationError as DjangoValidationError
from django.utils import timezone
from rest_framework import serializers
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

from .models import (
    Categoria, Consulta, Mascota, Producto, Servicio, Turno, Usuario, Vacuna, Vacunacion,
)


# ---------------------------------------------------------------------------
# Usuarios y autenticación
# ---------------------------------------------------------------------------

class UsuarioSerializer(serializers.ModelSerializer):
    rol_display = serializers.CharField(source='get_rol_display', read_only=True)

    class Meta:
        model = Usuario
        fields = [
            'id', 'email', 'nombre', 'telefono', 'direccion',
            'rol', 'rol_display', 'is_active', 'date_joined',
        ]
        read_only_fields = ['id', 'date_joined']


class UsuarioEscrituraSerializer(serializers.ModelSerializer):
    """Alta y edición de usuarios desde el panel de administración."""

    password = serializers.CharField(write_only=True, required=False, style={'input_type': 'password'})

    class Meta:
        model = Usuario
        fields = [
            'id', 'email', 'nombre', 'telefono', 'direccion',
            'rol', 'is_active', 'password',
        ]

    def validate_password(self, value):
        _validar_password(value)
        return value

    def create(self, validated_data):
        password = validated_data.pop('password', None)
        if not password:
            raise serializers.ValidationError({'password': ['Este campo es obligatorio.']})
        return Usuario.objects.create_user(password=password, **validated_data)

    def update(self, instance, validated_data):
        password = validated_data.pop('password', None)
        usuario = super().update(instance, validated_data)
        if password:
            usuario.set_password(password)
            usuario.save(update_fields=['password'])
        return usuario


class RegistroSerializer(serializers.ModelSerializer):
    """Alta pública de cuentas. Siempre crea el rol cliente."""

    password = serializers.CharField(write_only=True, style={'input_type': 'password'})
    acepta_terminos = serializers.BooleanField(write_only=True)

    class Meta:
        model = Usuario
        fields = ['id', 'email', 'nombre', 'telefono', 'direccion', 'password', 'acepta_terminos']

    def validate_email(self, value):
        if Usuario.objects.filter(email__iexact=value).exists():
            raise serializers.ValidationError('Ya existe un usuario con ese email.')
        return value.lower()

    def validate_password(self, value):
        _validar_password(value)
        return value

    def validate_acepta_terminos(self, value):
        if not value:
            raise serializers.ValidationError('Debe aceptar los términos y condiciones.')
        return value

    def create(self, validated_data):
        validated_data.pop('acepta_terminos')
        password = validated_data.pop('password')
        return Usuario.objects.create_user(
            password=password, rol=Usuario.Rol.CLIENTE, **validated_data
        )


class PerfilSerializer(serializers.ModelSerializer):
    """Datos propios del usuario autenticado. El rol no se puede cambiar solo."""

    rol_display = serializers.CharField(source='get_rol_display', read_only=True)

    class Meta:
        model = Usuario
        fields = ['id', 'email', 'nombre', 'telefono', 'direccion', 'rol', 'rol_display']
        read_only_fields = ['id', 'email', 'rol']


class LoginSerializer(TokenObtainPairSerializer):
    """Devuelve access, refresh y los datos del usuario en una sola llamada."""

    def validate(self, attrs):
        data = super().validate(attrs)
        data['usuario'] = UsuarioSerializer(self.user).data
        return data


def _validar_password(value):
    try:
        validate_password(value)
    except DjangoValidationError as exc:
        raise serializers.ValidationError(list(exc.messages))


# ---------------------------------------------------------------------------
# Mascotas
# ---------------------------------------------------------------------------

class MascotaSerializer(serializers.ModelSerializer):
    especie_display = serializers.CharField(source='get_especie_display', read_only=True)
    dueno_nombre = serializers.CharField(source='dueno.nombre', read_only=True)
    edad_anios = serializers.SerializerMethodField()

    class Meta:
        model = Mascota
        fields = [
            'id', 'nombre', 'especie', 'especie_display', 'raza', 'peso',
            'fecha_nacimiento', 'edad_anios', 'dueno', 'dueno_nombre',
            'creado_en', 'actualizado_en',
        ]
        read_only_fields = ['id', 'creado_en', 'actualizado_en']
        extra_kwargs = {'dueno': {'required': False}}

    def get_edad_anios(self, obj):
        hoy = timezone.localdate()
        nac = obj.fecha_nacimiento
        return hoy.year - nac.year - ((hoy.month, hoy.day) < (nac.month, nac.day))

    def validate_fecha_nacimiento(self, value):
        if value > timezone.localdate():
            raise serializers.ValidationError('La fecha de nacimiento no puede ser futura.')
        return value

    def validate(self, attrs):
        """
        El dueño lo decide el servidor, no el cliente.
        Un cliente sólo puede crear mascotas propias; admin y veterinario
        pueden indicar el dueño explícitamente.
        """
        request = self.context.get('request')
        if request is None:
            return attrs

        usuario = request.user
        if usuario.es_cliente:
            attrs['dueno'] = usuario
        elif not attrs.get('dueno') and self.instance is None:
            raise serializers.ValidationError(
                {'dueno': ['Indique el dueño de la mascota.']}
            )
        return attrs


# ---------------------------------------------------------------------------
# Vacunas y vacunaciones
# ---------------------------------------------------------------------------

class VacunaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Vacuna
        fields = ['id', 'nombre', 'descripcion', 'frecuencia']


class VacunacionSerializer(serializers.ModelSerializer):
    mascota_nombre = serializers.CharField(source='mascota.nombre', read_only=True)
    vacuna_nombre = serializers.CharField(source='vacuna.nombre', read_only=True)
    veterinario_nombre = serializers.CharField(source='veterinario.nombre', read_only=True, default=None)
    dias_para_proxima_dosis = serializers.SerializerMethodField()

    class Meta:
        model = Vacunacion
        fields = [
            'id', 'mascota', 'mascota_nombre', 'vacuna', 'vacuna_nombre',
            'fecha_aplicacion', 'proxima_dosis', 'dias_para_proxima_dosis',
            'veterinario', 'veterinario_nombre', 'creado_en',
        ]
        read_only_fields = ['id', 'creado_en']

    def get_dias_para_proxima_dosis(self, obj):
        """Negativo si la dosis está vencida. None si no hay próxima dosis."""
        if not obj.proxima_dosis:
            return None
        return (obj.proxima_dosis - timezone.localdate()).days

    def validate_fecha_aplicacion(self, value):
        if value > timezone.localdate():
            raise serializers.ValidationError('La fecha de aplicación no puede ser futura.')
        return value

    def validate(self, attrs):
        aplicacion = attrs.get('fecha_aplicacion') or getattr(self.instance, 'fecha_aplicacion', None)
        proxima = attrs.get('proxima_dosis') or getattr(self.instance, 'proxima_dosis', None)
        if aplicacion and proxima and proxima <= aplicacion:
            raise serializers.ValidationError(
                {'proxima_dosis': ['La próxima dosis debe ser posterior a la fecha de aplicación.']}
            )

        request = self.context.get('request')
        if request and self.instance is None and not attrs.get('veterinario'):
            if request.user.es_veterinario:
                attrs['veterinario'] = request.user
        return attrs


# ---------------------------------------------------------------------------
# Servicios y turnos
# ---------------------------------------------------------------------------

class ServicioSerializer(serializers.ModelSerializer):
    class Meta:
        model = Servicio
        fields = ['id', 'nombre', 'descripcion', 'duracion_minutos', 'activo']


class TurnoSerializer(serializers.ModelSerializer):
    mascota_nombre = serializers.CharField(source='mascota.nombre', read_only=True)
    servicio_nombre = serializers.CharField(source='servicio.nombre', read_only=True)
    veterinario_nombre = serializers.CharField(source='veterinario.nombre', read_only=True, default=None)
    estado_display = serializers.CharField(source='get_estado_display', read_only=True)
    dueno_nombre = serializers.CharField(source='mascota.dueno.nombre', read_only=True)
    dueno = serializers.IntegerField(source='mascota.dueno_id', read_only=True)
    dias_restantes = serializers.SerializerMethodField()

    class Meta:
        model = Turno
        fields = [
            'id', 'mascota', 'mascota_nombre', 'dueno', 'dueno_nombre',
            'servicio', 'servicio_nombre',
            'veterinario', 'veterinario_nombre', 'fecha', 'dias_restantes',
            'estado', 'estado_display', 'observaciones', 'creado_en',
        ]
        read_only_fields = ['id', 'creado_en']

    def get_dias_restantes(self, obj):
        """Negativo si el turno ya pasó."""
        return (obj.fecha.date() - timezone.localdate()).days

    def validate_fecha(self, value):
        if self.instance is None and value < timezone.now():
            raise serializers.ValidationError('No se puede pedir un turno en una fecha pasada.')
        return value

    def validate_mascota(self, value):
        """Un cliente no puede pedir turno para una mascota que no es suya."""
        request = self.context.get('request')
        if request and request.user.es_cliente and value.dueno_id != request.user.id:
            raise serializers.ValidationError('La mascota no pertenece al usuario autenticado.')
        return value

    def validate_estado(self, value):
        """
        El cliente sólo puede cancelar. Confirmar y completar son del
        veterinario: el dueño solicita, la clínica decide.
        """
        request = self.context.get('request')
        if not request or not request.user.es_cliente:
            return value

        if self.instance is None:
            if value != Turno.Estado.PENDIENTE:
                raise serializers.ValidationError('Un turno nuevo se crea siempre como pendiente.')
            return value

        if value == self.instance.estado:
            return value
        if value != Turno.Estado.CANCELADO:
            raise serializers.ValidationError('Como cliente sólo puede cancelar el turno.')
        if self.instance.estado in (Turno.Estado.CANCELADO, Turno.Estado.COMPLETADO):
            raise serializers.ValidationError('El turno ya está cerrado y no se puede cancelar.')
        return value

    def validate(self, attrs):
        """Al editar, el cliente sólo toca el estado y las observaciones."""
        request = self.context.get('request')
        if request and self.instance is not None and request.user.es_cliente:
            prohibidos = [c for c in ('mascota', 'servicio', 'veterinario', 'fecha') if c in attrs]
            if prohibidos:
                raise serializers.ValidationError({
                    campo: ['No tiene permiso para modificar este campo.'] for campo in prohibidos
                })
        return attrs


# ---------------------------------------------------------------------------
# Agenda
# ---------------------------------------------------------------------------

class ProximaDosisSerializer(serializers.Serializer):
    vacunacion_id = serializers.IntegerField()
    mascota_id = serializers.IntegerField()
    mascota_nombre = serializers.CharField()
    vacuna_nombre = serializers.CharField()
    fecha = serializers.DateField()
    dias_restantes = serializers.IntegerField()


class TurnoAgendaSerializer(serializers.Serializer):
    turno_id = serializers.IntegerField()
    mascota_id = serializers.IntegerField()
    mascota_nombre = serializers.CharField()
    servicio_nombre = serializers.CharField()
    fecha = serializers.DateTimeField()
    estado = serializers.CharField()
    dias_restantes = serializers.IntegerField()


# ---------------------------------------------------------------------------
# Contacto
# ---------------------------------------------------------------------------

class ConsultaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Consulta
        fields = ['id', 'nombre', 'email', 'mensaje', 'leida', 'creado_en']
        read_only_fields = ['id', 'leida', 'creado_en']

    def validate_mensaje(self, value):
        if len(value.strip()) < 10:
            raise serializers.ValidationError('El mensaje debe tener al menos 10 caracteres.')
        return value.strip()


class ConsultaAdminSerializer(serializers.ModelSerializer):
    class Meta:
        model = Consulta
        fields = ['id', 'nombre', 'email', 'mensaje', 'leida', 'creado_en']
        read_only_fields = ['id', 'nombre', 'email', 'mensaje', 'creado_en']


# ---------------------------------------------------------------------------
# Heredados del módulo web
# ---------------------------------------------------------------------------

class CategoriaSerializer(serializers.ModelSerializer):
    class Meta:
        model = Categoria
        fields = ['id', 'nombre']


class ProductoSerializer(serializers.ModelSerializer):
    categoria_nombre = serializers.CharField(source='categoria.nombre', read_only=True)

    class Meta:
        model = Producto
        fields = [
            'id', 'nombre', 'descripcion', 'precio', 'stock',
            'categoria', 'categoria_nombre', 'imagen',
        ]
