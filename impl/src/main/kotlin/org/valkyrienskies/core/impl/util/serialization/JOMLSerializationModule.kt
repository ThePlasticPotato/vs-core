package org.valkyrienskies.core.impl.util.serialization

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE
import com.fasterxml.jackson.databind.module.SimpleModule
import org.joml.Matrix3d
import org.joml.Matrix3dc
import org.joml.Matrix3f
import org.joml.Matrix3fc
import org.joml.Matrix4d
import org.joml.Matrix4dc
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector2d
import org.joml.Vector2dc
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import org.joml.Vector3ic
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBf
import org.joml.primitives.AABBfc
import org.joml.primitives.AABBi
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl

class JOMLSerializationModule : SimpleModule() {

    @JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
    private object JOMLMixin

    init {
        setupJOMLClass<Vector2ic, Vector2i>()
        setupJOMLClass<Vector2fc, Vector2f>()
        setupJOMLClass<Vector2dc, Vector2d>()

        setupJOMLClass<Vector3ic, Vector3i>()
        setupJOMLClass<Vector3fc, Vector3f>()
        setupJOMLClass<Vector3dc, Vector3d>()

        setupJOMLClass<Quaternionfc, Quaternionf>()
        setupJOMLClass<Quaterniondc, Quaterniond>()

        setupJOMLClass<Matrix4fc, Matrix4f>()
        setupJOMLClass<Matrix4dc, Matrix4d>()

        setupJOMLClass<Matrix3fc, Matrix3f>()
        setupJOMLClass<Matrix3dc, Matrix3d>()

        setupJOMLClass<AABBic, AABBi>()
        setupJOMLClass<AABBfc, AABBf>()
        setupJOMLClass<AABBdc, AABBd>()

        setupJOMLClass<ShipInertiaData, ShipInertiaDataImpl>()
    }

    private inline fun <reified A, reified B : A> setupJOMLClass() {
        addAbstractTypeMapping<A, B>()
        setMixInAnnotation<B, JOMLMixin>()
    }
}
