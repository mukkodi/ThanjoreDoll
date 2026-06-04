package com.thanjavur.wobbledoll

import kotlin.math.abs
import javax.inject.Inject

/**
 * Enhanced physics engine for the Thanjavur doll.
 * Each part (Head, Torso, Base) acts as an independent 2D damped pendulum
 * to simulate a "conical pendulum" motion (elliptical/circular wobble).
 */
class WobblePhysics @Inject constructor() {

    class PartState(
        var stiffness: Float,
        var damping: Float,
        var inertia: Float
    ) {
        // X-axis movement (Left-Right)
        var angleX: Float = 0f
        var velocityX: Float = 0f
        
        // Y-axis movement (Forward-Back / Vertical depth simulation)
        var angleY: Float = 0f
        var velocityY: Float = 0f

        fun update(timeStep: Float) {
            // Update X
            val accX = (-stiffness * angleX - damping * velocityX)
            velocityX += accX * timeStep
            angleX += velocityX * timeStep

            // Update Y
            val accY = (-stiffness * angleY - damping * velocityY)
            velocityY += accY * timeStep
            angleY += velocityY * timeStep

            // Clamp X
            if (angleX > 35f) { angleX = 35f; velocityX = -velocityX * 0.2f }
            if (angleX < -35f) { angleX = -35f; velocityX = -velocityX * 0.2f }
            
            // Clamp Y
            if (angleY > 20f) { angleY = 20f; velocityY = -velocityY * 0.2f }
            if (angleY < -20f) { angleY = -20f; velocityY = -velocityY * 0.2f }
        }

        fun applyImpulse(forceX: Float, forceY: Float) {
            velocityX += forceX / inertia
            velocityY += forceY / inertia
        }

        fun isAtRest(): Boolean = 
            abs(angleX) < 0.1f && abs(velocityX) < 0.1f && 
            abs(angleY) < 0.1f && abs(velocityY) < 0.1f
    }

    // Individual states for each part
    val head = PartState(stiffness = 5.5f, damping = 0.10f, inertia = 1.1f)
    val torso = PartState(stiffness = 2.5f, damping = 0.35f, inertia = 1.0f)
    val base = PartState(stiffness = 2.8f, damping = 0.25f, inertia = 1.5f)

    /**
     * Applies impulse to a specific part only.
     */
    fun applyHeadImpulse(forceX: Float) {
        head.applyImpulse(forceX * 0.5f, abs(forceX) * 0.3f)
    }

    fun applyTorsoImpulse(forceX: Float) {
        torso.applyImpulse(forceX, abs(forceX) * 0.4f)
    }

    fun applyBaseImpulse(forceX: Float) {
        base.applyImpulse(forceX, abs(forceX) * 0.6f)
    }
    
    fun applyGlobalImpulse(forceX: Float) {
        head.applyImpulse(forceX, abs(forceX) * 0.4f)
        torso.applyImpulse(forceX, abs(forceX) * 0.4f)
        base.applyImpulse(forceX, abs(forceX) * 0.4f)
    }

    fun update() {
        val dt = 0.016f
        head.update(dt)
        torso.update(dt)
        base.update(dt)
    }

    fun isAtRest(): Boolean = head.isAtRest() && torso.isAtRest() && base.isAtRest()
}
