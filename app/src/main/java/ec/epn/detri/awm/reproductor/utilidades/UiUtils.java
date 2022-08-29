package ec.epn.detri.awm.reproductor.utilidades;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

/**
 * Contiene métodos estáticos que pueden ser usados por varias Actividades
 */
public class UiUtils {
    /**
     * TAG de depuración
     */
    private static final String TAG =
        UiUtils.class.getCanonicalName();

    private UiUtils() {
        throw new AssertionError();
    }

    /**
     * Muestra un mensaje "toast" (temporal).
     */
    public static void mostrarMensajeToast(Context contexto,
                                           String mensaje) {
        Toast.makeText(contexto,
                       mensaje,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * Oculta el teclado después de que el usuario ha finalizado
     * el ingreso de un texto (URL en este caso)
     */
    public static void ocultarTeclado(Activity actividad,
                                      IBinder tokenVentana) {
        InputMethodManager mgr =
            (InputMethodManager) actividad.getSystemService
            (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(tokenVentana, 0);
    }

    /**
     * Asignar el resultado de una Actividad para indicar si la
     * operación sobre cierto contenido fue exitosa o no
     *
     * @param actividad
     *          Actividad cuyo resultado será asignado.
     * @param rutaAlContenido
     *          Ruta al archivo de contenido.
     * @param razonFalla
     *          Cadena para indicar la razón deu una falla (es enviado
     *          como extra en el intent)
     */
    public static void asignarResultadoActividad(Activity actividad,
                                                 Uri rutaAlContenido,
                                                 String razonFalla) {
        if (rutaAlContenido == null)
            // Indica por qué la operación sobre el contenido no fue
            //exitosa o fue cancelada
            actividad.setResult
                (Activity.RESULT_CANCELED,
                 new Intent("").putExtra("reason",
                                         razonFalla));
        else
            //  Indica por qué la operación sobre el contenido fue
            //  exitosa
            actividad.setResult(Activity.RESULT_OK,
                               new Intent("",
                                          rutaAlContenido));
    }

    /**
     * Animación del botón
     * @param boton Botón a mostrar
     */
    public static void mostrarBoton(FloatingActionButton boton) {
        boton.show();
        boton.animate()
           .translationY(0)
           .setInterpolator(new DecelerateInterpolator(2))
           .start();
    }

    /**
     * Oculta el botón
     * @param boton BOtón a ocultar
     */
    public static void ocultarBoton(FloatingActionButton boton) {
        boton.hide();
        boton.animate()
           .translationY(boton.getHeight() + 100)
           .setInterpolator(new AccelerateInterpolator(2))
           .start();
    }

    /**
     * Muestra el EditText.
     * @param texto EditText a mostrar
     */
    public static void mostrarEditText(EditText texto) {
        // Obtiene la posición x e y de la vista para desplazar y animar el elemento UI
        int cx = texto.getRight() - 30;
        int cy = texto.getBottom() - 60;

        // Muestra un contorno circular.
        int finalRadius = Math.max(texto.getWidth(),
                texto.getHeight());

        Animator anim =
                ViewAnimationUtils.createCircularReveal(texto,
                        cx,
                        cy,
                        0,
                        finalRadius);
        texto.setVisibility(View.VISIBLE);
        anim.start();
    }

    /**
     * Oculta el EditText
     * @param texto EditText to be hidden.
     */
    public static void ocultarEditText(final EditText texto) {
        int cx = texto.getRight() - 30;
        int cy = texto.getBottom() - 60;

        int initialRadius = texto.getWidth();

        Animator anim =
                ViewAnimationUtils.createCircularReveal(texto,
                        cx,
                        cy,
                        initialRadius,
                        0);

        // Cre un "listener" tal que se hace inviible el EditText una vez que se
        // solapa con la amimación circular
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                texto.setVisibility(View.INVISIBLE);
            }
        });

        anim.start();

        // Limpiar el texto del EdiText cuando el usuario presiona el botón
        texto.getText().clear();
    }

    /**
     * Retorna el texto en mayúsculas.
     */
    public static String uppercaseInput(Context context,
                                        String input,
                                        boolean showToast) {
        if (input.isEmpty()) {
            if (showToast)
                UiUtils.mostrarMensajeToast(context,
                        "no se proveyó un texto de entrada");
            return null;
        } else
            return input.toUpperCase(Locale.ENGLISH);
    }

    /**
     * @return True si quien lo invoca está ejecutándose sobre el hilo de la UI, caso contrario
     * retorna False.
     */
    public static boolean ejecutandoseHiloUI() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
}
