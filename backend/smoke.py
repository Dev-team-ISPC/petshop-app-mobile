import os, django, json
os.environ.setdefault('DJANGO_SETTINGS_MODULE','config.settings')
django.setup()
from rest_framework.test import APIClient

c = APIClient()
def login(email):
    r = c.post('/api/auth/login/', {'email': email, 'password': 'Petshop2026!'}, format='json')
    assert r.status_code == 200, (email, r.status_code, r.data)
    return r.data

def auth(tok): c.credentials(HTTP_AUTHORIZATION=f'Bearer {tok}')

ok = lambda m: print('OK  ', m)

# 1 login
d = login('cliente@petshop.test')
assert set(d) == {'access','refresh','usuario'}, d.keys()
assert d['usuario']['rol'] == 'cliente'
ok('login devuelve access, refresh y usuario')

auth(d['access'])
# 2 filtrado por rol
r = c.get('/api/mascotas/')
assert r.status_code == 200
assert set(r.data) == {'count','next','previous','results'}, r.data.keys()
nombres = sorted(m['nombre'] for m in r.data['results'])
assert nombres == ['Mía','Rocco'], nombres
ok(f'cliente ve solo sus mascotas y viene paginado: {nombres}')

# 3 IDOR: mascota ajena
from api.models import Mascota
toby = Mascota.objects.get(nombre='Toby')
r = c.get(f'/api/mascotas/{toby.id}/')
assert r.status_code == 404, r.status_code
ok('cliente no accede a la mascota ajena por id (404)')

# 4 endpoint de admin
r = c.get('/api/usuarios/')
assert r.status_code == 403, r.status_code
ok('cliente recibe 403 en /usuarios/')

# 5 me
r = c.get('/api/auth/me/')
assert r.status_code == 200 and r.data['email'] == 'cliente@petshop.test'
ok('GET /auth/me/ devuelve el perfil propio')

# 6 agenda
r = c.get('/api/agenda/')
assert r.status_code == 200 and set(r.data) == {'proximas_dosis','turnos'}
pd = r.data['proximas_dosis']
assert all(p['mascota_nombre'] in ('Rocco','Mía') for p in pd), pd
assert [p['fecha'] for p in pd] == sorted(p['fecha'] for p in pd)
ok(f'agenda: {len(pd)} proximas dosis y {len(r.data["turnos"])} turnos, ordenados')

# 7 alta de mascota: el dueno lo pone el servidor
r = c.post('/api/mascotas/', {'nombre':'Pelusa','especie':'conejo','raza':'Belier',
    'peso':'2.10','fecha_nacimiento':'2024-05-01','dueno': toby.dueno_id}, format='json')
assert r.status_code == 201, r.data
assert r.data['dueno'] == d['usuario']['id'], r.data['dueno']
ok('el dueno enviado por el cliente se ignora: lo asigna el servidor')

# 8 turno para mascota ajena
r = c.post('/api/turnos/', {'mascota': toby.id, 'servicio': 1, 'fecha':'2027-01-10T15:00:00Z'}, format='json')
assert r.status_code == 400 and 'mascota' in r.data, r.data
ok('cliente no puede pedir turno para una mascota ajena')

# 9 cliente no cambia estado
from api.models import Turno
t = Turno.objects.filter(mascota__dueno=d['usuario']['id'], estado='pendiente').first()
r = c.patch(f'/api/turnos/{t.id}/', {'estado':'confirmado'}, format='json')
assert r.status_code == 400 and 'estado' in r.data, (r.status_code, r.data)
ok('cliente no puede confirmar un turno (400 con el motivo)')

# 10 veterinario si
v = login('vet@petshop.test'); auth(v['access'])
r = c.patch(f'/api/turnos/{t.id}/', {'estado':'confirmado'}, format='json')
assert r.status_code == 200 and r.data['estado'] == 'confirmado', r.data
ok('veterinario confirma el turno y devuelve estado_display: ' + r.data['estado_display'])

r = c.get('/api/mascotas/')
assert r.data['count'] >= 3
ok(f'veterinario ve todas las mascotas: {r.data["count"]}')

# 11 admin CRUD servicios
a = login('admin@petshop.test'); auth(a['access'])
r = c.post('/api/servicios/', {'nombre':'Ecografia','descripcion':'x','duracion_minutos':45}, format='json')
assert r.status_code == 201, r.data
sid = r.data['id']
r = c.patch(f'/api/servicios/{sid}/', {'activo': False}, format='json'); assert r.status_code == 200
r = c.delete(f'/api/servicios/{sid}/'); assert r.status_code == 204
ok('admin hace CRUD completo de servicios')

r = c.get('/api/usuarios/'); assert r.status_code == 200
ok(f'admin lista usuarios: {r.data["count"]}')

# 12 politica de password
c.credentials()
for pwd, motivo in [('corta1!A','menos de 9'), ('sinespecial1A','sin especial'),
                    ('SINMINUSCULA1!','sin minuscula'), ('sinmayuscula1!','sin mayuscula'),
                    ('SinNumeros!!Ab','sin numero')]:
    r = c.post('/api/auth/registro/', {'email':f'x{motivo.replace(" ","")}@t.test','nombre':'X',
        'password':pwd,'acepta_terminos':True}, format='json')
    assert r.status_code == 400 and 'password' in r.data, (motivo, r.status_code, r.data)
ok('la politica de contrasenas rechaza los 5 casos invalidos')

r = c.post('/api/auth/registro/', {'email':'nuevo@t.test','nombre':'Nuevo',
    'password':'Petshop2026!','acepta_terminos':False}, format='json')
assert r.status_code == 400 and 'acepta_terminos' in r.data
ok('no se registra sin aceptar terminos')

r = c.post('/api/auth/registro/', {'email':'nuevo@t.test','nombre':'Nuevo',
    'password':'Petshop2026!','acepta_terminos':True}, format='json')
assert r.status_code == 201 and r.data['id']
ok('registro valido crea la cuenta con rol cliente')

r = c.post('/api/auth/registro/', {'email':'nuevo@t.test','nombre':'Otro',
    'password':'Petshop2026!','acepta_terminos':True}, format='json')
assert r.status_code == 400 and 'email' in r.data
ok('email duplicado rechazado')

# 13 contacto publico
r = c.post('/api/contacto/', {'nombre':'Visitante','email':'v@t.test',
    'mensaje':'Hola, quiero consultar por los turnos disponibles.'}, format='json')
assert r.status_code == 201, r.data
ok('contacto acepta el envio sin autenticacion')
r = c.post('/api/contacto/', {'nombre':'V','email':'malmail','mensaje':'corto'}, format='json')
assert r.status_code == 400 and 'email' in r.data and 'mensaje' in r.data
ok('contacto valida email y largo minimo del mensaje')
r = c.get('/api/contacto/'); assert r.status_code == 401, r.status_code
ok('listar consultas sin token da 401')

# 14 sin token
r = c.get('/api/mascotas/'); assert r.status_code == 401
ok('endpoint privado sin token da 401')

# 15 logout invalida el refresh
cl = login('cliente@petshop.test'); auth(cl['access'])
r = c.post('/api/auth/logout/', {'refresh': cl['refresh']}, format='json')
assert r.status_code == 204, (r.status_code, r.data)
c.credentials()
r = c.post('/api/auth/refresh/', {'refresh': cl['refresh']}, format='json')
assert r.status_code == 401, (r.status_code, r.data)
ok('logout pone el refresh en blacklist: renovar despues da 401')

print('\n15/15 verificaciones pasaron')
