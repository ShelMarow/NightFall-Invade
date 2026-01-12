package net.shelmarow.nightfall_invade.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureBlockEntity.class)
public class StructureBlockEntityMixin {
    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(III)I"
            )
    )
    private int redirectLoad(int pValue, int pMin, int pMax) {
        if(pMin == -48) {
            return Mth.clamp(pValue, -1024, 1024);
        }
        else{
            return Mth.clamp(pValue, 0, 1024);
        }
    }
}
