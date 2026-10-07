package com.devteamispc.petshop.data.api;

import com.devteamispc.petshop.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Único punto de entrada a la API. La URL sale de BuildConfig:
 * en debug apunta a 10.0.2.2:8000 (la máquina anfitriona vista desde el
 * emulador) y en release al dominio real.
 *
 * Uso desde cualquier pantalla:
 *     ApiClient.getApi().mascotas(filtros).enqueue(...)
 */
public final class ApiClient {

    private static PetshopApi api;
    private static PetshopApi apiSinAuth;

    private ApiClient() { }

    public static synchronized PetshopApi getApi() {
        if (api == null) {
            OkHttpClient cliente = baseBuilder()
                    .addInterceptor(new AuthInterceptor())
                    .authenticator(new TokenAuthenticator())
                    .build();
            api = construir(cliente);
        }
        return api;
    }

    /**
     * Cliente sin interceptor ni authenticator: lo usa TokenAuthenticator
     * para pedir el refresh sin entrar en recursión.
     */
    static synchronized PetshopApi getApiSinAuth() {
        if (apiSinAuth == null) {
            apiSinAuth = construir(baseBuilder().build());
        }
        return apiSinAuth;
    }

    private static OkHttpClient.Builder baseBuilder() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);
    }

    private static PetshopApi construir(OkHttpClient cliente) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PetshopApi.class);
    }
}
