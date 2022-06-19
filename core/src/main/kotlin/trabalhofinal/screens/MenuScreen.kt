package trabalhofinal.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ktx.graphics.use
import trabalhofinal.HEIGHT
import trabalhofinal.MyGame
import trabalhofinal.WIDTH
import trabalhofinal.utils.Button

class MenuScreen(game: MyGame):CustomScreen(game), InputProcessor {

    private val buttons = listOf(
        Button(
            "gridScreen",
            WIDTH/2,
            HEIGHT/2,
            500f, 100f,
            {game.setScreen<GridScreen>()}
        ),
        Button(
            "rayCastScreen",
            WIDTH/2,
            HEIGHT/2 - 150,
            500f, 100f,
            {game.setScreen<RayCastingTestScreen>()}
        )
    )

    override fun show() {
        Gdx.input.inputProcessor = this
    }

    override fun render(delta: Float) {
        Gdx.input.isCursorCatched = false
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            buttons.forEach { it.drawRect(renderer) }
        }
        batch.use {
            font.draw(batch, "MENU", WIDTH/2 - 100f, HEIGHT/2 + 200f)
            buttons.forEach { it.drawMessage(batch, font) }
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button != Buttons.LEFT) return true
        val mouse = Vector2(screenX.toFloat(), HEIGHT - screenY.toFloat())
        buttons.forEach {
            if (it.contains(mouse)){
                it.onClick()
            }
        }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button != Buttons.LEFT) return true
        val mouse = Vector2(screenX.toFloat(), HEIGHT - screenY.toFloat())
        buttons.forEach {
            if (it.contains(mouse)){
                it.onRelease()
            }
        }
        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val mouse = Vector2(screenX.toFloat(), HEIGHT - screenY.toFloat())
        buttons.forEach { button ->
            if (button.contains(mouse)){
                button.onHover()
            } else {
                button.resetColor()
            }
        }
        return true
    }

    override fun keyDown(keycode: Int): Boolean = true
    override fun keyUp(keycode: Int): Boolean = true
    override fun keyTyped(character: Char): Boolean = true
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = true
    override fun scrolled(amountX: Float, amountY: Float): Boolean = true
}