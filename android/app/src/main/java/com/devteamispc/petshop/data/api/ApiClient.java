package com.devteamispc.petshop.data.api;

import androidx.annotation.VisibleForTesting;

import com.devteamispc.petshop.BuildConfig;

import java.util.concurrent.TimeUnit;

import com.devteamispc.petshop.data.model.RefreshRequest;
import com.devteamispc.petshop.data.model.RefreshResponse;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Único punto de entrada a la API. La URL sale de BuildConfig.API_URL:
 * por defecto el backend desplegado en Render; cada integrante puede apuntar
 * a su backend local con API_URL en local.properties, sin tocar el código.
 *
 * Uso desde cualquier pantalla:
 *     ApiClient.getApi().mascotas(filtros).enqueue(...)
 */
public final class ApiClient {

    private static PetshopApi api;

    private ApiClient() { }

    public static synchronized PetshopApi getApi() {
        if (api == null) {
            api = construirCon(BuildConfig.API_URL, new TokensDeSesion());
        }
        return api;
    }

    /**
     * Sólo para los tests instrumentados (Espresso): apunta la app a un servidor
     * simulado en el propio teléfono, para no depender de internet ni de Render.
     */
    @VisibleForTesting
    public static synchronized void usarServidorDePrueba(String baseUrl) {
        api = construirCon(baseUrl, new TokensDeSesion());
    }

    /**
     * Arma el cliente completo contra cualquier servidor y almacén de tokens.
     * La app lo usa con Render y SessionManager; los tests de red, con
     * MockWebServer y un almacén en memoria.
     *
     * El refresh se pide con un cliente sin interceptor ni authenticator, para
     * no entrar en recursión.
     */
    static PetshopApi construirCon(String baseUrl, AlmacenTokens tokens) {
        PetshopApi sinAuth = construir(baseUrl, baseBuilder().build());
        TokenAuthenticator.Renovador renovador = refresh -> {
            retrofit2.Response<RefreshResponse> r = sinAuth.refresh(new RefreshRequest(refresh)).execute();
            return r.isSuccessful() ? r.body() : null;
        };
        OkHttpClient cliente = baseBuilder()
                .addInterceptor(new AuthInterceptor(tokens))
                .authenticator(new TokenAuthenticator(tokens, renovador))
                .build();
        return construir(baseUrl, cliente);
    }

    private static OkHttpClient.Builder baseBuilder() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        // Aun en debug, el token no se escribe en el Logcat.
        log.redactHeader("Authorization");

        return new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);
    }

    private static PetshopApi construir(String baseUrl, OkHttpClient cliente) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PetshopApi.class);
    }
}
