package dev.sora.protohax.ui.overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.hardware.input.InputManager
import android.net.VpnService
import android.os.Build
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import dev.sora.protohax.MyApplication
import dev.sora.protohax.R
import dev.sora.protohax.relay.MinecraftRelay
import dev.sora.protohax.relay.service.ServiceListener
import dev.sora.protohax.ui.components.screen.settings.Settings
import dev.sora.protohax.ui.overlay.menu.ConfigureMenu
import dev.sora.relay.cheat.module.CheatModule
import kotlin.math.abs

class OverlayManager : ServiceListener {

	var currentContext: Context? = null

	val ctx: Context
		get() = currentContext!!

	private var entranceView: View? = null
	private var renderLayerView: View? = null

	private val menu = ConfigureMenu(this)
	val shortcuts = mutableListOf<Shortcut>()

	@SuppressLint("ClickableViewAccessibility")
	override fun onServiceStarted() {
		val wm = MyApplication.instance.getSystemService(VpnService.WINDOW_SERVICE) as WindowManager
		val params = WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.TRANSLUCENT
		)
		params.gravity = Gravity.TOP or Gravity.START
		params.x = 20
		params.y = 100

		val imageView = ImageView(ctx)

		imageView.setBackgroundResource(R.drawable.ic_launcher_r)
		imageView.setImageResource(R.drawable.ic_launch_rou)

		/*ResourcesCompat.getDrawable(
			ctx.resources, R.mipmap.ic_launcher, ctx.theme
		)?.let { drawable ->
			val bitmap = Bitmap.createBitmap(
				(drawable.intrinsicWidth * 0.7).toInt(), (drawable.intrinsicHeight * 0.7).toInt(),
				Bitmap.Config.ARGB_8888
			)
			val canvas = Canvas(bitmap)
			drawable.setBounds(0, 0, canvas.width, canvas.height)
			drawable.draw(canvas)
			imageView.setImageBitmap(bitmap)
		} ?: imageView.setImageResource(R.drawable.notification_icon)

		 */
		imageView.setOnClickListener {
			MinecraftRelay.updateChineseNew()
			menu.visibility = !menu.visibility
		}

		imageView.draggable(params, wm)

		this.entranceView = imageView
		wm.addView(imageView, params)

		startRenderLayer(wm)
		menu.visibility = false
		menu.display(wm, ctx)

		shortcuts.forEach {
			it.display(wm)
		}
	}

	private fun startRenderLayer(windowManager: WindowManager) {
		val params = WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT
		)
		// this will fix https://developer.android.com/about/versions/12/behavior-changes-all#untrusted-touch-events
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !Settings.trustClicks.getValue(ctx)) {
			params.alpha = (ctx.getSystemService(Service.INPUT_SERVICE) as? InputManager)?.maximumObscuringOpacityForTouch ?: 0.8f
		}
		params.gravity = Gravity.TOP or Gravity.END
		val layout = RelativeLayout(ctx)
		layout.addView(
			RenderLayerView(ctx, MinecraftRelay.session),
			ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT))

		renderLayerView = layout
		windowManager.addView(layout, params)
	}

	fun toggleRenderLayerViewVisibility(state: Boolean) {
		val view = renderLayerView ?: return
		if (state != !view.isInvisible) { // value changed
			view.isInvisible = !state
		}
	}

	override fun onServiceStopped() {
		val wm = MyApplication.instance.getSystemService(VpnService.WINDOW_SERVICE) as WindowManager
		entranceView?.let { wm.removeView(it) }
		entranceView = null
		renderLayerView?.let { wm.removeView(it) }
		renderLayerView = null
		menu.destroy(wm)
		shortcuts.forEach {
			it.remove(wm)
		}
	}

	fun hasShortcut(module: CheatModule): Boolean {
		return shortcuts.any { it.module == module }
	}

	fun removeShortcut(module: CheatModule): Boolean {
		return shortcuts.removeIf { (it.module == module).also { v ->
			if (v) {
				it.remove(MyApplication.instance.getSystemService(VpnService.WINDOW_SERVICE) as WindowManager)
			}
		} }
	}

	fun addShortcut(shortcut: Shortcut) {
		if (hasShortcut(shortcut.module)) {
			throw IllegalStateException("Shortcut already exists for module: ${shortcut.module.name}")
		}

		shortcuts.add(shortcut)

		if (renderLayerView != null) {
			shortcut.display(MyApplication.instance.getSystemService(VpnService.WINDOW_SERVICE) as WindowManager)
		}
	}

	fun View.draggable(params: WindowManager.LayoutParams, windowManager: WindowManager) {
		var dragPosX = 0f
		var dragPosY = 0f
		var canDrag = false
		var pressDownTime = System.currentTimeMillis()
		setOnTouchListener { v, event ->
			when (event.action) {
				MotionEvent.ACTION_DOWN -> {
					dragPosX = event.rawX
					dragPosY = event.rawY
					pressDownTime = System.currentTimeMillis()
					canDrag = true
					true
				}
				MotionEvent.ACTION_UP -> {
					if (System.currentTimeMillis() - pressDownTime < 500) {
						v.performClick()
					}
					true
				}
				MotionEvent.ACTION_MOVE -> {
					if (!canDrag) {
						return@setOnTouchListener false
					}
					if (System.currentTimeMillis() - pressDownTime < 500) {
						if (abs(dragPosX - event.rawX) > 100 || abs(dragPosY - event.rawY) > 100) {
							canDrag = false
						}
						false
					} else {
						params.x += (event.rawX - dragPosX).toInt()
						params.y += (event.rawY - dragPosY).toInt()
						dragPosX = event.rawX
						dragPosY = event.rawY
						windowManager.updateViewLayout(this, params)
						true
					}
				}
				else -> false
			}
		}
	}
}
