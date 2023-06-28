package org.valkyrienskies.core.impl.datastructures.dynconn.dep;

/**
 * Wraps a value using reference equality.  In other words, two references are equal only if their values are the same
 * object instance, as in ==.
 * @param <T> The type of value.
 */
class NodeReference<T> {
    /** The value this wraps. */
    private final T value;

    public NodeReference(T value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NodeReference)) {
            return false;
        }
        NodeReference<?> reference = (NodeReference<?>)obj;
        return value == reference.value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }
}
