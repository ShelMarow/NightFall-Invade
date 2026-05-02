package net.shelmarow.nightfall_invade.entity.arterius;

import com.google.common.collect.ImmutableMap;
import com.hm.efn.gameasset.animations.EFNDodgeAnimations;
import com.hm.efn.gameasset.animations.EFNLanceAnimations;
import com.mojang.datafixers.util.Pair;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.shelmarow.combat_evolution.ai.CECombatBehaviors;
import net.shelmarow.combat_evolution.ai.CEHumanoidPatch;
import net.shelmarow.combat_evolution.ai.goal.CEAnimationAttackGoal;
import net.shelmarow.combat_evolution.ai.goal.CommonChasingGoal;
import net.shelmarow.combat_evolution.ai.iml.CustomExecuteEntity;
import net.shelmarow.combat_evolution.ai.util.BehaviorUtils;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.effect.CEMobEffects;
import net.shelmarow.combat_evolution.execution.ExecutionTypeManager;
import net.shelmarow.nightfall_invade.config.boss.BossConfig;
import net.shelmarow.nightfall_invade.entity.arterius.ai.ArteriusAI;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

import java.util.List;
import java.util.Set;

public class ArteriusPatch extends CEHumanoidPatch<Arterius> implements CustomExecuteEntity {

    private int stunLevel = 0;
    private int lastStunTime = 0;

    public ArteriusPatch() {
        super(Factions.NEUTRAL);
    }

    @Override
    public void setAIAsInfantry() {
        CECombatBehaviors.Builder<MobPatch<?>> builder = original.isDifficultyHard() ? ArteriusAI.HARD.get() : ArteriusAI.creatNormal();
        if(builder != null) {
            this.original.goalSelector.addGoal(0, new CEAnimationAttackGoal<>(this, builder.build()));
            this.original.goalSelector.addGoal(1, new CommonChasingGoal(this, attackRadius, this.chasingSpeed));
        }

    }

    @Override
    public void initLivingMotions(Animator animator) {
        animator.addLivingAnimation(LivingMotions.BLOCK, Animations.SPEAR_GUARD);
        animator.addLivingAnimation(LivingMotions.IDLE, EFNLanceAnimations.NF_MEEN_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, EFNLanceAnimations.NF_MEEN_WALK);
        animator.addLivingAnimation(LivingMotions.RUN, EFNLanceAnimations.NF_MEEN_RUN);
        animator.addLivingAnimation(LivingMotions.CHASE, EFNLanceAnimations.NF_MEEN_RUN);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_COMMON_NEUTRALIZED);
    }

    @Override
    protected void setWeaponMotions() {

        this.weaponLivingMotions.put(CapabilityItem.WeaponCategories.SPEAR,
                ImmutableMap.of(CapabilityItem.Styles.TWO_HAND, Set.of(
                        Pair.of(LivingMotions.BLOCK, Animations.SPEAR_GUARD),
                        Pair.of(LivingMotions.IDLE, EFNLanceAnimations.NF_MEEN_IDLE),
                        Pair.of(LivingMotions.WALK, EFNLanceAnimations.NF_MEEN_WALK),
                        Pair.of(LivingMotions.RUN, EFNLanceAnimations.NF_MEEN_RUN),
                        Pair.of(LivingMotions.CHASE, EFNLanceAnimations.NF_MEEN_RUN),
                        Pair.of(LivingMotions.DEATH, Animations.BIPED_COMMON_NEUTRALIZED)
                )));

        this.guardHitMotions.put(CapabilityItem.WeaponCategories.SPEAR,
                ImmutableMap.of(CapabilityItem.Styles.TWO_HAND, List.of(
                        Animations.LONGSWORD_GUARD_ACTIVE_HIT1,
                        Animations.LONGSWORD_GUARD_ACTIVE_HIT2,
                        Animations.SWORD_GUARD_ACTIVE_HIT1,
                        Animations.SWORD_GUARD_ACTIVE_HIT2,
                        Animations.SWORD_GUARD_ACTIVE_HIT3
                )));

        this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.SPEAR,
                ImmutableMap.of(CapabilityItem.Styles.TWO_HAND, ArteriusAI.creatNormal()));
    }

    @Override
    public void playGuardBreakSound(){
        this.playSound(EpicFightSounds.NEUTRALIZE_BOSSES.get(), 0F,0F);
    }

    @Override
    public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
        if(!(target instanceof Player)){
            damageSource.setBaseImpact(damageSource.getBaseImpact() * 2F);
        }
        AttackResult result = super.attack(damageSource,target,hand);
        if(result.resultType == AttackResult.ResultType.SUCCESS && target.isAlive()) {
            if(target.getRemainingFireTicks() <= 0){
                target.setSecondsOnFire(5);
            }

            EpicFightDamageSource source = getDamageSource(damageSource.getAnimation(), hand)
                    .addRuntimeTag(DamageTypeTags.IS_FIRE)
                    .addRuntimeTag(EpicFightDamageTypeTags.NO_STUN);

            int pre = target.invulnerableTime;
            target.invulnerableTime = 0;
            float fireDamage = (float) (original.getAttributeValue(Attributes.ATTACK_DAMAGE) * BossConfig.ARTERIUS_EX_FIRE_DAMAGE.get().floatValue());
            target.hurt(source, fireDamage);
            target.invulnerableTime = pre;
        }
        return result;
    }

    @Override
    public void tick(LivingEvent.LivingTickEvent event) {
        super.tick(event);
        //同步boss信息
        original.setStamina(CEPatchUtils.getStaminaPercent(this),CEPatchUtils.getStaminaStatus(this));

        if(stunLevel > 0 && original.tickCount - lastStunTime >= 100) {
            --stunLevel;
            lastStunTime = original.tickCount;
        }
    }

    @Override
    public void onDeath(LivingDeathEvent event) {
        super.onDeath(event);
    }

    @Override
    public void onCommonHurt(DamageSource damageSource) {
        Arterius arterius = (Arterius) getOriginal();
        if(arterius.isInBattle()){
            super.onCommonHurt(damageSource);
        }
    }

    @Override
    public float getHurtImpactPercent(DamageSource damageSource){
        Entity entity = damageSource.getEntity();
        if(entity instanceof LivingEntity livingEntity){
            CapabilityItem capabilityItem = EpicFightCapabilities.getItemStackCapability(livingEntity.getItemInHand(InteractionHand.MAIN_HAND));
            if(capabilityItem != null){
                WeaponCategory category = capabilityItem.getWeaponCategory();
                if(category == CapabilityItem.WeaponCategories.DAGGER){
                    return 0.15F;
                }
                else if(category == CapabilityItem.WeaponCategories.SWORD){
                    return 0.35F;
                }
                else if(category == CapabilityItem.WeaponCategories.AXE){
                    return 0.45F;
                }
                else if(category == CapabilityItem.WeaponCategories.SPEAR){
                    return 0.5F;
                }
                else if(category == CapabilityItem.WeaponCategories.GREATSWORD){
                    return 0.55F;
                }
            }
        }
        return 0.4F;
    }


    @Override
    public void onAttackParried(DamageSource damageSource, LivingEntityPatch<?> blocker) {
        CECombatBehaviors.Behavior<?> currentBehavior = BehaviorUtils.getCurrentBehavior(this);
        if(currentBehavior != null) {
            String rootName = currentBehavior.getBehaviorRoot().getRootName();
            String behaviorName = currentBehavior.getBehaviorName();
            if (!rootName.equals("ArteriusDashing") && !behaviorName.equals("ArteriusDashing")) {
                dealStaminaDamage(null, (float) (original.getAttributeValue(EpicFightAttributes.MAX_STAMINA.get()) * 0.012F));
            }
        }
    }

    @Override
    public boolean applyStun(StunType stunType, float stunTime){
        Arterius arterius = (Arterius) getOriginal();
        if(arterius.getInvulnerableTimer() > 0) return false;

        if(stunTime > 0.5F) stunTime = 0.5F;
        boolean applied = super.applyStun(stunType, stunTime);
        if(applied && stunType != StunType.NEUTRALIZE){
            stunLevel++;
            lastStunTime = original.tickCount;
            if(stunLevel >= 3){
                double percent = Mth.clamp(0.25D * (stunLevel - 2),0,1);
                double random = Math.random();
                if(random < percent){

                    original.forceAddEffect(
                            new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(),(stunLevel - 2) * 80),
                            original
                    );

                    if(!isLogicalClient()) {
                        playAnimationSynchronized(EFNDodgeAnimations.DODGE_ROLL_B, 0F, SPAnimatorControl::new);
                    }
                    stunLevel = 0;
                }
            }
        }
        return applied;
    }

    @Override
    public OpenMatrix4f getModelMatrix(float partialTicks) {
        float scale = this.original.getScale();
        return super.getModelMatrix(partialTicks).scale(scale, scale, scale);
    }

    @Override
    public boolean canBeExecuted(LivingEntityPatch<?> entityPatch) {
        Arterius arterius = (Arterius) this.original;
        return arterius.isInBattle() && arterius.getInvulnerableTimer() == 0;
    }

    @Override
    public boolean canUseCustomType(LivingEntityPatch<?> executorPatch, ExecutionTypeManager.Type executionType) {
        return false;
    }

    @Override
    public ExecutionTypeManager.Type getExecutionType(LivingEntityPatch<?> executorPatch, ExecutionTypeManager.Type executionType) {
        return ExecutionTypeManager.DEFAULT_TYPE;
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
            case NEUTRALIZE -> {
                return Animations.GREATSWORD_GUARD_BREAK;
            }
            case FALL -> {
                return Animations.BIPED_LANDING;
            }
            default -> {
                return null;
            }
        }
    }
}
