package org.valkyrienskies.core.datastructures;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.util.Iterator;
import javax.annotation.Nonnull;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.valkyrienskies.core.api.util.functions.IntTernaryConsumer;

/**
 * An implementation of IBlockPosSet that stores block positions as 1 integer. This is accomplished by storing each
 * position as its relative coordinates to the centerX and centerZ values of this set. In this implementation the x and
 * z positions are 12 bits each, so they can range anywhere from -2048 to + 2047 relative to centerX and centerZ. This
 * leaves 8 bits for storing the y coordinate, which allows it the range of 0 to 255, exactly the same as Minecraft.
 */
@JsonDeserialize(using = SmallBlockPosSet.SmallBlockPosSetDeserializer.class)
@JsonSerialize(using = SmallBlockPosSet.SmallBlockPosSetSerializer.class)
public class SmallBlockPosSet implements IBlockPosSet {

    private static final int BOT_12_BITS = 0x00000FFF;
    private static final int BOT_8_BITS = 0x000000FF;

    @Nonnull
    private final IntList compressedBlockPosList;
    @Nonnull
    private final Int2IntMap listValueToIndex;
    private final int centerX;
    private final int centerY;
    private final int centerZ;

    public SmallBlockPosSet(final int centerX, final int centerY, final int centerZ) {
        this.compressedBlockPosList = new IntArrayList();
        this.listValueToIndex = new Int2IntOpenHashMap();
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
    }

    @Override
    public boolean add(final int x, final int y, final int z) throws IllegalArgumentException {
        if (!canStore(x, y, z)) {
            throw new IllegalArgumentException("Cannot store block position at <" + x + "," + y + "," + z +
                "> into SmallBlockPosSet centered at <" + centerX + "," + centerY + "," + centerZ + ">");
        }
        final int compressedPos = compress(x, y, z);
        if (listValueToIndex.containsKey(compressedPos)) {
            return false;
        }
        compressedBlockPosList.add(compressedPos);
        listValueToIndex.put(compressedPos, compressedBlockPosList.size() - 1);
        return true;
    }

    @Override
    public boolean remove(final int x, final int y, final int z) {
        if (!canStore(x, y, z)) {
            throw new IllegalArgumentException("Cannot remove block position at <" + x + "," + y + "," + z + ">");
        }
        final int compressedPos = compress(x, y, z);
        if (!listValueToIndex.containsKey(compressedPos)) {
            return false;
        }

        final int elementIndex = listValueToIndex.get(compressedPos);

        if (elementIndex == compressedBlockPosList.size() - 1) {
            // If the element we're removing is at the end then its EZ
            compressedBlockPosList.removeInt(elementIndex);
        } else {
            // Otherwise, swap the last element with the one we're removing, and then remove the end
            final int lastElementValue = compressedBlockPosList.removeInt(compressedBlockPosList.size() - 1);
            compressedBlockPosList.set(elementIndex, lastElementValue);
            listValueToIndex.put(lastElementValue, elementIndex);
        }
        listValueToIndex.remove(compressedPos);

        return true;
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        if (!canStore(x, y, z)) {
            // This pos cannot exist in this set
            return false;
        }
        return listValueToIndex.containsKey(compress(x, y, z));
    }

    @Override
    public boolean canStore(final int x, final int y, final int z) {
        final int xLocal = x - centerX;
        final int yLocal = y - centerY;
        final int zLocal = z - centerZ;
        return !(yLocal < -128 | yLocal > 127 | xLocal < -2048 | xLocal > 2047 | zLocal < -2048 | zLocal > 2047);
    }

    @Override
    public int size() {
        return compressedBlockPosList.size();
    }

    @Nonnull
    @Override
    public Iterator<Vector3ic> iterator() {
        return new SmallBlockPosIterator(compressedBlockPosList.iterator());
    }

    @Override
    public void forEach(@Nonnull final IntTernaryConsumer action) {
        final IntIterator iterator = compressedBlockPosList.iterator();
        while (iterator.hasNext()) {
            final int compressed = iterator.nextInt();
            // Repeated code from decompress() because java has no output parameters.
            final int z = compressed >> 20;
            int y = (compressed >> 12) & BOT_8_BITS;
            // Sign-extend all the upper bits to be 1
            if ((y & 0x80) != 0) {
                y |= ~BOT_8_BITS;
            }
            // this basically left-pads the int when casting so that the sign is preserved
            // not sure if there is a better way
            final int x = (compressed & BOT_12_BITS) << 20 >> 20;
            action.accept(x + centerX, y + centerY, z + centerZ);
        }
    }

    @Override
    public void clear() {
        compressedBlockPosList.clear();
        listValueToIndex.clear();
    }

    @Nonnull
    private Vector3ic decompress(final int compressed) {
        return decompressMutable(compressed, new Vector3i());
    }

    private Vector3i decompressMutable(final int compressed, final Vector3i mutableBlockPos) {
        final int z = compressed >> 20;
        int y = (compressed >> 12) & BOT_8_BITS;
        // Sign-extend all the upper bits to be 1
        if ((y & 0x80) != 0) {
            y |= ~BOT_8_BITS;
        }
        // this basically left-pads the int when casting so that the sign is preserved
        // not sure if there is a better way
        final int x = (compressed & BOT_12_BITS) << 20 >> 20;
        mutableBlockPos.set(x + centerX, y + centerY, z + centerZ);
        return mutableBlockPos;
    }

    private int compress(final int x, final int y, final int z) {
        // Allocate 12 bits for x, 12 bits for z, and 8 bits for y.
        final int xBits = (x - centerX) & BOT_12_BITS;
        final int yBits = (y - centerY) & BOT_8_BITS;
        final int zBits = (z - centerZ) & BOT_12_BITS;
        return xBits | (yBits << 12) | (zBits << 20);
    }

    @Override
    public void forEachUnsafe(@Nonnull final IntTernaryConsumer action) {
        int curIndex = 0;
        final Vector3i mutableBlockPos = new Vector3i();
        while (compressedBlockPosList.size() >= curIndex) {
            try {
                final int currentValue = compressedBlockPosList.get(curIndex);
                curIndex++;
                decompressMutable(currentValue, mutableBlockPos);
                action.accept(mutableBlockPos.x(), mutableBlockPos.y(), mutableBlockPos.z());
            } catch (final Exception e) {
                // Catch concurrent read/write race condition
                return;
            }
        }
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getCenterZ() {
        return centerZ;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof IBlockPosSet) {
            return (((IBlockPosSet) other).size() == size()) && ((IBlockPosSet) other).containsAll(this);
        }
        return false;
    }

    private class SmallBlockPosIterator implements Iterator<Vector3ic> {

        private final IntIterator iterator;

        SmallBlockPosIterator(final IntIterator intIterator) {
            this.iterator = intIterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Vector3ic next() {
            return decompress(iterator.next());
        }

    }

    public static class SmallBlockPosSetSerializer extends StdSerializer<SmallBlockPosSet> {

        public SmallBlockPosSetSerializer() {
            super((Class<SmallBlockPosSet>) null);
        }

        @Override
        public void serialize(final SmallBlockPosSet value, final JsonGenerator gen,
            final SerializerProvider provider) throws IOException {

            gen.writeStartObject();

            gen.writeFieldName("positions");
            gen.writeStartArray(value.compressedBlockPosList, value.compressedBlockPosList.size());
            final IntIterator iter = value.compressedBlockPosList.iterator();

            while (iter.hasNext()) {
                gen.writeNumber(iter.nextInt());
            }
            gen.writeEndArray();

            gen.writeNumberField("centerX", value.centerX);
            gen.writeNumberField("centerY", value.centerY);
            gen.writeNumberField("centerZ", value.centerZ);

            gen.writeEndObject();
        }

    }

    public static class SmallBlockPosSetDeserializer extends StdDeserializer<SmallBlockPosSet> {

        public SmallBlockPosSetDeserializer() {
            super((Class<?>) null);
        }

        @Override
        public SmallBlockPosSet deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException {
            final JsonNode node = p.getCodec().readTree(p);

            final int centerX = node.get("centerX").asInt();
            final int centerY = node.get("centerY").asInt();
            final int centerZ = node.get("centerZ").asInt();

            final SmallBlockPosSet set = new SmallBlockPosSet(centerX, centerY, centerZ);

            for (final JsonNode elem : node.get("positions")) {
                final int positionInt = elem.asInt();
                set.compressedBlockPosList.add(positionInt);
                set.listValueToIndex.put(positionInt, set.compressedBlockPosList.size() - 1);
            }

            return set;
        }
    }
}
