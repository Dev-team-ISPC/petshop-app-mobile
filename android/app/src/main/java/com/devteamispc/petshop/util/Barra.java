package com.devteamispc.petshop.util;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.devteamispc.petshop.R;

/**
 * Subtítulo de la barra superior en blanco puro.
 *
 * El tema DarkActionBar pinta el subtítulo blanco semitransparente, y sobre
 * el azul de la barra eso queda por debajo de 4,5:1 (el mismo problema que tuvo
 * el splash). Blanco puro sobre #2563EB da 5,17:1.
 */
public final class Barra {

    private Barra() { }

    public static void subtitulo(AppCompatActivity activity, String texto) {
        ActionBar barra = activity.getSupportActionBar();
        if (barra == null) return;
        if (texto == null || texto.isEmpty()) {
            barra.setSubtitle(null);
            return;
        }
        SpannableString s = new SpannableString(texto);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, R.color.blanco)),
                0, texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        barra.setSubtitle(s);
    }
}
