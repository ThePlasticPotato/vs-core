package org.valkyrienskies.core.util.serialization

import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector

/**
 * Will ignore fields annotated with specified annotations
 */
internal class IgnoringAnnotationIntrospector(
    /**
     * Fields with these annotations are ignored
     */
    private vararg val annotationsToIgnore: Class<out Annotation>
) : JacksonAnnotationIntrospector() {
    override fun hasIgnoreMarker(m: AnnotatedMember): Boolean {
        return m.hasOneOf(annotationsToIgnore) || super.hasIgnoreMarker(m)
    }
}
