package realm.io.realmpop.ui.game

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

import android.content.Context.AUDIO_SERVICE

class PopSound(context: Context, soundResId: Int) {

    private val soundPool: SoundPool
    private val soundID: Int
    private var loaded = false
    private val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

    init {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            loaded = true
        }
        soundID = soundPool.load(context, soundResId, 1)
    }

    fun playSound() {
        val actualVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volume = actualVolume / maxVolume

        if (loaded) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f)
        }
    }


}
