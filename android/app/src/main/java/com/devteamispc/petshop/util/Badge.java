package com.devteamispc.petshop.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.devteamispc.petshop.R;

/**
 * Pinta un badge (TextView con fondo @drawable/fondo_badge) según su nivel,
 * con los colores de estado de colors.xml. Todos superan 4,5:1 de contraste.
 */
public final class Badge {

    private Badge() { }

    public static void pintar(TextView badge, Estados.Nivel nivel) {
        int[] col = colores(nivel);
        pintar(badge, col[0], col[1]);
    }

    /** {fondo, texto} del nivel, como recursos de color. */
    public static int[] colores(Estados.Nivel nivel) {
        int fondo;
        int texto;
        switch (nivel) {
            case PELIGRO:
                fondo = R.color.cancelado_fondo;
                texto = R.color.cancelado_texto;
                break;
            case ATENCION:
                fondo = R.color.pendiente_fondo;
                texto = R.color.pendiente_texto;
                break;
            case OK:
                fondo = R.color.confirmado_fondo;
                texto = R.color.confirmado_texto;
                break;
            default:
                fondo = R.color.completado_fondo;
                texto = R.color.completado_texto;
        }
        return new int[]{ fondo, texto };
    }

    /**
     * Caja de días de una próxima dosis (número grande y "días" abajo), con los
     * colores del nivel. TalkBack la lee entera, por ejemplo "En 25 días".
     */
    public static void pintarDias(View caja, TextView numero, TextView etiqueta, Integer dias) {
        Context c = caja.getContext();
        int[] col = colores(Estados.nivelDosis(dias));
        ViewCompat.setBackgroundTintList(caja, ColorStateList.valueOf(ContextCompat.getColor(c, col[0])));
        int colorTexto = ContextCompat.getColor(c, col[1]);
        numero.setTextColor(colorTexto);
        etiqueta.setTextColor(colorTexto);

        if (dias == null) {
            numero.setText("–");
            etiqueta.setText("");
        } else if (dias == 0) {
            numero.setText(c.getString(R.string.dias_hoy));
            etiqueta.setText("");
        } else if (dias < 0) {
            numero.setText(String.valueOf(-dias));
            etiqueta.setText(c.getString(R.string.dias_vencida));
        } else {
            numero.setText(String.valueOf(dias));
            etiqueta.setText(c.getString(dias == 1 ? R.string.dias_uno : R.string.dias_varios));
        }
        etiqueta.setVisibility(etiqueta.getText().length() == 0 ? View.GONE : View.VISIBLE);
        caja.setContentDescription(Fechas.textoDias(dias));
    }

    /** Para colores que no son de estado, como el violeta del rol administrador. */
    public static void pintar(TextView badge, @ColorRes int fondo, @ColorRes int texto) {
        Context c = badge.getContext();
        ViewCompat.setBackgroundTintList(badge, ColorStateList.valueOf(ContextCompat.getColor(c, fondo)));
        badge.setTextColor(ContextCompat.getColor(c, texto));
    }
}
