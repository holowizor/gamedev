package com.devbuild.gamedev

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage


object worldBounds {
    var width: Float = 0f;
    var height: Float = 0f;
}

class Dungeon : Actor {
    constructor(x: Float, y: Float, s: Stage) {
        this.x = x
        this.y = y
        s.addActor(this)

        this.width = 3500f
        this.height = 4000f

        worldBounds.width = this.width
        worldBounds.height = this.height
    }

    var texture = TextureRegion(Texture(Gdx.files.internal("sample-bg.jpg")))

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        batch.draw(texture,
                x, y, originX, originY,
                width, height, scaleX, scaleY, rotation)
    }
}

class Knight : Actor {
    constructor(x: Float, y: Float, s: Stage) {
        this.x = x
        this.y = y
        s.addActor(this)

        this.width = 16f
        this.height = 32f
    }

    val texture = TextureRegion(Texture(Gdx.files.internal("frames/knight_f_idle_anim_f0.png")))
    var animation = loadAnimationFromSheet("knight_l.png", 16, 32, 1, 9)//loadAnimationFromFiles()
    var elapsedTime = 0f

    val anim1 = loadAnimationFromSheet("knight_l.png", 16, 32, 1, 9)//loadAnimationFromFiles()
    val anim2 = loadAnimationFromSheet("knight_r.png", 16, 32, 1, 9)//loadAnimationFromFiles()

    private val velocityVec: Vector2 = Vector2(0f,0f)
    private val accelerationVec: Vector2 = Vector2(0f,0f)
    private var acceleration = 400f
    private var maxSpeed = 100f
    private var deceleration = 400f

    fun anim1() {animation = anim1}
    fun anim2() {animation = anim2}

    private fun loadAnimationFromFiles(): Animation<TextureRegion> {
        val fileNames = arrayOf("frames/knight_f_idle_anim_f0.png", "frames/knight_f_idle_anim_f1.png", "frames/knight_f_idle_anim_f2.png", "frames/knight_f_idle_anim_f3.png")
        val fileCount: Int = fileNames.size
        val textureArray = com.badlogic.gdx.utils.Array<TextureRegion>()

        for (n in 0 until fileCount) {
            val fileName: String = fileNames.get(n)
            val texture = Texture(Gdx.files.internal(fileName))
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
            textureArray.add(TextureRegion(texture))
        }


        val anim = Animation<TextureRegion>(0.1f, textureArray)
        anim.playMode = Animation.PlayMode.LOOP

        return anim
    }

    fun loadAnimationFromSheet(fileName: String, frameWidth: Int, frameHeight: Int, startCol: Int, endCol: Int, frameDuration: Float = .1f, loop: Boolean = true): Animation<TextureRegion> {
        val texture = Texture(Gdx.files.internal(fileName), true)
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
        val temp = TextureRegion.split(texture, frameWidth, frameHeight)
        val textureArray = com.badlogic.gdx.utils.Array<TextureRegion>()
        for (c in startCol until endCol) textureArray.add(temp[0][c])
        val anim = Animation<TextureRegion>(frameDuration, textureArray)
        if (loop) anim.setPlayMode(Animation.PlayMode.LOOP) else anim.setPlayMode(Animation.PlayMode.NORMAL)
        //if (animation == null) setAnimation(anim)
        return anim
    }

    fun alignCamera() {
        val cam: Camera = stage.camera

//        // center camera on actor
//        cam.position.set(x + originX, y + originY, 0f)

        val bW = cam.viewportWidth / 4
        val bH = cam.viewportHeight / 4

        val camx = cam.position.x
        val camy = cam.position.y

        if (x > camx && x - camx > bW) {
            cam.position.x = x + originX - bW
        }
        if (x < camx &&  camx - x > bW) {
            cam.position.x = x + originX + bW
        }

        if (y > camy && y - camy > bH) {
            cam.position.y = y + originY - bH
        }
        if (y < camy &&  camy - y  > bH) {
            cam.position.y = y + originY + bH
        }

        // bound camera to layout
        cam.position.x = MathUtils.clamp(cam.position.x,
                cam.viewportWidth / 2, worldBounds.width - cam.viewportWidth / 2)
        cam.position.y = MathUtils.clamp(cam.position.y,
                cam.viewportHeight / 2, worldBounds.height - cam.viewportHeight / 2)
        cam.update()
    }

    override fun act(dt: Float) {
        super.act(dt)

        elapsedTime += dt;

        alignCamera()
        applyPhysics(dt)
    }

    fun accelerateAtAngle(angle: Float) {
        accelerationVec.add(
                Vector2(acceleration, 0f).setAngle(angle))
    }

    fun applyPhysics(dt: Float) {
        // apply acceleration
        velocityVec.add(accelerationVec.x * dt, accelerationVec.y * dt)
        var speed: Float = getSpeed()

        // decrease speed (decelerate) when not accelerating
        if (accelerationVec.len() == 0f) speed -= deceleration * dt

        // keep speed within set bounds
        speed = MathUtils.clamp(speed, 0f, maxSpeed)

        // update velocity
        setSpeed(speed)

        // update position according to value stored in velocity vector
        moveBy(velocityVec.x * dt, velocityVec.y * dt)

        // reset acceleration
        accelerationVec[0f] = 0f
    }

    fun setSpeed(speed: Float) {
        if (velocityVec.len() == 0f) velocityVec[speed] = 0f else velocityVec.setLength(speed)
    }

    fun getSpeed(): Float {
        return velocityVec.len()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {

        batch.draw( animation.getKeyFrame(elapsedTime),
                x, y, originX, originY,
                width, height, scaleX, scaleY, rotation );

        super.draw(batch, parentAlpha)

//        batch.draw(texture,
//                x, y, originX, originY,
//                width, height, scaleX, scaleY, rotation)
    }
}

class LevelScreen : Screen {
    val mainStage = Stage()
    val dungeon = Dungeon(0f, 0f, this.mainStage)
    val knight = Knight(10f, 10f, this.mainStage)

    override fun hide() {
    }

    override fun show() {
    }

    override fun render(dt: Float) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            knight.accelerateAtAngle(180f);
            knight.anim1()
            //animation = anim1
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            knight.accelerateAtAngle(0f);
            knight.anim2()
            //animation = anim2
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            knight.accelerateAtAngle(90f);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            knight.accelerateAtAngle(270f);


        mainStage.act(dt);
        // update(dt);

        // clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw the graphics
        mainStage.draw();
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun dispose() {
    }
}

class GameDev : Game() {

    override fun create() {
        screen = LevelScreen()
    }

    override fun dispose() {
        screen.dispose()
    }
}