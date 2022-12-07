package org.valkyrienskies.core.apigame.world


interface VSPipeline {
    var arePhysicsRunning: Boolean
    var deleteResources: Boolean
    val isUsingDummyPhysics: Boolean
    val shipWorld: ServerShipWorldCore
    fun preTickGame()
    fun postTickGame()
    fun computePhysTps(): Double
}
