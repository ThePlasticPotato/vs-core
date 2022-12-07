package org.valkyrienskies.core.impl.util.serialization

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE
import com.fasterxml.jackson.databind.module.SimpleModule
import org.joml.*
import org.joml.primitives.*

class JOMLSerializationModule : SimpleModule() {

    @JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
    private object JOMLMixin

    init {
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
    }

    private inline fun <reified A, reified B : A> setupJOMLClass() {
        addAbstractTypeMapping<A, B>()
        setMixInAnnotation<B, JOMLMixin>()
    }
}
