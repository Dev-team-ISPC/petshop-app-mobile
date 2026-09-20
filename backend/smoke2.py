import os, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE','config.settings')
django.setup()
from rest_framework.test import APIClient
from api.models import Turno, Mascota, Usuario, Vacunacion, Vacuna

c = APIClient()
def login(email):
    r = c.post('/api/auth/login/', {'email': email, 'password': 'Petshop2026!'}, format='json')
    assert r.status_code == 200, (email, r.status_code); return r.data
def auth(t): c.credentials(HTTP_AUTHORIZATION=f'Bearer {t}')
ok = lambda m: print('OK  ', m)

cl = login('cliente@petshop.test'); auth(cl['access'])
t = Turno.objects.filter(mascota__dueno=cl['usuario']['id'], estado='pendiente').first()

# --- CAMBIO 1: el cliente cancela, no borra ---
r = c.patch(f'/api/turnos/{t.id}/', {'estado':'confirmado'}, format='json')
assert r.status_code == 400 and 'estado' in r.data, (r.status_code, r.data)
ok('cliente NO puede confirmar su turno: ' + r.data['estado'][0])

r = c.patch(f'/api/turnos/{t.id}/', {'estado':'completado'}, format='json')
assert r.status_code == 400, r.status_code
ok('cliente NO puede marcar completado')

r = c.patch(f'/api/turnos/{t.id}/', {'fecha':'2027-01-01T10:00:00Z'}, format='json')
assert r.status_code == 400 and 'fecha' in r.data, r.data
ok('cliente NO puede mover la fecha de un turno ya pedido')

r = c.patch(f'/api/turnos/{t.id}/', {'estado':'cancelado'}, format='json')
assert r.status_code == 200 and r.data['estado'] == 'cancelado', r.data
ok('cliente SI puede cancelar su propio turno')

r = c.patch(f'/api/turnos/{t.id}/', {'estado':'pendiente'}, format='json')
assert r.status_code == 400, r.status_code
ok('cliente NO puede reabrir un turno cancelado')

r = c.delete(f'/api/turnos/{t.id}/')
assert r.status_code == 403, r.status_code
ok('cliente NO puede BORRAR el turno: el historico se conserva')

assert Turno.objects.filter(pk=t.id).exists()
ok('el turno sigue en la base, con estado cancelado')

# --- CAMBIO 2: el admin no escribe en la historia clinica ---
a = login('admin@petshop.test'); auth(a['access'])
m = Mascota.objects.first()
vac = Vacuna.objects.first()
r = c.post('/api/vacunaciones/', {'mascota': m.id, 'vacuna': vac.id,
     'fecha_aplicacion':'2026-09-01','proxima_dosis':'2027-09-01'}, format='json')
assert r.status_code == 403, (r.status_code, r.data)
ok('admin NO puede registrar vacunaciones: ' + r.data['detail'])

r = c.get('/api/vacunaciones/')
assert r.status_code == 200 and r.data['count'] >= 4
ok(f'admin SI lee la historia clinica: {r.data["count"]} vacunaciones')

v = login('vet@petshop.test'); auth(v['access'])
r = c.post('/api/vacunaciones/', {'mascota': m.id, 'vacuna': vac.id,
     'fecha_aplicacion':'2026-09-01','proxima_dosis':'2027-09-01'}, format='json')
assert r.status_code == 201, r.data
assert r.data['veterinario_nombre'] == 'Dra. Gómez', r.data
ok('veterinario SI registra, y queda firmado: ' + r.data['veterinario_nombre'])

# --- CAMBIO 3: el veterinario abre la libreta pero no la destruye ---
r = c.post('/api/mascotas/', {'nombre':'Nuevo','especie':'perro','raza':'Mestizo',
     'peso':'10.00','fecha_nacimiento':'2024-01-01','dueno': cl['usuario']['id']}, format='json')
assert r.status_code == 201, r.data
nueva = r.data['id']
ok('veterinario SI da de alta una mascota y le asigna dueno')

r = c.patch(f'/api/mascotas/{nueva}/', {'peso':'11.50'}, format='json')
assert r.status_code == 200 and r.data['peso'] == '11.50'
ok('veterinario SI corrige el peso medido en la consulta')

r = c.delete(f'/api/mascotas/{nueva}/')
assert r.status_code == 403, r.status_code
ok('veterinario NO puede eliminar una mascota')

auth(a['access'])
r = c.delete(f'/api/mascotas/{nueva}/')
assert r.status_code == 204, r.status_code
ok('admin SI puede eliminarla')


# --- CAMBIO 4: el catalogo de vacunas es clinico ---
auth(a['access'])
r = c.post('/api/vacunas/', {'nombre':'Antigripal X','descripcion':'x','frecuencia':'anual'}, format='json')
assert r.status_code == 403, (r.status_code, r.data)
ok('admin NO puede tocar el catalogo de vacunas: ' + r.data['detail'])
r = c.get('/api/vacunas/'); assert r.status_code == 200
ok(f'admin SI lo lee: {r.data["count"]} vacunas')

auth(v['access'])
r = c.post('/api/vacunas/', {'nombre':'Antigripal X','descripcion':'x','frecuencia':'anual'}, format='json')
assert r.status_code == 201, r.data
ok('veterinario SI crea una vacuna en el catalogo')
r = c.delete(f'/api/vacunas/{r.data["id"]}/')
assert r.status_code == 204, r.status_code
ok('veterinario SI la elimina')

auth(cl['access'])
r = c.get('/api/vacunas/'); assert r.status_code == 200
ok('cliente SI lee el catalogo (lo necesita el carnet)')
r = c.post('/api/vacunas/', {'nombre':'Z','descripcion':'z','frecuencia':'anual'}, format='json')
assert r.status_code == 403
ok('cliente NO escribe el catalogo')

print('\n19/19 verificaciones de los cambios nuevos pasaron')
