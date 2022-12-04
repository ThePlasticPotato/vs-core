package org.valkyrienskies.core.impl.datastructures;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Naive implementation of IBlockPosSet, basically just a wrapper around a HashSet&lt;BlockPos&gt;. Used for testing
 * purposes only.
 */
public class NaiveBlockPosSet implements IBlockPosSet {

    private final Set<Vector3ic> blockPosSet;

    public NaiveBlockPosSet() {
        this.blockPosSet = new HashSet<>();
    }

    @Override
    public boolean add(final int x, final int y, final int z) {
        return blockPosSet.add(new Vector3i(x, y, z));
    }

    @Override
    public boolean remove(final int x, final int y, final int z) {
        return blockPosSet.remove(new Vector3i(x, y, z));
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        return blockPosSet.contains(new Vector3i(x, y, z));
    }

    @Override
    public boolean canStore(final int x, final int y, final int z) {
        return true;
    }

    @Override
    public int size() {
        return blockPosSet.size();
    }

    @NotNull
    @Override
    public Iterator<Vector3ic> iterator() {
        return blockPosSet.iterator();
    }

    @Override
    public void clear() {
        blockPosSet.clear();
    }

}
