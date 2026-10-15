"""
Manejador de excepciones de la API (REST_FRAMEWORK['EXCEPTION_HANDLER']).

Hace dos cosas además del comportamiento estándar de DRF:

1. Registra los accesos denegados (403) y los bloqueos por límite de uso (429)
   en el log de seguridad.
2. Convierte el ProtectedError de Django en un 409 Conflict. Sin esto, borrar
   un servicio con turnos o una vacuna ya aplicada devolvía un 500.
"""
from django.db.models import ProtectedError
from rest_framework import status
from rest_framework.exceptions import PermissionDenied, Throttled
from rest_framework.response import Response
from rest_framework.views import exception_handler

from . import seguridad


def manejar_excepcion(exc, context):
    request = context.get('request')
    vista = context.get('view')
    accion = getattr(vista, 'action', None)

    if isinstance(exc, ProtectedError):
        seguridad.registrar(
            'eliminacion_rechazada', request,
            recurso=request.path if request else None,
            resultado='conflicto', nivel=seguridad.WARNING,
            motivo='registro en uso',
        )
        return Response(
            {'detail': 'No se puede eliminar: hay registros que lo usan. '
                       'Podés desactivarlo en lugar de borrarlo.'},
            status=status.HTTP_409_CONFLICT,
        )

    respuesta = exception_handler(exc, context)

    if isinstance(exc, PermissionDenied):
        seguridad.registrar(
            'acceso_denegado', request, resultado='denegado',
            nivel=seguridad.WARNING, accion=accion, motivo=str(exc.detail),
        )
    elif isinstance(exc, Throttled):
        seguridad.registrar(
            'limite_superado', request, resultado='bloqueado',
            nivel=seguridad.WARNING, ambito=getattr(vista, 'throttle_scope', None),
            reintentar_en_segundos=exc.wait,
        )

    return respuesta
