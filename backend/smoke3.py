"""
Verifica lo que necesita cada pantalla de la app: filtros, buscadores,
paginación configurable, campos derivados y el resumen del inicio.

    python smoke3.py
"""
import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
django.setup()

from rest_framework.test import APIClient  # noqa: E402
from api.models import Mascota, Turno  # noqa: E402

c = APIClient()


def login(email):
    r = c.post('/api/auth/login/', {'email': email, 'password': 'Petshop2026!'}, format='json')
    assert r.status_code == 200, (email, r.status_code)
    return r.data


def auth(token):
    c.credentials(HTTP_AUTHORIZATION=f'Bearer {token}')


def ok(msg):
    print('OK  ', msg)


# ---------------------------------------------------------------- resumen
cl = login('cliente@petshop.test'); auth(cl['access'])
r = c.get('/api/resumen/')
assert r.status_code == 200 and r.data['rol'] == 'cliente', r.data
assert r.data['total_mascotas'] == 2, r.data
pd = r.data['proxima_dosis']
assert pd and pd['mascota_nombre'] and pd['dias_restantes'] >= 0, pd
ok(f"resumen del cliente: {r.data['total_mascotas']} mascotas, próxima dosis "
   f"{pd['vacuna_nombre']} de {pd['mascota_nombre']} en {pd['dias_restantes']} días")

v = login('vet@petshop.test'); auth(v['access'])
r = c.get('/api/resumen/')
assert r.data['rol'] == 'veterinario' and 'turnos_pendientes' in r.data, r.data
ok(f"resumen del veterinario: {r.data['turnos_pendientes']} turnos pendientes, "
   f"{r.data['total_mascotas']} mascotas")

a = login('admin@petshop.test'); auth(a['access'])
r = c.get('/api/resumen/')
for k in ('total_usuarios', 'total_mascotas', 'total_turnos', 'consultas_sin_leer', 'usuarios_por_rol'):
    assert k in r.data, (k, r.data)
ok(f"resumen del admin: {r.data['total_usuarios']} usuarios, {r.data['total_turnos']} turnos, "
   f"{r.data['consultas_sin_leer']} consulta sin leer")

# ---------------------------------------------------------- filtros turnos
auth(v['access'])
r = c.get('/api/turnos/?estado=pendiente')
assert r.status_code == 200 and all(t['estado'] == 'pendiente' for t in r.data['results'])
pendientes = r.data['count']
ok(f'chip "Pendientes": {pendientes} turnos, todos en ese estado')

r = c.get('/api/turnos/?estado=pendiente,confirmado')
assert all(t['estado'] in ('pendiente', 'confirmado') for t in r.data['results'])
ok(f'chip combinado pendiente+confirmado: {r.data["count"]} turnos')

r = c.get('/api/turnos/?futuros=1')
assert all(t['dias_restantes'] >= 0 for t in r.data['results']), r.data['results']
ok('filtro de turnos futuros')

m = Mascota.objects.get(nombre='Rocco')
r = c.get(f'/api/turnos/?mascota={m.id}')
assert all(t['mascota'] == m.id for t in r.data['results'])
ok(f'turnos de una mascota concreta: {r.data["count"]}')

# --------------------------------------------- campos que piden las vistas
t = r.data['results'][0]
assert t['dueno_nombre'] and t['dueno'], t
ok(f'el turno trae el dueño para la pantalla del veterinario: {t["dueno_nombre"]}')
assert isinstance(t['dias_restantes'], int)
ok('el turno trae los días restantes ya calculados')

r = c.get(f'/api/mascotas/{m.id}/')
assert isinstance(r.data['edad_anios'], int) and r.data['edad_anios'] > 0
ok(f'la mascota trae la edad calculada: {r.data["edad_anios"]} años')

r = c.get(f'/api/vacunaciones/?mascota={m.id}')
vac = r.data['results'][0]
assert vac['dias_para_proxima_dosis'] is not None
ok(f'la vacunación trae el badge del carnet: {vac["vacuna_nombre"]}, '
   f'{vac["dias_para_proxima_dosis"]} días')

r = c.get('/api/vacunaciones/?proximas=1')
assert all(x['dias_para_proxima_dosis'] >= 0 for x in r.data['results'])
ok('filtro de vacunaciones con próxima dosis pendiente')

# ------------------------------------------------------------- buscadores
r = c.get('/api/mascotas/?search=Rocco')
assert r.data['count'] == 1 and r.data['results'][0]['nombre'] == 'Rocco'
ok('buscador de mascotas por nombre')

r = c.get('/api/mascotas/?search=Ana')
assert r.data['count'] >= 1 and r.data['results'][0]['dueno_nombre'].startswith('Ana')
ok('buscador de mascotas por dueño: encuentra a Toby buscando "Ana"')

auth(a['access'])
r = c.get('/api/usuarios/?search=petshop.test')
assert r.data['count'] >= 4
ok(f'buscador de usuarios por email: {r.data["count"]} resultados')

r = c.get('/api/usuarios/?rol=veterinario')
assert all(u['rol'] == 'veterinario' for u in r.data['results'])
ok(f'filtro de usuarios por rol: {r.data["count"]} veterinario')

r = c.get('/api/servicios/?activo=true')
assert all(s['activo'] for s in r.data['results'])
ok(f'filtro de servicios activos: {r.data["count"]}')

r = c.get('/api/contacto/?leida=false')
assert all(not x['leida'] for x in r.data['results'])
ok(f'filtro de consultas sin leer: {r.data["count"]}')

# ------------------------------------------------------------- ordenamiento
r = c.get('/api/mascotas/?ordering=-nombre')
nombres = [x['nombre'] for x in r.data['results']]
assert nombres == sorted(nombres, reverse=True), nombres
ok(f'ordenamiento descendente: {nombres}')

# -------------------------------------------------------------- paginación
r = c.get('/api/mascotas/?page_size=1')
assert len(r.data['results']) == 1 and r.data['next']
ok('page_size configurable desde la app')

# ------------------------------------- el veterinario queda asignado al turno
auth(v['access'])
libre = Turno.objects.filter(veterinario__isnull=True, estado='pendiente').first()
r = c.patch(f'/api/turnos/{libre.id}/', {'estado': 'confirmado'}, format='json')
assert r.status_code == 200 and r.data['veterinario_nombre'] == 'Dra. Gómez', r.data
ok('al confirmar, el turno queda asignado al veterinario que lo confirmó')

print('\n21/21 verificaciones de filtros y resumen pasaron')
