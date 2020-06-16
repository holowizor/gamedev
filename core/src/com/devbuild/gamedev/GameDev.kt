package com.devbuild.gamedev

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
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

class Knight(x: Float, y: Float, stage: Stage) : BaseActor(x, y, stage) {
    val anim1 = textureHelper.loadAnimation("knight_l.png", 16, 32, 1, 9)//loadAnimationFromFiles()
    val anim2 = textureHelper.loadAnimation("knight_r.png", 16, 32, 1, 9)//loadAnimationFromFiles()

    fun anim1() {
        animation = anim1
    }

    fun anim2() {
        animation = anim2
    }

    override fun act(dt: Float) {
        super.act(dt)
        alignCamera()
    }
}

fun Stage.knight(init: Knight.() -> Unit): Knight {
    val html = Knight(0f, 0f, this)
    html.init()
    return html
}

class LevelScreen : Screen {
    val mainStage = Stage()
    //val dungeon = Dungeon(0f, 0f, this.mainStage)
    val world = World(mainStage)
    val knight = mainStage.knight {
        x = 10f
        y = 10f
        width = 16f
        height = 32f
        animation = anim1
        maxSpeed = 200f
        deceleration = 300f
    }

    override fun hide() {
    }

    override fun show() {
    }

    override fun render(dt: Float) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            knight.accelerateAtAngle(180f);
            knight.anim1()
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            knight.accelerateAtAngle(0f);
            knight.anim2()
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            knight.accelerateAtAngle(90f);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            knight.accelerateAtAngle(270f);

        mainStage.act(dt);

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