package ec.epn.detri.awm.reproductor.servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * Servicio de música que extiende a la clase Service y usa MediaPlayer
 * para descargar y reproducir una canción en segundo plano. Aunque este
 * se ejecuta en el hilo principal, este implementa MdiaPlayer.OnPreparedListener
 * para evitar bloquear el hilo principal mientras la canción se reproduce.
 */
public class ServicioReproductor extends Service implements MediaPlayer.OnPreparedListener {
    /**
     * TAg de depuración
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Auxiliar que guarda el estado de si una canción se está reproduciendo actualmente.
     */
    private static boolean auxReproduciendo;

    /**
     * Referencia al objeto MediaPlayer que reproduce una canción en background
     */
    private MediaPlayer mPlayer;

    /**
     * Fabrica un intent explícito usando para iniciar y detener la reproducción
     * de una canción
     */
    public static Intent fabricarIntent(Context context, Uri URLCancion) {
        return new Intent(context,
                          ServicioReproductor.class)
            .setData(URLCancion);

    }

    /**
     * Callback invocado cuando se crea la instancia de un Servicio.
     * El código de inicialización va aquí.
     */
    @Override
    public void onCreate() {
        Log.i(TAG,"Service - ejecutando onCreate()");

        super.onCreate();

        // Crea un objeto MediaPlayer que reproducirá una canción.
        mPlayer = new MediaPlayer();

        // Indica que el MediaPlayer transmitirá el audio.
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * Callbak invocado cuando el Servicio de Música es detenido.
     */
    @Override
    public void onDestroy() {
        Log.i(TAG,"Service - ejecutando onDestroy()");

        // Detener la canción
        detenerCancion();

        super.onDestroy();
    }

    /**
     * Callback invocado cada vez que se llama el startService(). Se envía el Intent con la información requerida para reproducir la canción
     */
    @Override
    public int onStartCommand(Intent intent,
                              int flags,
                              int startid) {
        // Extrae la URL de la canción a reproducir.
        final String UrlCancion = intent.getDataString();

        Log.i(TAG,
              "Service -ejecutando onStartCommand()"  + UrlCancion);

        if (auxReproduciendo)
            // Detener la reproducción actual
            detenerCancion();

        try {
            // Indica la URL de la canción a reproducir.
            mPlayer.setDataSource(UrlCancion);
                
            // Registra "this" como el callback cuando la canción designada esté lista para ser reproducida.
            mPlayer.setOnPreparedListener(this);

            // Esta llamada no bloquea el hilo de la UI.
            mPlayer.prepareAsync(); 
        } catch (IOException e) {
        	e.printStackTrace();
        }

        // No reinicie el servicio si éste se apaga.
        return START_NOT_STICKY;
    }

    /**
     * Se requiere porque se trata de un método abstracto
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** 
     * Callback invocado cuando el MediaPlayer está listo para reproducir una canción.
     */
    public void onPrepared(MediaPlayer player) {
        Log.i(TAG,"Service MediaPlayer - ejecutando onPrepared()");

        // Sólo reproduce la canción una vez, en lugar de tenerla en bucle  sin fin.
        player.setLooping(false);

        // Indica que la canción se está reprociendo
        auxReproduciendo = true;

        // Empieza a reproducir la canción
        player.start();
    }

    /**
     * Detiene el MediaPlayer
     */
    private void detenerCancion() {
        Log.i(TAG,"Service - Ejecutando stopSong()");

        // detener la canción
        mPlayer.stop();
        
        // resetear la máquina de estados de MediaPlayer
        mPlayer.reset();

        // Indicamos que no está ejecutando una canción.
        auxReproduciendo = false;
    }

}
