package org.valkyrienskies.core.impl.datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import kotlin.random.Random;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.valkyrienskies.core.impl.VSRandomUtils;
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil;

public class BlockPosSetAABBGeneratorTest {

    private static final ObjectMapper serializer = VSJacksonUtil.INSTANCE.getDefaultMapper();

    @Test
    public void testSmallBlockPosSetAABB() {
        final BlockPosSetAABBGenerator toTest = new BlockPosSetAABBGenerator(0, 0, 0, 1024, 1024, 1024);
        testBlockPosSetAABB(toTest);
    }

    @Test
    public void testDynamicBlockPosSetAABB() {
        testBlockPosSetAABB(new DynamicBlockPosSetAABB());
    }

    private void testBlockPosSetAABB(final IBlockPosSetAABB toTest) {
        final ExtremelyNaiveVoxelFieldAABBMaker
            aabbMaker = new ExtremelyNaiveVoxelFieldAABBMaker(0, 0);

        // Test adding new positions
        final Vector3ic pos0 = new Vector3i(5, 10, 3);
        assertEquals(toTest.add(pos0), aabbMaker.addVoxel(pos0));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        final Vector3ic pos1 = new Vector3i(2, 5, 3);
        assertEquals(toTest.add(pos1), aabbMaker.addVoxel(pos1));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        final Vector3ic pos2 = new Vector3i(1, 20, 0);
        assertEquals(toTest.add(pos2), aabbMaker.addVoxel(pos2));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        // Test adding duplicates
        final Vector3ic pos3 = new Vector3i(1, 20, 0);
        assertEquals(toTest.add(pos3), aabbMaker.addVoxel(pos3));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        // Test removing what doesn't exist
        final Vector3ic pos4 = new Vector3i(6, 7, 8);
        assertEquals(toTest.remove(pos4), aabbMaker.removeVoxel(pos4));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        // Test removing what does exist
        final Vector3ic pos5 = new Vector3i(5, 10, 3);
        assertEquals(toTest.remove(pos5), aabbMaker.removeVoxel(pos5));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        final Vector3ic pos6 = new Vector3i(2, 5, 3);
        assertEquals(toTest.remove(pos6), aabbMaker.removeVoxel(pos6));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        final Vector3ic pos7 = new Vector3i(1, 20, 0);
        assertEquals(toTest.remove(pos7), aabbMaker.removeVoxel(pos7));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        // Test adding new positions
        final Vector3ic pos8 = new Vector3i(25, 2, 35);
        assertEquals(toTest.add(pos8), aabbMaker.addVoxel(pos8));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        // Test negative positions
        final Vector3ic pos9 = new Vector3i(-25, -2, -35);
        assertEquals(toTest.add(pos8), aabbMaker.addVoxel(pos8));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        final Vector3ic pos10 = new Vector3i(0, -50, 0);
        assertEquals(toTest.add(pos8), aabbMaker.addVoxel(pos8));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        // Test clear
        toTest.clear();
        aabbMaker.clear();
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());
    }

    /**
     * Tests the correctness of SmallBlockPosSetAABB serialization and deserialization.
     */
    @Disabled("We don't currently need to serialize the AABB generator")
    @RepeatedTest(25)
    public void testSerializationAndDeSerialization() throws IOException {
        final Random random = VSRandomUtils.INSTANCE.getDefaultRandom();

        final BlockPosSetAABBGenerator blockPosSet =
            VSRandomUtils.INSTANCE.randomBlockPosSetAABB(Random.Default, random.nextInt(500));

        // Now serialize and deserialize and verify that they are the same
        final byte[] blockPosSetSerialized = serializer.writeValueAsBytes(blockPosSet);
        final BlockPosSetAABBGenerator blockPosSetDeserialized =
            serializer.readValue(blockPosSetSerialized, BlockPosSetAABBGenerator.class);

        // Verify both sets are equal
        assertEquals(blockPosSet, blockPosSetDeserialized);
    }
}
