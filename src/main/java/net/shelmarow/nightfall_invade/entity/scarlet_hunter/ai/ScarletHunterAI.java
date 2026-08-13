package net.shelmarow.nightfall_invade.entity.scarlet_hunter.ai;

import com.asanginxst.epicfightx.gameassets.animations.AnimationsX;
import com.asanginxst.epicfightx.gameassets.animations.ExtraAnimations;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hm.efn.gameasset.EFNAnimations;
import com.hm.efn.gameasset.animations.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.shelmarow.combat_evolution.ai.CECombatBehaviors;
import net.shelmarow.combat_evolution.ai.StaminaStatus;
import net.shelmarow.combat_evolution.ai.condition.HealthCheck;
import net.shelmarow.combat_evolution.ai.event.*;
import net.shelmarow.combat_evolution.ai.params.AnimationParams;
import net.shelmarow.combat_evolution.ai.params.PhaseParams;
import net.shelmarow.combat_evolution.ai.util.BehaviorUtils;
import net.shelmarow.combat_evolution.ai.util.CEParticleUtils;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.client.particle.CEParticles;
import net.shelmarow.combat_evolution.effect.CEMobEffects;
import net.shelmarow.nightfall_invade.entity.scarlet_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.scarlet_hunter.ScarletHunterPatch;
import net.shelmarow.nightfall_invade.entity.misc.blood_bomb.BloodBoom;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.BloodSlashEntity;
import net.shelmarow.nightfall_invade.particle.NFIParticles;
import net.shelmarow.nightfall_invade.utils.EntityUtils;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.forgeevent.EntityStunEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class ScarletHunterAI {

    public static CECombatBehaviors.Builder<MobPatch<?>> createNormal() {
        return CECombatBehaviors.builder()
                /*------------------------------------------------------------------------------------------*/
                .addStunEvent(StunType.SHORT, mobPatch -> {
                    if(mobPatch.getTarget() == null) return;
                    CEPatchUtils.setPhase(mobPatch, 10);
                    mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.HIGH_STUN_IMMUNITY.get(), 30, 0, false, false));
                    if(Math.random() <= 0.3){
                        switch (mobPatch.getOriginal().level().random.nextInt(0,4)){
                            case 0->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_L, 0F);
                            }
                            case 1->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_R, 0F);
                            }
                            case 2->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_B, 0F);
                            }
                            case 3->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_F, 0F);
                            }
                        }
                        mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT, 0, 0);
                    }
                })

                .addStunEvent(StunType.LONG, mobPatch -> {
                    if(mobPatch.getTarget() == null) return;
                    CEPatchUtils.setPhase(mobPatch, 10);
                    mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.HIGH_STUN_IMMUNITY.get(), 30, 0, false, false));
                    if(Math.random() <= 0.3){
                        switch (mobPatch.getOriginal().level().random.nextInt(0,4)){
                            case 0->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_L, 0F);
                            }
                            case 1->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_R, 0F);
                            }
                            case 2->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_B, 0F);
                            }
                            case 3->{
                                mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_F, 0F);
                            }
                        }
                        mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT, 0, 0);
                    }
                })

                .setNoBehaviorHurt((mobPatch, damageSource, attackResult) -> {
                    if(!mobPatch.isStunned() && CEPatchUtils.getStaminaStatus(mobPatch) != StaminaStatus.BREAK){
                        if(Math.random() <= 0.5){
                            switch (mobPatch.getOriginal().level().random.nextInt(0,4)){
                                case 0->{
                                    mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_L, 0F);
                                }
                                case 1->{
                                    mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_R, 0F);
                                }
                                case 2->{
                                    mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_B, 0F);
                                }
                                case 3->{
                                    mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_F, 0F);
                                }
                            }
                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT, 0, 0);
                            return new AttackResult(AttackResult.ResultType.MISSED, 0);
                        }
                    }
                    return attackResult;
                })


                /*------------------------------------------------------------------------------------------*/

                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("全局-防反A")
                        .backAfterFinished(true)
                        .maxCooldown(200)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .attackLevel(1,2)
                                .withinDistance(0,5)
                                .guard(10)
                                .counterAnimation(EFNScytheAnimations.SCYTHE_HARVEST, new AnimationParams()
                                        .playSpeed(1.0F)
                                        .damageMultiplier(1.25F)
                                        .armorNegationMultiplier(2.0F))
                                .counterType(CECombatBehaviors.CounterType.END)
                                .maxGuardHit(3)
                                .setPhase(0)
                                .resetGuardTime(true)
                                .onCounterStart(
                                        new CounterStartEvent(highStunImmunity(60)),
                                        new CounterStartEvent(mobPatch -> {CEPatchUtils.setPhase(mobPatch,1);})
                                )

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .phaseContain(0)
                                        .animationBehavior(AnimationsX.BIPED_STEP_LEFT , new AnimationParams()
                                                .playSpeed(1.5F))
                                        .addTimeEvent(lookAtTarget())
                                        .onBehaviorStart(highStunImmunity(40))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0,6)
                                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_DASH, new AnimationParams()
                                                        .transitionTime(0.1F).playSpeed(1.5F)
                                                )
                                                .onBehaviorStart(highStunImmunity(40))
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0,3)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_AIR_SLASH, 0.1F)
                                                .onBehaviorStart(highStunImmunity(40))
                                        )
                                )

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .phaseContain(0)
                                        .animationBehavior(AnimationsX.BIPED_STEP_RIGHT, new AnimationParams()
                                                .playSpeed(1.5F))
                                        .addTimeEvent(lookAtTarget())
                                        .onBehaviorStart(highStunImmunity(40))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0,6)
                                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_DASH, new AnimationParams()
                                                        .transitionTime(0.1F).playSpeed(1.5F)
                                                )
                                                .onBehaviorStart(highStunImmunity(40))
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0,3)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_AIR_SLASH, 0.1F)
                                                .onBehaviorStart(highStunImmunity(40))
                                        )
                                )

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .phaseContain(1)
                                        .wander(0,0,0)
                                        .setPhase(0)
                                )
                        )
                )

                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("全局-防反B")
                        .backAfterFinished(false)
                        .maxCooldown(200)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .attackLevel(1,2)
                                .withinDistance(0,5)
                                .counterAnimation(EFNScytheAnimations.SCYTHE_HARVEST, new AnimationParams())
                                .guard(60)
                                .resetGuardTime(true)
                                .counterType(CECombatBehaviors.CounterType.END)
                                .maxGuardHit(3)
                                .setPhase(0)
                                .setBeforeCounterEvent(new BeforeCounterEvent(mobPatch -> {
                                    mobPatch.playAnimationSynchronized(EFNDodgeAnimations.YAMATO_STEP_F, 0F);
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT, 0, 0);
                                    return true;
                                }))
                                .onCounterStart(highStunImmunity(60))

                        )
                )

                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("全局-投技")
                        .backAfterFinished(false)
                        .maxCooldown(200)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .interruptedByTime(0.9F, 1.2F)
                                .animationBehavior(ExtraAnimations.SCYTHE_SKILL_01_GRAB_PRE, new AnimationParams()
                                        .transitionTime(0.35F)
                                )
                                .setPhase(0)
                                .addTimeEvent(lookAtTarget())
                                .addHitEvent(new HitEvent(AttackResult.ResultType.SUCCESS, (mobPatch, entity) -> {
                                    EpicFightCapabilities.getUnparameterizedEntityPatch(entity, LivingEntityPatch.class).ifPresent(entityPatch -> {
                                        EntityStunEvent entityStunEvent = new EntityStunEvent(EpicFightDamageSources.mobAttack((LivingEntity) entityPatch.getOriginal()), entityPatch, StunType.LONG);
                                        if(!MinecraftForge.EVENT_BUS.post(entityStunEvent) && entityPatch.isStunned()){
                                            CEPatchUtils.setPhase(mobPatch,1);
                                            if(entityPatch instanceof PlayerPatch<?> playerPatch){
                                                Vec3 targetPos = playerPatch.getOriginal().position();
                                                Vec3 selfPos = mobPatch.getOriginal().position();
                                                Vec3 lookVec = selfPos.subtract(targetPos);
                                                double yRot = MathUtils.getYRotOfVector(lookVec);
                                                playerPatch.setModelYRot((float) yRot, true);
                                            }
                                            else{
                                                entityPatch.getOriginal().lookAt(EntityAnchorArgument.Anchor.FEET, mobPatch.getOriginal().position());
                                            }
                                        }
                                    });
                                }))
                                .onBehaviorStart(spawnBypassGuardParticle())

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .phaseContain(1)
                                        .withinDistance(0,2)
                                        .canInterruptParent(true)
                                        .animationBehavior(ExtraAnimations.SCYTHE_SKILL_01_GRAB_DRAIN, new AnimationParams())
                                        .setPhase(0)
                                        .addTimeEvent(lookAtTarget())
                                        .onBehaviorStart(mobPatch -> {
                                            LivingEntity target = mobPatch.getTarget();
                                            if(target != null && target.position().distanceToSqr(mobPatch.getOriginal().position()) < 2 * 2){
                                                LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
                                                if(entityPatch != null && entityPatch.getArmature() instanceof HumanoidArmature){
                                                    entityPatch.playAnimationSynchronized(ExtraAnimations.BIPED_SCYTHE_SKILL_01_GRAB_DRAIN_HIT, 0F);
                                                }
                                            }
                                        })
                                        .addTimeEvent(new TimeEvent(0.5F, 1.6F,(mobPatch) -> {
                                            mobPatch.getOriginal().heal(mobPatch.getOriginal().getMaxHealth() * 0.005F);
                                        }))
                                )

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .phaseContain(1)
                                        .withinDistance(0,2)
                                        .canInterruptParent(true)
                                        .animationBehavior(ExtraAnimations.SCYTHE_SKILL_01_GRAB_BEHEAD, new AnimationParams())
                                        .setPhase(0)
                                        .addTimeEvent(lookAtTarget())
                                        .onBehaviorStart(mobPatch -> {
                                            LivingEntity target = mobPatch.getTarget();
                                            if(target != null && target.position().distanceToSqr(mobPatch.getOriginal().position()) < 2 * 2){
                                                LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
                                                if(entityPatch != null && entityPatch.getArmature() instanceof HumanoidArmature){
                                                    entityPatch.playAnimationSynchronized(ExtraAnimations.BIPED_SCYTHE_SKILL_01_GRAB_BEHEAD_HIT, 0F);
                                                }
                                            }
                                        })
                                )
                        )
                )

                /*------------------------------------------------------------------------------------------*/

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("眩晕后防御")
                        .priority(1.2).weight(1)
                        .maxCooldown(600)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,6)
                                .phaseContain(10)
                                .guard(60)
                                .counterAnimation(EFNScytheAnimations.SCYTHE_AUTO5, new AnimationParams().transitionTime(0.25F))
                                .counterType(CECombatBehaviors.CounterType.END)
                                .maxGuardHit(2)
                                .setPhase(0)
                                .onCounterStart(highStunImmunity(60))
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .phaseContain(10)
                                .guard(60)
                                .counterAnimation(EFNDodgeAnimations.YAMATO_STEP_U, 0)
                                .counterType(CECombatBehaviors.CounterType.END)
                                .maxGuardHit(2)
                                .setPhase(0)
                                .onCounterStart(
                                        new CounterStartEvent(highStunImmunity(40)),
                                        new CounterStartEvent(mobPatch -> {
                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 2.5, 0);
                                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                            CEPatchUtils.setPhase(mobPatch,1);
                                        })
                                )

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .phaseContain(1)
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AIR_SLASH, new AnimationParams())
                                        .setPhase(0)
                                        .addTimeEvent(new TimeEvent(0.56F, mobPatch -> {
                                            if(mobPatch instanceof ScarletHunterPatch scarletHunterPatch){
                                                scarletHunterPatch.getOriginal().setCanBypassSpeedLimit(true);
                                                scarletHunterPatch.getOriginal().addDeltaMovement(new Vec3(0,-10,0));
                                            }
                                        }))
                                )
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("Phase非0重置")
                        .priority(1.1).weight(1)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .phaseBetween(1, 1000)
                                .wander(0,0,0)
                                .setPhase(0)
                        )
                )

                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("近距离随机闪避")
                        .priority(1).weight(1)
                        .maxCooldown(200)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,2)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_B, 0)
                                .onBehaviorStart(mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,2)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_L, 0)
                                .onBehaviorStart(mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,2)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_R, 0)
                                .onBehaviorStart(mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("普攻")
                        .priority(1).weight(24)
                        .maxCooldown(10)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4)
                                .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO1, new AnimationParams()
                                        .transitionTime(0.25F).playSpeed(1.0F)
                                )
                                .setPhase(0)
                                .onBehaviorStart(highStunImmunity(5))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 6)
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO2, new AnimationParams()
                                                .transitionTime(0.25F).playSpeed(1.2F)
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInsertGlobalBehavior(true, "全局-防反A","全局-防反B")
                                                .withinDistance(0, 6)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO3, new AnimationParams()
                                                        .transitionTime(0.25F).playSpeed(1.2F)
                                                )
                                                .onBehaviorStart(highStunImmunity(40))
                                                .waitTime(20)

                                                //75%血以下变为完整版
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .withinDistance(0, 6)
                                                        .health(0.75F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO4, new AnimationParams()
                                                                .transitionTime(0.05F).playSpeed(1.2F)
                                                        )

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .canInsertGlobalBehavior(true, "全局-防反A","全局-防反B")
                                                                .withinDistance(0, 6)
                                                                .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO5, new AnimationParams())
                                                                .onBehaviorStart(highStunImmunity(40))
                                                                .waitTime(20)
                                                        )
                                                )
                                        )
                                )
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4)
                                .animationBehavior(EFNScytheAnimations.SCYTHE_DASH, new AnimationParams()
                                        .transitionTime(0.25F).playSpeed(1.0F)
                                )
                                .setPhase(0)
                                .onBehaviorStart(highStunImmunity(5))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 6)
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO3, new AnimationParams()
                                                .transitionTime(0.15F).playSpeed(1.2F)
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInsertGlobalBehavior(true, "全局-防反A","全局-防反B")
                                                .withinDistance(0, 6)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO4, new AnimationParams()
                                                        .transitionTime(0.15F).playSpeed(1.2F)
                                                )
                                                .onBehaviorStart(highStunImmunity(40))
                                                .waitTime(20)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .canInsertGlobalBehavior(true, "全局-防反A","全局-防反B")
                                                        .withinDistance(0, 6)
                                                        .health(0.75F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO5, new AnimationParams())
                                                        .onBehaviorStart(highStunImmunity(40))
                                                        .waitTime(20)
                                                )
                                        )
                                )
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4)
                                .setPhase(0)
                                .animationBehavior(EFNScytheAnimations.SCYTHE_AIR_SLASH, new AnimationParams()
                                        .transitionTime(0.25F).playSpeed(1.0F)
                                )
                                .onBehaviorStart(highStunImmunity(5))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 6)
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO4, new AnimationParams()
                                                .transitionTime(0.15F).playSpeed(1.35F)
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInsertGlobalBehavior(true, "全局-防反A","全局-防反B")
                                                .withinDistance(0, 6)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO5, new AnimationParams()
                                                        .transitionTime(0.35F).playSpeed(1.15F)
                                                        .addPhase(0, new PhaseParams().damageMultiplier(1.25F))
                                                        .addPhase(1, new PhaseParams().damageMultiplier(1.75F))
                                                )
                                                .onBehaviorStart(highStunImmunity(40))
                                                .waitTime(40)
                                        )
                                )
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("前闪转刀")
                        .priority(1).weight(14)
                        .maxCooldown(120)
                        .cooldown(80)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4)
                                .animationBehavior(AnimationsX.SPEAR_TWOHAND_AUTO1, new AnimationParams()
                                        .transitionTime(0.25F))
                                .onBehaviorStart(highStunImmunity(20))
                                .setPhase(0)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 6)
                                        .animationBehavior(AnimationsX.SPEAR_TWOHAND_AUTO2, new AnimationParams())
                                        .onBehaviorStart(highStunImmunity(20))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .wander(5,0,0)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_F, new AnimationParams())
                                                        .onBehaviorStart(highStunImmunity(20), mobPatch -> {
                                                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                                        })

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .withinDistance(0, 6)
                                                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO5, new AnimationParams()
                                                                        .transitionTime(0.25F)
                                                                        .playSpeed(0.65F)
                                                                        .addPhase(2, new PhaseParams()
                                                                                .stunType(StunType.LONG)
                                                                                .damageMultiplier(2.0F)
                                                                                .damageSource(Set.of(
                                                                                        EpicFightDamageTypeTags.GUARD_PUNCTURE
                                                                                ))
                                                                        )
                                                                )
                                                                .onBehaviorStart(highStunImmunity(60))
                                                                .addHitEvent(new HitEvent(2, (mobPatch, entity) ->{
                                                                    entity.level().playSound(null,entity.getX(), entity.getY(), entity.getZ(),EpicFightSounds.BLADE_RUSH_FINISHER.get(), SoundSource.HOSTILE,1,1);
                                                                }))
                                                                .addTimeEvent(
                                                                        lookAtTarget(),
                                                                        new TimeEvent(0.45F, spawnBypassGuardParticle()),
                                                                        new TimeEvent(0.45F, mobPatch -> {
                                                                            CEPatchUtils.setPlaySpeed(mobPatch,0.4F);
                                                                        }),
                                                                        new TimeEvent(0.9F, mobPatch -> {
                                                                            CEPatchUtils.setPlaySpeed(mobPatch,1.0F);
                                                                        })
                                                                )
                                                        )
                                                )
                                        )


                                )
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("左闪接平A第五段")
                        .priority(1).weight(14)
                        .maxCooldown(120)
                        .cooldown(80)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_L, new AnimationParams())
                                .onBehaviorStart(highStunImmunity(20), mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                                .addTimeEvent(lookAtTarget())
                                .setPhase(0)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO5, new AnimationParams()
                                                .transitionTime(0.25F)
                                                .playSpeed(0.75F)
                                                .addPhase(0, new PhaseParams()
                                                        .damageMultiplier(1.75F)
                                                        .damageSource(Set.of(
                                                                EpicFightDamageTypeTags.BYPASS_DODGE
                                                        ))
                                                )
                                                .addPhase(1, new PhaseParams()
                                                        .damageMultiplier(1.75F)
                                                        .damageSource(Set.of(
                                                                EpicFightDamageTypeTags.UNBLOCKALBE
                                                        ))
                                                )
                                        )
                                        .onBehaviorStart(highStunImmunity(60), spawnBypassDodgeParticle())
                                        .addTimeEvent(new TimeEvent(0.75F, mobPatch -> {
                                            if(mobPatch.getTarget() instanceof Player) {
                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false, false));
                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.75, 0));
                                            }
                                        }))
                                )
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("右闪接转圈圈")
                        .priority(1).weight(12)
                        .maxCooldown(120)
                        .cooldown(80)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_R, new AnimationParams())
                                .onBehaviorStart(highStunImmunity(20), mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                                .addTimeEvent(lookAtTarget())
                                .setPhase(0)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_HARVEST, new AnimationParams()
                                                .transitionTime(0.25F)
                                                .addPhase(1, new PhaseParams()
                                                        .damageMultiplier(1.5F)
                                                        .damageSource(Set.of(
                                                                EpicFightDamageTypeTags.BYPASS_DODGE
                                                        ))
                                                )
                                        )
                                        .onBehaviorStart(highStunImmunity(60), spawnBypassDodgeParticle())
                                )
                        )
                )

                //远距离追击
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("远距离追击")
                        .priority(1).weight(5)
                        .maxCooldown(200)
                        .cooldown(80)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(5, 64)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_F, new AnimationParams())
                                .addTimeEvent(lookAtTarget())
                                .setPhase(0)
                                .onBehaviorStart(highStunImmunity(20), mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                    teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 4, 0);
                                })

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInsertGlobalBehavior(true, "全局-防反A","全局-防反B")
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_DASH, new AnimationParams())
                                        .addTimeEvent(lookAtTarget())
                                        .waitTime(20)
                                )
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("投技")
                        .priority(1).weight(24)
                        .maxCooldown(800)
                        .cooldown(200)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .health(0.8F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .withinDistance(0, 5)
                                .canInsertGlobalBehavior(true,"全局-投技")
                                .wander(0,0,0)
                                .onBehaviorStart(highStunImmunity(100))
                                .setPhase(0)
                        )
                )

                //75%血以下新增连段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("连闪穿插攻击")
                        .priority(1).weight(12)
                        .maxCooldown(200)
                        .cooldown(200)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4)
                                .health(0.75F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_F, 0F)
                                .onBehaviorStart(highStunImmunity(100), mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                    teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 0, 0);
                                })
                                .setPhase(0)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0,6)
                                        .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO1, new AnimationParams()
                                                .playSpeed(1.5F)
                                        )
                                        .addTimeEvent(lookAtTarget())

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0, 8)
                                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO2, new AnimationParams()
                                                        .playSpeed(1.5F)
                                                )
                                                .addTimeEvent(lookAtTarget())

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_F, 0F)
                                                        .onBehaviorStart(highStunImmunity(100), mobPatch -> {
                                                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 0, 0);
                                                        })

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .withinDistance(0,6)
                                                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO3, new AnimationParams()
                                                                        .playSpeed(1.5F)
                                                                )
                                                                .addTimeEvent(lookAtTarget())

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO4, new AnimationParams()
                                                                                .playSpeed(1.5F)
                                                                        )
                                                                        .addTimeEvent(lookAtTarget())

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_F, 0F)
                                                                                .onBehaviorStart(highStunImmunity(100), mobPatch -> {
                                                                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                                                                    if(mobPatch.getTarget() != null){
                                                                                        teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 1.5, 0);
                                                                                        if(mobPatch.getTarget() instanceof Player) {
                                                                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                                                                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                                                            CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                                            CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                                                        }
                                                                                    }
                                                                                })

                                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                        .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_U, 0F)
                                                                                        .onBehaviorStart(highStunImmunity(100), mobPatch -> {
                                                                                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                                                                        })
                                                                                        .addTimeEvent(lookAtTarget())

                                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                                .interruptedByTime(3.75F, 10F)
                                                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_FINISHER, new AnimationParams()
                                                                                                        .transitionTime(-2.35F)
                                                                                                        .addPhase(-1, new PhaseParams()
                                                                                                                .damageMultiplier(5F)
                                                                                                                .stunType(StunType.KNOCKDOWN)
                                                                                                                .damageSource(Set.of(EpicFightDamageTypeTags.FINISHER))
                                                                                                        )
                                                                                                )
                                                                                                .onBehaviorStart(mobPatch -> {
                                                                                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.BYPASS_DODGE_EFFECT.get(), 40, 255));
                                                                                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.BYPASS_GUARD_EFFECT.get(), 40, 255));
                                                                                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 255));
                                                                                                })
                                                                                                .addTimeEvent(new TimeEvent(3.75F, mobPatch -> {
                                                                                                    CEPatchUtils.setPlaySpeed(mobPatch, 1F);
                                                                                                    if(mobPatch instanceof ScarletHunterPatch scarletHunterPatch){
                                                                                                        scarletHunterPatch.getOriginal().setCanBypassStunImmunity(true);
                                                                                                        mobPatch.playAnimationSynchronized(Animations.BIPED_COMMON_NEUTRALIZED,0.35F);
                                                                                                    }
                                                                                                }))
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                //65%血出现战吼血飞弹，衔接蓄力突进
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("战吼血飞弹，衔接蓄力突进")
                        .priority(1.0).weight(60)
                        .maxCooldown(1200)
                        .cooldown(100)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .setPhase(0)
                                .withinDistance(0,16)
                                .custom(isPhaseTwo())
                                .animationBehavior(EFNClawAnimations_N.NF_CLAW_BEASTROAR, new AnimationParams()
                                        .transitionTime(0.15F)
                                        .playSpeed(0.7F)
                                        .addPhase(-1, new PhaseParams()
                                                .stunType(StunType.NONE)
                                                .damageMultiplier(0)
                                                .damageSource(Set.of(
                                                        EpicFightDamageTypeTags.NO_STUN
                                                ))
                                        )
                                )
                                .addTimeEvent(
                                        new TimeEvent(0.3F, mobPatch -> {
                                            EntityUtils.pushEntitiesAway(
                                                    mobPatch.getOriginal(),
                                                    mobPatch.getOriginal().level(),
                                                    8, 2.0F, 0
                                            );
                                        }),
                                        new TimeEvent(0.35F, mobPatch -> {
                                            double verticalSpeed = 0.3 + Math.random() * 0.3;
                                            float damage = (float) mobPatch.getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.08F;
                                            spawnBloodBoom(mobPatch, 70, 20, 30, 4, verticalSpeed, damage);
                                        })
                                )
                                .onBehaviorStart(fullStunImmunity(300))


                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .interruptedByTime(1.75F, 10F)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARGING_MOB, new AnimationParams()
                                                .transitionTime(0.3F)
                                                .playSpeed(0.35F))
                                        .addTimeEvent(lookAtTarget())
                                        .setOnHurtEvent(new OnHurtEvent((mobPatch, damageSource, attackResult) -> {
                                            return new AttackResult(AttackResult.ResultType.MISSED, 0F);
                                        }))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInterruptParent(true)
                                                .interruptedByTime(0.15F, 10F)
                                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_F, 0)
                                                .addTimeEvent(lookAtTarget())
                                                .onBehaviorStart(mobPatch -> {
                                                    teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(),3,0);
                                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                                })

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .canInterruptParent(true)
                                                        .interruptedByLevel(3)
                                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_FIRST, new AnimationParams()
                                                                .playSpeed(1.35F)
                                                                .addPhase(-1, new PhaseParams()
                                                                        .impactMultiplier(20F)
                                                                        .stunType(StunType.KNOCKDOWN)
                                                                )
                                                        )

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .canInterruptParent(true)
                                                                .interruptedByTime(0.3F,0.4F)
                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2,new AnimationParams()
                                                                        .transitionTime(0.25F).playSpeed(1F)
                                                                )
                                                                .onBehaviorStart(mobPatch -> {
                                                                    mobPatch.playSound(SoundEvents.TRIDENT_RIPTIDE_3,1,0,0);
                                                                })
                                                                .onBehaviorStart(highStunImmunity(120))

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .canInterruptParent(true)
                                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1,new AnimationParams()
                                                                                .transitionTime(0.5F).playSpeed(1F)
                                                                                .addPhase(-1, new PhaseParams()
                                                                                        .damageMultiplier(2.5F)
                                                                                        .damageSource(Set.of(
                                                                                                EpicFightDamageTypeTags.FINISHER
                                                                                        ))
                                                                                )
                                                                        )
                                                                        .onBehaviorStart(spawnBypassGuardParticle(0.75))
                                                                        .addTimeEvent(
                                                                                new TimeEvent(0.6F,mobPatch -> {
                                                                                    CEPatchUtils.setPlaySpeed(mobPatch,0.75F);
                                                                                }),
                                                                                new TimeEvent(0.7F,mobPatch -> {
                                                                                    CEPatchUtils.setPlaySpeed(mobPatch,1.25F);
                                                                                }),
                                                                                new TimeEvent(1.3F,mobPatch -> {
                                                                                    CEPatchUtils.setPlaySpeed(mobPatch,0.65F);
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                //50%血出现赤色末路大招并转阶段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("赤色末路")
                        .priority(100).weight(100)
                        .maxCooldown(1200)

                        //转阶段专用
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .name("转阶段")
                                .priority(100)
                                .custom(mobPatch -> {
                                    ScarletHunter scarletHunter = (ScarletHunter) mobPatch.getOriginal();
                                    return scarletHunter.getBossPhase() == 0 && scarletHunter.getPhaseChangeCounter() > 0;
                                })
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_B, new AnimationParams())
                                .onBehaviorStart(mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                                .addTimeEvent(lookAtTarget())
                                .onBehaviorStart(fullStunImmunity(100))
                                .addCooldown(-1000)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .interruptedByTime(0.05F, 10F)
                                        .animationBehavior(EFNAnimations.DMC5_V_JC, 0)
                                        .onBehaviorStart(mobPatch -> {
                                            if(mobPatch.getTarget() instanceof Player) {
                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));
                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.25, 0));
                                            }
                                        })


                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInterruptParent(true)
                                                .setCooldown(0)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_SCARLET_END, new AnimationParams()
                                                        .transitionTime(0.15F)
                                                        .playSpeed(0.3F)
                                                        .addPhase(-1, new PhaseParams()
                                                                .damageMultiplier(0.75F)
                                                                .impactMultiplier(10)
                                                                .damageSource(Set.of(
                                                                        EpicFightDamageTypeTags.BYPASS_DODGE
                                                                ))
                                                        )
                                                        .addPhase(2, new PhaseParams()
                                                                .damageMultiplier(5F)
                                                                .impactMultiplier(10)
                                                                .damageSource(Set.of(
                                                                        EpicFightDamageTypeTags.BYPASS_DODGE,
                                                                        DamageTypeTags.BYPASSES_ENCHANTMENTS
                                                                ))
                                                        )
                                                )
                                                .addTimeEvent(
                                                        new TimeEvent(0.35F,mobPatch -> {
                                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 4.0, 0);
                                                            CEPatchUtils.setPlaySpeed(mobPatch,0.75F);
                                                        })
                                                )
                                                .onBehaviorStart(mobPatch -> {

                                                    CEPatchUtils.setStaminaStatus(mobPatch, StaminaStatus.COMMON);
                                                    CEPatchUtils.setStamina(mobPatch,CEPatchUtils.getMaxStamina(mobPatch));

                                                    ScarletHunter scarletHunter = (ScarletHunter) mobPatch.getOriginal();
                                                    scarletHunter.setPhaseChangeCounter(80);

                                                    mobPatch.playSound(SoundEvents.WITHER_SPAWN,1,0,0);

                                                    List<LivingEntity> entitiesInRange = mobPatch.getOriginal().level().getEntitiesOfClass(LivingEntity.class, mobPatch.getOriginal().getBoundingBox().inflate(30F), living -> {
                                                        return (mobPatch.getTarget() != null && living == mobPatch.getTarget()) ||
                                                                living != mobPatch.getOriginal() &&
                                                                        living.isAlive() &&
                                                                        (living instanceof Player player && !player.isCreative() && !player.isSpectator()) &&
                                                                        living.distanceToSqr(mobPatch.getOriginal().position()) <= 16 * 16 &&
                                                                        mobPatch.getOriginal().canAttack(living, TargetingConditions.forCombat())
                                                                ;
                                                    });

                                                    for (LivingEntity entity : entitiesInRange) {
                                                        entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                                                        entity.addEffect(new MobEffectInstance(ModEffect.EFFECTSTUN.get(), 30, 3));
                                                        if(entity instanceof Player player){
                                                            PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
                                                            if (playerPatch != null) {
                                                                playerPatch.setStamina(0F);
                                                                playerPatch.setStaminaRegenAwaitTicks(0);
                                                            }
                                                        }
                                                    }

                                                })
                                        )
                                )
                        )

                        //普通释放款
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .name("二阶段普通释放用")
                                .priority(1)
                                .withinDistance(0,8)
                                .custom(isPhaseTwo())
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_B, new AnimationParams())
                                .onBehaviorStart(fullStunImmunity(120),mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                                .setPhase(0)
                                .addTimeEvent(lookAtTarget())
                                .onBehaviorStart(fullStunImmunity(100))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .interruptedByTime(0.05F, 10F)
                                        .animationBehavior(EFNAnimations.DMC5_V_JC, 0)
                                        .onBehaviorStart(mobPatch -> {
                                            if(mobPatch.getTarget() instanceof Player) {
                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));
                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.25, 0));
                                            }
                                        })


                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInterruptParent(true)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_SCARLET_END, new AnimationParams()
                                                        .transitionTime(0.15F)
                                                        .playSpeed(0.3F)
                                                        .addPhase(-1, new PhaseParams()
                                                                .damageMultiplier(0.75F)
                                                                .impactMultiplier(10)
                                                                .damageSource(Set.of(
                                                                        EpicFightDamageTypeTags.BYPASS_DODGE
                                                                ))
                                                        )
                                                        .addPhase(2, new PhaseParams()
                                                                .damageMultiplier(5F)
                                                                .impactMultiplier(10)
                                                                .damageSource(Set.of(
                                                                        EpicFightDamageTypeTags.BYPASS_DODGE,
                                                                        DamageTypeTags.BYPASSES_ENCHANTMENTS
                                                                ))
                                                        )
                                                )
                                                .addTimeEvent(
                                                        new TimeEvent(0.35F,mobPatch -> {
                                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 4.0, 0);
                                                            CEPatchUtils.setPlaySpeed(mobPatch,0.75F);

                                                            float damage = (float) (mobPatch.getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.20);
                                                            spawnBloodBoom(mobPatch, 75, 10, 80, 1F, 0.4, damage);
                                                        })
                                                )
                                                .onBehaviorStart(mobPatch -> {
                                                    mobPatch.playSound(SoundEvents.WITHER_SPAWN,1,0,0);
                                                    List<LivingEntity> entitiesInRange = mobPatch.getOriginal().level().getEntitiesOfClass(LivingEntity.class, mobPatch.getOriginal().getBoundingBox().inflate(30F), living -> {
                                                        return (mobPatch.getTarget() != null && living == mobPatch.getTarget()) ||
                                                                living != mobPatch.getOriginal() &&
                                                                        living.isAlive() &&
                                                                        (living instanceof Player player && !player.isCreative() && !player.isSpectator()) &&
                                                                        living.distanceToSqr(mobPatch.getOriginal().position()) <= 16 * 16 &&
                                                                        mobPatch.getOriginal().canAttack(living, TargetingConditions.forCombat())
                                                                ;
                                                    });

                                                    for (LivingEntity entity : entitiesInRange) {
                                                        entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                                                        entity.addEffect(new MobEffectInstance(ModEffect.EFFECTSTUN.get(), 30, 3));
                                                        if(entity instanceof Player player){
                                                            PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
                                                            if (playerPatch != null) {
                                                                playerPatch.setStamina(0F);
                                                                playerPatch.setStaminaRegenAwaitTicks(0);
                                                            }
                                                        }
                                                    }

                                                })
                                        )
                                )
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .name("三阶段普通释放用")
                                .priority(2)
                                .withinDistance(0,8)
                                .custom(isPhaseThree())
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_B, new AnimationParams())
                                .onBehaviorStart(fullStunImmunity(120),mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,0,0);
                                })
                                .setPhase(0)
                                .addTimeEvent(lookAtTarget())
                                .onBehaviorStart(fullStunImmunity(100))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .interruptedByTime(0.05F, 10F)
                                        .animationBehavior(EFNAnimations.DMC5_V_JC, 0)
                                        .onBehaviorStart(mobPatch -> {
                                            if(mobPatch.getTarget() instanceof Player) {
                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));
                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.25, 0));
                                            }
                                        })


                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInterruptParent(true)
                                                .animationBehavior(EFNScytheAnimations.SCYTHE_SCARLET_END, new AnimationParams()
                                                        .transitionTime(0.15F)
                                                        .playSpeed(0.3F)
                                                        .addPhase(-1, new PhaseParams()
                                                                .damageMultiplier(0.75F)
                                                                .impactMultiplier(10)
                                                                .damageSource(Set.of(
                                                                        EpicFightDamageTypeTags.BYPASS_DODGE
                                                                ))
                                                        )
                                                        .addPhase(2, new PhaseParams()
                                                                .damageMultiplier(5F)
                                                                .impactMultiplier(10)
                                                                .damageSource(Set.of(
                                                                        EpicFightDamageTypeTags.BYPASS_DODGE,
                                                                        DamageTypeTags.BYPASSES_ENCHANTMENTS
                                                                ))
                                                        )
                                                )
                                                .addTimeEvent(
                                                        new TimeEvent(0.35F,mobPatch -> {
                                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 4.0, 0);
                                                            CEPatchUtils.setPlaySpeed(mobPatch,0.75F);

                                                            float damage = (float) (mobPatch.getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.08);
                                                            spawnBloodBoom(mobPatch, 75, 5, 80, 1F, 0.4, damage);
                                                        }),
                                                        new TimeEvent(1.65F,mobPatch -> {
                                                            if(mobPatch.getTarget() instanceof Player) {
                                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                            }
                                                        }),
                                                        new TimeEvent(1.35F, 1.85F, mobPatch -> {LivingEntity original = mobPatch.getOriginal();
                                                            Level level = original.level();
                                                            if(level instanceof ServerLevel serverLevel){
                                                                float yaw = mobPatch.getYRot();
                                                                float pitch = 0;
                                                                for (int i = 0; i < 8; i++){
                                                                    spawnWarningLineParticle(serverLevel, original.position(), yaw + i * 360F / 8F, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                }
                                                            }
                                                        }),
                                                        new TimeEvent(2.0F, mobPatch -> {
                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                            float yaw = mobPatch.getYRot();
                                                            float pitch = 0;
                                                            float roll = 0;
                                                            for (int i = 0; i < 8; i++){
                                                                spawnBloodSlash(mobPatch, yaw + i * 360F / 8F, pitch, roll, 2, 0.1F, 0.25F);
                                                            }

                                                        }),
                                                        new TimeEvent(2.45F,mobPatch -> {
                                                            if(mobPatch.getTarget() instanceof Player) {
                                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                            }
                                                        }),
                                                        new TimeEvent(2.15F, 2.65F, mobPatch -> {LivingEntity original = mobPatch.getOriginal();
                                                            Level level = original.level();
                                                            if(level instanceof ServerLevel serverLevel){
                                                                float yaw = mobPatch.getYRot();
                                                                float pitch = 0;
                                                                for (int i = 0; i < 8; i++){
                                                                    spawnWarningLineParticle(serverLevel, original.position(), yaw + 22.5F + i * 360F / 8F, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                }
                                                            }
                                                        }),
                                                        new TimeEvent(2.8F, mobPatch -> {
                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                            float yaw = mobPatch.getYRot();
                                                            float pitch = 0;
                                                            float roll = 90;
                                                            for (int i = 0; i < 8; i++){
                                                                spawnBloodSlash(mobPatch, yaw + 22.5F + i * 360F / 8F, pitch, roll, 2, 0.1F, 0.25F);
                                                            }

                                                        })

                                                )
                                                .onBehaviorStart(mobPatch -> {
                                                    mobPatch.playSound(SoundEvents.WITHER_SPAWN,1,0,0);
                                                    List<LivingEntity> entitiesInRange = mobPatch.getOriginal().level().getEntitiesOfClass(LivingEntity.class, mobPatch.getOriginal().getBoundingBox().inflate(30F), living -> {
                                                        return (mobPatch.getTarget() != null && living == mobPatch.getTarget()) ||
                                                                living != mobPatch.getOriginal() &&
                                                                        living.isAlive() &&
                                                                        (living instanceof Player player && !player.isCreative() && !player.isSpectator()) &&
                                                                        living.distanceToSqr(mobPatch.getOriginal().position()) <= 16 * 16 &&
                                                                        mobPatch.getOriginal().canAttack(living, TargetingConditions.forCombat())
                                                                ;
                                                    });

                                                    for (LivingEntity entity : entitiesInRange) {
                                                        entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                                                        entity.addEffect(new MobEffectInstance(ModEffect.EFFECTSTUN.get(), 30, 3));
                                                        if(entity instanceof Player player){
                                                            PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
                                                            if (playerPatch != null) {
                                                                playerPatch.setStamina(0F);
                                                                playerPatch.setStaminaRegenAwaitTicks(0);
                                                            }
                                                        }
                                                    }

                                                })
                                                //罚站一会
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .wander(30,0,0)
                                                )
                                        )
                                )
                        )
                )


                //二阶段追加远程飞弹
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(30)
                        .maxCooldown(900)
                        .cooldown(100)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .custom(isPhaseTwo())
                                .withinDistance(0,32)
                                .setPhase(0)
                                .animationBehavior(EFNSkillAnimations.STOMP, new AnimationParams())
                                .addTimeEvent(new TimeEvent(0.6F, mobPatch -> {
                                    double damage = mobPatch.getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.08F;
                                    spawnBloodBoom(mobPatch, 60, 10, 80, 2, 0.3, (float) damage);
                                }))
                                .onBehaviorStart(highStunImmunity(100))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_B, 0F)
                                        .onBehaviorStart(mobPatch -> {
                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), -0.5, 0);
                                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,1,0,0);
                                        })
                                        .addTimeEvent(lookAtTarget())

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .interruptedByTime(1.0F, 10F)
                                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_AIRSLASH_NEW, new AnimationParams()
                                                        .transitionTime(0.25F)
                                                        .addPhase(-1, new PhaseParams()
                                                                .damageSource(Set.of(
                                                                        EpicFightDamageTypeTags.BYPASS_DODGE
                                                                ))
                                                        )
                                                )
                                                .onBehaviorStart(spawnBypassDodgeParticle())

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .canInterruptParent(true)
                                                        .animationBehavior(EFNSkillAnimations.EFN_GUARD_ACTIVE_HIT3, 0)

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .animationBehavior(ExtraAnimations.SPEAR_TWOHAND_AUTO5, new AnimationParams()
                                                                        .transitionTime(0.25F)
                                                                        .playSpeed(0.6F)
                                                                )

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .wander(40, 0, 0)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                //二阶段追加猎魂
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(40)
                        .maxCooldown(300)
                        .cooldown(100)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,6)
                                .custom(isPhaseTwo())
                                .setPhase(0)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_B, 0F)
                                .onBehaviorStart(mobPatch -> {
                                    teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), -0.25, 0.25);
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,1,0,0);
                                    if (mobPatch.getTarget() != null) {
                                        mobPatch.getOriginal().lookAt(EntityAnchorArgument.Anchor.FEET, mobPatch.getTarget().position());
                                    }
                                })
                                .addTimeEvent(lookAtTarget())

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNSkillAnimations.EXECUTION, new AnimationParams()
                                                .transitionTime(-0.9F)
                                                .playSpeed(0.85F)
                                                .addPhase(-1, new PhaseParams()
                                                        .damageMultiplier(2.5F)
                                                        .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE))
                                                )
                                        )
                                        .onBehaviorStart(spawnBypassDodgeParticle(), mobPatch -> {
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.BYPASS_DODGE_EFFECT.get(), 20, 0, false, false));
                                        })

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .interruptedByLevel(3)
                                                .wander(20,0,0)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .canInterruptParent(true)
                                                        .interruptedByTime(0.2F, 10F)
                                                        .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_U, new AnimationParams())
                                                        .onBehaviorStart(mobPatch -> {
                                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), -2.5, 0);
                                                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,1,0,0);
                                                            if (mobPatch.getTarget() != null) {
                                                                mobPatch.getOriginal().lookAt(EntityAnchorArgument.Anchor.FEET, mobPatch.getTarget().position());
                                                            }
                                                        })

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .canInterruptParent(true)
                                                                .interruptedByLevel(3)
                                                                .animationBehavior(EFNScytheAnimations.SCYTHE_AIR_SLASH, new AnimationParams()
                                                                        .addPhase(-1, new PhaseParams()
                                                                                .damageMultiplier(3F)
                                                                                .damageSource(Set.of(EpicFightDamageTypeTags.UNBLOCKALBE))
                                                                        )
                                                                )
                                                                .onBehaviorStart(mobPatch -> {
                                                                    if(mobPatch.getTarget() instanceof Player) {
                                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.75, 0));
                                                                    }
                                                                }, mobPatch -> {
                                                                    if (mobPatch.getTarget() != null) {
                                                                        mobPatch.getOriginal().lookAt(EntityAnchorArgument.Anchor.FEET, mobPatch.getTarget().position());
                                                                    }
                                                                })

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .canInterruptParent(true)
                                                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_AIRSLASH_NEW, new AnimationParams()
                                                                                .transitionTime(0.1F)
                                                                                .playSpeed(1F)
                                                                                .addPhase(-1, new PhaseParams()
                                                                                        .damageMultiplier(3.5F)
                                                                                        .impactMultiplier(0.25F)
                                                                                        .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE))
                                                                                )
                                                                        )
                                                                        .addTimeEvent(
                                                                                new TimeEvent(0.3F, mobPatch -> {
                                                                                    CEPatchUtils.setPlaySpeed(mobPatch,0.1F);
                                                                                    if(mobPatch.getTarget() instanceof Player) {
                                                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.25, 0));
                                                                                    }
                                                                                }),
                                                                                new TimeEvent(0.4F, mobPatch -> {
                                                                                    CEPatchUtils.setPlaySpeed(mobPatch,1.25F);

                                                                                    if(mobPatch instanceof ScarletHunterPatch scarletHunterPatch){
                                                                                        scarletHunterPatch.getOriginal().setCanBypassSpeedLimit(true);
                                                                                        scarletHunterPatch.getOriginal().addDeltaMovement(new Vec3(0,-1.5,0));
                                                                                    }
                                                                                }),
                                                                                new TimeEvent(0.8F, mobPatch -> {
                                                                                    CEPatchUtils.setPlaySpeed(mobPatch,0.5F);
                                                                                })
                                                                        )
                                                                        .addBlockedEvent(new BlockedEvent(true, (mobPatch, entityPatch) -> {
                                                                            if(mobPatch instanceof ScarletHunterPatch scarletHunterPatch){
                                                                                ScarletHunter scarletHunter = scarletHunterPatch.getOriginal();
                                                                                scarletHunter.setCanBypassStunImmunity(true);
                                                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.HIGH_STUN_IMMUNITY.get(), 30, 0, false, false, false));
                                                                                mobPatch.playAnimationSynchronized(AnimationsX.BIPED_COMMON_NEUTRALIZED, 0F);
                                                                                mobPatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(),  1,0,0);
                                                                                BehaviorUtils.stopCurrentBehavior(mobPatch.getOriginal());
                                                                                scarletHunterPatch.dealStaminaDamage(null,
                                                                                        Math.min(CEPatchUtils.getMaxStamina(mobPatch) * 0.2F, CEPatchUtils.getStamina(mobPatch) - 0.1F)
                                                                                );
                                                                            }
                                                                        }))
                                                                        .onBehaviorStart(mobPatch -> {
                                                                            if(mobPatch instanceof ScarletHunterPatch scarletHunterPatch) {
                                                                                scarletHunterPatch.getOriginal().setCanBypassSpeedLimit(true);
                                                                                mobPatch.getOriginal().addDeltaMovement(new Vec3(0, 1, 0));
                                                                            }
                                                                        })

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .wander(30,0,0)
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                //二阶段新增连段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(12)
                        .maxCooldown(100)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,4)
                                .custom(isPhaseTwo())
                                .setPhase(0)
                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO1, new AnimationParams()
                                        .playSpeed(1.25F))
                                .onBehaviorStart(highStunImmunity(20))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0,6)
                                        .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO2, new AnimationParams()
                                                .playSpeed(1.25F)
                                                .addPhase(-1, new PhaseParams()
                                                        .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE))
                                                )
                                        )
                                        .onBehaviorStart(spawnBypassDodgeParticle(),highStunImmunity(20))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .animationBehavior(AnimationsX.BIPED_STEP_RIGHT, new AnimationParams()
                                                        .playSpeed(2F))
                                                .addTimeEvent(lookAtTarget())
                                                .onBehaviorStart(mobPatch -> {
                                                    teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 1.5, 0);
                                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,1,0,0);
                                                })
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .withinDistance(0,7)
                                                        .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO3, new AnimationParams()
                                                                .playSpeed(1.25F))
                                                        .onBehaviorStart(highStunImmunity(20))

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .withinDistance(0,8)
                                                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO4, new AnimationParams()
                                                                        .playSpeed(1.25F)
                                                                        .addPhase(-1, new PhaseParams()
                                                                                .damageSource(Set.of(EpicFightDamageTypeTags.UNBLOCKALBE))
                                                                        )
                                                                )
                                                                .onBehaviorStart(spawnBypassGuardParticle(),highStunImmunity(20))

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .animationBehavior(AnimationsX.BIPED_STEP_LEFT, new AnimationParams()
                                                                                .playSpeed(2F))
                                                                        .addTimeEvent(lookAtTarget())
                                                                        .onBehaviorStart(mobPatch -> {
                                                                            teleportInFrontAlongLine(mobPatch.getOriginal(), mobPatch.getTarget(), 1.5, 0);
                                                                            mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT,1,0,0);
                                                                        })

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .withinDistance(0,8)
                                                                                .animationBehavior(ExtraAnimations.SCYTHE_TWOHAND_AUTO5, new AnimationParams()
                                                                                        .transitionTime(0.25F)
                                                                                        .playSpeed(0.75F)
                                                                                        .addPhase(2, new PhaseParams()
                                                                                                .stunType(StunType.LONG)
                                                                                                .damageMultiplier(2.0F)
                                                                                                .damageSource(Set.of(
                                                                                                        EpicFightDamageTypeTags.BYPASS_DODGE
                                                                                                ))
                                                                                        )
                                                                                )
                                                                                .addTimeEvent(lookAtTarget(), new TimeEvent(0.45F, spawnBypassDodgeParticle()))
                                                                                .onBehaviorStart(highStunImmunity(40))

                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("全局-空中剑气A")
                        .backAfterFinished(true)
                        .maxCooldown(900)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .name("空中蓄力派生")
                                .interruptedByTime(0.3F, 10F)
                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_AIR, new AnimationParams()
                                        .transitionTime(0.15F))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_CHARGE_AIR, new AnimationParams()
                                                .playSpeed(0.7F))
                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                        .addTimeEvent(
                                                new TimeEvent(0,0.7F,mobPatch -> {
                                                    LivingEntity original = mobPatch.getOriginal();
                                                    Level level = original.level();
                                                    if(level instanceof ServerLevel serverLevel){
                                                        float yaw = mobPatch.getYRot();
                                                        float pitch = 0;
                                                        if(mobPatch.getTarget() != null){
                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                            pitch = Mth.clamp(pitch, -35.0F, 35.0F) + 2F;
                                                        }
                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw - 30, pitch + 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw - 15, pitch - 5, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw +  0, pitch + 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw + 15, pitch - 5, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw + 30, pitch + 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                    }
                                                }),
                                                new TimeEvent(0.3F,mobPatch -> {
                                                    if(mobPatch.getTarget() instanceof Player) {
                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                    }
                                                }),
                                                new TimeEvent(0.7F, mobPatch -> {
                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                    float yaw = mobPatch.getYRot();
                                                    float pitch = 0;
                                                    if(mobPatch.getTarget() != null){
                                                        pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                        pitch = Mth.clamp(pitch, -35.0F, 35.0F) + 2F;
                                                    }
                                                    float roll = 90;
                                                    spawnBloodSlash(mobPatch, yaw - 30, pitch + 0, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw - 15, pitch - 5, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw +  0, pitch + 0, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw + 15, pitch - 5, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw + 30, pitch + 0, roll, 2, 0.1F, 0.25F);

                                                })
                                        )
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .wander(30,0,0)
                                        )
                                )
                        )
                )

                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("全局-空中剑气B")
                        .backAfterFinished(true)
                        .maxCooldown(900)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .name("空中连斩落地派生")
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_U, new AnimationParams())
                                .onBehaviorStart(mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT, 2, 0, 0);
                                })

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_X_AIR, new AnimationParams()
                                                .transitionTime(0.05F)
                                                .playSpeed(1.5F))
                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 2.5F);})
                                        .addTimeEvent(
                                                new TimeEvent(0,0.25F,mobPatch -> {
                                                    LivingEntity original = mobPatch.getOriginal();
                                                    Level level = original.level();
                                                    if(level instanceof ServerLevel serverLevel){
                                                        float yaw = mobPatch.getYRot();
                                                        float pitch = 0;
                                                        if(mobPatch.getTarget() != null){
                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                            pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                        }
                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                    }
                                                }),
                                                new TimeEvent(0.25F, mobPatch -> {
                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                    float yaw = mobPatch.getYRot();
                                                    float pitch = 0;
                                                    if(mobPatch.getTarget() != null){
                                                        pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                        pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                    }
                                                    float roll = -45;
                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll, 2, 0.05F, 0.25F);
                                                })
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XX_AIR, new AnimationParams()
                                                        .transitionTime(0.05F)
                                                        .playSpeed(1.5F))
                                                .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 2.5F);})
                                                .addTimeEvent(
                                                        new TimeEvent(0,0.25F,mobPatch -> {
                                                            LivingEntity original = mobPatch.getOriginal();
                                                            Level level = original.level();
                                                            if(level instanceof ServerLevel serverLevel){
                                                                float yaw = mobPatch.getYRot();
                                                                float pitch = 0;
                                                                if(mobPatch.getTarget() != null){
                                                                    pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                    pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                                }
                                                                spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                            }
                                                        }),
                                                        new TimeEvent(0.25F, mobPatch -> {
                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                            float yaw = mobPatch.getYRot();
                                                            float pitch = 0;
                                                            if(mobPatch.getTarget() != null){
                                                                pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                            }
                                                            float roll = 0;
                                                            spawnBloodSlash(mobPatch, yaw, pitch, roll, 2, 0.05F, 0.25F);
                                                        })
                                                )

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_X_AIR, new AnimationParams()
                                                                .transitionTime(0.05F)
                                                                .playSpeed(1.5F))
                                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 2.5F);})
                                                        .addTimeEvent(
                                                                new TimeEvent(0,0.25F,mobPatch -> {
                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                    Level level = original.level();
                                                                    if(level instanceof ServerLevel serverLevel){
                                                                        float yaw = mobPatch.getYRot();
                                                                        float pitch = 0;
                                                                        if(mobPatch.getTarget() != null){
                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                            pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                                        }
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                    }
                                                                }),
                                                                new TimeEvent(0.25F, mobPatch -> {
                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                    float yaw = mobPatch.getYRot();
                                                                    float pitch = 0;
                                                                    if(mobPatch.getTarget() != null){
                                                                        pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                        pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                                    }
                                                                    float roll = -45;
                                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll, 2, 0.05F, 0.25F);
                                                                })
                                                        )

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .interruptedByTime(0.5F, 10F)
                                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XX_AIR, new AnimationParams()
                                                                        .transitionTime(0.05F)
                                                                        .playSpeed(1.5F))
                                                                .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 2.5F);})
                                                                .addTimeEvent(
                                                                        new TimeEvent(0,0.25F,mobPatch -> {
                                                                            LivingEntity original = mobPatch.getOriginal();
                                                                            Level level = original.level();
                                                                            if(level instanceof ServerLevel serverLevel){
                                                                                float yaw = mobPatch.getYRot();
                                                                                float pitch = 0;
                                                                                if(mobPatch.getTarget() != null){
                                                                                    pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                                    pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                                                }
                                                                                spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                            }
                                                                        }),
                                                                        new TimeEvent(0.25F, mobPatch -> {
                                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                            float yaw = mobPatch.getYRot();
                                                                            float pitch = 0;
                                                                            if(mobPatch.getTarget() != null){
                                                                                pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                                pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                                            }
                                                                            float roll = 0;
                                                                            spawnBloodSlash(mobPatch, yaw, pitch, roll, 2, 0.05F, 0.25F);
                                                                        })
                                                                )

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .canInterruptParent(true)
                                                                        .interruptedByTime(0.3F, 10F)
                                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_AIR, new AnimationParams()
                                                                                .transitionTime(0.15F)
                                                                                .playSpeed(2F))

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .interruptedByTime(0.25F, 10F)
                                                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XXY, new AnimationParams()
                                                                                        .transitionTime(0.15F).playSpeed(0.5F))

                                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                        .canInterruptParent(true)
                                                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XXY_CHARGE, new AnimationParams()
                                                                                                .playSpeed(0.85F))
                                                                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 2.5F);})
                                                                                        .addTimeEvent(
                                                                                                new TimeEvent(0,1F,mobPatch -> {
                                                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                                                    Level level = original.level();
                                                                                                    if(level instanceof ServerLevel serverLevel){
                                                                                                        float yaw = mobPatch.getYRot();
                                                                                                        float pitch = 0;
                                                                                                        if(mobPatch.getTarget() != null){
                                                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                                                            pitch = Mth.clamp(pitch, -60.0F, 60.0F);
                                                                                                        }
                                                                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                    }
                                                                                                }),
                                                                                                new TimeEvent(0.6F,mobPatch -> {
                                                                                                    if(mobPatch.getTarget() instanceof Player) {
                                                                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                                                                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                                                                    }
                                                                                                }),
                                                                                                new TimeEvent(1.0F, mobPatch -> {
                                                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                                                    float yaw = mobPatch.getYRot();
                                                                                                    float pitch = 0;
                                                                                                    float roll = 45;
                                                                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll, 1, 0F, 0.25F);
                                                                                                }),
                                                                                                new TimeEvent(1.05F, mobPatch -> {
                                                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                                                    float yaw = mobPatch.getYRot();
                                                                                                    float pitch = 0;
                                                                                                    float roll = -45;
                                                                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll, 1, 0F, 0.25F);
                                                                                                }),
                                                                                                new TimeEvent(1.1F, mobPatch -> {
                                                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                                                    float yaw = mobPatch.getYRot();
                                                                                                    float pitch = 0;
                                                                                                    float roll = 45;
                                                                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll, 1, 0F, 0.25F);
                                                                                                }),
                                                                                                new TimeEvent(1.15F, mobPatch -> {
                                                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                                                    float yaw = mobPatch.getYRot();
                                                                                                    float pitch = 0;
                                                                                                    float roll = -45;
                                                                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll, 1, 0F, 0.25F);
                                                                                                })
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                //三阶段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("飞空蓄力剑气")
                        .priority(2).weight(1)
                        .maxCooldown(800)
                        .cooldown(600)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .custom(isPhaseThree())
                                .withinDistance(0,12)
                                .setPhase(0)
                                .animationBehavior(EFNDodgeAnimations.MURASAMA_ROLL_B, new AnimationParams())
                                .addTimeEvent(lookAtTarget())
                                .onBehaviorStart(highStunImmunity(200))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .wander(5,0,0)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInsertGlobalBehavior(true, "全局-空中剑气A","全局-空中剑气B")
                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XY_CHARGE, new AnimationParams()
                                                        .transitionTime(0.2F))
                                                .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                                .addTimeEvent(
                                                        new TimeEvent(0,1.2F,mobPatch -> {
                                                            LivingEntity original = mobPatch.getOriginal();
                                                            Level level = original.level();
                                                            if(level instanceof ServerLevel serverLevel){
                                                                float yaw = mobPatch.getYRot();
                                                                float pitch = 0;
                                                                Vec3 position = original.position();
                                                                spawnWarningLineParticle(serverLevel, position, yaw - 30, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                spawnWarningLineParticle(serverLevel, position, yaw - 15, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                spawnWarningLineParticle(serverLevel, position, yaw -  0, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                spawnWarningLineParticle(serverLevel, position, yaw + 15, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                spawnWarningLineParticle(serverLevel, position, yaw + 30, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                            }
                                                        }),
                                                        new TimeEvent(1.2F, mobPatch -> {
                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                            float yaw = mobPatch.getYRot();
                                                            float pitch = 0;
                                                            float roll = 0;
                                                            spawnBloodSlash(mobPatch, yaw - 30, pitch, roll, 2, 0.1F, 0.25F);
                                                            spawnBloodSlash(mobPatch, yaw - 15, pitch, roll, 2, 0.1F, 0.25F);
                                                            spawnBloodSlash(mobPatch, yaw +  0, pitch, roll, 2, 0.1F, 0.25F);
                                                            spawnBloodSlash(mobPatch, yaw + 15, pitch, roll, 2, 0.1F, 0.25F);
                                                            spawnBloodSlash(mobPatch, yaw + 30, pitch, roll, 2, 0.1F, 0.25F);
                                                        })
                                                )
                                                .onBehaviorStart(mobPatch -> {
                                                    if(mobPatch.getTarget() instanceof Player) {
                                                        mobPatch.playSound(SoundEvents.FIRECHARGE_USE,2,0,0);
                                                        EntityUtils.pushEntitiesAwayByDistance(mobPatch.getOriginal(), mobPatch.getOriginal().level(), 16, 2.0F, 0.15F);
                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                    }
                                                })
                                        )
                                )
                        )
                )


                //远距离追击
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("居合突进")
                        .priority(2).weight(1)
                        .maxCooldown(800)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .custom(isPhaseThree())
                                .withinDistance(0,16)
                                .animationBehavior(EFNDodgeAnimations.YAMATO_STEP_B, new AnimationParams())
                                .addTimeEvent(lookAtTarget())
                                .onBehaviorStart(mobPatch -> {
                                    mobPatch.playSound(SoundEvents.ENDERMAN_TELEPORT, 2,0,0);
                                })

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_CHARGE_THROUGH, new AnimationParams()
                                                .transitionTime(0.15F).playSpeed(0.7F))
                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                        .onBehaviorStart(highStunImmunity(200),mobPatch -> {
                                            LivingEntity original = mobPatch.getOriginal();
                                            Level level = original.level();
                                            EntityUtils.pushEntitiesAwayByDistance(original, level, 12, 2.25F, 0.2F);
                                            if(level instanceof ServerLevel serverLevel){
                                                serverLevel.sendParticles(ParticleTypes.SOUL, original.getX(), original.getY() + 1, original.getZ(),100,0,0,0,1);
                                            }
                                        })
                                        .addTimeEvent(
                                                new TimeEvent(0,0.5F,mobPatch -> {
                                                    LivingEntity original = mobPatch.getOriginal();
                                                    Level level = original.level();
                                                    if(level instanceof ServerLevel serverLevel){
                                                        float yaw = mobPatch.getYRot();
                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                    }
                                                }),
                                                new TimeEvent(0.4F, mobPatch -> {
                                                    if(mobPatch.getTarget() instanceof Player) {
                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                    }
                                                }),
                                                new TimeEvent(0.5F, mobPatch -> {
                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                    float yaw = mobPatch.getYRot();
                                                    float pitch = 0;
                                                    float roll = 0;
                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll + 90, 2, 0.1F, 0.25F);
                                                }),
                                                new TimeEvent(0.58F, mobPatch -> {
                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                    float yaw = mobPatch.getYRot();
                                                    float pitch = 0;
                                                    float roll = 0;
                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll + 45, 5, 0.1F, 0.25F);
                                                }),
                                                new TimeEvent(0.66F, mobPatch -> {
                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                    float yaw = mobPatch.getYRot();
                                                    float pitch = 0;
                                                    float roll = 0;
                                                    spawnBloodSlash(mobPatch, yaw, pitch, roll - 45, 2, 0.1F, 0.25F);
                                                })
                                        )


                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .interruptedByTime(0.25F, 10F)
                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XXY, new AnimationParams()
                                                        .transitionTime(0.15F)
                                                        .playSpeed(0.5F))

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .canInterruptParent(true)
                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XXY_CHARGE, new AnimationParams()
                                                                .playSpeed(0.85F))
                                                        .onBehaviorStart(mobPatch -> {
                                                            EntityUtils.pushEntitiesAwayByDistance(mobPatch.getOriginal(), mobPatch.getOriginal().level(), 10, 2F, 0.2F);
                                                        })
                                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                                        .addTimeEvent(
                                                                new TimeEvent(0,1F,mobPatch -> {
                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                    Level level = original.level();
                                                                    if(level instanceof ServerLevel serverLevel){
                                                                        float yaw = mobPatch.getYRot();
                                                                        for(int i = 0; i < 8; i++){
                                                                            spawnWarningLineParticle(serverLevel, original.position(), yaw + 45F * i, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                            spawnWarningLineParticle(serverLevel, original.position(), yaw + 22.5F + 45F * i, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        }
                                                                    }
                                                                }),
                                                                new TimeEvent(1.0F, mobPatch -> {
                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                    float yaw = mobPatch.getYRot();
                                                                    float pitch = 0;
                                                                    float roll = 0;
                                                                    for(int i = 0; i < 8; i++) {
                                                                        spawnBloodSlash(mobPatch, yaw + 45 * i, pitch, roll, 2, 0.1F, 0.5F);
                                                                    }
                                                                }),
                                                                new TimeEvent(1.15F, mobPatch -> {
                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                    float yaw = mobPatch.getYRot();
                                                                    float pitch = 0;
                                                                    float roll = 0;
                                                                    for(int i = 0; i < 8; i++) {
                                                                        spawnBloodSlash(mobPatch, yaw + 22.5F + 45F * i, pitch, roll, 2, 0.1F, 0.5F);
                                                                    }
                                                                })
                                                        )
                                                )
                                        )

                                )
                        )
                )

                //其他蓄力组合
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("普攻接踢腿，冲刺蓄力，上挑发波")
                        .priority(1).weight(100)
                        .maxCooldown(300)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,6)
                                .custom(isPhaseThree())
                                .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO1, new AnimationParams()
                                        .transitionTime(0.25F).playSpeed(1.25F)
                                )
                                .onBehaviorStart(highStunImmunity(200))
                                .setPhase(0)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNScytheAnimations.SCYTHE_AUTO2, new AnimationParams()
                                                .transitionTime(0.25F).playSpeed(1.25F)
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_KICK_Y, new AnimationParams()
                                                        .transitionTime(0.25F).playSpeed(0.85F)
                                                        .addPhase(1, new PhaseParams()
                                                                .impactMultiplier(10)
                                                                .damageSource(Set.of(EpicFightDamageTypeTags.NO_STUN))
                                                        )
                                                )
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .wander(5,0,0)

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_CHARGE, new AnimationParams())
                                                                .onBehaviorStart(mobPatch -> {
                                                                    mobPatch.playSound(SoundEvents.FIRECHARGE_USE,2,0,0);
                                                                    EntityUtils.pushEntitiesAwayByDistance(mobPatch.getOriginal(), mobPatch.getOriginal().level(), 10, 2F, 0.2F);
                                                                })
                                                                .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                                                .addTimeEvent(
                                                                        new TimeEvent(0,0.8F,mobPatch -> {
                                                                            LivingEntity original = mobPatch.getOriginal();
                                                                            Level level = original.level();
                                                                            if(level instanceof ServerLevel serverLevel){
                                                                                float yaw = mobPatch.getYRot();
                                                                                spawnWarningLineParticle(serverLevel, original.position(), yaw, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                            }
                                                                        }),
                                                                        new TimeEvent(0.35F, mobPatch -> {
                                                                            if(mobPatch.getTarget() instanceof Player){
                                                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                                                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                                            }
                                                                        }),
                                                                        new TimeEvent(0.8F, mobPatch -> {
                                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                            float yaw = mobPatch.getYRot();
                                                                            float pitch = 0;
                                                                            float roll = 0;
                                                                            spawnBloodSlash(mobPatch, yaw, pitch, roll + 45, 1, 0.1F, 0.15F);
                                                                        }),
                                                                        new TimeEvent(0.9F, mobPatch -> {
                                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                            float yaw = mobPatch.getYRot();
                                                                            float pitch = 0;
                                                                            float roll = 0;
                                                                            spawnBloodSlash(mobPatch, yaw, pitch, roll - 45, 1, 0.1F, 0.15F);
                                                                        })
                                                                )

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XY_CHARGE, new AnimationParams())
                                                                        .onBehaviorStart(mobPatch -> {
                                                                            mobPatch.playSound(SoundEvents.FIRECHARGE_USE,2,0,0);
                                                                            EntityUtils.pushEntitiesAwayByDistance(mobPatch.getOriginal(), mobPatch.getOriginal().level(), 10, 2F, 0.2F);
                                                                        })
                                                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                                                        .addTimeEvent(
                                                                                new TimeEvent(0,1.2F,mobPatch -> {
                                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                                    Level level = original.level();
                                                                                    if(level instanceof ServerLevel serverLevel){
                                                                                        float yaw = mobPatch.getYRot();
                                                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw - 20, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw +  0, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw + 20, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                    }
                                                                                }),
                                                                                new TimeEvent(0.6F,mobPatch -> {
                                                                                    if(mobPatch.getTarget() instanceof Player) {
                                                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                                                    }
                                                                                }),
                                                                                new TimeEvent(1.2F, mobPatch -> {
                                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                                    float yaw = mobPatch.getYRot();
                                                                                    float pitch = 0;
                                                                                    float roll = 0;
                                                                                    spawnBloodSlash(mobPatch, yaw - 20, pitch, roll, 2, 0.1F, 0.3F);
                                                                                    spawnBloodSlash(mobPatch, yaw +  0, pitch, roll, 2, 0.1F, 0.3F);
                                                                                    spawnBloodSlash(mobPatch, yaw + 20, pitch, roll, 2, 0.1F, 0.3F);
                                                                                })
                                                                        )

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .name("空中变招A")
                                                                                .interruptedByTime(0.3F, 10F)
                                                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_AIR, new AnimationParams()
                                                                                        .transitionTime(0.15F)
                                                                                        .playSpeed(2F))

                                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                        .interruptedByTime(0.25F, 10F)
                                                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XXY, new AnimationParams()
                                                                                                .transitionTime(0.15F).playSpeed(0.5F))

                                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                                .canInterruptParent(true)
                                                                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XXY_CHARGE, new AnimationParams()
                                                                                                        .playSpeed(0.85F))
                                                                                                .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                                                                                .addTimeEvent(
                                                                                                        new TimeEvent(0,1.0F,mobPatch -> {
                                                                                                            LivingEntity original = mobPatch.getOriginal();
                                                                                                            Level level = original.level();
                                                                                                            if(level instanceof ServerLevel serverLevel){
                                                                                                                float yaw = mobPatch.getYRot();
                                                                                                                spawnWarningLineParticle(serverLevel, original.position(), yaw - 30, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                                spawnWarningLineParticle(serverLevel, original.position(), yaw - 15, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                                spawnWarningLineParticle(serverLevel, original.position(), yaw +  0, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                                spawnWarningLineParticle(serverLevel, original.position(), yaw + 15, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                                spawnWarningLineParticle(serverLevel, original.position(), yaw + 30, 0, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                            }
                                                                                                        }),
                                                                                                        new TimeEvent(0.6F,mobPatch -> {
                                                                                                            if(mobPatch.getTarget() instanceof Player){
                                                                                                                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                                                                                                                mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                                                                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                                                                            }
                                                                                                        }),
                                                                                                        new TimeEvent(1.0F, mobPatch -> {
                                                                                                            mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                                                            float yaw = mobPatch.getYRot();
                                                                                                            float pitch = 0;
                                                                                                            float roll = 0;
                                                                                                            spawnBloodSlash(mobPatch, yaw - 30, pitch, roll, 1, 0.1F, 0.3F);
                                                                                                            spawnBloodSlash(mobPatch, yaw - 15, pitch, roll, 1, 0.1F, 0.3F);
                                                                                                            spawnBloodSlash(mobPatch, yaw -  0, pitch, roll, 1, 0.1F, 0.3F);
                                                                                                            spawnBloodSlash(mobPatch, yaw + 15, pitch, roll, 1, 0.1F, 0.3F);
                                                                                                            spawnBloodSlash(mobPatch, yaw + 30, pitch, roll, 1, 0.1F, 0.3F);
                                                                                                        })
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .name("空中变招B")
                                                                                .interruptedByTime(0.3F, 10F)
                                                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_AIR, new AnimationParams()
                                                                                        .transitionTime(0.15F))

                                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                        .canInterruptParent(true)
                                                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_CHARGE_AIR, new AnimationParams()
                                                                                                .playSpeed(0.7F))
                                                                                        .onBehaviorStart(mobPatch -> {costStamina(mobPatch, 5F);})
                                                                                        .addTimeEvent(
                                                                                                new TimeEvent(0,0.7F,mobPatch -> {
                                                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                                                    Level level = original.level();
                                                                                                    if(level instanceof ServerLevel serverLevel){
                                                                                                        float yaw = mobPatch.getYRot();
                                                                                                        float pitch = 0;
                                                                                                        if(mobPatch.getTarget() != null){
                                                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                                                            pitch = Mth.clamp(pitch, -35.0F, 35.0F);
                                                                                                        }
                                                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw - 30, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw - 15, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw +  0, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw + 15, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw + 30, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                                                    }
                                                                                                }),
                                                                                                new TimeEvent(0.3F,mobPatch -> {
                                                                                                    if(mobPatch.getTarget() instanceof Player){
                                                                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                                                                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                                                                    }
                                                                                                }),
                                                                                                new TimeEvent(0.7F, mobPatch -> {
                                                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                                                    float yaw = mobPatch.getYRot();
                                                                                                    float pitch = 0;
                                                                                                    if(mobPatch.getTarget() != null){
                                                                                                        pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                                                        pitch = Mth.clamp(pitch, -35.0F, 35.0F);
                                                                                                    }
                                                                                                    float roll = 0;
                                                                                                    spawnBloodSlash(mobPatch, yaw - 30, pitch, roll, 2, 0.1F, 0.25F);
                                                                                                    spawnBloodSlash(mobPatch, yaw - 15, pitch, roll, 2, 0.1F, 0.25F);
                                                                                                    spawnBloodSlash(mobPatch, yaw +  0, pitch, roll, 2, 0.1F, 0.25F);
                                                                                                    spawnBloodSlash(mobPatch, yaw + 15, pitch, roll, 2, 0.1F, 0.25F);
                                                                                                    spawnBloodSlash(mobPatch, yaw + 30, pitch, roll, 2, 0.1F, 0.25F);
                                                                                                })
                                                                                        )
                                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                                .wander(30,0,0)
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                //远距离剑气（全阶段通用）
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .rootName("远距离惩罚剑气")
                        .priority(0.1).weight(1)
                        .maxCooldown(300)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .health(0.80F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .custom(mobPatch -> {
                                    if(mobPatch.getTarget() != null){
                                        Vec3 targetPos = mobPatch.getTarget().position();
                                        Vec3 selfPos = mobPatch.getOriginal().position();
                                        //平面距离
                                        return targetPos.distanceToSqr(new Vec3(selfPos.x, targetPos.y, selfPos.z)) >= 12 * 12 || targetPos.distanceToSqr(new Vec3(targetPos.x, selfPos.y, targetPos.z)) >= 5 * 5;
                                    }
                                    return false;
                                })
                                .interruptedByTime(0.25F, 10F)
                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XY, new AnimationParams())

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_XY_CHARGE, new AnimationParams()
                                                .transitionTime(0.1F)
                                                .playSpeed(0.65F))
                                        .addTimeEvent(
                                                new TimeEvent(0,1.2F,mobPatch -> {
                                                    LivingEntity original = mobPatch.getOriginal();
                                                    Level level = original.level();
                                                    if(level instanceof ServerLevel serverLevel){
                                                        float yaw = mobPatch.getYRot();
                                                        float pitch = 0;
                                                        if(mobPatch.getTarget() != null){
                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                        }
                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                    }
                                                }),
                                                new TimeEvent(0.2F, mobPatch -> {
                                                    if(mobPatch.getTarget() instanceof Player){
                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 2, 0, 0);
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 0.75, 0));
                                                        CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, 1.5, 0));
                                                    }
                                                }),
                                                new TimeEvent(1.1F,1.3F,mobPatch -> {
                                                    LivingEntity original = mobPatch.getOriginal();
                                                    Level level = original.level();
                                                    if(level instanceof ServerLevel serverLevel){
                                                        float yaw = mobPatch.getYRot();
                                                        float pitch = 0;
                                                        if(mobPatch.getTarget() != null){
                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                            pitch = Mth.clamp(pitch, -35.0F, 35.0F);
                                                        }
                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw - 40, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw - 20, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw +  0, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw + 20, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                        spawnWarningLineParticle(serverLevel, original.position(), yaw + 40, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                    }
                                                }),
                                                new TimeEvent(1.3F, mobPatch -> {
                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                    float yaw = mobPatch.getYRot();
                                                    float pitch = 0;
                                                    if(mobPatch.getTarget() != null){
                                                        pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                    }
                                                    float roll = 0;
                                                    spawnBloodSlash(mobPatch, yaw - 30, pitch, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw - 15, pitch, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw +  0, pitch, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw + 15, pitch, roll, 2, 0.1F, 0.25F);
                                                    spawnBloodSlash(mobPatch, yaw + 30, pitch, roll, 2, 0.1F, 0.25F);
                                                })
                                        )
                                        .addTimeEvent(createSlashEvents(0.4F, 0.1F, 8, 0, 45))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .interruptedByTime(0.3F, 10F)
                                                .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_AIR, new AnimationParams()
                                                        .transitionTime(0.15F))

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .canInterruptParent(true)
                                                        .animationBehavior(EFNMurasamaAnimations.HF_MURASAMA_Y_CHARGE_AIR, new AnimationParams()
                                                                .playSpeed(0.5F))
                                                        .addTimeEvent(
                                                                new TimeEvent(0,0.4F,mobPatch -> {
                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                    Level level = original.level();
                                                                    if(level instanceof ServerLevel serverLevel){
                                                                        float yaw = mobPatch.getYRot();
                                                                        float pitch = 0;
                                                                        if(mobPatch.getTarget() != null){
                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                        }
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw +  0, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                    }
                                                                }),
                                                                new TimeEvent(0.4F,0.6F,mobPatch -> {
                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                    Level level = original.level();
                                                                    if(level instanceof ServerLevel serverLevel){
                                                                        float yaw = mobPatch.getYRot();
                                                                        float pitch = 0;
                                                                        if(mobPatch.getTarget() != null){
                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                        }
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw +  30, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw +  10, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw +   0, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw -  10, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw -  30, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                    }
                                                                }),
                                                                new TimeEvent(0.6F,mobPatch -> {
                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                    Level level = original.level();
                                                                    if(level instanceof ServerLevel){
                                                                        float yaw = mobPatch.getYRot();
                                                                        float pitch = 0;
                                                                        if(mobPatch.getTarget() != null){
                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                        }
                                                                        spawnBloodSlash(mobPatch, yaw + 30, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw + 15, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw +  0, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw - 15, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw - 30, pitch, 0, 1, 0.1F, 0.15F);
                                                                    }
                                                                }),
                                                                new TimeEvent(0.7F,0.85F,mobPatch -> {
                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                    Level level = original.level();
                                                                    if(level instanceof ServerLevel serverLevel){
                                                                        float yaw = mobPatch.getYRot();
                                                                        float pitch = 0;
                                                                        if(mobPatch.getTarget() != null){
                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                        }
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw + 40, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw + 20, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw +  0, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw - 20, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                        spawnWarningLineParticle(serverLevel, original.getEyePosition(), yaw - 40, pitch, 24, 48, NFIParticles.BLOOD_B.get());
                                                                    }
                                                                }),
                                                                new TimeEvent(0.85F,mobPatch -> {
                                                                    mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2,0,0);
                                                                    LivingEntity original = mobPatch.getOriginal();
                                                                    Level level = original.level();
                                                                    if(level instanceof ServerLevel){
                                                                        float yaw = mobPatch.getYRot();
                                                                        float pitch = 0;
                                                                        if(mobPatch.getTarget() != null){
                                                                            pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                                                                        }
                                                                        spawnBloodSlash(mobPatch, yaw + 40, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw + 20, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw +  0, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw - 20, pitch, 0, 1, 0.1F, 0.15F);
                                                                        spawnBloodSlash(mobPatch, yaw - 40, pitch, 0, 1, 0.1F, 0.15F);
                                                                    }
                                                                    CEPatchUtils.setPlaySpeed(mobPatch,0.7F);
                                                                })
                                                        )
                                                        .addTimeEvent(createSlashEvents(0.2F, 0.1F, 2, 0, 45))
                                                )
                                        )
                                )
                        )
                )

                ;

    }

    private static TimeEvent[] createSlashEvents(float startTime, float timeStep, int count, float roll, float rollStep) {
        List<TimeEvent> events = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float time = startTime + timeStep * i;
            float rollC = roll + i * rollStep;

            events.add(new TimeEvent(time, mobPatch -> {
                mobPatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 2, 0, 0);
                float yaw = mobPatch.getYRot();
                float pitch = 0;
                if (mobPatch.getTarget() != null) {
                    pitch = (float) MathUtils.getXRotOfVector(mobPatch.getTarget().position().subtract(mobPatch.getOriginal().position()));
                }
                spawnBloodSlash(mobPatch, yaw, pitch, rollC, 1, 0.1F, 0.15F);
            }));
        }

        return events.toArray(new TimeEvent[0]);
    }

    private static void costStamina(MobPatch<?> mobPatch, float amount){
        float current = CEPatchUtils.getStamina(mobPatch);
        if(current > amount){
            CEPatchUtils.addStamina(mobPatch, -amount);
        }
        else {
            CEPatchUtils.setStamina(mobPatch, 0.1F);
        }
    }

    private static void spawnWarningLineParticle(ServerLevel level, Vec3 start, float yaw, float pitch, double length, int count, ParticleOptions particle) {
        float fYaw = (float) Math.toRadians(-yaw);
        float fPitch = (float) Math.toRadians(-pitch);

        double xDir = Math.sin(fYaw) * Math.cos(fPitch);
        double yDir = Math.sin(fPitch);
        double zDir = Math.cos(fYaw) * Math.cos(fPitch);

        for (int i = 0; i < count; i++) {
            double t = (double) i / count * length;

            double x = start.x + xDir * t;
            double y = start.y + yDir * t;
            double z = start.z + zDir * t;

            level.sendParticles(particle, x, y, z,1, 0, 0, 0,0);
        }
    }

    private static void spawnBloodSlash(MobPatch<?> mobPatch, float yaw, float pitch, float roll, double basicDamage, float maxHpDamage, float hpDamage) {
        if(mobPatch.getOriginal().level() instanceof ServerLevel serverLevel){
            BloodSlashEntity bloodSlash = new BloodSlashEntity(mobPatch.getOriginal(), serverLevel);
            bloodSlash.setSlashYRot(yaw);
            bloodSlash.setSlashZRot(roll);
            bloodSlash.setSlashXRot(pitch);
            bloodSlash.setBasicDamage(basicDamage);
            bloodSlash.setMaxHpDamage(maxHpDamage);
            bloodSlash.setHpDamage(hpDamage);
            bloodSlash.setPos(mobPatch.getOriginal().position().add(0,1,0));
            serverLevel.addFreshEntity(bloodSlash);
        }
    }

    private static @NotNull Function<MobPatch<?>, Boolean> isPhaseTwo() {
        return mobPatch -> {
            if (mobPatch instanceof ScarletHunterPatch scarletHunterPatch) {
                return scarletHunterPatch.getOriginal().getBossPhase() >= 1;
            }
            return false;
        };
    }

    private static @NotNull Function<MobPatch<?>, Boolean> isPhaseThree() {
        return mobPatch -> {
            if (mobPatch instanceof ScarletHunterPatch scarletHunterPatch) {
                return scarletHunterPatch.getOriginal().getBossPhase() >= 2;
            }
            return false;
        };
    }

    private static void spawnBloodBoom(LivingEntityPatch<?> caster, double maxAngle, int count, int waitTime, float delay, double verticalSpeed, float damage) {
        Level level = caster.getOriginal().level();
        if(level instanceof ServerLevel serverLevel){

            double maxAngleRad = Math.toRadians(maxAngle);
            double cosMax = Math.cos(maxAngleRad);

            for (int i = 0; i < count; i++) {
                BloodBoom bloodBoom = new BloodBoom(serverLevel);
                bloodBoom.setPos(caster.getOriginal().getEyePosition());
                bloodBoom.setOwnerPatch(caster);
                bloodBoom.setWaitTick(waitTime + (int)(delay * i));
                bloodBoom.setDamage(damage);

                double u = Math.random();
                double v = Math.random();
                double theta = 2 * Math.PI * u;
                double cosPhi = Mth.lerp(v, cosMax, 1.0);
                double sinPhi = Math.sqrt(1 - cosPhi * cosPhi);

                double x = sinPhi * Math.cos(theta);
                double z = sinPhi * Math.sin(theta);

                Vec3 dir = new Vec3(x, cosPhi, z).normalize();
                bloodBoom.setDeltaMovement(dir.scale(verticalSpeed));

                serverLevel.addFreshEntity(bloodBoom);
            }
        }
    }


    public static void teleportInFrontAlongLine(Entity from, Entity to, double distance, double yOffset) {
        if (from == null || to == null) return;

        Vec3 fromPos = from.position();
        Vec3 toPos = to.position();

        Vec3 dir = toPos.subtract(fromPos);
        dir = dir.normalize();

        Vec3 dest = toPos.subtract(dir.scale(distance));
        dest = new Vec3(dest.x, dest.y + yOffset, dest.z);

        if(distance < 0){
            double height = from.getBoundingBox().getYsize();
            double width = from.getBoundingBox().getXsize();
            AABB box = new AABB(
                    dest.x - width / 2, dest.y, dest.z - width / 2,
                    dest.x + width / 2, dest.y + height, dest.z + width / 2
            );
            if(!from.level().noCollision(box)){
                dest = toPos;
            }
        }

        from.teleportTo(dest.x, dest.y + 0.25, dest.z);
    }


    private static @NotNull Consumer<MobPatch<?>> spawnBypassGuardParticle() {
        return spawnBypassGuardParticle(1.0F);
    }

    private static @NotNull Consumer<MobPatch<?>> spawnBypassGuardParticle(double offsetY) {
        return mobPatch -> {
            if(mobPatch.getTarget() instanceof Player) {
                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_GUARD_WARNING.get(), mobPatch.getTarget(), new Vec3(0, offsetY, 0));
            }
        };
    }

    private static @NotNull Consumer<MobPatch<?>> spawnBypassDodgeParticle(){
        return spawnBypassDodgeParticle(1.0F);
    }

    private static @NotNull Consumer<MobPatch<?>> spawnBypassDodgeParticle(double offsetY) {
        return mobPatch -> {
            if(mobPatch.getTarget() instanceof Player){
                mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
                mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                CEParticleUtils.spawnWarningParticle(CEParticles.BYPASS_DODGE_WARNING.get(), mobPatch.getTarget(), new Vec3(0, offsetY, 0));
            }
        };
    }

    private static @NotNull TimeEvent lookAtTarget() {
        return new TimeEvent(0,1F,mobPatch -> {
            if (mobPatch.getTarget() != null) {
                mobPatch.getOriginal().lookAt(EntityAnchorArgument.Anchor.FEET, mobPatch.getTarget().position());
            }
        });
    }

    private static @NotNull Consumer<MobPatch<?>> fullStunImmunity(int time) {
        return mobPatch -> {
            mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), time, 0, false, false, false));
        };
    }

    private static @NotNull Consumer<MobPatch<?>> highStunImmunity(int time) {
        return mobPatch -> {
            mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.HIGH_STUN_IMMUNITY.get(), time, 0, false, false, false));
        };
    }
}
