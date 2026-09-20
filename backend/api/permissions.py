from rest_framework.permissions import SAFE_METHODS, BasePermission

from .models import Mascota, Turno, Usuario, Vacunacion


def _rol(request):
    user = getattr(request, 'user', None)
    return getattr(user, 'rol', None) if user and user.is_authenticated else None


class EsAdmin(BasePermission):
    message = 'Esta operación requiere rol administrador.'

    def has_permission(self, request, view):
        return _rol(request) == Usuario.Rol.ADMIN


class EsVeterinario(BasePermission):
    """
    Escritura clínica. El administrador queda fuera a propósito: su rol es
    administrativo, no clínico (principio de mínimo privilegio).
    """

    message = 'Sólo un veterinario puede registrar información clínica.'

    def has_permission(self, request, view):
        return _rol(request) == Usuario.Rol.VETERINARIO


class EsVeterinarioOAdmin(BasePermission):
    message = 'Esta operación requiere rol veterinario o administrador.'

    def has_permission(self, request, view):
        return _rol(request) in (Usuario.Rol.VETERINARIO, Usuario.Rol.ADMIN)


class LecturaAutenticadaEscrituraAdmin(BasePermission):
    """Cualquier usuario autenticado puede leer; sólo el admin puede escribir."""

    message = 'Sólo el administrador puede modificar este recurso.'

    def has_permission(self, request, view):
        if not (request.user and request.user.is_authenticated):
            return False
        if request.method in SAFE_METHODS:
            return True
        return _rol(request) == Usuario.Rol.ADMIN


class LecturaAutenticadaEscrituraClinica(BasePermission):
    """
    Cualquier usuario autenticado puede leer; sólo el veterinario escribe.
    Se usa en el catálogo de vacunas: es criterio clínico, no configuración
    administrativa.
    """

    message = 'Sólo un veterinario puede modificar el catálogo de vacunas.'

    def has_permission(self, request, view):
        if not (request.user and request.user.is_authenticated):
            return False
        if request.method in SAFE_METHODS:
            return True
        return _rol(request) == Usuario.Rol.VETERINARIO


class EsDuenoOAdmin(BasePermission):
    """
    Como EsDuenoOStaff pero sin el veterinario: se usa para el borrado.
    El veterinario puede consultar y corregir datos clínicos, no eliminar
    registros ajenos.
    """

    message = 'No tiene permiso para eliminar este recurso.'

    def has_object_permission(self, request, view, obj):
        if _rol(request) == Usuario.Rol.ADMIN:
            return True
        dueno = EsDuenoOStaff._dueno_de(obj)
        return dueno is not None and dueno == request.user


class EsDuenoOStaff(BasePermission):
    """
    Permiso a nivel de objeto. Resuelve el caso "cambiar el ID en la URL":
    un cliente sólo accede a lo que le pertenece, aunque conozca el id ajeno.
    """

    message = 'No tiene permiso sobre este recurso.'

    def has_object_permission(self, request, view, obj):
        rol = _rol(request)
        if rol in (Usuario.Rol.ADMIN, Usuario.Rol.VETERINARIO):
            return True

        dueno = self._dueno_de(obj)
        return dueno is not None and dueno == request.user

    @staticmethod
    def _dueno_de(obj):
        if isinstance(obj, Mascota):
            return obj.dueno
        if isinstance(obj, (Vacunacion, Turno)):
            return obj.mascota.dueno
        return None
