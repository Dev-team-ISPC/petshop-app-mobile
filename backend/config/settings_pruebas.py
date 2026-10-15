"""
Configuración para correr la suite de la API (Newman) sin MySQL.

Usa la misma configuración que producción, salvo la base: SQLite en un
archivo temporal. La usan el pipeline de GitHub Actions y quien quiera correr
las pruebas de API en su máquina sin levantar MySQL.

    DJANGO_SETTINGS_MODULE=config.settings_pruebas python manage.py migrate
"""
from .settings import *  # noqa: F401,F403

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': BASE_DIR / 'pruebas.sqlite3',  # noqa: F405
    }
}

# El pipeline corre con DEBUG=False (como producción) pero sin HTTPS:
# se desactiva sólo lo que exige un certificado.
SECURE_SSL_REDIRECT = False
SESSION_COOKIE_SECURE = False
CSRF_COOKIE_SECURE = False
SECURE_HSTS_SECONDS = 0
