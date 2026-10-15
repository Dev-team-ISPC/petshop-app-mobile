import os
from datetime import timedelta
from pathlib import Path

import dj_database_url
import environ

BASE_DIR = Path(__file__).resolve().parent.parent

env = environ.Env(
    DEBUG=(bool, False),
    ALLOWED_HOSTS=(list, []),
    CORS_ALLOWED_ORIGINS=(list, []),
    CSRF_TRUSTED_ORIGINS=(list, []),
    ACCESS_TOKEN_MINUTES=(int, 60),
    REFRESH_TOKEN_DAYS=(int, 7),
)
environ.Env.read_env(os.path.join(BASE_DIR, '.env'))

# Sin valor por omisión: si falta, el proyecto no arranca.
SECRET_KEY = env('SECRET_KEY')

# Por omisión False: un descuido de configuración falla del lado seguro.
DEBUG = env('DEBUG')

ALLOWED_HOSTS = env('ALLOWED_HOSTS')

INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',

    'rest_framework',
    'rest_framework_simplejwt.token_blacklist',
    'corsheaders',

    'api',
]

MIDDLEWARE = [
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.security.SecurityMiddleware',
    # Sirve los archivos estáticos en el entorno desplegado, donde Django no lo hace.
    'whitenoise.middleware.WhiteNoiseMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'config.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'config.wsgi.application'

# En el entorno desplegado la base llega como una sola URL de conexión.
# En desarrollo se arma con las variables sueltas.
DATABASE_URL = env('DATABASE_URL', default='')

if DATABASE_URL:
    DATABASES = {'default': dj_database_url.parse(DATABASE_URL, conn_max_age=600)}
    DATABASES['default'].setdefault('OPTIONS', {})
    DATABASES['default']['OPTIONS'].update({
        'charset': 'utf8mb4',
        # El proveedor exige conexión cifrada y entrega el certificado de la CA.
        'ssl': {'ca': env('DB_SSL_CA', default='ca.pem')},
    })
else:
    DATABASES = {
        'default': {
            'ENGINE': 'django.db.backends.mysql',
            'NAME': env('DB_NAME'),
            'USER': env('DB_USER'),
            'PASSWORD': env('DB_PASSWORD'),
            'HOST': env('DB_HOST', default='127.0.0.1'),
            'PORT': env('DB_PORT', default='3306'),
            'OPTIONS': {'charset': 'utf8mb4'},
        }
    }

AUTH_USER_MODEL = 'api.Usuario'

# Política de contraseñas (RNF15). MinimumLength en 9 = "más de 8 caracteres".
AUTH_PASSWORD_VALIDATORS = [
    {'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator'},
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
        'OPTIONS': {'min_length': 9},
    },
    {'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator'},
    {'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator'},
    {'NAME': 'api.validators.PasswordRobustoValidator'},
]

LANGUAGE_CODE = 'es-ar'
TIME_ZONE = 'America/Argentina/Cordoba'
USE_I18N = True
USE_TZ = True

STATIC_URL = 'static/'
STATIC_ROOT = BASE_DIR / 'staticfiles'

STORAGES = {
    'default': {
        'BACKEND': 'django.core.files.storage.FileSystemStorage',
    },
    'staticfiles': {
        'BACKEND': 'whitenoise.storage.CompressedManifestStaticFilesStorage',
    },
}

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

# Lista explícita de orígenes. Nunca CORS_ALLOW_ALL_ORIGINS.
CORS_ALLOWED_ORIGINS = env('CORS_ALLOWED_ORIGINS')

REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework_simplejwt.authentication.JWTAuthentication',
    ],
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.IsAuthenticated',
    ],
    'DEFAULT_PAGINATION_CLASS': 'api.pagination.PaginacionEstandar',
    'PAGE_SIZE': 20,
    'DEFAULT_FILTER_BACKENDS': [
        'rest_framework.filters.SearchFilter',
        'rest_framework.filters.OrderingFilter',
    ],
    'DEFAULT_THROTTLE_CLASSES': [
        'rest_framework.throttling.ScopedRateThrottle',
    ],
    'DEFAULT_THROTTLE_RATES': {
        'contacto': '10/hour',
        'login': '20/hour',
        'registro': '10/hour',
    },
    # Registra 403 y 429 en el log de seguridad, y devuelve 409 (no 500)
    # al borrar un registro que está en uso.
    'EXCEPTION_HANDLER': 'api.excepciones.manejar_excepcion',
}

# Registros de seguridad (TK-S2-15). Una línea JSON por evento en la salida
# estándar (Render la muestra en Logs). En local se puede sumar un archivo
# rotativo con SECURITY_LOG_FILE=logs/seguridad.log
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'formatters': {
        'linea_json': {'format': '%(message)s'},
    },
    'handlers': {
        'consola_seguridad': {
            'class': 'logging.StreamHandler',
            'formatter': 'linea_json',
        },
    },
    'loggers': {
        'petshop.seguridad': {
            'handlers': ['consola_seguridad'],
            'level': 'INFO',
            'propagate': False,
        },
    },
}

SECURITY_LOG_FILE = env('SECURITY_LOG_FILE', default='')
if SECURITY_LOG_FILE:
    os.makedirs(os.path.dirname(os.path.join(BASE_DIR, SECURITY_LOG_FILE)), exist_ok=True)
    LOGGING['handlers']['archivo_seguridad'] = {
        'class': 'logging.handlers.RotatingFileHandler',
        'filename': os.path.join(BASE_DIR, SECURITY_LOG_FILE),
        'maxBytes': 1024 * 1024,
        'backupCount': 5,
        'encoding': 'utf-8',
        'formatter': 'linea_json',
    }
    LOGGING['loggers']['petshop.seguridad']['handlers'].append('archivo_seguridad')

SIMPLE_JWT = {
    'ACCESS_TOKEN_LIFETIME': timedelta(minutes=env('ACCESS_TOKEN_MINUTES')),
    'REFRESH_TOKEN_LIFETIME': timedelta(days=env('REFRESH_TOKEN_DAYS')),
    'ROTATE_REFRESH_TOKENS': True,
    'BLACKLIST_AFTER_ROTATION': True,
    'UPDATE_LAST_LOGIN': True,
    'AUTH_HEADER_TYPES': ('Bearer',),
    'USER_ID_FIELD': 'id',
    'USER_ID_CLAIM': 'user_id',
}

# Endurecimiento para el entorno desplegado (RNF14).
if not DEBUG:
    SECURE_SSL_REDIRECT = True
    SESSION_COOKIE_SECURE = True
    CSRF_COOKIE_SECURE = True
    SECURE_HSTS_SECONDS = 31536000
    SECURE_HSTS_INCLUDE_SUBDOMAINS = True
    SECURE_HSTS_PRELOAD = True
    SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTO', 'https')
    SECURE_CONTENT_TYPE_NOSNIFF = True
    X_FRAME_OPTIONS = 'DENY'
    # Detrás del proxy del proveedor, el admin rechaza el login sin esto.
    CSRF_TRUSTED_ORIGINS = env('CSRF_TRUSTED_ORIGINS')