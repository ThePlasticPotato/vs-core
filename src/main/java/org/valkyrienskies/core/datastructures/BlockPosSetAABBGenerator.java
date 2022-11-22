package org.valkyrienskies.core.datastructures;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.primitives.AABBi;
import org.valkyrienskies.core.game.ChunkClaim;
import org.valkyrienskies.core.util.IntTernaryConsumer;

/**
 * A wrapper around SmallBlockPosSet that can make create tight AxisAlignedBB containing all BlockPos in the Set. All
 * operations (except clear) run in O(1) average time.
 */
@JsonDeserialize(using = BlockPosSetAABBGenerator.SmallBlockPosSetAABBDeserializer.class)
@JsonSerialize(using = BlockPosSetAABBGenerator.SmallBlockPosSetAABBSerializer.class)
public class BlockPosSetAABBGenerator implements IBlockPosSetAABB {

    private final IBlockPosSet blockPosSet;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    private final int xSize;
    private final int ySize;
    private final int zSize;
    private final FastMinMaxMap xMap;
    private final FastMinMaxMap yMap;
    private final FastMinMaxMap zMap; // Only non-final so we can clear() quickly.

    public BlockPosSetAABBGenerator(final ChunkClaim chunkClaim) {
        this(chunkClaim, new DenseBlockPosSet());
    }

    public BlockPosSetAABBGenerator(final ChunkClaim chunkClaim, final IBlockPosSet backingSet) {
        final Vector3ic centerCoordinates = chunkClaim.getCenterBlockCoordinates(new Vector3i());
        final Vector3ic claimSize = chunkClaim.getBlockSize(new Vector3i());

        this.blockPosSet = backingSet;
        this.centerX = centerCoordinates.x();
        this.centerY = centerCoordinates.y();
        this.centerZ = centerCoordinates.z();
        this.xSize = claimSize.x();
        this.ySize = claimSize.y();
        this.zSize = claimSize.z();
        this.xMap = new FastMinMaxMap(xSize);
        this.yMap = new FastMinMaxMap(ySize);
        this.zMap = new FastMinMaxMap(zSize);
    }

    public BlockPosSetAABBGenerator(final int centerX, final int centerY, final int centerZ, final int xSize,
        final int ySize, final int zSize) {
        this(new DenseBlockPosSet(), centerX, centerY, centerZ, xSize, ySize, zSize);
    }

    private BlockPosSetAABBGenerator(final IBlockPosSet blockPosSet, final int centerX, final int centerY,
        final int centerZ, final int xSize, final int ySize, final int zSize) {
        this.blockPosSet = blockPosSet;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.xMap = new FastMinMaxMap(xSize);
        this.yMap = new FastMinMaxMap(ySize);
        this.zMap = new FastMinMaxMap(zSize);
    }

    @Nullable
    @Override
    public AABBi makeAABB() {
        if (blockPosSet.isEmpty()) {
            return null;
        } else {
            int minX = xMap.getFront() - (xSize / 2);
            int maxX = xMap.getBack() - (xSize / 2);
            int minY = yMap.getFront() - (ySize / 2);
            int maxY = yMap.getBack() - (ySize / 2);
            int minZ = zMap.getFront() - (zSize / 2);
            int maxZ = zMap.getBack() - (zSize / 2);
            minX += centerX;
            maxX += centerX;
            minY += centerY;
            maxY += centerY;
            minZ += centerZ;
            maxZ += centerZ;
            return new AABBi(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    @Override
    public boolean add(final int x, final int y, final int z) throws IllegalArgumentException {
        final boolean setResult = blockPosSet.add(x, y, z);
        if (setResult) {
            incrementAABBMaker(x, y, z);
        }
        return setResult;
    }

    private void incrementAABBMaker(final int x, final int y, final int z) {
        xMap.increment(x - centerX + (xSize / 2));
        yMap.increment(y - centerY + (ySize / 2));
        zMap.increment(z - centerZ + (zSize / 2));
    }

    @Override
    public boolean remove(final int x, final int y, final int z) {
        final boolean setResult = blockPosSet.remove(x, y, z);
        if (setResult) {
            decrementAABBMaker(x, y, z);
        }
        return setResult;
    }

    private void decrementAABBMaker(final int x, final int y, final int z) {
        xMap.decrement(x - centerX + (xSize / 2));
        yMap.decrement(y - centerY + (ySize / 2));
        zMap.decrement(z - centerZ + (zSize / 2));
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        return blockPosSet.contains(x, y, z);
    }

    @Override
    public boolean canStore(final int x, final int y, final int z) {
        return blockPosSet.canStore(x, y, z);
    }

    @Override
    public int size() {
        return blockPosSet.size();
    }

    @Override
    @Nonnull
    public Iterator<Vector3ic> iterator() {
        return blockPosSet.iterator();
    }

    @Override
    public void clear() {
        blockPosSet.clear();
        this.xMap.clear();
        this.yMap.clear();
        this.zMap.clear();
    }

    @Override
    public void forEach(@Nonnull final IntTernaryConsumer action) {
        blockPosSet.forEach(action);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof IBlockPosSetAABB) {
            return (((IBlockPosSetAABB) other).size() == size()) && ((IBlockPosSetAABB) other).containsAll(this);
        }
        return false;
    }

    public static class SmallBlockPosSetAABBSerializer extends StdSerializer<BlockPosSetAABBGenerator> {

        public SmallBlockPosSetAABBSerializer() {
            super((Class<BlockPosSetAABBGenerator>) null);
        }

        @Override
        public void serialize(final BlockPosSetAABBGenerator value, final JsonGenerator gen,
            final SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("blockPosSet", value.blockPosSet);
            gen.writeNumberField("centerX", value.centerX);
            gen.writeNumberField("centerY", value.centerY);
            gen.writeNumberField("centerZ", value.centerZ);
            gen.writeNumberField("xSize", value.xSize);
            gen.writeNumberField("ySize", value.ySize);
            gen.writeNumberField("zSize", value.zSize);
            gen.writeEndObject();
        }
    }

    public static class SmallBlockPosSetAABBDeserializer extends StdDeserializer<BlockPosSetAABBGenerator> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        public SmallBlockPosSetAABBDeserializer() {
            super((Class<?>) null);
        }

        @Override
        public BlockPosSetAABBGenerator deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException {
            final JsonNode node = p.getCodec().readTree(p);
            // The blockPosSet gets loaded
            final SmallBlockPosSet blockPosSet =
                objectMapper.treeToValue(node.get("blockPosSet"), SmallBlockPosSet.class);
            final int centerX = node.get("centerX").asInt();
            final int centerY = node.get("centerY").asInt();
            final int centerZ = node.get("centerZ").asInt();
            final int xSize = node.get("xSize").asInt();
            final int ySize = node.get("ySize").asInt();
            final int zSize = node.get("zSize").asInt();
            final BlockPosSetAABBGenerator wrapper =
                new BlockPosSetAABBGenerator(blockPosSet, centerX, centerY, centerZ, xSize, ySize, zSize);
            // The AABB maker is a derivative of the blockPosSet.
            blockPosSet.forEach(wrapper::incrementAABBMaker);
            return wrapper;
        }
    }
}
