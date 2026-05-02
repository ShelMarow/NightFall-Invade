package net.shelmarow.nightfall_invade.structure;

import net.minecraft.resources.ResourceLocation;

public class StructurePart {
    public final int offsetX;
    public final int offsetZ;
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    public final ResourceLocation part;

    public StructurePart(int offsetX, int offsetZ, int offsetSizeX, int offsetSizeY, int offsetSizeZ, ResourceLocation part) {
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.sizeX = offsetSizeX;
        this.sizeY = offsetSizeY;
        this.sizeZ = offsetSizeZ;
        this.part = part;
    }
}
