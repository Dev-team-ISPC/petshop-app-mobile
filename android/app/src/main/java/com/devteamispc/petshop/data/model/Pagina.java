package com.devteamispc.petshop.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Envoltorio de todos los listados de la API: {count, next, previous, results}.
 * Se usa como Pagina&lt;Mascota&gt;, Pagina&lt;Turno&gt;, etc.
 */
public class Pagina<T> {

    private int count;
    private String next;
    private String previous;
    private List<T> results;

    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }

    public List<T> getResults() {
        return results == null ? new ArrayList<T>() : results;
    }

    public boolean hayMas() { return next != null; }
}
