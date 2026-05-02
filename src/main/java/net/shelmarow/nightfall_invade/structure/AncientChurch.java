package net.shelmarow.nightfall_invade.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.fallen_knight.FallenKnight;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AncientChurch extends Structure {

    public static final List<StructurePart> p1List = new ArrayList<>();
    public static final List<StructurePart> p2List = new ArrayList<>();
    public static final List<StructurePart> p3List = new ArrayList<>();
    public static final List<StructurePart> p4List = new ArrayList<>();
    public static final Codec<AncientChurch> CODEC = simpleCodec(AncientChurch::new);
    private static final int TEMPLATE_SIZE_Y = 48;

    static {
        p1List.add(new StructurePart(-1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_1")));
        p1List.add(new StructurePart(0, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_2")));
        p1List.add(new StructurePart(1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_3")));

        p1List.add(new StructurePart(-1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_4")));
        p1List.add(new StructurePart(0, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_5")));
        p1List.add(new StructurePart(1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_6")));

        p1List.add(new StructurePart(-1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_7")));
        p1List.add(new StructurePart(0, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_8")));
        p1List.add(new StructurePart(1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_9")));

        p1List.add(new StructurePart(-1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_10")));
        p1List.add(new StructurePart(0, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_11")));
        p1List.add(new StructurePart(1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_12")));

        p1List.add(new StructurePart(-1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_13")));
        p1List.add(new StructurePart(0, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_14")));
        p1List.add(new StructurePart(1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p1_15")));



        p2List.add(new StructurePart(-1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_1")));
        p2List.add(new StructurePart(0, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_2")));
        p2List.add(new StructurePart(1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_3")));

        p2List.add(new StructurePart(-1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_4")));
        p2List.add(new StructurePart(0, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_5")));
        p2List.add(new StructurePart(1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_6")));

        p2List.add(new StructurePart(-1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_7")));
        p2List.add(new StructurePart(0, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_8")));
        p2List.add(new StructurePart(1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_9")));

        p2List.add(new StructurePart(-1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_10")));
        p2List.add(new StructurePart(0, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_11")));
        p2List.add(new StructurePart(1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_12")));

        p2List.add(new StructurePart(-1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_13")));
        p2List.add(new StructurePart(0, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_14")));
        p2List.add(new StructurePart(1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p2_15")));



        p3List.add(new StructurePart(-1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_1")));
        p3List.add(new StructurePart(0, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_2")));
        p3List.add(new StructurePart(1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_3")));

        p3List.add(new StructurePart(-1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_4")));
        p3List.add(new StructurePart(0, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_5")));
        p3List.add(new StructurePart(1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_6")));

        p3List.add(new StructurePart(-1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_7")));
        p3List.add(new StructurePart(0, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_8")));
        p3List.add(new StructurePart(1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_9")));

        p3List.add(new StructurePart(-1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_10")));
        p3List.add(new StructurePart(0, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_11")));
        p3List.add(new StructurePart(1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_12")));

        p3List.add(new StructurePart(-1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_13")));
        p3List.add(new StructurePart(0, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_14")));
        p3List.add(new StructurePart(1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p3_15")));



        p4List.add(new StructurePart(-1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_1")));
        p4List.add(new StructurePart(0, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_2")));
        p4List.add(new StructurePart(1, 1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_3")));

        p4List.add(new StructurePart(-1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_4")));
        p4List.add(new StructurePart(0, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_5")));
        p4List.add(new StructurePart(1, 0, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_6")));

        p4List.add(new StructurePart(-1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_7")));
        p4List.add(new StructurePart(0, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_8")));
        p4List.add(new StructurePart(1, -1, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_9")));

        p4List.add(new StructurePart(-1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_10")));
        p4List.add(new StructurePart(0, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_11")));
        p4List.add(new StructurePart(1, -2, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_12")));

        p4List.add(new StructurePart(-1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_13")));
        p4List.add(new StructurePart(0, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_14")));
        p4List.add(new StructurePart(1, -3, 48, 48, 48, ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "ancient_church/ancient_church_p4_15")));
    }

    protected AncientChurch(StructureSettings pSettings) {
        super(pSettings);
    }

    @Override
    protected @NotNull Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext context) {
        Rotation rotation = Rotation.getRandom(context.random());

        // 计算所有第一层部件的底部位置
        List<Integer> bottomHeights = new ArrayList<>();
        int centerX = context.chunkPos().getMaxBlockX();
        int centerZ = context.chunkPos().getMaxBlockZ();

        for (StructurePart part : p1List) {
            int colOffset = part.sizeX * part.offsetX;
            int rowOffset = part.sizeZ * part.offsetZ;
            BlockPos rotatedOffset = new BlockPos(colOffset, 0, rowOffset).rotate(rotation);
            int partCenterX = centerX + rotatedOffset.getX();
            int partCenterZ = centerZ + rotatedOffset.getZ();

            int groundHeight = context.chunkGenerator().getFirstFreeHeight(
                    partCenterX, partCenterZ, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            bottomHeights.add(groundHeight);

            BlockPos groundPos = new BlockPos(partCenterX, groundHeight - 1, partCenterZ);
            if (isWaterBlock(context, groundPos)) {
                return Optional.empty();
            }
        }

        if (bottomHeights.isEmpty()) {
            return Optional.empty();
        }

        int minHeight = bottomHeights.stream().min(Integer::compareTo).orElse(0);
        int maxHeight = bottomHeights.stream().max(Integer::compareTo).orElse(0);
        int heightDiff = maxHeight - minHeight;

        if (heightDiff > 20) {
            return Optional.empty();
        }

        BlockPos spawnPos = new BlockPos(centerX, maxHeight + 15, centerZ);

        return Optional.of(new GenerationStub(spawnPos, (builder) -> generatePieces(builder, context, rotation)));
    }

    private boolean isWaterBlock(GenerationContext context, BlockPos pos) {
        return context.chunkGenerator().getBaseColumn(pos.getX(), pos.getZ(),
                        context.heightAccessor(), context.randomState())
                .getBlock(pos.getY()).getFluidState().isSource();
    }

    @Override
    public @NotNull StructureType<?> type() {
        return NFIStructureType.ANCIENT_CHURCH.get();
    }

    public void generatePieces(StructurePiecesBuilder builder, GenerationContext context, Rotation rotation) {
        StructureTemplateManager templateManager = context.structureTemplateManager();
        int centerX = context.chunkPos().getMaxBlockX();
        int centerZ = context.chunkPos().getMaxBlockZ();
        int height = context.chunkGenerator().getFirstFreeHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());

        BlockPos spawnPos = new BlockPos(centerX, height, centerZ);
        start(templateManager, spawnPos, rotation, builder);
    }

    public static void start(StructureTemplateManager templateManager, BlockPos pos, Rotation rotation, StructurePieceAccessor pieceList) {
        addLayerParts(templateManager, pos, rotation, pieceList, 0, p1List, true);
        addLayerParts(templateManager, pos, rotation, pieceList, TEMPLATE_SIZE_Y, p2List, false);
        addLayerParts(templateManager, pos, rotation, pieceList, TEMPLATE_SIZE_Y * 2, p3List, false);
        addLayerParts(templateManager, pos, rotation, pieceList, TEMPLATE_SIZE_Y * 3, p4List, false);
    }

    private static void addLayerParts(StructureTemplateManager templateManager, BlockPos pos, Rotation rotation, StructurePieceAccessor pieceList, int yOffset, List<StructurePart> parts, boolean bottom) {
        int x = pos.getX();
        int z = pos.getZ();

        for (StructurePart part : parts) {
            int colOffset = part.sizeX * part.offsetX;
            int rowOffset = part.sizeZ * part.offsetZ;
            BlockPos offset = new BlockPos(colOffset, yOffset, rowOffset);
            BlockPos rotatedOffset = offset.rotate(rotation);
            BlockPos piecePos = rotatedOffset.offset(x, pos.getY(), z);
            pieceList.addPiece(new Piece(templateManager, part.part, piecePos, rotation, bottom));
        }
    }


    public static class Piece extends TemplateStructurePiece {
        private final boolean isBottom;

        public Piece(StructureTemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotation, boolean isBottom) {
            super(NFIStructureType.ANCIENT_CHURCH_PIECE.get(), 0, templateManagerIn, resourceLocationIn, resourceLocationIn.toString(), makeSettings(rotation), pos);
            this.isBottom = isBottom;
        }

        public Piece(StructureTemplateManager templateManagerIn, CompoundTag tagCompound) {
            super(NFIStructureType.ANCIENT_CHURCH_PIECE.get(), tagCompound, templateManagerIn, (resourceLocation) -> makeSettings(Rotation.valueOf(tagCompound.getString("Rot"))));
            this.isBottom = tagCompound.getBoolean("IsBottom");
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            this(context.structureTemplateManager(), tag);
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation) {
            return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE));
        }

        @Override
        protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext pContext, @NotNull CompoundTag tagCompound) {
            super.addAdditionalSaveData(pContext, tagCompound);
            tagCompound.putString("Rot", this.placeSettings.getRotation().name());
            tagCompound.putBoolean("IsBottom", this.isBottom);
        }

        @Override
        public void postProcess(@NotNull WorldGenLevel level, @NotNull StructureManager manager, @NotNull ChunkGenerator generator, @NotNull RandomSource random, @NotNull BoundingBox box, @NotNull ChunkPos chunkPos, @NotNull BlockPos pos) {
            super.postProcess(level, manager, generator, random, box, chunkPos, pos);
            if(isBottom) {
                BoundingBox pieceBox = this.getBoundingBox();
                int minX = pieceBox.minX();
                int maxX = pieceBox.maxX();
                int minZ = pieceBox.minZ();
                int maxZ = pieceBox.maxZ();
                int bottomY = pieceBox.minY();

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos.MutableBlockPos supportPos = new BlockPos.MutableBlockPos(x, bottomY - 1, z);
                        if (!box.isInside(supportPos)) continue;
                        while (canReplace(level,supportPos) && supportPos.getY() > level.getMinBuildHeight()) {
                            level.setBlock(supportPos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                            supportPos.move(0, -1, 0);
                        }
                    }
                }
            }
        }

        public boolean canReplace(WorldGenLevel level, BlockPos blockPos){
            BlockState blockState = level.getBlockState(blockPos);
            BlockState topState = level.getBlockState(blockPos.offset(0, 1, 0));
            if(!topState.isAir()){
                return blockState.getCollisionShape(level, blockPos).isEmpty();
            }
            return false;
        }

        @Override
        protected void handleDataMarker(@NotNull String name, @NotNull BlockPos pos, @NotNull ServerLevelAccessor level, @NotNull RandomSource random, @NotNull BoundingBox box) {
            if (name.equals("boss_spawn")) {
                ScarletHunter boss = NFIEntities.SCARLET_HUNTER.get().create(level.getLevel());
                if (boss != null) {
                    boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(boss);
                }
            }
            else if (name.equals("fallen_knight")) {
                FallenKnight fallenKnight = NFIEntities.FALLEN_KNIGHT.get().create(level.getLevel());
                if (fallenKnight != null) {
                    fallenKnight.setPersistenceRequired();
                    fallenKnight.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(fallenKnight);
                }
            }
            else if (name.equals("church_knight")) {
                Husk churchKnight = EntityType.HUSK.create(level.getLevel());
                if (churchKnight != null) {
                    churchKnight.setPersistenceRequired();
                    churchKnight.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(churchKnight);
                }
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}