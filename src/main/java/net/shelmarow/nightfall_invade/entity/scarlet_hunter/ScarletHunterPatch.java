package net.shelmarow.nightfall_invade.entity.scarlet_hunter;

import com.asanginxst.epicfightx.gameassets.animations.AnimationsX;
import com.google.common.collect.ImmutableMap;
import com.hm.efn.client.sound.EFNSounds;
import com.hm.efn.gameasset.animations.EFNScytheAnimations;
import com.hm.efn.gameasset.animations.EFNSkillAnimations;
import com.hm.efn.particle.EFNParticles;
import com.hm.efn.registries.EFNMobEffectRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.shelmarow.combat_evolution.ai.CEHumanoidPatch;
import net.shelmarow.combat_evolution.ai.StaminaStatus;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.effect.CEStunImmunityEffect;
import net.shelmarow.nightfall_invade.assets.NFIAnimations;
import net.shelmarow.nightfall_invade.entity.scarlet_hunter.ai.ScarletHunterAI;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import java.util.List;
import java.util.Set;

public class ScarletHunterPatch extends CEHumanoidPatch<ScarletHunter> {


    public ScarletHunterPatch() {
        super(Factions.NEUTRAL);
        this.chasingSpeed = 1.35F;
        this.breakTime = 80;
    }

    @Override
    public void onStartTracking(ServerPlayer trackingPlayer) {
        super.onStartTracking(trackingPlayer);
    }

    @Override
    public void tick(LivingEvent.LivingTickEvent event) {
        super.tick(event);

        if(original.bloodShieldTimer >= 1500 && CEPatchUtils.getStaminaStatus(this) == StaminaStatus.COMMON){
            lastActionTime = original.tickCount;
            dealStaminaDamage(null, CEPatchUtils.getMaxStamina(this) / 15 / 20);
        }
    }

    @Override
    public void onBreak(DamageSource damageSource) {
        super.onBreak(damageSource);

        ScarletHunter scarletHunter = getOriginal();
        if(scarletHunter.hasBloodShield()){
            scarletHunter.setBloodShield(false);
            scarletHunter.setBloodShieldCooldown(2400);
            //破防受到伤害
            if(damageSource != null && damageSource.getEntity() instanceof LivingEntity){
                scarletHunter.totalDamageTaken = 0;
                scarletHunter.totalHitCounter = 0;

                float damageAmount = scarletHunter.getMaxHealth() * 0.15F;
                if(scarletHunter.getHealth() <= damageAmount){
                    damageAmount = scarletHunter.getHealth() - 1F;
                }

                scarletHunter.hurt(
                        new DamageSource(scarletHunter.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(DamageTypes.GENERIC_KILL), null, null),
                        damageAmount
                );
            }
            //首次破盾转阶段
            if(scarletHunter.getBossPhase() == 1){
                scarletHunter.setPhaseChangeCounter(1);
            }
        }
    }

    @Override
    protected void setWeaponMotions() {
        this.weaponLivingMotions.put(CapabilityItem.WeaponCategories.LONGSWORD,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, Set.of(
                        Pair.of(LivingMotions.IDLE, EFNScytheAnimations.SCYTHE_IDLE_COMBAT),
                        Pair.of(LivingMotions.WALK, EFNScytheAnimations.SCYTHE_WALK_COMBAT),
                        Pair.of(LivingMotions.RUN, EFNScytheAnimations.SCYTHE_RUN_COMBAT),
                        Pair.of(LivingMotions.CHASE, EFNScytheAnimations.SCYTHE_RUN_COMBAT),
                        Pair.of(LivingMotions.BLOCK, EFNScytheAnimations.SCYTHE_BLOCK),
                        Pair.of(LivingMotions.DEATH, Animations.BIPED_COMMON_NEUTRALIZED)
                )));

        this.guardHitMotions.put(CapabilityItem.WeaponCategories.LONGSWORD,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, List.of(
                        EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT1,
                        EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT2,
                        AnimationsX.SWORD_GUARD_ACTIVE_HIT1,
                        AnimationsX.SWORD_GUARD_ACTIVE_HIT2,
                        AnimationsX.SWORD_GUARD_ACTIVE_HIT3,
                        AnimationsX.LONGSWORD_GUARD_ACTIVE_HIT1,
                        AnimationsX.LONGSWORD_GUARD_ACTIVE_HIT2

                )));

        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.LONGSWORD,
                ImmutableMap.of(CapabilityItem.Styles.COMMON, ScarletHunterAI.createNormal()));
    }

    @Override
    public void playGuardBreakSound() {
        this.playSound(EpicFightSounds.NEUTRALIZE_BOSSES.get(), 1.0F, 1.0F);
    }

    @Override
    public void playGuardHitAnimation(DamageSource damageSource, boolean canCounter) {
        if(!isLogicalClient()){
            Vec3 pos = original.getEyePosition().add(original.getLookAngle().normalize().scale(1));
            ((ServerLevel)original.level()).sendParticles(EFNParticles.EFN_PARRY_FLASH_MAIN.get(), pos.x, pos.y, pos.z,1,0,0,0,1);
            ((ServerLevel)original.level()).sendParticles(EFNParticles.ALL_SPARK.get(), pos.x, pos.y, pos.z,1,0,0,0,1);
        }
        AnimationManager.AnimationAccessor<? extends StaticAnimation> guardHit = this.getGuardHitAnimation(damageSource);
        this.playAnimationSynchronized(guardHit, 0.0F);
        if(canCounter){
            playSound(EFNSounds.PARRY.get(),0,0);
        }
        else{
            this.playGuardHitSound();
        }
    }

    @Override
    public float getGuardHitImpactPercent(DamageSource damageSource) {
        return original.hasBloodShield() ? 0.1F : 0.5F;
    }

    @Override
    public float getHurtImpactPercent(DamageSource damageSource) {
        if(damageSource.getEntity() instanceof Player) {
            return 0.25F;
        }
        return 0.75F;
    }

    @Override
    public void onAttackParried(DamageSource damageSource, LivingEntityPatch<?> blocker) {
        super.onAttackParried(damageSource, blocker);
        dealStaminaDamage(
                blocker.getDamageSource(Animations.EMPTY_ANIMATION, InteractionHand.MAIN_HAND),
                original.hasBloodShield() ? 3F : 1F
        );
    }


    public boolean dealStaminaDamage(DamageSource damageSource, float amount) {
        if(damageSource != null){
            if(damageSource.getDirectEntity() instanceof AbstractArrow){
                amount = 0;
            }
            if(amount >= CEPatchUtils.getMaxStamina(this) * 0.2F){
                amount *= 0.25F;
            }
        }

        return super.dealStaminaDamage(damageSource, amount);
    }

    @Override
    public void onDeath(LivingDeathEvent event) {
        if(!isLogicalClient()){
            playAnimationSynchronized(animator.getLivingAnimation(LivingMotions.DEATH, Animations.BIPED_COMMON_NEUTRALIZED), 0F);
        }
        this.currentLivingMotion = LivingMotions.DEATH;
    }

    @Override
    public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
        AttackResult result = super.attack(damageSource, target, hand);

        if(result.resultType == AttackResult.ResultType.SUCCESS){
            if(target instanceof LivingEntity livingTarget){
                int level = 0;
                if(livingTarget.hasEffect(EFNMobEffectRegistry.CURSE_OF_BLOOD.get())){
                    MobEffectInstance instance = livingTarget.getEffect(EFNMobEffectRegistry.CURSE_OF_BLOOD.get());
                    if (instance != null) {
                        level = instance.getAmplifier() + 1;
                    }
                }
                livingTarget.addEffect(new MobEffectInstance(EFNMobEffectRegistry.CURSE_OF_BLOOD.get(), 100, level));
            }
        }

        return result;
    }

    @Override
    public OpenMatrix4f getModelMatrix(float partialTicks) {
        float scale = 1.25F;
        return super.getModelMatrix(partialTicks).scale(scale, scale, scale);
    }

    @Override
    public boolean applyStun(StunType stunType, float stunTime) {
        if(stunType == StunType.SHORT || stunType == StunType.HOLD){
            stunType = StunType.LONG;
        }
        return super.applyStun(stunType, 0F);
    }


    @Override
    public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
        int hurtLevel = animation.get().getState(EntityState.HURT_LEVEL, this, 0);
        if(original.getHealth() > 0 && hurtLevel > 0 && CEPatchUtils.getStamina(this) != 0 && !original.isCanBypassStunImmunity()){
            for (MobEffect effect : original.getActiveEffectsMap().keySet()) {
                if(effect instanceof CEStunImmunityEffect){
                    return;
                }
            }
        }
        if(original.isCanBypassStunImmunity()){
            original.setCanBypassStunImmunity(false);
        }
        super.playAnimationSynchronized(animation, transitionTimeModifier);
    }

    @Override
    public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, ServerAnimationPacketProvider packetProvider) {
        int hurtLevel = animation.get().getState(EntityState.HURT_LEVEL, this, 0);
        if(original.getHealth() > 0 && hurtLevel > 0 && CEPatchUtils.getStamina(this) != 0 && !original.isCanBypassStunImmunity()){
            for (MobEffect effect : original.getActiveEffectsMap().keySet()) {
                if(effect instanceof CEStunImmunityEffect){
                    return;
                }
            }
        }
        if(original.isCanBypassStunImmunity()){
            original.setCanBypassStunImmunity(false);
        }
        super.playAnimationSynchronized(animation, transitionTimeModifier, packetProvider);
    }

    @Override
    public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        switch (stunType) {
            case LONG -> {
                return Animations.BIPED_HIT_LONG;
            }
            case SHORT,HOLD -> {
                return Animations.BIPED_HIT_SHORT;
            }
            case KNOCKDOWN -> {
                return Animations.BIPED_KNOCKDOWN;
            }
            case FALL -> {
                return Animations.BIPED_LANDING;
            }
            case NEUTRALIZE -> {
                return NFIAnimations.SCARLET_HUNTER_BROKEN;
            }
            default -> {
                return null;
            }
        }
    }
}
