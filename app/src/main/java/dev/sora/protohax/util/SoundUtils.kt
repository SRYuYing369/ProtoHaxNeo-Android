package dev.sora.protohax.util

import android.media.AudioAttributes
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

object SoundUtils {
	fun playSound(rawId: Int, context: Context?) {
		val soundPool: SoundPool

		soundPool = if (Build.VERSION.SDK_INT >= 21) {
			val builder = SoundPool.Builder()
			builder.setMaxStreams(5)
			val attrBuilder = AudioAttributes.Builder()
			attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC)
			builder.setAudioAttributes(attrBuilder.build())
			builder.build()
		} else {
			SoundPool(1, AudioManager.STREAM_SYSTEM, 5)
		}

		soundPool.load(context, rawId, 1)
		soundPool.setOnLoadCompleteListener { soundPool, sampleId, status -> soundPool.play(1, 1f, 1f, 0, 0, 1f) }
	}
}
