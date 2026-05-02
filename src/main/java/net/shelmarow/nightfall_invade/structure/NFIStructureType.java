package net.shelmarow.nightfall_invade.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;

public class NFIStructureType {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, NightFallInvade.MOD_ID);
    public static final RegistryObject<StructureType<GateOfDisaster>> GATE_OF_DISASTER = STRUCTURE_TYPES.register("gate_of_disaster", () -> () -> GateOfDisaster.CODEC);
    public static final RegistryObject<StructureType<AncientChurch>> ANCIENT_CHURCH = STRUCTURE_TYPES.register("ancient_church", () -> () -> AncientChurch.CODEC);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE = DeferredRegister.create(Registries.STRUCTURE_PIECE, NightFallInvade.MOD_ID);
    public static final RegistryObject<StructurePieceType> GATE_OF_DISASTER_PIECE = STRUCTURE_PIECE.register("gate_of_disaster_piece",()-> GateOfDisaster.Piece::new);
    public static final RegistryObject<StructurePieceType> ANCIENT_CHURCH_PIECE = STRUCTURE_PIECE.register("ancient_church_piece_piece",()-> AncientChurch.Piece::new);

}




























