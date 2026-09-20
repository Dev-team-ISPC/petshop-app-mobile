import re

from django.core.exceptions import ValidationError


class PasswordRobustoValidator:
    """
    Política de contraseñas del plan de seguridad (RNF15):
    mayúsculas, minúsculas, números y al menos un carácter especial.

    El largo mínimo lo controla MinimumLengthValidator, configurado en 9
    para cumplir con "más de 8 caracteres".
    """

    ESPECIALES = r'[!@#$%^&*()\-_=+\[\]{};:,.<>/?\\|`~"\']'

    def validate(self, password, user=None):
        errores = []

        if not re.search(r'[A-ZÁÉÍÓÚÑ]', password):
            errores.append('al menos una letra mayúscula')
        if not re.search(r'[a-záéíóúñ]', password):
            errores.append('al menos una letra minúscula')
        if not re.search(r'\d', password):
            errores.append('al menos un número')
        if not re.search(self.ESPECIALES, password):
            errores.append('al menos un carácter especial')

        if errores:
            raise ValidationError(
                'La contraseña debe incluir %(faltantes)s.',
                code='password_no_robusta',
                params={'faltantes': ', '.join(errores)},
            )

    def get_help_text(self):
        return (
            'La contraseña debe tener más de 8 caracteres e incluir mayúsculas, '
            'minúsculas, números y al menos un carácter especial.'
        )
