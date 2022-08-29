package ec.epn.detri.awm.reproductor.actividades;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ec.epn.detri.awm.reproductor.R;
import ec.epn.detri.awm.reproductor.servicios.ServicioReproductor;
import ec.epn.detri.awm.reproductor.utilidades.UiUtils;

/**
 * Esta actividad obtiene el URL de una canción en formato MP3 y lo usa
 * para crear un Intent para iniciar un Servicio Iniciado para reproducir la
 * canción
 */
public class ActividadReproductor
       extends ActividadLoggingCicloDeVida {
    /**
     * URL de la canción por defecto
     */
    private final static String CANCION_POR_DEFECTO =
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";

    /**
     * Referencia hacia el textbox para ingresar el URL de la canción
     */
    private EditText txtURLCancion;

    /**
     * Intent usado para iniciar/detener el Servicio de Música (ServicioReproductor)
     */
    private Intent intentServicioMusica;

    /**
     * Referencia hacia el botón para agregar un URL.
     */
    private FloatingActionButton btnAgregarURL;

    /**
     * Referencia hacia el botón para iniciar o detener la reproducción de una acción.
     */
    private FloatingActionButton btnIniciarDetener;

    /**
     * Variable auxiliar  para controlar cuando el cuadro de texto para ingresar unURL
     * está visible
     */
    private boolean auxBtnVisible = false;

    /**
     * Callback onCreate de una Actividad
     * @param estadoGuardado
     *            obejto que guarda el estado de información de la actividad.
     */
    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);

        setContentView(R.layout.activity_main);
        // Inicialización de los elementos UI de la vista
        inicializarVista();
    }

    /**
     * Inicializa las vistas
     */
    private void inicializarVista() {
        txtURLCancion = (EditText) findViewById(R.id.txtUrl);

        btnAgregarURL =
            (FloatingActionButton) findViewById(R.id.btnAgregar);

        btnIniciarDetener =
            (FloatingActionButton) findViewById(R.id.btnIniciarDetener);

        // hace TXT invisible para propósitos de animación
        txtURLCancion.setVisibility(View.INVISIBLE);

        // hace BTN invisible para propósitos de animación
        btnIniciarDetener.setVisibility(View.INVISIBLE);

        // Registra un "listener" para ayudar a mostrar el botón de Inicio/Stop
        // cuando el usuario presione ENTER
        txtURLCancion.setOnEditorActionListener ((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    UiUtils.ocultarTeclado(ActividadReproductor.this,
                                         txtURLCancion.getWindowToken());
                    // Insertar el valor por defecto cuando no se especifica una entrada.
                    if (TextUtils.isEmpty(
                            txtURLCancion.getText().toString().trim())) {
                        txtURLCancion.setText(
                                String.valueOf(CANCION_POR_DEFECTO));
                    }
                    UiUtils.mostrarBoton(btnIniciarDetener);
                    return true;
                } else
                    return false;
            });
    }

    /**
     * Llamado por el Framework de Android cuando el usuario presiona el botón de agregar
     * @param view La vista
     */
    public void agregarUrl(View view) {
        // Comprueba si el EditText es visible para determinar el tipo de animaciones a utilizar.
        if (auxBtnVisible) {
            // Oculta el EditText usando una animación circular y pone el booleano en false.
            UiUtils.ocultarEditText(txtURLCancion);
            auxBtnVisible = false;

            // Rotar el texto de la imagen de 'x' a '+'
            int animRedId = R.anim.fab_rotate_backward;

            // Cargar e iniciar la animación.
            btnAgregarURL.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            // Oculta el botón Iniciar/Detener.
            UiUtils.ocultarBoton(btnIniciarDetener);
        } else {
            // Muestra el EditText usando una animación circular y pone el booleano en true.
            UiUtils.mostrarEditText(txtURLCancion);
            auxBtnVisible = true;
            txtURLCancion.requestFocus();

            // Rotar el texto de la imagen de '+' a 'x'
            int animRedId = R.anim.fab_rotate_forward;

            // Cargar e iniciar la animación.
            btnAgregarURL.startAnimation(AnimationUtils.loadAnimation(this,
                    animRedId));
        }
    }
    /**
     * Llamdo por el frm de Android cuando el usuario presiona el botón Iniciar/Detener .
     *
     * @param view La vista
     */
    public void iniciarDetenerReproduccion(View view) {
        if (intentServicioMusica == null)
            iniciarCancion();
        else
            detenerCancion();
    }

    /** LLamado por Android cuando elusuario presiona el botón de Iniciar canción
     */
    public void iniciarCancion() {
        // Ocultar el teclado
        UiUtils.ocultarTeclado(this,
                             txtURLCancion.getWindowToken());

        // Obtener el URL de la canción
        Uri url = obtenerUrl();

        // asegurarse de que la URL es válida.
        if (!URLUtil.isValidUrl(url.toString()))
            UiUtils.mostrarMensajeToast(this,
                              "URL inválida "
                              + url.toString());
        else {
            // Crea un intent que iniciará el Servicio de Música para
            // reproducir una canción solicitada.
            intentServicioMusica =
                ServicioReproductor.fabricarIntent(this,
                                        url);

            // Inicia el servicio por medio de un intent explícito
            startService(intentServicioMusica);

            // Actualiza el ícono del botón a "detener"
            btnIniciarDetener.setImageResource(R.drawable.ic_media_stop);
        }
    }

    /**
     * Detiene la reproducción de una canción a través del Servicio de Música
     */
    public void detenerCancion() {
        // Detiene a través del método del API.
        stopService(intentServicioMusica);
        intentServicioMusica = null;

        // Actualiza el ícono del botón a "iniciar"
        btnIniciarDetener.setImageResource(android.R.drawable.ic_media_play);
    }	

    /**
     * Obtiene la URL a descargar en base a la entrada del usuario.
     */
    protected Uri obtenerUrl() {
        // Obtiene el texto que el usuario escribió en el texto de edición
        String userInput = txtURLCancion.getText().toString();

        // Si el usuario no ha proporcionado una URL, se utilizará la predeterminada.
        if ("".equals(userInput))
            userInput = CANCION_POR_DEFECTO;

        return Uri.parse(userInput);
    }
}
