package dev.sora.protohax.relay.modules

import android.graphics.*
import dev.sora.protohax.relay.service.AppService
import dev.sora.protohax.ui.overlay.RenderLayerView
import dev.sora.protohax.ui.render.EntityESP
import dev.sora.relay.cheat.module.CheatCategory
import dev.sora.relay.cheat.module.CheatModule
import dev.sora.relay.cheat.module.impl.combat.ModuleAntiBot.isBot
import dev.sora.relay.cheat.module.impl.combat.ModuleTeams.isTeammate
import dev.sora.relay.cheat.value.NamedChoice
import dev.sora.relay.game.entity.Entity
import dev.sora.relay.game.entity.EntityPlayer
import org.cloudburstmc.math.matrix.Matrix4f
import org.cloudburstmc.math.vector.Vector2d
import kotlin.math.cos
import kotlin.math.sin


class ModuleESP : CheatModule("ESP","实体绘制",CheatCategory.VISUAL) {

    private val modeValue by listValue("Mode",ESPMode.values(), ESPMode.BOX)
	private val fovValue by intValue("Fov", 110, 40..110)
	private val colorRedValue by intValue("ColorRed", 61, 0..255)
	private val colorBlueValue by intValue("ColorBlue", 220, 0..255)
	private val strokeWidthValue by intValue("StrokeWidth", 2, 1..10)
	private val colorGreenValue by intValue("ColorGreen", 154, 0..255)
	private val botsValue by boolValue("Bots", false)
	private val rainbowValue by boolValue("Rainbow", true)
	private val allObjectsValue by boolValue("AllObjects", false)
	private val avoidScreenValue by boolValue("AvoidScreen", true)
	private val originalSizeValue by boolValue("OriginalSize", true)
    private val movePredictionValue by boolValue("MovePrediction", false)

    override fun onEnable() {
        session.eventManager.emit(RenderLayerView.EventRefreshRender(session))
        displayList.clear()
    }

    var displayList = HashMap<Entity, EntityESP>()

	private val handleRender = handle<RenderLayerView.EventRender> { event ->
        event.needRefresh = true
        if (avoidScreenValue && event.session.thePlayer.openContainer != null) return@handle
        val map = event.session.theWorld.entityMap.values
            .let {
				if (allObjectsValue) it else
					it.filter { e -> e is EntityPlayer && (botsValue || (!e.isBot() && !e.isTeammate()) )} }

        if (map.isEmpty()) return@handle

        val player = event.session.thePlayer
        val canvas = event.canvas
        val realSize = Point()
        (event.context as AppService).windowManager.defaultDisplay.getRealSize(realSize)
        val screenWidth = if(originalSizeValue) realSize.x else canvas.width
        val screenHeight = if(originalSizeValue) realSize.y else canvas.height

        val viewProjMatrix =  Matrix4f.createPerspective(fovValue.toFloat()+10, screenWidth.toFloat() / screenHeight, 0.1f, 128f)
            .mul(Matrix4f.createTranslation(player.vec3Position)
                .mul(rotY(-player.rotationYaw-180))
                .mul(rotX(-player.rotationPitch))
                .invert())
//
        val paint = Paint()
        paint.strokeWidth = strokeWidthValue.toFloat()
        val colors = dev.sora.protohax.util.ColorUtils.getChromaRainbow(100.0, 10.0)
        paint.color = if(rainbowValue) Color.rgb(colors.r, colors.g, colors.b) else Color.rgb( colorRedValue, colorGreenValue, colorBlueValue)

        map.forEach {
            drawEntityBox(it, viewProjMatrix, screenWidth, screenHeight, canvas, paint)
        }

    }

    private fun drawEntityBox(entity: Entity, viewProjMatrix: Matrix4f, screenWidth: Int, screenHeight: Int, canvas: Canvas, paint: Paint) {
        if(displayList[entity]==null){
            displayList[entity]=EntityESP()
        }
        displayList[entity]!!.draw(entity,viewProjMatrix,screenWidth,screenHeight,canvas,paint,modeValue.choiceName,movePredictionValue,this)
    }

    fun worldToScreen(posX: Double, posY: Double, posZ: Double, viewProjMatrix: Matrix4f, screenWidth: Int, screenHeight: Int): Vector2d? {
        val w = viewProjMatrix.get(3, 0) * posX +
                viewProjMatrix.get(3, 1) * posY +
                viewProjMatrix.get(3, 2) * posZ +
                viewProjMatrix.get(3, 3)
        if (w < 0.01f) return null
        val inverseW = 1 / w

        val screenX = screenWidth / 2f + (0.5f * ((viewProjMatrix.get(0, 0) * posX + viewProjMatrix.get(0, 1) * posY +
                viewProjMatrix.get(0, 2) * posZ + viewProjMatrix.get(0, 3)) * inverseW) * screenWidth + 0.5f)
        val screenY = screenHeight / 2f - (0.5f * ((viewProjMatrix.get(1, 0) * posX + viewProjMatrix.get(1, 1) * posY +
                viewProjMatrix.get(1, 2) * posZ + viewProjMatrix.get(1, 3)) * inverseW) * screenHeight + 0.5f)
        return Vector2d.from(screenX, screenY)
    }

    private fun rotX(angle: Float): Matrix4f {
        val rad = Math.toRadians(angle.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()

        return Matrix4f.from(1f, 0f, 0f, 0f,
            0f, c, -s, 0f,
            0f, s, c, 0f,
            0f, 0f, 0f, 1f)
    }

    private fun rotY(angle: Float): Matrix4f {
        val rad = Math.toRadians(angle.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()

        return Matrix4f.from(c, 0f, s, 0f,
            0f, 1f, 0f, 0f,
            -s, 0f, c, 0f,
            0f, 0f, 0f, 1f)
    }

	private enum class ESPMode(override val choiceName: String) : NamedChoice {
		BOX("Box"),
		ROUNDBOX("RoundBox"),
		TWOD("2D"),
		GLOW("Glow")
	}
}
