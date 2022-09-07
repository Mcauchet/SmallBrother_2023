package com.projet.sluca.smallbrother.libs

interface AccelerometerListener {
    fun onAccelerationChanged(x: Float, y: Float, z: Float)
    fun onShake(force: Float)
}