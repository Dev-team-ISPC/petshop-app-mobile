from django.urls import include, path
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenRefreshView

from .views import (
    AgendaView, CategoriaViewSet, ConsultaViewSet, LoginView, LogoutView,
    MascotaViewSet, PerfilView, ProductoViewSet, RegistroView, ResumenView,
    ServicioViewSet, TurnoViewSet, UsuarioViewSet, VacunaViewSet, VacunacionViewSet,
)

router = DefaultRouter()
router.register('usuarios', UsuarioViewSet, basename='usuario')
router.register('mascotas', MascotaViewSet, basename='mascota')
router.register('vacunas', VacunaViewSet, basename='vacuna')
router.register('vacunaciones', VacunacionViewSet, basename='vacunacion')
router.register('servicios', ServicioViewSet, basename='servicio')
router.register('turnos', TurnoViewSet, basename='turno')
router.register('contacto', ConsultaViewSet, basename='consulta')

# Heredados del módulo Programador Web
router.register('categorias', CategoriaViewSet, basename='categoria')
router.register('productos', ProductoViewSet, basename='producto')

urlpatterns = [
    path('auth/registro/', RegistroView.as_view(), name='registro'),
    path('auth/login/', LoginView.as_view(), name='login'),
    path('auth/refresh/', TokenRefreshView.as_view(), name='token-refresh'),
    path('auth/logout/', LogoutView.as_view(), name='logout'),
    path('auth/me/', PerfilView.as_view(), name='perfil'),

    path('resumen/', ResumenView.as_view(), name='resumen'),
    path('agenda/', AgendaView.as_view(), name='agenda'),

    path('', include(router.urls)),
]
