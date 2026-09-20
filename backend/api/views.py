from django.db.models import Count, Q
from django.utils import timezone
from rest_framework import status, viewsets
from rest_framework.decorators import action
from rest_framework.generics import CreateAPIView, RetrieveUpdateDestroyAPIView
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.throttling import ScopedRateThrottle
from rest_framework.views import APIView
from rest_framework_simplejwt.exceptions import TokenError
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView

from .models import (
    Categoria, Consulta, Mascota, Producto, Servicio, Turno, Usuario, Vacuna, Vacunacion,
)
from .permissions import (
    EsAdmin, EsDuenoOAdmin, EsDuenoOStaff, EsVeterinario, EsVeterinarioOAdmin,
    LecturaAutenticadaEscrituraAdmin, LecturaAutenticadaEscrituraClinica,
)
from .serializers import (
    CategoriaSerializer, ConsultaAdminSerializer, ConsultaSerializer, LoginSerializer,
    MascotaSerializer, PerfilSerializer, ProductoSerializer, ProximaDosisSerializer,
    RegistroSerializer, ServicioSerializer, TurnoAgendaSerializer, TurnoSerializer,
    UsuarioEscrituraSerializer, UsuarioSerializer, VacunaSerializer, VacunacionSerializer,
)


# ---------------------------------------------------------------------------
# Autenticación
# ---------------------------------------------------------------------------

class RegistroView(CreateAPIView):
    serializer_class = RegistroSerializer
    permission_classes = [AllowAny]
    throttle_classes = [ScopedRateThrottle]
    throttle_scope = 'registro'


class LoginView(TokenObtainPairView):
    serializer_class = LoginSerializer
    permission_classes = [AllowAny]
    throttle_classes = [ScopedRateThrottle]
    throttle_scope = 'login'


class LogoutView(APIView):
    """Invalida el refresh token en el servidor (blacklist de SimpleJWT)."""

    permission_classes = [IsAuthenticated]

    def post(self, request):
        refresh = request.data.get('refresh')
        if not refresh:
            return Response(
                {'refresh': ['Este campo es obligatorio.']},
                status=status.HTTP_400_BAD_REQUEST,
            )
        try:
            RefreshToken(refresh).blacklist()
        except TokenError:
            return Response(
                {'detail': 'El token ya es inválido o expiró.'},
                status=status.HTTP_400_BAD_REQUEST,
            )
        return Response(status=status.HTTP_204_NO_CONTENT)


class PerfilView(RetrieveUpdateDestroyAPIView):
    """
    GET    -> datos del usuario autenticado
    PATCH  -> edición del perfil propio
    DELETE -> botón de arrepentimiento: baja de la cuenta propia
    """

    serializer_class = PerfilSerializer
    permission_classes = [IsAuthenticated]

    def get_object(self):
        return self.request.user

    def perform_destroy(self, instance):
        instance.delete()


# ---------------------------------------------------------------------------
# Usuarios  (CRUD 1)
# ---------------------------------------------------------------------------

class UsuarioViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated, EsAdmin]
    search_fields = ['nombre', 'email']
    ordering_fields = ['nombre', 'email', 'date_joined']

    def get_queryset(self):
        queryset = Usuario.objects.all()
        rol = self.request.query_params.get('rol')
        if rol:
            queryset = queryset.filter(rol=rol)
        activo = self.request.query_params.get('activo')
        if activo is not None:
            queryset = queryset.filter(is_active=activo.lower() in ('1', 'true', 'si', 'sí'))
        return queryset

    def get_serializer_class(self):
        if self.action in ('create', 'update', 'partial_update'):
            return UsuarioEscrituraSerializer
        return UsuarioSerializer

    @action(detail=False, methods=['get'], permission_classes=[IsAuthenticated])
    def veterinarios(self, request):
        """Lista de veterinarios para los selectores de la app."""
        queryset = Usuario.objects.filter(rol=Usuario.Rol.VETERINARIO, is_active=True)
        pagina = self.paginate_queryset(queryset)
        serializer = UsuarioSerializer(pagina, many=True)
        return self.get_paginated_response(serializer.data)


# ---------------------------------------------------------------------------
# Mascotas
# ---------------------------------------------------------------------------

class MascotaViewSet(viewsets.ModelViewSet):
    serializer_class = MascotaSerializer
    permission_classes = [IsAuthenticated, EsDuenoOStaff]

    def get_permissions(self):
        # El veterinario abre la libreta y corrige datos clínicos (el peso se
        # mide en la consulta), pero eliminar la mascota es del dueño o del admin.
        if self.action == 'destroy':
            return [IsAuthenticated(), EsDuenoOAdmin()]
        return [IsAuthenticated(), EsDuenoOStaff()]

    search_fields = ['nombre', 'raza', 'dueno__nombre']
    ordering_fields = ['nombre', 'creado_en']

    def get_queryset(self):
        """El filtrado por propietario se resuelve acá, nunca en el cliente."""
        usuario = self.request.user
        queryset = Mascota.objects.select_related('dueno')
        if usuario.es_cliente:
            queryset = queryset.filter(dueno=usuario)

        dueno = self.request.query_params.get('dueno')
        if dueno and not usuario.es_cliente:
            queryset = queryset.filter(dueno_id=dueno)
        especie = self.request.query_params.get('especie')
        if especie:
            queryset = queryset.filter(especie=especie)
        return queryset

    @action(detail=True, methods=['get'])
    def vacunaciones(self, request, pk=None):
        mascota = self.get_object()
        queryset = mascota.vacunaciones.select_related('vacuna', 'mascota', 'veterinario')
        pagina = self.paginate_queryset(queryset)
        serializer = VacunacionSerializer(pagina, many=True)
        return self.get_paginated_response(serializer.data)

    @action(detail=True, methods=['get'])
    def turnos(self, request, pk=None):
        mascota = self.get_object()
        queryset = mascota.turnos.select_related('servicio', 'mascota', 'veterinario')
        pagina = self.paginate_queryset(queryset)
        serializer = TurnoSerializer(pagina, many=True)
        return self.get_paginated_response(serializer.data)


# ---------------------------------------------------------------------------
# Vacunas y vacunaciones
# ---------------------------------------------------------------------------

class VacunaViewSet(viewsets.ModelViewSet):
    queryset = Vacuna.objects.all()
    serializer_class = VacunaSerializer
    permission_classes = [IsAuthenticated, LecturaAutenticadaEscrituraClinica]
    search_fields = ['nombre']
    ordering_fields = ['nombre']


class VacunacionViewSet(viewsets.ModelViewSet):
    serializer_class = VacunacionSerializer

    def get_permissions(self):
        if self.action in ('list', 'retrieve'):
            return [IsAuthenticated(), EsDuenoOStaff()]
        return [IsAuthenticated(), EsVeterinario()]

    search_fields = ['mascota__nombre', 'vacuna__nombre']
    ordering_fields = ['fecha_aplicacion', 'proxima_dosis']

    def get_queryset(self):
        usuario = self.request.user
        queryset = Vacunacion.objects.select_related(
            'mascota', 'mascota__dueno', 'vacuna', 'veterinario'
        )
        if usuario.es_cliente:
            queryset = queryset.filter(mascota__dueno=usuario)

        mascota = self.request.query_params.get('mascota')
        if mascota:
            queryset = queryset.filter(mascota_id=mascota)
        if self.request.query_params.get('proximas') in ('1', 'true'):
            queryset = queryset.filter(proxima_dosis__gte=timezone.localdate())
        return queryset


# ---------------------------------------------------------------------------
# Servicios y turnos  (CRUD 2)
# ---------------------------------------------------------------------------

class ServicioViewSet(viewsets.ModelViewSet):
    serializer_class = ServicioSerializer
    permission_classes = [IsAuthenticated, LecturaAutenticadaEscrituraAdmin]
    search_fields = ['nombre']
    ordering_fields = ['nombre', 'duracion_minutos']

    def get_queryset(self):
        queryset = Servicio.objects.all()
        activo = self.request.query_params.get('activo')
        if activo is not None:
            queryset = queryset.filter(activo=activo.lower() in ('1', 'true', 'si', 'sí'))
        return queryset


class TurnoViewSet(viewsets.ModelViewSet):
    serializer_class = TurnoSerializer
    permission_classes = [IsAuthenticated, EsDuenoOStaff]

    search_fields = ['mascota__nombre', 'servicio__nombre', 'mascota__dueno__nombre']
    ordering_fields = ['fecha', 'estado']

    def get_queryset(self):
        usuario = self.request.user
        queryset = Turno.objects.select_related(
            'mascota', 'mascota__dueno', 'servicio', 'veterinario'
        )
        if usuario.es_cliente:
            queryset = queryset.filter(mascota__dueno=usuario)

        # ?estado=pendiente o ?estado=pendiente,confirmado (los chips de filtro)
        estado = self.request.query_params.get('estado')
        if estado:
            queryset = queryset.filter(estado__in=[e.strip() for e in estado.split(',') if e.strip()])
        mascota = self.request.query_params.get('mascota')
        if mascota:
            queryset = queryset.filter(mascota_id=mascota)
        desde = self.request.query_params.get('desde')
        if desde:
            queryset = queryset.filter(fecha__date__gte=desde)
        hasta = self.request.query_params.get('hasta')
        if hasta:
            queryset = queryset.filter(fecha__date__lte=hasta)
        if self.request.query_params.get('futuros') in ('1', 'true'):
            queryset = queryset.filter(fecha__gte=timezone.now())
        return queryset

    def perform_update(self, serializer):
        """El veterinario que confirma un turno queda asignado a él."""
        usuario = self.request.user
        turno = serializer.instance
        if usuario.es_veterinario and turno.veterinario_id is None:
            serializer.save(veterinario=usuario)
        else:
            serializer.save()

    def get_permissions(self):
        # El cliente puede cancelar su propio turno cambiando el estado; el
        # serializer limita a qué estados puede pasar. Borrar destruye el
        # histórico, así que queda reservado al administrador.
        if self.action == 'destroy':
            return [IsAuthenticated(), EsAdmin()]
        return [IsAuthenticated(), EsDuenoOStaff()]


# ---------------------------------------------------------------------------
# Agenda
# ---------------------------------------------------------------------------

class AgendaView(APIView):
    """
    Próximas dosis de vacunación y turnos futuros del usuario, según su rol.
    No se pagina: es una vista agregada pensada para una sola pantalla.
    """

    permission_classes = [IsAuthenticated]

    def get(self, request):
        usuario = request.user
        hoy = timezone.localdate()
        ahora = timezone.now()

        vacunaciones = Vacunacion.objects.select_related('mascota', 'vacuna').filter(
            proxima_dosis__gte=hoy
        )
        turnos = Turno.objects.select_related('mascota', 'servicio').filter(
            fecha__gte=ahora
        ).exclude(estado=Turno.Estado.CANCELADO)

        if usuario.es_cliente:
            vacunaciones = vacunaciones.filter(mascota__dueno=usuario)
            turnos = turnos.filter(mascota__dueno=usuario)
        elif usuario.es_veterinario:
            vacunaciones = vacunaciones.filter(
                Q(veterinario=usuario) | Q(veterinario__isnull=True)
            )
            turnos = turnos.filter(Q(veterinario=usuario) | Q(veterinario__isnull=True))

        proximas = [
            {
                'vacunacion_id': v.id,
                'mascota_id': v.mascota_id,
                'mascota_nombre': v.mascota.nombre,
                'vacuna_nombre': v.vacuna.nombre,
                'fecha': v.proxima_dosis,
                'dias_restantes': (v.proxima_dosis - hoy).days,
            }
            for v in vacunaciones.order_by('proxima_dosis')
        ]

        agenda_turnos = [
            {
                'turno_id': t.id,
                'mascota_id': t.mascota_id,
                'mascota_nombre': t.mascota.nombre,
                'servicio_nombre': t.servicio.nombre,
                'fecha': t.fecha,
                'estado': t.estado,
                'dias_restantes': (t.fecha.date() - hoy).days,
            }
            for t in turnos.order_by('fecha')
        ]

        return Response({
            'proximas_dosis': ProximaDosisSerializer(proximas, many=True).data,
            'turnos': TurnoAgendaSerializer(agenda_turnos, many=True).data,
        })


class ResumenView(APIView):
    """
    Los números y el destacado que muestra la pantalla de inicio, en una
    sola llamada. Sin esto la app tendría que pedir cuatro listados enteros
    sólo para contar.
    """

    permission_classes = [IsAuthenticated]

    def get(self, request):
        usuario = request.user
        hoy = timezone.localdate()
        ahora = timezone.now()
        datos = {'rol': usuario.rol, 'nombre': usuario.nombre}

        if usuario.es_cliente:
            mascotas = Mascota.objects.filter(dueno=usuario)
            datos['total_mascotas'] = mascotas.count()
            datos['turnos_pendientes'] = Turno.objects.filter(
                mascota__dueno=usuario, estado=Turno.Estado.PENDIENTE, fecha__gte=ahora
            ).count()

            proxima = Vacunacion.objects.select_related('mascota', 'vacuna').filter(
                mascota__dueno=usuario, proxima_dosis__gte=hoy
            ).order_by('proxima_dosis').first()
            datos['proxima_dosis'] = None if proxima is None else {
                'vacunacion_id': proxima.id,
                'mascota_id': proxima.mascota_id,
                'mascota_nombre': proxima.mascota.nombre,
                'vacuna_nombre': proxima.vacuna.nombre,
                'fecha': proxima.proxima_dosis,
                'dias_restantes': (proxima.proxima_dosis - hoy).days,
            }

        elif usuario.es_veterinario:
            datos['total_mascotas'] = Mascota.objects.count()
            datos['turnos_pendientes'] = Turno.objects.filter(
                estado=Turno.Estado.PENDIENTE, fecha__gte=ahora
            ).count()
            datos['turnos_hoy'] = Turno.objects.filter(
                fecha__date=hoy
            ).exclude(estado=Turno.Estado.CANCELADO).count()

        else:
            datos['total_usuarios'] = Usuario.objects.count()
            datos['total_mascotas'] = Mascota.objects.count()
            datos['total_turnos'] = Turno.objects.count()
            datos['total_servicios'] = Servicio.objects.filter(activo=True).count()
            datos['consultas_sin_leer'] = Consulta.objects.filter(leida=False).count()
            datos['usuarios_por_rol'] = {
                fila['rol']: fila['total']
                for fila in Usuario.objects.values('rol').annotate(total=Count('id'))
            }

        return Response(datos)


# ---------------------------------------------------------------------------
# Contacto
# ---------------------------------------------------------------------------

class ConsultaViewSet(viewsets.ModelViewSet):
    http_method_names = ['get', 'post', 'patch', 'delete', 'head', 'options']
    search_fields = ['nombre', 'email', 'mensaje']
    ordering_fields = ['creado_en']

    def get_queryset(self):
        queryset = Consulta.objects.all()
        leida = self.request.query_params.get('leida')
        if leida is not None:
            queryset = queryset.filter(leida=leida.lower() in ('1', 'true', 'si', 'sí'))
        return queryset

    def get_serializer_class(self):
        return ConsultaSerializer if self.action == 'create' else ConsultaAdminSerializer

    def get_permissions(self):
        # El envío es público; leer y gestionar las consultas es del admin.
        if self.action == 'create':
            return [AllowAny()]
        return [IsAuthenticated(), EsAdmin()]

    def get_throttles(self):
        if self.action == 'create':
            self.throttle_scope = 'contacto'
            return [ScopedRateThrottle()]
        return super().get_throttles()


# ---------------------------------------------------------------------------
# Heredados del módulo web (sin consumidor en la app móvil)
# ---------------------------------------------------------------------------

class CategoriaViewSet(viewsets.ModelViewSet):
    queryset = Categoria.objects.all()
    serializer_class = CategoriaSerializer
    permission_classes = [IsAuthenticated, LecturaAutenticadaEscrituraAdmin]


class ProductoViewSet(viewsets.ModelViewSet):
    queryset = Producto.objects.select_related('categoria')
    serializer_class = ProductoSerializer

    def get_permissions(self):
        if self.request.method in ('GET', 'HEAD', 'OPTIONS'):
            return [AllowAny()]
        return [IsAuthenticated(), EsAdmin()]
