package com.devbuild.gamedev

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage

abstract class BaseActor : Actor {
    constructor(x: Float, y: Float, s: Stage) {
        this.x = x
        this.y = y
        s.addActor(this)
    }

    lateinit var animation: Animation<TextureRegion>

    private var elapsedTime = 0f
    private val velocityVec: Vector2 = Vector2(0f, 0f)
    private val accelerationVec: Vector2 = Vector2(0f, 0f)
    private var acceleration = 400f
    var maxSpeed = 100f
    var deceleration = 400f

    fun alignCamera() {
        val cam: Camera = stage.camera

        val bW = cam.viewportWidth / 4
        val bH = cam.viewportHeight / 4

        val camx = cam.position.x
        val camy = cam.position.y

        if (x > camx && x - camx > bW) {
            cam.position.x = x + originX - bW
        }
        if (x < camx && camx - x > bW) {
            cam.position.x = x + originX + bW
        }

        if (y > camy && y - camy > bH) {
            cam.position.y = y + originY - bH
        }
        if (y < camy && camy - y > bH) {
            cam.position.y = y + originY + bH
        }

        // bound camera to layout
        cam.position.x = MathUtils.clamp(cam.position.x, cam.viewportWidth / 2, worldBounds.width - cam.viewportWidth / 2)
        cam.position.y = MathUtils.clamp(cam.position.y, cam.viewportHeight / 2, worldBounds.height - cam.viewportHeight / 2)
        cam.update()
    }

    fun accelerateAtAngle(angle: Float) {
        accelerationVec.add(Vector2(acceleration, 0f).setAngle(angle))
    }

    private fun setSpeed(speed: Float) {
        if (velocityVec.len() == 0f) velocityVec.set(speed, 0f) else velocityVec.setLength(speed)
    }

    private fun applyPhysics(dt: Float) {
        // apply acceleration
        velocityVec.add(accelerationVec.x * dt, accelerationVec.y * dt)
        var speed: Float = velocityVec.len()

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

    override fun act(dt: Float) {
        super.act(dt)
        elapsedTime += dt;
        applyPhysics(dt)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(animation.getKeyFrame(elapsedTime),
                x, y, originX, originY,
                width, height, scaleX, scaleY, rotation);
        super.draw(batch, parentAlpha)
    }
}
