package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /agenda/. No viene paginado: es una vista agregada de una pantalla. */
public class Agenda {

    @SerializedName("proximas_dosis")
    private List<ProximaDosis> proximasDosis;

    private List<TurnoAgenda> turnos;

    public List<ProximaDosis> getProximasDosis() {
        return proximasDosis == null ? new ArrayList<ProximaDosis>() : proximasDosis;
    }

    public List<TurnoAgenda> getTurnos() {
        return turnos == null ? new ArrayList<TurnoAgenda>() : turnos;
    }

    public boolean estaVacia() {
        return getProximasDosis().isEmpty() && getTurnos().isEmpty();
    }

    public static class ProximaDosis {

        @SerializedName("vacunacion_id")
        private int vacunacionId;

        @SerializedName("mascota_id")
        private int mascotaId;

        @SerializedName("mascota_nombre")
        private String mascotaNombre;

        @SerializedName("vacuna_nombre")
        private String vacunaNombre;

        private String fecha;

        @SerializedName("dias_restantes")
        private int diasRestantes;

        public int getVacunacionId() { return vacunacionId; }
        public int getMascotaId() { return mascotaId; }
        public String getMascotaNombre() { return mascotaNombre; }
        public String getVacunaNombre() { return vacunaNombre; }
        public String getFecha() { return fecha; }
        public int getDiasRestantes() { return diasRestantes; }
    }

    public static class TurnoAgenda {

        @SerializedName("turno_id")
        private int turnoId;

        @SerializedName("mascota_id")
        private int mascotaId;

        @SerializedName("mascota_nombre")
        private String mascotaNombre;

        @SerializedName("servicio_nombre")
        private String servicioNombre;

        private String fecha;
        private String estado;

        @SerializedName("dias_restantes")
        private int diasRestantes;

        public int getTurnoId() { return turnoId; }
        public int getMascotaId() { return mascotaId; }
        public String getMascotaNombre() { return mascotaNombre; }
        public String getServicioNombre() { return servicioNombre; }
        public String getFecha() { return fecha; }
        public String getEstado() { return estado; }
        public int getDiasRestantes() { return diasRestantes; }
    }
}
