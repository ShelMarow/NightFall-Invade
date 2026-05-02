package net.shelmarow.nightfall_invade.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.entity.arterius.Arterius;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GateOfDisaster extends Structure {
    public static final ResourceLocation GATE_OF_DISASTER_P1 = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "gate_of_disaster/gate_of_disaster_part1");
    public static final ResourceLocation GATE_OF_DISASTER_P2 = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "gate_of_disaster/gate_of_disaster_part2");
    public static final ResourceLocation GATE_OF_DISASTER_P3 = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "gate_of_disaster/gate_of_disaster_part3");
    public static final Codec<GateOfDisaster> CODEC = simpleCodec(GateOfDisaster::new);
    private static final Map<ResourceLocation, BlockPos> OFFSET = new HashMap<>();

    static {
        OFFSET.put(GATE_OF_DISASTER_P1, new BlockPos(0, 0, 0));
        OFFSET.put(GATE_OF_DISASTER_P2, new BlockPos(0, 0, 0));
        OFFSET.put(GATE_OF_DISASTER_P3, new BlockPos(0, 0, 0));
    }

    protected GateOfDisaster(StructureSettings pSettings) {
        super(pSettings);
    }

    @Override
    protected @NotNull Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext context) {
        StructureTemplateManager templateManager = context.structureTemplateManager();

        StructureTemplate template = templateManager.getOrCreate(GATE_OF_DISASTER_P1);
        StructureTemplate template2 = templateManager.getOrCreate(GATE_OF_DISASTER_P3);

        int centerX = (context.chunkPos().x << 4) + 7;
        int centerZ = (context.chunkPos().z << 4) + 7;

        int centerY = context.chunkGenerator().getFirstFreeHeight(centerX, centerZ,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());

        BlockPos basePos = new BlockPos(centerX, centerY, centerZ);

        Rotation rotation = Rotation.getRandom(context.random());
        Vec3i size = template.getSize(rotation);
        Vec3i size2 = template2.getSize(rotation);

        BlockPos[] cornerOffsets = new BlockPos[]{
                new BlockPos(-size.getX() / 2, 0, -size.getZ() / 2),
                new BlockPos(size.getX() / 2, 0, -size.getZ() / 2),
                new BlockPos(-size2.getX() / 2, 0, size2.getZ() / 2),
                new BlockPos(size2.getX() / 2, 0, size2.getZ() / 2)
        };

        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        RandomState randomState = context.randomState();

        int minAllowedY = context.chunkGenerator().getSeaLevel() - 30;
        int maxAllowedY = context.chunkGenerator().getSeaLevel() + 30;

        for (BlockPos offset : cornerOffsets) {
            BlockPos rotatedCorner = basePos.offset(offset.rotate(rotation));
            int groundY = generator.getFirstFreeHeight(rotatedCorner.getX(), rotatedCorner.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
            BlockState groundBlock = generator.getBaseColumn(rotatedCorner.getX(), rotatedCorner.getZ(), heightAccessor, randomState).getBlock(groundY - 1);

            if (groundY < minAllowedY || groundY > maxAllowedY || Math.abs(groundY - centerY) > 5 || !groundBlock.getFluidState().isEmpty()) {
                return Optional.empty();
            }
        }

        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, (builder) -> generatePieces(builder,context, rotation));
    }

    @Override
    public @NotNull StructureType<?> type() {
        return NFIStructureType.GATE_OF_DISASTER.get();
    }


    public static void start(StructureTemplateManager templateManager, BlockPos pos, Rotation rotation, StructurePieceAccessor pieceList, RandomSource random) {
        int x = pos.getX();
        int z = pos.getZ();

        BlockPos rotationOffSet = (new BlockPos(-48, 0, 0)).rotate(rotation);
        BlockPos blockpos = rotationOffSet.offset(x, pos.getY(), z);
        pieceList.addPiece(new Piece(templateManager, GATE_OF_DISASTER_P1, blockpos, rotation));

        rotationOffSet = (new BlockPos(0, 0, 0)).rotate(rotation);
        blockpos = rotationOffSet.offset(x, pos.getY(), z);
        pieceList.addPiece(new Piece(templateManager, GATE_OF_DISASTER_P2, blockpos, rotation));

        rotationOffSet = (new BlockPos(48, 0, 0)).rotate(rotation);
        blockpos = rotationOffSet.offset(x, pos.getY(), z);
        pieceList.addPiece(new Piece(templateManager, GATE_OF_DISASTER_P3, blockpos, rotation));
    }

    public void generatePieces(StructurePiecesBuilder builder, GenerationContext context, Rotation rotation) {
        StructureTemplateManager templateManager = context.structureTemplateManager();
        int centerX = context.chunkPos().getMinBlockX();
        int centerZ = context.chunkPos().getMinBlockZ();
        int height = context.chunkGenerator().getFirstFreeHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos spawnPos = new BlockPos(centerX, height, centerZ);
        start(templateManager,spawnPos,rotation,builder,context.random());
    }

    public static class Piece extends TemplateStructurePiece {
        public Piece(StructureTemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotation) {
            super(NFIStructureType.GATE_OF_DISASTER_PIECE.get(), 0, templateManagerIn, resourceLocationIn, resourceLocationIn.toString(), makeSettings(rotation), makePosition(resourceLocationIn, pos));
        }

        public Piece(StructureTemplateManager templateManagerIn, CompoundTag tagCompound) {
            super(NFIStructureType.GATE_OF_DISASTER_PIECE.get(), tagCompound, templateManagerIn, (resourceLocation) -> makeSettings(Rotation.valueOf(tagCompound.getString("Rot"))));
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            this(context.structureTemplateManager(), tag);
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation) {
            return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE));
        }

        private static BlockPos makePosition(ResourceLocation location, BlockPos pos) {
            return pos.offset(OFFSET.get(location));
        }

        @Override
        protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext pContext, @NotNull CompoundTag tagCompound) {
            super.addAdditionalSaveData(pContext, tagCompound);
            tagCompound.putString("Rot", this.placeSettings.getRotation().name());
        }


        @Override
        public void postProcess(@NotNull WorldGenLevel level, @NotNull StructureManager manager, @NotNull ChunkGenerator generator, @NotNull RandomSource random, @NotNull BoundingBox box, @NotNull ChunkPos chunkPos, @NotNull BlockPos pos) {
            super.postProcess(level, manager, generator, random, box, chunkPos, pos);
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
                        level.setBlock(supportPos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                        supportPos.move(0, -1, 0);
                    }
                }
            }
        }

        public boolean canReplace(WorldGenLevel level, BlockPos blockPos){
            BlockState blockState = level.getBlockState(blockPos);
            return blockState.getCollisionShape(level, blockPos).isEmpty();
        }

        @Override
        protected void handleDataMarker(@NotNull String pName, @NotNull BlockPos pPos, @NotNull ServerLevelAccessor pLevel, @NotNull RandomSource pRandom, @NotNull BoundingBox pBox) {
            if (pName.equals("boss_spawn")) {
                Arterius boss = NFIEntities.ARTERIUS.get().create(pLevel.getLevel());
                if (boss != null) {
                    boss.moveTo(pPos.getX() + 0.5, pPos.getY() + 1, pPos.getZ() + 0.5, 0, 0);
                    pLevel.addFreshEntity(boss);
                }
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }
}
