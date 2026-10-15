"""
Registros de seguridad (TK-S2-15 · Ciberseguridad, punto 6).

Cada evento sensible se escribe como UNA línea JSON en el logger
"petshop.seguridad". En Render la salida estándar queda en los Logs del
servicio; en local, además, se puede volcar a archivo con SECURITY_LOG_FILE.

Qué se registra: fecha y hora, evento, usuario (id y rol), IP, método, ruta,
recurso afectado y resultado.

Qué NUNCA se registra: contraseñas, tokens (access o refresh), ni el email en
claro. Para poder correlacionar los intentos fallidos contra una misma cuenta
sin guardar el email, se registra un seudónimo (hash SHA-256 truncado) y una
versión enmascarada para que una persona lo pueda leer.
"""
import hashlib
import json
import logging

from django.utils import timezone

logger = logging.getLogger('petshop.seguridad')

INFO = logging.INFO
WARNING = logging.WARNING


def seudonimo_email(email):
    """Hash corto y estable del email: identifica la cuenta sin exponerla."""
    if not email:
        return None
    return hashlib.sha256(email.strip().lower().encode('utf-8')).hexdigest()[:12]


def enmascarar_email(email):
    """'cliente@petshop.test' -> 'c******@petshop.test'."""
    if not email or '@' not in email:
        return None
    local, dominio = email.strip().lower().split('@', 1)
    return f'{local[:1]}{"*" * max(len(local) - 1, 1)}@{dominio}'


def ip_de(request):
    """
    IP del cliente. Detrás del proxy de Render la IP real llega en
    X-Forwarded-For (la primera de la lista); en local, en REMOTE_ADDR.
    """
    if request is None:
        return None
    reenviada = request.META.get('HTTP_X_FORWARDED_FOR', '')
    if reenviada:
        return reenviada.split(',')[0].strip()
    return request.META.get('REMOTE_ADDR')


def registrar(evento, request=None, usuario=None, recurso=None,
              resultado='ok', nivel=INFO, **detalle):
    """
    Escribe un evento de seguridad.

    evento    : nombre corto en snake_case (login_fallido, rol_cambiado, ...)
    usuario   : quien ejecuta la acción. Si no se pasa, se toma de request.user.
    recurso   : lo afectado, como "usuario:12" o "servicio:3".
    resultado : ok | rechazado | denegado | bloqueado | conflicto
    detalle   : datos extra SIN información sensible.
    """
    if usuario is None and request is not None:
        actual = getattr(request, 'user', None)
        if actual is not None and actual.is_authenticated:
            usuario = actual

    registro = {
        'momento': timezone.now().isoformat(),
        'evento': evento,
        'resultado': resultado,
        'usuario_id': getattr(usuario, 'id', None),
        'rol': getattr(usuario, 'rol', None),
        'ip': ip_de(request),
        'metodo': getattr(request, 'method', None),
        'ruta': getattr(request, 'path', None),
        'recurso': recurso,
    }
    if detalle:
        registro['detalle'] = detalle

    logger.log(nivel, json.dumps(registro, ensure_ascii=False, default=str))
