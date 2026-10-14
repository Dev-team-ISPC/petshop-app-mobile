package com.devteamispc.petshop.soporte;

import okhttp3.mockwebserver.MockResponse;

/** Respuestas JSON con la forma real del backend, para el servidor simulado. */
public final class Respuestas {

    private Respuestas() { }

    public static MockResponse json(int codigo, String cuerpo) {
        return new MockResponse()
                .setResponseCode(codigo)
                .setHeader("Content-Type", "application/json")
                .setBody(cuerpo);
    }

    public static MockResponse noEncontrado() {
        return json(404, "{\"detail\":\"No encontrado.\"}");
    }

    public static final String LOGIN_CLIENTE = "{\"access\":\"access-de-prueba\",\"refresh\":\"refresh-de-prueba\","
            + "\"usuario\":{\"id\":3,\"nombre\":\"Juan Pérez\",\"email\":\"cliente@petshop.test\",\"rol\":\"cliente\"}}";

    public static final String RESUMEN_CLIENTE = "{\"nombre\":\"Juan Pérez\",\"rol\":\"cliente\","
            + "\"total_mascotas\":2,\"turnos_pendientes\":1,\"proxima_dosis\":null}";

    public static final String MASCOTA_ROCCO = "{\"id\":1,\"nombre\":\"Rocco\",\"especie\":\"perro\",\"especie_display\":\"Perro\","
            + "\"raza\":\"Labrador\",\"peso\":\"28.50\",\"fecha_nacimiento\":\"2021-03-10\",\"edad_anios\":5,"
            + "\"dueno\":3,\"dueno_nombre\":\"Juan Pérez\"}";

    public static final String PAGINA_MASCOTAS = "{\"count\":1,\"next\":null,\"previous\":null,\"results\":[" + MASCOTA_ROCCO + "]}";

    public static final String PAGINA_VACUNACIONES = "{\"count\":1,\"next\":null,\"previous\":null,\"results\":["
            + "{\"id\":7,\"mascota\":1,\"mascota_nombre\":\"Rocco\",\"vacuna\":1,\"vacuna_nombre\":\"Quíntuple\","
            + "\"fecha_aplicacion\":\"2025-10-13\",\"proxima_dosis\":\"2026-10-13\",\"dias_para_proxima_dosis\":8,"
            + "\"veterinario\":2,\"veterinario_nombre\":\"Dra. Gómez\"}]}";

    public static final String PAGINA_TURNOS = "{\"count\":1,\"next\":null,\"previous\":null,\"results\":["
            + "{\"id\":4,\"mascota\":1,\"mascota_nombre\":\"Rocco\",\"dueno\":3,\"dueno_nombre\":\"Juan Pérez\","
            + "\"servicio\":1,\"servicio_nombre\":\"Consulta general\",\"veterinario\":null,\"veterinario_nombre\":null,"
            + "\"fecha\":\"2026-10-20T15:30:00-03:00\",\"dias_restantes\":15,\"estado\":\"pendiente\","
            + "\"estado_display\":\"Pendiente\",\"observaciones\":\"\"}]}";
}
