package com.devteamispc.petshop.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.devteamispc.petshop.BuildConfig;
import com.devteamispc.petshop.databinding.ActivitySplashBinding;
import com.devteamispc.petshop.ui.BaseActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 0 · SplashActivity
 *
 * Primera pantalla de la app. Además de mostrar la marca, resuelve un
 * problema real del entorno desplegado: Render apaga el servidor gratuito
 * tras 15 minutos sin uso, y la primera consulta tarda entre 30 y 60
 * segundos. Mientras se ve el logo, le pegamos a la API para despertarla,
 * así el usuario no espera ese minuto en el login.
 *
 * Flujo:
 *   1. Se muestra la marca con una animación de entrada.
 *   2. En paralelo, un GET a la URL base de la API. Cualquier respuesta
 *      (200, 401, 404…) sirve: lo único que importa es que el servidor contestó.
 *   3. Si a los 3 segundos no contestó, se avisa que está conectando.
 *   4. Cuando contesta, falla o pasan 65 segundos, se sigue igual:
 *      con sesión guardada va al Home; sin sesión, al Login.
 *
 * Los tiempos y la elección de la pantalla siguiente están en ReglasSplash,
 * para poder probarlos con tests unitarios.
 */
public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding vista;
    private final Handler hilo = new Handler(Looper.getMainLooper());
    private Call ping;
    private long inicio;
    private boolean salio = false;

    private final Runnable mostrarAvisoLento = () -> {
        vista.progreso.setVisibility(View.VISIBLE);
        vista.textoEstado.setVisibility(View.VISIBLE);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vista = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        // Accesibilidad: TalkBack anuncia el nombre de la app como encabezado.
        ViewCompat.setAccessibilityHeading(vista.textoTitulo, true);

        inicio = SystemClock.elapsedRealtime();
        animarEntrada();
        hilo.postDelayed(mostrarAvisoLento, ReglasSplash.AVISO_LENTO_MS);
        despertarServidor();
    }

    private void animarEntrada() {
        vista.contenedorMarca.setAlpha(0f);
        vista.contenedorMarca.setScaleX(0.9f);
        vista.contenedorMarca.setScaleY(0.9f);
        vista.contenedorMarca.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .start();
    }

    /**
     * Usa un cliente propio y no ApiClient a propósito: ApiClient le agrega el
     * token y el TokenAuthenticator. Si este pedido devolviera 401, intentaría
     * renovar la sesión, y acá sólo queremos saber si el servidor está vivo.
     */
    private void despertarServidor() {
        OkHttpClient cliente = new OkHttpClient.Builder()
                .connectTimeout(ReglasSplash.TIEMPO_MAXIMO_MS, TimeUnit.MILLISECONDS)
                .readTimeout(ReglasSplash.TIEMPO_MAXIMO_MS, TimeUnit.MILLISECONDS)
                .callTimeout(ReglasSplash.TIEMPO_MAXIMO_MS, TimeUnit.MILLISECONDS)
                .build();

        Request pedido = new Request.Builder()
                .url(BuildConfig.API_URL)
                .get()
                .build();

        ping = cliente.newCall(pedido);
        ping.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response respuesta) {
                respuesta.close();
                hilo.post(() -> continuar());
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Sin internet o servidor caído: seguimos igual. El login ya
                // sabe mostrar el error de conexión cuando el usuario intente entrar.
                if (call.isCanceled()) return;
                hilo.post(() -> continuar());
            }
        });
    }

    /** Respeta el tiempo mínimo del logo antes de cambiar de pantalla. */
    private void continuar() {
        long transcurrido = SystemClock.elapsedRealtime() - inicio;
        hilo.postDelayed(this::irALaSiguiente, ReglasSplash.esperaRestante(transcurrido));
    }

    private void irALaSiguiente() {
        if (salio || isFinishing()) return;
        salio = true;

        Intent i = new Intent(this, ReglasSplash.destino(sesion.haySesion()));
        // Limpia la pila: con "atrás" desde el Login o el Home no se vuelve al splash.
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        hilo.removeCallbacksAndMessages(null);
        if (ping != null) ping.cancel();
        super.onDestroy();
    }
}
