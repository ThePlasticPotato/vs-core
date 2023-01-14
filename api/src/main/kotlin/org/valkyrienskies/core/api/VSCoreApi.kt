package org.valkyrienskies.core.api

import org.joml.Vector3dc
import org.valkyrienskies.core.api.attachment.AttachmentSerializationStrategy
import org.valkyrienskies.core.api.bodies.properties.BodyCollisionShape

interface VSCoreApi {

    fun createSphereCollisionShape(radius: Double): BodyCollisionShape.Sphere

    fun createBoxCollisionShape(lengths: Vector3dc): BodyCollisionShape.Box

    fun createWheelCollisionShape(radius: Double, halfThickness: Double): BodyCollisionShape.Wheel

    fun createCapsuleCollisionShape(radius: Double, halfLength: Double): BodyCollisionShape.Capsule

    fun <T> registerAttachmentSerializationStrategy(name: String, strategy: Class<out AttachmentSerializationStrategy>)

    fun registerAttachments(vararg classes: Class<*>)

    fun registerAttachmentsInPackage(packageName: String)

}