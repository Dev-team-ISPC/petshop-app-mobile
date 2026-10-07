package com.devteamispc.petshop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devteamispc.petshop.data.local.SessionManager;
import com.devteamispc.petshop.ui.auth.LoginActivity;

import retrofit2.Response;

/**
 * Base de todas las pantallas. Resuelve tres cosas que si no cada uno
 * termina escribiendo a su manera:
 *
 *   - la flecha de retorno del ActionBar
 *   - qué hacer cuando la sesión expira (401)
 *   - el acceso a la sesión y los mensajes al usuario
 *
 * Toda Activity de la app extiende de acá.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected SessionManager sesion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sesion = SessionManager.get(this);
    }

    /** Llamar después de setContentView en las pantallas que no son el inicio. */
    protected void mostrarFlechaAtras() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Corta el paso si alguien abre una pantalla privada sin sesión. */
    protected boolean exigirSesion() {
        if (!sesion.haySesion()) {
            irAlLogin();
            return false;
        }
        return true;
    }

    /**
     * Manejo común del error de una llamada. Devuelve true si lo manejó,
     * para que la pantalla no tenga que repetir el caso del 401.
     */
    protected boolean manejarErrorComun(Response<?> respuesta) {
        if (respuesta.code() == 401) {
            sesion.cerrarSesion();
            aviso("Tu sesión expiró. Volvé a iniciar sesión.");
            irAlLogin();
            return true;
        }
        return false;
    }

    protected void irAlLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    protected void aviso(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}
