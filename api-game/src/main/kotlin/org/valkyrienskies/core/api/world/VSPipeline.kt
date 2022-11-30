package org.valkyrienskies.core.api.world


interface VSPipeline {
    var arePhysicsRunning: Boolean
    var deleteResources: Boolean
    val isUsingDummyPhysics: Boolean
    val shipWorld: ServerShipWorldCore
    fun preTickGame()
    fun postTickGame()
    fun computePhysTps(): Double
}
