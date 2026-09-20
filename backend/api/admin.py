from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from django.contrib.auth.forms import AdminPasswordChangeForm, UserChangeForm, UserCreationForm

from .models import (
    Categoria, Consulta, Mascota, Producto, Servicio, Turno, Usuario, Vacuna, Vacunacion,
)


@admin.register(Usuario)
class UsuarioAdmin(BaseUserAdmin):
    form = UserChangeForm
    add_form = UserCreationForm
    change_password_form = AdminPasswordChangeForm

    ordering = ['email']
    list_display = ['email', 'nombre', 'rol', 'is_active', 'date_joined']
    list_filter = ['rol', 'is_active', 'is_staff']
    search_fields = ['email', 'nombre']

    fieldsets = [
        (None, {'fields': ['email', 'password']}),
        ('Datos personales', {'fields': ['nombre', 'telefono', 'direccion']}),
        ('Rol y permisos', {
            'fields': ['rol', 'is_active', 'is_staff', 'is_superuser', 'groups', 'user_permissions'],
        }),
        ('Fechas', {'fields': ['last_login', 'date_joined']}),
    ]
    add_fieldsets = [
        (None, {
            'classes': ['wide'],
            'fields': ['email', 'nombre', 'rol', 'password1', 'password2'],
        }),
    ]


@admin.register(Mascota)
class MascotaAdmin(admin.ModelAdmin):
    list_display = ['nombre', 'especie', 'raza', 'peso', 'dueno']
    list_filter = ['especie']
    search_fields = ['nombre', 'raza', 'dueno__nombre']
    autocomplete_fields = ['dueno']


@admin.register(Vacuna)
class VacunaAdmin(admin.ModelAdmin):
    list_display = ['nombre', 'frecuencia']
    search_fields = ['nombre']


@admin.register(Vacunacion)
class VacunacionAdmin(admin.ModelAdmin):
    list_display = ['mascota', 'vacuna', 'fecha_aplicacion', 'proxima_dosis', 'veterinario']
    list_filter = ['vacuna']
    date_hierarchy = 'fecha_aplicacion'


@admin.register(Servicio)
class ServicioAdmin(admin.ModelAdmin):
    list_display = ['nombre', 'duracion_minutos', 'activo']
    list_filter = ['activo']
    search_fields = ['nombre']


@admin.register(Turno)
class TurnoAdmin(admin.ModelAdmin):
    list_display = ['mascota', 'servicio', 'fecha', 'estado', 'veterinario']
    list_filter = ['estado', 'servicio']
    date_hierarchy = 'fecha'


@admin.register(Consulta)
class ConsultaAdmin(admin.ModelAdmin):
    list_display = ['nombre', 'email', 'leida', 'creado_en']
    list_filter = ['leida']
    search_fields = ['nombre', 'email']
    readonly_fields = ['nombre', 'email', 'mensaje', 'creado_en']


@admin.register(Categoria)
class CategoriaAdmin(admin.ModelAdmin):
    list_display = ['nombre']
    search_fields = ['nombre']


@admin.register(Producto)
class ProductoAdmin(admin.ModelAdmin):
    list_display = ['nombre', 'precio', 'stock', 'categoria']
    list_filter = ['categoria']
    search_fields = ['nombre']
