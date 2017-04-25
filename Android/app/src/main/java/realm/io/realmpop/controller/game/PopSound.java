package realm.io.realmpop.controller.game;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import static android.content.Context.AUDIO_SERVICE;

public class PopSound {

    private SoundPool soundPool;
    private int soundID;
    private boolean loaded = false;
    private AudioManager audioManager;

    public PopSound(Context context, int soundResId) {
        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(context, soundResId, 1);
    }

    public void playSound() {
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;

        if (loaded) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
        }
    }




}
