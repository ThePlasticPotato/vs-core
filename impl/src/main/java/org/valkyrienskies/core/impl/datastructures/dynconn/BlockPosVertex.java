package org.valkyrienskies.core.impl.datastructures.dynconn;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public class BlockPosVertex extends ConnVertex {
    int posX;
    int posY;
    int posZ;

    public BlockPosVertex(int x, int y, int z) {
        posX = x;
        posY = y;
        posZ = z;
    }

    public void setBlockPos(int x, int y, int z) {
        posX = x;
        posY = y;
        posZ = z;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosZ() {
        return posZ;
    }

    public Vector3ic toJOML() {
        return new Vector3i(posX, posY, posZ);
    }
}
