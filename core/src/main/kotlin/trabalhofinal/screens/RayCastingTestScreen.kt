package trabalhofinal.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import trabalhofinal.HEIGHT
import trabalhofinal.MyGame
import trabalhofinal.WIDTH
import trabalhofinal.components.*
import trabalhofinal.components.general.RayCastComponent
import trabalhofinal.utils.AStar
import trabalhofinal.utils.IVector2
import trabalhofinal.utils.MapReader
import trabalhofinal.utils.RayCaster
import trabalhofinal.utils.graphics.fragmentShader
import trabalhofinal.utils.graphics.vertexShader
import kotlin.math.PI


class RayCastingTestScreen(game: MyGame): CustomScreen(game), InputProcessor {

    private val tiles = mutableListOf<MutableList<Tile>>()
    private val shader = ShaderProgram(vertexShader, fragmentShader)
    private val shipRenderer = ShipRenderer(renderer, viewport.camera, shader, 0.3f)
    private val textures = listOf(
        Texture(Gdx.files.local("assets/wolftex/pics/eagle.png")),
        Texture(Gdx.files.local("assets/wolftex/pics/redbrick.png")),
        Texture(Gdx.files.local("assets/wolftex/pics/purplestone.png")),
        Texture(Gdx.files.local("assets/wolftex/pics/greystone.png")),
        Texture(Gdx.files.local("assets/wolftex/pics/trig.png")),
        Texture(Gdx.files.local("assets/wolftex/pics/mossy.png")),
        Texture(Gdx.files.local("assets/wolftex/pics/wood.png")),
        Texture(Gdx.files.local("assets/wolftex/pics/colorstone.png")),
    )


    private var rayCaster: RayCaster
    private var player: Player
    private var mapWidth = 0
    private var mapHeight = 0
    private var tileWidth = 0f
    private var tileHeight = 0f
    private val components = RayCastCompList()
    private var rayCastIsMinimap = true

    init {
        val reader = MapReader("assets/maps/test.map")
        val mapString = reader.contents().reversed()
        mapWidth = mapString[0].length
        mapHeight = mapString.size

        tileWidth = WIDTH/mapWidth
        tileHeight = HEIGHT/mapHeight

        //cria a matriz de tiles

        for (i in 0 until mapWidth){
            val line = mutableListOf<Tile>()
            for (j in 0 until mapHeight){
                val id = mapString[i][j] - '0'
                val color = when (id){
                    1 -> Color.RED
                    2 -> Color(0f, 40f/255f, 0f, 1f) // Verde mais escuro
                    3 -> Color.PURPLE
                    4 -> Color.WHITE
                    5 -> Color.YELLOW
                    6 -> Color.BLUE
                    7 -> Color.CORAL
                    8 -> Color.MAGENTA
                    else -> Color.BLACK
                }
                line.add(
                    Tile(i, j, tileWidth, tileHeight, if (id == 0) null else Wall(color, textures[id-1]))
                )
            }
            tiles.add(line)
        }

        player = Player(21*tileWidth + tileWidth/2, 4*tileHeight + tileHeight/2, 10f,
            Texture(Gdx.files.local("assets/wolftex/pics/alien.png")),
            tiles[21][4]
        )
        // adiciona os componentes
        run {
            components.add(
                player
            )
            components.add(
                RayCastComponent(
                    Texture(Gdx.files.local("assets/wolftex/pics/alien.png")),
                    tileWidth * 21 + tileWidth / 2, tileHeight * 12 + tileHeight / 2,
                    Color.BROWN, tiles[21][12], ComponentType.ALIEN
                )
            )

            components.add(
                RayCastComponent(
                    Texture(Gdx.files.local("assets/wolftex/pics/barrel-no-bg.png")),
                    tileWidth * 20 + tileWidth / 2, tileHeight * 10 + tileHeight / 2,
                    Color.BROWN, tiles[20][10], ComponentType.EGG
                )
            )

            components.add(
                RayCastComponent(
                    Texture(Gdx.files.local("assets/wolftex/pics/squareweb.png")),
                    tileWidth * 22 + tileWidth / 2, tileHeight * 2 + tileHeight / 2,
                    Color.BROWN, tiles[22][2], ComponentType.DOOR
                )
            )
        }


        rayCaster = RayCaster(tiles, tileWidth, tileHeight)
    }

    override fun show() {
        Gdx.input.inputProcessor = this
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Keys.Q)) Gdx.app.exit()
        if (Gdx.input.isKeyJustPressed(Keys.M)) game.setScreen<MenuScreen>()


//        tempController()

        // sempre fazer o raycast antes de criar as meshes dos componentes!
        rayCaster.multipleRayCast3D(player)
        components.createMeshes(player, rayCaster.zBuffer, tileWidth, tileHeight)
        val playerPos = getTilePos(player.y - player.dir.y*tileHeight/2, player.x - player.dir.x*tileWidth/2)

        player.update(tileWidth, tileHeight, tiles[playerPos.i][playerPos.j])

        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) rayCastIsMinimap = !rayCastIsMinimap
        Gdx.input.isCursorCatched = !rayCastIsMinimap


            if (player.targetComponent.type == ComponentType.DOOR && Gdx.input.isButtonPressed(Buttons.LEFT)){
                player.targetComponent.color = Color.BLACK
                (player.targetComponent.component as RayCastComponent).die()
                components.remove(player.targetComponent.component)
            }

        shipRenderer.renderShip(rayCastIsMinimap, rayCaster, player, tiles, components)

    }
    private fun tempController(){
        val speed = 4
        val theta = (2*PI/180).toFloat()

        if (!rayCastIsMinimap){
            // mouse invisivel
            if (!Gdx.input.isCursorCatched) Gdx.input.isCursorCatched = true
            val deltaX = Gdx.input.deltaX.toFloat()/10
            player.rotate(deltaX* theta)
            Gdx.input.setCursorPosition((WIDTH/2).toInt(), (HEIGHT/2).toInt())

        } else {
            Gdx.input.isCursorCatched = false
        }


        if (Gdx.input.isKeyPressed(Keys.W)) player.y += player.dir.y*speed
        if (Gdx.input.isKeyPressed(Keys.S)) player.y -= player.dir.y*speed

        var playerRect = Rectangle(player.x - player.radius, player.y - player.radius, 2*player.radius, 2*player.radius)
        tiles.forEach{ line ->
            line.forEach { tile ->
                if (tile.isWall && tile.r.overlaps(playerRect)){
                    if (Gdx.input.isKeyPressed(Keys.S) && player.dir.y > 0 ||
                        Gdx.input.isKeyPressed(Keys.W) && player.dir.y < 0)
                        player.y = tile.y + tile.height + player.radius
                    else
                        player.y = tile.y - player.radius
                }
            }
        }

        if (Gdx.input.isKeyPressed(Keys.W)) player.x += player.dir.x*speed
        if (Gdx.input.isKeyPressed(Keys.S)) player.x -= player.dir.x*speed

        playerRect = Rectangle(player.x - player.radius, player.y - player.radius, 2*player.radius, 2*player.radius)

        tiles.forEach{ line ->
            line.forEach { tile ->
                if (tile.isWall && tile.r.overlaps(playerRect)){
                    if (Gdx.input.isKeyPressed(Keys.S) && player.dir.x > 0 ||
                        Gdx.input.isKeyPressed(Keys.W) && player.dir.x < 0)
                        player.x = tile.x + tile.width + player.radius
                    else
                        player.x = tile.x - player.radius
                }
            }
        }
    }

    private fun getTilePos(y: Float, x: Float, ratio:Float = 1f): IVector2 {
        var idx = (x/(tileWidth*ratio)).toInt()
        var idy = (y/(tileHeight*ratio)).toInt()

        if (idx < 0) idx = 0
        else if (idx >= tiles.size) idx = tiles.size - 1

        if (idy < 0) idy = 0
        else if (idy >= tiles[0].size) idy = tiles.size - 1
        return IVector2(idx, idy)
    }

    private fun mouseController(activePlayer: Player?, mouseX: Int, mouseY: Int) {
        if (activePlayer == null || activePlayer.isMoving) return

        val xPos = (WIDTH*0.75f - mouseX)
        val yPos = (HEIGHT - mouseY)

        val dest = getTilePos(yPos, xPos, 0.75f)

        val currTile = tiles[dest.i][dest.j]

        if (currTile.component == null){
            val graph = AStar(tiles)
            player.calculatePath(dest, graph)
        } else
            println("Invalido")

//        else if (currTile.component?.let { it.type == ComponentType.PLAYER } == true){
//            changePlayer(currTile.component as Player)
//        }
//        else{
//            when(currTile.component!!.type){
//                //TODO: implementar clique em jogador -> trocar jogador
//                ComponentType.PLAYER -> TODO()
//                ComponentType.WALL -> TODO()
//                ComponentType.DOOR -> TODO()
//                ComponentType.EGG -> TODO()
//                ComponentType.ALIEN -> TODO()
//                ComponentType.FUNGUS -> TODO()
//            }
//        }
    }

    override fun dispose() {
        shader.dispose()
        textures.forEach { it.dispose() }
    }

    override fun keyDown(keycode: Int): Boolean = true
    override fun keyUp(keycode: Int): Boolean = true
    override fun keyTyped(character: Char): Boolean = true
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (rayCastIsMinimap)
            mouseController(player, screenX, screenY)
        return true
    }
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = true
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = true
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        if (!rayCastIsMinimap) {
            val theta = (2 * PI / 180).toFloat()
            // mouse invisivel
            val deltaX = (screenX - WIDTH/2) / 100

            player.rotate(deltaX * theta)
            Gdx.input.setCursorPosition((WIDTH / 2).toInt(), (HEIGHT / 2).toInt())
        }
        return true
    }
    override fun scrolled(amountX: Float, amountY: Float): Boolean = true

}