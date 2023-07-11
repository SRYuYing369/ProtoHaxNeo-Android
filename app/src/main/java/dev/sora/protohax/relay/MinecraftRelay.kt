package dev.sora.protohax.relay

import kotlin.concurrent.thread
import java.net.InetSocketAddress
import dev.sora.relay.utils.logInfo
import io.netty.channel.ServerChannel
import dev.sora.protohax.MyApplication
import dev.sora.relay.game.GameSession
import io.netty.channel.ChannelFactory
import dev.sora.relay.MinecraftRelayListener
import dev.sora.protohax.relay.modules.ModuleESP
import dev.sora.relay.cheat.module.ModuleManager
import dev.sora.protohax.relay.modules.ModuleMusic
import dev.sora.relay.cheat.command.CommandManager
import dev.sora.relay.session.MinecraftRelaySession
import dev.sora.protohax.relay.modules.ModuleNameTags
import dev.sora.protohax.ui.overlay.ConfigSectionShortcut
import dev.sora.relay.cheat.config.ConfigManagerFileSystem
import org.cloudburstmc.netty.channel.raknet.RakReliability
import dev.sora.protohax.relay.netty.channel.NativeRakConfig
import dev.sora.relay.cheat.command.impl.CommandDownloadWorld
import dev.sora.relay.session.listener.RelayListenerAutoCodec
import dev.sora.relay.cheat.config.section.ConfigSectionModule
import dev.sora.protohax.ui.components.screen.settings.Settings
import dev.sora.relay.session.listener.xbox.RelayListenerXboxLogin
import dev.sora.relay.session.listener.RelayListenerNetworkSettings
import dev.sora.protohax.relay.netty.channel.NativeRakServerChannel
import dev.sora.relay.session.listener.RelayListenerEncryptedSession
import dev.sora.relay.cheat.module.impl.misc.ModuleResourcePackSpoof

object MinecraftRelay {

    private var relay: Relay? = null

    val session = GameSession()
	var chineseMode = false
    val moduleManager: ModuleManager
    val configManager: ConfigManagerFileSystem

	var loaderThread: Thread? = null

    init {
        moduleManager = ModuleManager(session)

		// load asynchronously
		loaderThread = thread {
			moduleManager.init()
			registerAdditionalModules(moduleManager)
			MyApplication.instance.getExternalFilesDir("resource_packs")?.also {
				if (!it.exists()) it.mkdirs()
				ModuleResourcePackSpoof.resourcePackProvider = ModuleResourcePackSpoof.FileSystemResourcePackProvider(it)
			}

			if (Settings.enableCommandManager.getValue(MyApplication.instance)) {
				// command manager will register listener itself
				val commandManager = CommandManager(session)
				commandManager.init(moduleManager)
				MyApplication.instance.getExternalFilesDir("downloaded_worlds")?.also {
					commandManager.registerCommand(CommandDownloadWorld(session.eventManager, it))
				}
			}

			// clean-up
			loaderThread = null
		}

        configManager = ConfigManagerFileSystem(MyApplication.instance.getExternalFilesDir("configs")!!, ".json").also {
			it.addSection(ConfigSectionModule(moduleManager))
			it.addSection(ConfigSectionShortcut(MyApplication.overlayManager))
		}
    }

    private fun registerAdditionalModules(moduleManager: ModuleManager) {
		moduleManager.registerModule(ModuleESP())
		moduleManager.registerModule(ModuleMusic())
		moduleManager.registerModule(ModuleNameTags())
	}

    private fun constructRelay(): Relay {
        var sessionEncryptor: RelayListenerEncryptedSession? = null
        return Relay(object : MinecraftRelayListener {
            override fun onSessionCreation(session: MinecraftRelaySession): InetSocketAddress {
                // add listeners
                session.listeners.add(RelayListenerNetworkSettings(session))
                session.listeners.add(RelayListenerAutoCodec(session))
                this@MinecraftRelay.session.netSession = session
                session.listeners.add(this@MinecraftRelay.session)
                if (sessionEncryptor == null) {
                    sessionEncryptor = AccountManager.currentAccount?.let {
                        val accessToken = it.refresh()
                        logInfo("logged in as ${it.remark}")
                        RelayListenerXboxLogin(accessToken, it.platform)
                    }
                } else if (Settings.offlineSessionEncryption.getValue(MyApplication.instance)) {
					sessionEncryptor = RelayListenerEncryptedSession()
				}
                sessionEncryptor?.let {
                    it.session = session
                    session.listeners.add(it)
                }

                // resolve original ip and pass to relay client
                val address = session.peer.channel.config().getOption(NativeRakConfig.RAK_NATIVE_TARGET_ADDRESS)
                logInfo("SessionCreation $address")
				return address
            }
        })
    }

	fun updateReliability() {
		relay?.optionReliability = if (Settings.enableRakReliability.getValue(MyApplication.instance))
			RakReliability.RELIABLE_ORDERED else RakReliability.RELIABLE
	}

	fun updateChinese() {
		chineseMode = !Settings.enableChinese.getValue(MyApplication.instance)
	}

	fun updateChineseNew() {
		chineseMode = Settings.enableChinese.getValue(MyApplication.instance)
	}

	fun announceRelayUp() {
		if (relay == null) {
			relay = constructRelay()
			updateReliability()
		}
		loaderThread?.join()
		if (!relay!!.isRunning) {
			relay!!.bind(InetSocketAddress("0.0.0.0", 1337))
			logInfo("relay started")
		}
	}

	class Relay(listener: MinecraftRelayListener) : dev.sora.relay.MinecraftRelay(listener) {

		override fun channelFactory(): ChannelFactory<out ServerChannel> {
			return ChannelFactory {
				NativeRakServerChannel()
			}
		}
	}
}
