package net.shelmarow.nightfall_invade.entity.fallen_knight;

import com.asanginxst.epicfightx.gameassets.animations.AnimationsX;
import com.google.common.collect.ImmutableMap;
import com.hm.efn.client.sound.EFNSounds;
import com.hm.efn.gameasset.animations.EFNGreatSwordAnimations;
import com.hm.efn.gameasset.animations.EFNScytheAnimations;
import com.hm.efn.gameasset.animations.EFNSkillAnimations;
import com.hm.efn.particle.EFNParticles;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.shelmarow.combat_evolution.ai.CEHumanoidPatch;
import net.shelmarow.combat_evolution.ai.iml.CustomExecuteEntity;
import net.shelmarow.combat_evolution.execution.ExecutionTypeManager;
import net.shelmarow.nightfall_invade.assets.NFIAnimations;
import net.shelmarow.nightfall_invade.entity.fallen_knight.ai.FallenKnightAI;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FallenKnightPatch extends CEHumanoidPatch<FallenKnight> implements CustomExecuteEntity {

    public FallenKnightPatch() {
        super(Factions.NEUTRAL);
    }

    @Override
    protected void setWeaponMotions() {
        this.weaponLivingMotions.put(CapabilityItem.WeaponCategories.GREATSWORD,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, Set.of(
                        Pair.of(LivingMotions.IDLE, EFNGreatSwordAnimations.NG_GREATSWORD_IDLE),
                        Pair.of(LivingMotions.WALK, EFNGreatSwordAnimations.NG_GREATSWOED_WALK),
                        Pair.of(LivingMotions.RUN, EFNGreatSwordAnimations.NG_GREATSWORD_RUN),
                        Pair.of(LivingMotions.CHASE, EFNGreatSwordAnimations.NG_GREATSWORD_RUN),
                        Pair.of(LivingMotions.BLOCK, AnimationsX.GREATSWORD_GUARD)
                )));

        this.guardHitMotions.put(CapabilityItem.WeaponCategories.GREATSWORD,
                Map.of(CapabilityItem.Styles.COMMON, List.of(
                        AnimationsX.GREATSWORD_GUARD_HIT
                )));
        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.GREATSWORD,
                Map.of(CapabilityItem.Styles.COMMON, FallenKnightAI.createNormal()));
    }

    @Override
    public void playGuardHitAnimation(DamageSource damageSource, boolean canCounter) {
        if(!isLogicalClient()){
            Vec3 pos = original.getEyePosition().add(original.getLookAngle().normalize().scale(1));
            ((ServerLevel)original.level()).sendParticles(EFNParticles.EFN_PARRY_FLASH_MAIN.get(), pos.x, pos.y, pos.z,1,0,0,0,1);
            ((ServerLevel)original.level()).sendParticles(EFNParticles.ALL_SPARK.get(), pos.x, pos.y, pos.z,1,0,0,0,1);
        }
        if(canCounter){
            playSound(EFNSounds.PARRY.get(),0,0);
            this.playAnimationSynchronized(EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT1, 0.0F);
        }
        else{
            this.playGuardHitSound();
            AnimationManager.AnimationAccessor<? extends StaticAnimation> guardHit = this.getGuardHitAnimation(damageSource);
            this.playAnimationSynchronized(guardHit, 0.0F);
        }
    }

    @Override
    public boolean canBeExecuted(LivingEntityPatch<?> livingEntityPatch) {
        return true;
    }

    @Override
    public boolean canUseCustomType(LivingEntityPatch<?> livingEntityPatch, ExecutionTypeManager.Type type) {
        return true;
    }

    @Override
    public ExecutionTypeManager.Type getExecutionType(LivingEntityPatch<?> livingEntityPatch, ExecutionTypeManager.Type type) {
        return new ExecutionTypeManager.Type(type.executionAnimation(), NFIAnimations.BIPED_EXECUTED, type.offset(), type.rotationOffset(), type.totalTick());
    }
}
