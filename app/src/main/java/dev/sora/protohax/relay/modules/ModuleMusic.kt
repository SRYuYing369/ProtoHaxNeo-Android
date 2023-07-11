package dev.sora.protohax.relay.modules

import dev.sora.protohax.MyApplication.Companion.overlayManager
import dev.sora.protohax.R
import dev.sora.protohax.util.SoundUtils
import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.value.NamedChoice

class ModuleMusic:CheatModule("Music","音乐",CheatCategory.MISC) {
	private var musicValue by listValue("Music", ModuleMusic.MusicValue.values(), MusicValue.NU)

	private enum class MusicValue(override val choiceName: String) : NamedChoice {
		MYNAME("MyName"),
		NU("Nu"),
		PAYPHONE("Payphone"),
		LOVESTORY("LoveStory"),
		NiManWoMan("你瞒我瞒")
	}

	override fun onEnable() {
		super.onEnable()
		when(musicValue) {
			MusicValue.MYNAME -> SoundUtils.playSound(R.raw.myname, overlayManager.ctx)
			MusicValue.NU -> SoundUtils.playSound(R.raw.nu, overlayManager.ctx)
			MusicValue.PAYPHONE -> SoundUtils.playSound(R.raw.payphone, overlayManager.ctx)
			MusicValue.LOVESTORY -> SoundUtils.playSound(R.raw.love_story, overlayManager.ctx)
			MusicValue.NiManWoMan -> SoundUtils.playSound(R.raw.ni_man_wo_man, overlayManager.ctx)
		}
	}
}
