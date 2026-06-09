package net.shelmarow.nightfall_invade.entity.misc.blood_slash;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.shelmarow.nightfall_invade.assets.NFIAnimations;
import net.shelmarow.nightfall_invade.assets.NFIFaction;
import net.shelmarow.nightfall_invade.damage_source.NFIDamageTypes;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Collection;
import java.util.Set;

public class BloodSlashPatch extends MobPatch<BloodSlashEntity> {

    public BloodSlashPatch() {
        super(NFIFaction.NFII_NO_ALIVE);
    }

    @Override
    public void onAddedToWorld() {
        playAnimationSynchronized(NFIAnimations.SHOOT, 0F);
    }


    @Override
    protected void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, NFIAnimations.IDLE);

    }


    @Override
    public void tick(LivingEvent.LivingTickEvent event) {
        super.tick(event);
    }

    @Override
    public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
        if(original.getOwner() instanceof Player) {
            damageSource.addRuntimeTag(EpicFightDamageTypeTags.GUARD_PUNCTURE);
            damageSource.addRuntimeTag(EpicFightDamageTypeTags.UNBLOCKALBE);
        }
        if(target instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
            if(playerPatch != null) {
                AttackResult.ResultType result = playerPatch.getEntityState().attackResult(damageSource);
                if(result == AttackResult.ResultType.MISSED){
                    playerPatch.playAnimationSynchronized(playerPatch.getHitAnimation(damageSource.getStunType()),0F);
                }
            }
        }

        AttackResult attack = super.attack(damageSource, target, hand);
        if(target instanceof Player player && !player.isCreative() && !player.isSpectator() && attack.resultType != AttackResult.ResultType.SUCCESS){
            EpicFightDamageSource source = getDamageSource(damageSource.getAnimation(), InteractionHand.MAIN_HAND);
            source.setStunType(StunType.NONE);
            source.addRuntimeTag(DamageTypeTags.BYPASSES_INVULNERABILITY);
            source.addRuntimeTag(DamageTypeTags.BYPASSES_COOLDOWN);
            player.hurt(source, original.getTotalDamage(target) * 0.25F);
        }
        return attack;
    }

    @Override
    public EpicFightDamageSource getDamageSource(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation, InteractionHand hand) {
        Holder.Reference<DamageType> holderOrThrow = original.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NFIDamageTypes.BLOOD_SLASH);
        if(original.getOwner() != null){
            return new EpicFightDamageSource(holderOrThrow, this.original, original.getOwner(), original.position())
                    .setAnimation(animation)
                    .setBaseArmorNegation(this.getArmorNegation(hand))
                    .setBaseImpact(this.getImpact(hand))
                    .setUsedItem(this.original.getItemInHand(hand));
        }
        return new EpicFightDamageSource(holderOrThrow, this.original, this.original, original.position())
                .setAnimation(animation)
                .setBaseArmorNegation(this.getArmorNegation(hand))
                .setBaseImpact(this.getImpact(hand))
                .setUsedItem(this.original.getItemInHand(hand));
    }

    public boolean isOwner(Entity target) {
        if(original.getOwner() != null){
            if (target instanceof PartEntity<?> part) {
                return part.getParent() == original.getOwner();
            } else {
                return target == original.getOwner();
            }
        }
        return false;
    }

    @Override
    public boolean isTargetInvulnerable(Entity target) {
        return this.isOwner(target) || super.isTargetInvulnerable(target);
    }

    @Override
    protected boolean checkLastAttackSuccess(Entity target) {
        boolean success = super.checkLastAttackSuccess(target);
        if (original.getOwner() != null) {
            EpicFightCapabilities.getUnparameterizedEntityPatch(original.getOwner(), LivingEntityPatch.class).ifPresent(entityPatch -> {
                entityPatch.setLastAttackEntity(null);
            });
        }
        return success;
    }

    @Override
    public void updateMotion(boolean considerInaction) {

    }

    @Override
    public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return null;
    }

    @Override
    public OpenMatrix4f getModelMatrix(float partialTicks) {
        return super.getModelMatrix(partialTicks).scale(2,2,2);
    }

}
