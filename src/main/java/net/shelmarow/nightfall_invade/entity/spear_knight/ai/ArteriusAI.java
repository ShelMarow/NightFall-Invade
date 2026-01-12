package net.shelmarow.nightfall_invade.entity.spear_knight.ai;

import com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Flame_Jet_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Abyss_Fireball_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Fireball_Entity;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.hm.efn.gameasset.animations.EFNDodgeAnimations;
import com.hm.efn.gameasset.animations.EFNGreatSwordAnimations;
import com.hm.efn.gameasset.animations.EFNLanceAnimations;
import com.hm.efn.registries.EFNMobEffectRegistry;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import net.shelmarow.combat_evolution.ai.CECombatBehaviors;
import net.shelmarow.combat_evolution.ai.condition.HealthCheck;
import net.shelmarow.combat_evolution.ai.event.TimeEvent;
import net.shelmarow.combat_evolution.ai.params.AnimationParams;
import net.shelmarow.combat_evolution.ai.params.PhaseParams;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.client.particle.CEParticles;
import net.shelmarow.combat_evolution.client.particle.follow.CEFollowParticleOptions;
import net.shelmarow.combat_evolution.effect.CEMobEffects;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.spear_knight.Arterius;
import net.shelmarow.nightfall_invade.network.server.S2CPushEntityAwayPacket;
import net.shelmarow.nightfall_invade.utils.EntityUtils;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

public class ArteriusAI {
    public static final CECombatBehaviors.Builder<MobPatch<?>> NORMAL;
    public static final CECombatBehaviors.Builder<MobPatch<?>> HARD;

    static {
        HARD = CECombatBehaviors.builder()

                /*
                困难模式出招逻辑：

                所有转阶段均为完整蓄力，但是每阶段的效果不同

                一阶段：
                普通的所有普攻连段，蓄力扫地和突刺（无特效版），以及冲锋，远距离攻击等。

                二阶段：
                新增普攻招式，以及原攻击的差分版，蓄力技能出现特效强化，新增特殊技能

                三阶段：
                进一步强化技能效果，额外出现新的派生，部分攻击也获得强化，出现新的特殊技能

                四阶段：
                出现并强化原大招，技能释放频率增加

                 */

                //普攻不同攻击段数的派生招式

                //普攻连段1派生
                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(200)
                        .backAfterFinished(false).rootName("common_comboA1")

                        //后撤
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,3)
                                .animationBehavior(EFNDodgeAnimations.DODGE_ROLL_B,0F)

                                //接冲刺
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH,0)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .wander(8, 0, 0)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3,0.35F)
                                                        .addExBehavior(applyStunImmunity(80))
                                                )

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO4,0.35F)
                                                        .addExBehavior(applyStunImmunity(80))
                                                )
                                        )
                                )

                                //接大剑追击
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_FIRST,0.25F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .interruptedByTime(0.5F,0.6F)
                                                .withinDistance(0,4)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1, new AnimationParams()
                                                        .transitionTime(0.45F).playSpeed(0.8F))
                                                .addExBehavior(applyStunImmunity(100),bypassGuardWarning())
                                                .addTimeEvent(
                                                        new TimeEvent(1F, mobPatch -> {
                                                            if(mobPatch.getOriginal() instanceof Arterius arterius) {
                                                                if (arterius.getBossPhase() >= 1) {
                                                                    Level world = mobPatch.getOriginal().level();
                                                                    LivingEntity caster = mobPatch.getOriginal();
                                                                    double spawnX = caster.getX();
                                                                    double spawnY = caster.getY();
                                                                    double spawnZ = caster.getZ();
                                                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.0F;
                                                                    spawnEightDirectionJets(world, spawnX, spawnY, spawnZ, caster.getYRot(), 15, 2, damage, caster);
                                                                    spawnEightDirectionJets(world, spawnX, spawnY, spawnZ, caster.getYRot() + 22.5F, 15, 2, damage, caster);
                                                                }
                                                            }
                                                        })
                                                )
                                        )

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .interruptedByTime(0.5F,0.6F)
                                                .withinDistance(4,100)
                                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.15F)
                                                .addExBehavior(applyStunImmunity(80))
                                        )
                                )
                        )


                        //距离远直接追击
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(6,16)
                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_FIRST,0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .interruptedByTime(0.5F,0.6F)
                                        .withinDistance(0,4)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1, new AnimationParams()
                                                .transitionTime(0.45F).playSpeed(0.8F))
                                        .addExBehavior(applyStunImmunity(100),bypassGuardWarning())
                                        .addTimeEvent(
                                                new TimeEvent(1F, mobPatch -> {
                                                    if(mobPatch.getOriginal() instanceof Arterius arterius) {
                                                        if (arterius.getBossPhase() >= 1) {
                                                            Level world = mobPatch.getOriginal().level();
                                                            LivingEntity caster = mobPatch.getOriginal();
                                                            double spawnX = caster.getX();
                                                            double spawnY = caster.getY();
                                                            double spawnZ = caster.getZ();
                                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.0F;
                                                            spawnEightDirectionJets(world, spawnX, spawnY, spawnZ, caster.getYRot(), 15, 2, damage, caster);
                                                            spawnEightDirectionJets(world, spawnX, spawnY, spawnZ, caster.getYRot() + 22.5F, 15, 2, damage, caster);
                                                        }
                                                    }
                                                })
                                        )
                                )

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .interruptedByTime(0.5F,0.6F)
                                        .withinDistance(4,100)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.15F)
                                        .addExBehavior(applyStunImmunity(80))
                                )
                        )
                )

                //普攻连段2派生
                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(200)
                        .backAfterFinished(false).rootName("common_comboA2")

                        //远距离突刺
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(6,16)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2,new AnimationParams()
                                        .transitionTime(0.45F).playSpeed(0.8F))
                                .addExBehavior(applyStunImmunity(100),bypassDodgeWarning())
                        )

                        //小人车
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,6)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                        .transitionTime(0.35F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.SHORT))
                                .addExBehavior(applyStunImmunity(20))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .name("ArteriusDashing")
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                                .transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.SHORT))
                                        .addExBehavior(applyStunImmunity(20))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams()
                                                        .transitionTime(0.1F).playSpeed(1.0F))
                                                .addExBehavior(applyStunImmunity(80))
                                                .addTimeEvent(
                                                        new TimeEvent(0.25F,mobPatch -> {
                                                            CEPatchUtils.setPlaySpeed(mobPatch,0.275F);
                                                        }),
                                                        new TimeEvent(0.4F,mobPatch -> {
                                                            CEPatchUtils.setPlaySpeed(mobPatch,1.25F);
                                                        }),
                                                        new TimeEvent(1.25F,mobPatch -> {
                                                            CEPatchUtils.setPlaySpeed(mobPatch,0.5F);
                                                        })
                                                )
                                        )
                                )
                        )
                )


                //普攻连段3派生
                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(200)
                        .backAfterFinished(false).rootName("common_comboA3")

                        //长枪技能
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,6)
                                .interruptedByLevel(3)
                                .animationBehavior(Animations.SPEAR_DASH, new AnimationParams()
                                        .transitionTime(0.2F).playSpeed(0.9F)
                                        .impactMultiplier(0.7F).stunType(StunType.HOLD))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .withinDistance(0,6)
                                        .animationBehavior(Animations.GRASPING_SPIRAL_SECOND,new AnimationParams()
                                                .playSpeed(0.9F).damageMultiplier(1.3F).stunType(StunType.LONG))
                                )
                        )

                        //跳攻+冲刺
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,4)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AIRSLASH,new AnimationParams()
                                        .transitionTime(0.25F).playSpeed(1.15F))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0,6)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH,new AnimationParams()
                                                .transitionTime(0.25F).playSpeed(1.35F))
                                )
                        )

                        //远距离突刺+扫地
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .interruptedByLevel(3)
                                .withinDistance(6,16)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2,new AnimationParams()
                                        .transitionTime(0.45F).playSpeed(0.8F))
                                .addExBehavior(applyStunImmunity(100),bypassDodgeWarning())

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1,new AnimationParams()
                                                .transitionTime(0.45F).playSpeed(0.8F))
                                        .addExBehavior(applyStunImmunity(100),bypassGuardWarning())
                                        .addTimeEvent(
                                                new TimeEvent(1F, mobPatch -> {
                                                    if(mobPatch.getOriginal() instanceof Arterius arterius) {
                                                        if (arterius.getBossPhase() >= 1) {
                                                            Level world = mobPatch.getOriginal().level();
                                                            LivingEntity caster = mobPatch.getOriginal();
                                                            double spawnX = caster.getX();
                                                            double spawnY = caster.getY();
                                                            double spawnZ = caster.getZ();
                                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.0F;
                                                            spawnEightDirectionJets(world, spawnX, spawnY, spawnZ, caster.getYRot(), 15, 2, damage, caster);
                                                            spawnEightDirectionJets(world, spawnX, spawnY, spawnZ, caster.getYRot() + 22.5F, 15, 2, damage, caster);
                                                        }
                                                    }
                                                })
                                        )
                                )
                        )
                )

                .newGlobalBehavior(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(3).weight(1)//.maxCooldown(200)
                        .backAfterFinished(false).rootName("turning_slash")

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(2)
                                .interruptedByTime(0.3F,0.4F)
                                .withinDistance(0,5)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2,new AnimationParams()
                                        .transitionTime(0.25F).playSpeed(1F))
                                .addExBehavior(mobPatch -> mobPatch.playSound(SoundEvents.TRIDENT_RIPTIDE_3,1,0,0))
                                .addExBehavior(applyStunImmunity(120))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1,new AnimationParams()
                                                .transitionTime(0.5F).playSpeed(0.75F))
                                        .addExBehavior(bypassGuardWarning())
                                        .addTimeEvent(
                                                new TimeEvent(0.6F,mobPatch -> {
                                                    CEPatchUtils.setPlaySpeed(mobPatch,0.125F);
                                                }),
                                                new TimeEvent(0.7F,mobPatch -> {
                                                    CEPatchUtils.setPlaySpeed(mobPatch,1.15F);
                                                }),
                                                new TimeEvent(1.3F,mobPatch -> {
                                                    CEPatchUtils.setPlaySpeed(mobPatch,0.65F);
                                                })
                                        )
                                )
                        )
                )

                //基础普攻连段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(5)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .weight(200)
                                .canInsertGlobalBehavior(true,"common_comboA1","turning_slash")
                                .withinDistance(0,4).withinAngle(0,60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO1,0)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInsertGlobalBehavior(true,"common_comboA2")
                                        .withinDistance(0,6)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO2,0F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInsertGlobalBehavior(true,"common_comboA3")
                                                .withinDistance(0,6)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3,0F)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .weight(2)
                                                        .withinDistance(0,6)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO4,0F)
                                                        .addExBehavior(applyStunImmunity(80))
                                                )
                                        )
                                )
                        )

                        //跳攻起手
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).withinAngle(0, 60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AIRSLASH, 0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 8)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, 0.25F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .wander(8, 0, 0)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .canInsertGlobalBehavior(true,"common_comboA1")
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams()
                                                                .transitionTime(0.25F).stunType(StunType.LONG))
                                                        .addExBehavior(applyStunImmunity(80))
                                                )

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNDodgeAnimations.DODGE_ROLL_B, 0F)

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2, new AnimationParams()
                                                                        .transitionTime(0.45F).damageMultiplier(0.65F))
                                                                .addExBehavior(mobPatch -> {
                                                                    if(mobPatch.getOriginal() instanceof Arterius arterius) {
                                                                        mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                                                        mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                                                        mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                                        if (mobPatch.getTarget() != null) {
                                                                            LivingEntity target = mobPatch.getTarget();
                                                                            Vec3 pos = target.position();
                                                                            ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(), target.getId(), new Vec3(0, 1.5, 0));
                                                                            ((ServerLevel) target.level()).sendParticles(options, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
                                                                        }
                                                                        if(arterius.getBossPhase() >= 1) {
                                                                            summonFireBall(mobPatch, 7);
                                                                        }
                                                                    }
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )

                        //EF长枪起手
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .interruptedByTime(0.6F,0.7F)
                                .withinDistance(0, 4)
                                .withinAngle(0, 60)
                                .animationBehavior(Animations.BIPED_MOB_SPEAR_TWOHAND1, new AnimationParams()
                                        .transitionTime(0.1F).playSpeed(1.25F))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInsertGlobalBehavior(true,"common_comboA3")
                                        .canInterruptParent(true)
                                        .interruptedByTime(0.7F,0.8F)
                                        .withinDistance(0, 8)
                                        .animationBehavior(Animations.BIPED_MOB_SPEAR_TWOHAND2, new AnimationParams()
                                                .transitionTime(0.1F).playSpeed(1.25F))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .canInsertGlobalBehavior(true,"common_comboA2")
                                                .weight(2)
                                                .canInterruptParent(true)
                                                .withinDistance(0, 8)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO2, new AnimationParams()
                                                        .transitionTime(0.25F).stunType(StunType.LONG))

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .withinDistance(0, 8)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams()
                                                                .transitionTime(0.35F).playSpeed(0.85F).stunType(StunType.LONG))
                                                        .addExBehavior(applyStunImmunity(80))
                                                )
                                        )
                                )
                        )
                )

                //额外新增远距离攻击
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(400)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(6, 12).withinAngle(0, 60)
                                .interruptedByTime(2.1F,2.2F)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_FINISHER, 0.25F)
                                .addTimeEvent(
                                        new TimeEvent(0F,0.2F,mobPatch -> {
                                            if(mobPatch.getTarget() != null){
                                                mobPatch.rotateTo(mobPatch.getTarget(),360,false);
                                            }
                                        }),
                                        new TimeEvent(0.5F, mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float rot = caster.getYRot() + 90;
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F;
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 30,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 0, 30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 30,30,1,damage,caster);
                                        }),
                                        new TimeEvent(0.85F, mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float rot = caster.getYRot() + 90;
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F;
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 35,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 15,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 15,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 35,30,1,damage,caster);
                                        })
                                )

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.5F)
                                        .addTimeEvent(
                                                new TimeEvent(0.05F,mobPatch -> {
                                                    int random = mobPatch.getOriginal().getRandom().nextInt(5);
                                                    summonFireBall(mobPatch,random);
                                                }),
                                                new TimeEvent(0.25F,mobPatch -> {
                                                    if(mobPatch.getTarget() != null){
                                                        teleportInFrontAlongLine(mobPatch.getOriginal(),mobPatch.getTarget(),1,0);
                                                    }
                                                })
                                        )
                                )
                        )
                )


                //人车连段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(800)
                        .rootName("ArteriusDashing")
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(4.5, 8).withinAngle(0, 60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                        .transitionTime(0.5F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                        .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                .addExBehavior(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                    mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                    if (mobPatch.getTarget() != null) {
                                        LivingEntity target = mobPatch.getTarget();
                                        Vec3 pos = target.position();

                                        ParticleOptions options1 = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(), target.getId(), new Vec3(0, 1.0, 0));
                                        ((ServerLevel) target.level()).sendParticles(options1, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);

                                        ParticleOptions options2 = new CEFollowParticleOptions(CEParticles.BYPASS_DODGE_WARNING.get(), target.getId(), new Vec3(0, 1.75, 0));
                                        ((ServerLevel) target.level()).sendParticles(options2, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
                                    }
                                })
                                .addTimeEvent(new TimeEvent(lookAtTarget()))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                                .transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                                .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                        .addExBehavior(mobPatch -> {
                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                        })

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                                        .transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                                        .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                                .addExBehavior(mobPatch -> {
                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                })

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                                                .transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                                                .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                                        .addExBehavior(mobPatch -> {
                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                        })

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                                                        .transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                                                        .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                                                .addExBehavior(mobPatch -> {
                                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                                })

                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                                                                .transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                                                                .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                                                        .addExBehavior(mobPatch -> {
                                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                                        })

                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams()
                                                                                        .transitionTime(-0.45F)
                                                                                        .addPhase(0, new PhaseParams().damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                                                                                .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                                                                        .addPhase(1, new PhaseParams().damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG)
                                                                                                .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                                                                        .addPhase(2, new PhaseParams().damageMultiplier(2.0F).impactMultiplier(2.5F).stunType(StunType.LONG)
                                                                                                .damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE, EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)))
                                                                                )
                                                                                .addExBehavior(mobPatch -> {
                                                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )


                /*----------二阶段-------------*/












                /*-----------转阶段-----------*/

                //一阶段转二阶段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(8).weight(1000).maxCooldown(100)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .custom(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    return arterius.getSkillReleased() == 0 && arterius.getBossPhase() == 1;
                                })
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGING_MOB, 0.15F)
                                .addExBehavior(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    arterius.setSkillReleased(1);
                                    arterius.setInvulnerableTimer(120);
                                    arterius.setCanSetDeltaMovement(false);
                                    arterius.setCanSetDeltaMovementTimer(120);
                                    mobPatch.playSound(SoundEvents.FIRECHARGE_USE,3, 0, 0);

                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 120), mobPatch.getOriginal());
                                    ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();
                                    Vec3 pos = mobPatch.getOriginal().position();
                                    serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 200, 0, 1.5, 0, 0.25);

                                })
                                .addTimeEvent(
                                        new TimeEvent(lookAtTarget()),
                                        new TimeEvent(0,4.75F,mobPatch -> {
                                            Arterius arterius = (Arterius) mobPatch.getOriginal();
                                            ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();
                                            //推开周围实体
                                            int range = 10;
                                            float strength = 0.2F;
                                            float airBorne = 0.05F;
                                            EntityUtils.pushEntityAwayByDistance(arterius, serverLevel, range, strength, airBorne);
                                            NightFallInvade.CHANNEL.send(
                                                    PacketDistributor.TRACKING_ENTITY.with(() -> arterius),
                                                    new S2CPushEntityAwayPacket(arterius.getId(), range, strength, airBorne,1)
                                            );
                                        }),
                                        new TimeEvent(4.75F,mobPatch -> {
                                            Arterius arterius = (Arterius) mobPatch.getOriginal();
                                            ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();
                                            //推开周围实体
                                            float strength = 2.5F;
                                            float airBorne = 0.35F;
                                            int range = 12;
                                            EntityUtils.pushEntityAway(arterius, serverLevel, range, strength, airBorne);
                                            NightFallInvade.CHANNEL.send(
                                                    PacketDistributor.TRACKING_ENTITY.with(() -> arterius),
                                                    new S2CPushEntityAwayPacket(arterius.getId(), range, strength, airBorne,0)
                                            );
                                        }),
                                        new TimeEvent(5F,mobPatch -> {
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                            int random = mobPatch.getOriginal().getRandom().nextInt(7);
                                            summonFireBall(mobPatch,random);
                                        }),
                                        new TimeEvent(4.25F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 5F;
                                            spawnFlameStrike(
                                                    world,spawnX,spawnZ,
                                                    spawnY-2, spawnY + 2,caster.getYRot(),
                                                    20, 10, 0,
                                                    10F,damage,true, caster
                                            );
                                        }),
                                        new TimeEvent(4.5F,mobPatch -> {
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(EFNMobEffectRegistry.MEEN_LANCE.get(), Integer.MAX_VALUE,1));
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3F;
                                            spawnSixDirectionFlameStrike(world,spawnX,spawnY,spawnZ,caster.getYRot() + 30,14,1.2F,damage,caster);
                                        }),
                                        new TimeEvent(4.75F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3F;
                                            spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,22,30,4,1.2F,damage,caster);
                                        })
                                )
                        )
                )

        ;












        NORMAL = CECombatBehaviors.builder()

                //主动普通闪避 50%血量以上
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(60).cooldown(100)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).health(0.5F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_B, 0F))
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).health(0.5F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_L, 0F))
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).health(0.5F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_R, 0F)))

                //被动普通闪避 50%血量以上
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(5).maxCooldown(60).cooldown(200)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).attackLevel(1, 2).health(0.5F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_B, 0F))
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).attackLevel(1, 2).health(0.5F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_L, 0F))
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).attackLevel(1, 2).health(0.5F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_R, 0F)))

                //主动普通防御 75%血量以上
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(300).cooldown(300)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 2.5).attackLevel(0, 0)
                                .health(0.75F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .guard(100)
                                .maxGuardHit(3)
                                .counterAnimation(EFNLanceAnimations.NF_MEEN_AUTO3,new AnimationParams().transitionTime(0.25F).stunType(StunType.SHORT))
                                .counterType(CECombatBehaviors.CounterType.END)
                                .onCounterStart(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                })
                        ))

                //被动普通防御 75%血量以上
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(5).maxCooldown(300).cooldown(200)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 4).attackLevel(1, 2)
                                .health(0.75F, HealthCheck.Comparator.GREATER_RATIO_CONTAIN)
                                .guard(60)
                                .maxGuardHit(3)
                                .counterAnimation(EFNLanceAnimations.NF_MEEN_AUTO3,new AnimationParams().transitionTime(0.25F).stunType(StunType.SHORT))
                                .counterType(CECombatBehaviors.CounterType.END)
                                .onCounterStart(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                })
                        ))


                //被动强化闪避 50%血量以下 概率派生突刺，或连续执行闪避
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(15).maxCooldown(60)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 5).attackLevel(1, 3).health(0.5F, HealthCheck.Comparator.LESS_RATIO)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_B, 0F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 5).attackLevel(1, 3)
                                        .animationBehavior(EFNDodgeAnimations.DODGE_STEP_B, 0F))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 5).attackLevel(1, 3)
                                        .randomChance(0.65F)
                                        .addCooldown(140)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2,  new AnimationParams().transitionTime(0.45F).damageMultiplier(0.65F))
                                        .addExBehavior(mobPatch -> {
                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                            summonFireBall(mobPatch,7);
                                            if(mobPatch.getTarget() != null) {
                                                LivingEntity target = mobPatch.getTarget();
                                                Vec3 pos = target.position();
                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                            }
                                        })))



                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 5).attackLevel(1, 3).health(0.5F, HealthCheck.Comparator.LESS_RATIO)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_L, 0F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 5).attackLevel(1, 3)
                                        .animationBehavior(EFNDodgeAnimations.DODGE_STEP_R, 0F))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 5).attackLevel(1, 3)
                                        .randomChance(0.65F)
                                        .addCooldown(140)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2, new AnimationParams().transitionTime(0.45F).damageMultiplier(0.65F))
                                        .addExBehavior(mobPatch -> {
                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                            summonFireBall(mobPatch,7);
                                            if(mobPatch.getTarget() != null) {
                                                LivingEntity target = mobPatch.getTarget();
                                                Vec3 pos = target.position();
                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                            }
                                        })))

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0, 5).attackLevel(1, 3).health(0.5F, HealthCheck.Comparator.LESS_RATIO)
                                .animationBehavior(EFNDodgeAnimations.DODGE_STEP_R, 0F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 5).attackLevel(1, 3)
                                        .animationBehavior(EFNDodgeAnimations.DODGE_STEP_L, 0F))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 5).attackLevel(1, 3)
                                        .randomChance(0.65F)
                                        .addCooldown(140)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2, new AnimationParams().transitionTime(0.45F).damageMultiplier(0.65F))
                                        .addExBehavior(mobPatch -> {
                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                            summonFireBall(mobPatch,7);
                                            if(mobPatch.getTarget() != null) {
                                                LivingEntity target = mobPatch.getTarget();
                                                Vec3 pos = target.position();
                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                            }
                                        }))))


                //被动强化防御 75%血量以下
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(15).maxCooldown(300)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0, 5).attackLevel(1, 2)
                                .health(0.75F, HealthCheck.Comparator.LESS_RATIO)
                                .health(0.25F, HealthCheck.Comparator.GREATER_RATIO)
                                .guard(80)
                                .counterType(CECombatBehaviors.CounterType.RANDOM)
                                .counterChance(0.75F)
                                .counterAnimation(EFNLanceAnimations.NF_MEEN_CHARGE1, 0.35F)
                                .onCounterStart(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                    mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                    Level world = mobPatch.getOriginal().level();
                                    LivingEntity caster = mobPatch.getOriginal();
                                    double spawnX = caster.getX();
                                    double spawnY = caster.getY();
                                    double spawnZ = caster.getZ();
                                    float rot = caster.getYRot() + 90;
                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 35,12,30,1, damage,caster);
                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 15, 12,30,1, damage,caster);
                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 15,12,30,1, damage,caster);
                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 35,12,30,1, damage,caster);

                                    if(mobPatch.getTarget() != null) {
                                        LivingEntity target = mobPatch.getTarget();
                                        Vec3 pos = target.position();
                                        ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                        ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                    }
                                }))

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0, 5).attackLevel(1, 2)
                                .health(0.75F, HealthCheck.Comparator.LESS_RATIO)
                                .health(0.25F, HealthCheck.Comparator.GREATER_RATIO)
                                .guard(80)
                                .counterType(CECombatBehaviors.CounterType.RANDOM)
                                .counterChance(0.75F)
                                .counterAnimation(EFNLanceAnimations.NF_MEEN_CHARGE1, 0.35F)
                                .onCounterStart(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                    mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                    Level world = mobPatch.getOriginal().level();
                                    LivingEntity caster = mobPatch.getOriginal();
                                    double spawnX = caster.getX();
                                    double spawnY = caster.getY();
                                    double spawnZ = caster.getZ();
                                    float rot = caster.getYRot() + 90;
                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 35,12,30,1,damage,caster);
                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 0, 12,30,1,damage,caster);
                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 35,12,30,1,damage,caster);

                                    if(mobPatch.getTarget() != null) {
                                        LivingEntity target = mobPatch.getTarget();
                                        Vec3 pos = target.position();
                                        ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                        ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                    }
                                }))

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(0.5)
                                .withinDistance(0, 5).attackLevel(1, 2)
                                .health(0.75F, HealthCheck.Comparator.LESS_RATIO)
                                .health(0.25F, HealthCheck.Comparator.GREATER_RATIO)
                                .guard(80)
                                .counterType(CECombatBehaviors.CounterType.RANDOM)
                                .counterChance(0.75F)
                                .counterAnimation(EFNLanceAnimations.NF_MEEN_AUTO3,new AnimationParams().transitionTime(0.4F).stunType(StunType.SHORT))
                                .onCounterStart(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                })))


                //远距离有可能会出前翻滚
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(100)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(6, 12).withinAngle(0, 60)
                                .animationBehavior(EFNDodgeAnimations.DODGE_ROLL_F, 0F)))

                //普通攻击连段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(10)
                        //长枪a1
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0, 4).withinAngle(0, 60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO1, new AnimationParams().stunType(StunType.SHORT))

                                //长枪a2
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(1)
                                        .withinDistance(0, 8).withinAngle(0, 60)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO2, 0F)

                                        //50%以下额外派生
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .priority(1).weight(1.0)
                                                .health(0.50F, HealthCheck.Comparator.LESS_RATIO)
                                                .withinDistance(5, 12)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2, new AnimationParams().transitionTime(0.45F).damageMultiplier(0.65F))
                                                .addExBehavior(mobPatch -> {
                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                                    mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                    summonFireBall(mobPatch,7);

                                                    if(mobPatch.getTarget() != null) {
                                                        LivingEntity target = mobPatch.getTarget();
                                                        Vec3 pos = target.position();
                                                        ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                        ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                                    }
                                                }))

                                        //远距离派生
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .priority(1).weight(1.0)
                                                .withinDistance(5, 12).interruptedByLevel(3)
                                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_FIRST, new AnimationParams().transitionTime(0.25F).damageMultiplier(0.6F))
                                                //被攻击时概率派生
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1.0).attackLevel(1,2)
                                                        .canInterruptParent(true)
                                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.4F))
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1.0).randomChance(0.5F)
                                                        .canInterruptParent(true)
                                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.4F))

                                                //50%以下额外派生
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1.0).randomChance(0.65F)
                                                        .health(0.5F, HealthCheck.Comparator.LESS_RATIO)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1, 0.45F)
                                                        .addExBehavior(mobPatch -> {
                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                                            if(mobPatch.getTarget() != null) {
                                                                LivingEntity target = mobPatch.getTarget();
                                                                Vec3 pos = target.position();
                                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                                            }
                                                        })
                                                        .addTimeEvent(
                                                                new TimeEvent(1F, mobPatch -> {
                                                                    Level world = mobPatch.getOriginal().level();
                                                                    LivingEntity caster = mobPatch.getOriginal();
                                                                    double spawnX = caster.getX();
                                                                    double spawnY = caster.getY();
                                                                    double spawnZ = caster.getZ();
                                                                    float rot = caster.getYRot() + 90;
                                                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 35,12,1,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 15, 12,1,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 0, 12,1,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 15,12,1,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 35,12,1,damage,caster);
                                                                })
                                                        )
                                                ))

                                        //长枪a3
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .priority(1).weight(1)
                                                .withinDistance(0, 5).withinAngle(0, 60)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams().transitionTime(0.15F).playSpeed(1.25F).damageMultiplier(1.3F).impactMultiplier(1.6F).stunType(StunType.SHORT))

                                                //长枪a4
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1)
                                                        .withinDistance(0, 6).withinAngle(0, 60)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO4, new AnimationParams().transitionTime(0.25F).playSpeed(1.35F).damageMultiplier(1.3F).impactMultiplier(1.6F).stunType(StunType.SHORT)))))

                                //75%以下额外派生
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(0.5).health(0.75F, HealthCheck.Comparator.LESS_RATIO)
                                        .withinDistance(0, 4).withinAngle(0, 60)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1, 0.35F)
                                        .addExBehavior(mobPatch -> {
                                            
                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                            if(mobPatch.getTarget() != null) {
                                                LivingEntity target = mobPatch.getTarget();
                                                Vec3 pos = target.position();
                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                            }
                                        })
                                        .addTimeEvent(new TimeEvent(1F, mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float rot = caster.getYRot() + 90;
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 30, 12,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 0, 12,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 30,12,1,damage,caster);
                                        })))))


                //普攻连段-带基础派生
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(10)
                        //长枪a1
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0, 4).withinAngle(0, 60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO1, new AnimationParams().stunType(StunType.SHORT))

                                //闪避追击派生
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(1)
                                        .withinDistance(0, 4)
                                        .animationBehavior(EFNDodgeAnimations.DODGE_ROLL_B, 0F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, 0F)


                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .wander(12, 0, 0)

                                                        //65%以下额外派生
                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .priority(1).weight(1).health(0.65F, HealthCheck.Comparator.LESS_RATIO)
                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1, 0.25F)
                                                                .addExBehavior(mobPatch -> {
                                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 60), mobPatch.getOriginal());
                                                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                                                    mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                                                    if(mobPatch.getTarget() != null) {
                                                                        LivingEntity target = mobPatch.getTarget();
                                                                        Vec3 pos = target.position();
                                                                        ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                                        ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                                                    }
                                                                })
                                                                .addTimeEvent(new TimeEvent(1F, mobPatch -> {
                                                                    Level world = mobPatch.getOriginal().level();
                                                                    LivingEntity caster = mobPatch.getOriginal();
                                                                    double spawnX = caster.getX();
                                                                    double spawnY = caster.getY();
                                                                    double spawnZ = caster.getZ();
                                                                    float rot = caster.getYRot() + 90;
                                                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 35,12,1,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 15, 12,1,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 0, 30,2,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 15,12,1,damage,caster);
                                                                    spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 35,12,1,damage,caster);
                                                                }))))))

                                //长枪a2
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(1.5)
                                        .withinDistance(0, 8).withinAngle(0, 60)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO2, 0F)

                                        //长枪a3
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .priority(1).weight(1)
                                                .withinDistance(0, 6).withinAngle(0, 60)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams().stunType(StunType.SHORT))

                                                //长枪a4
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1)
                                                        .withinDistance(0, 4).withinAngle(0, 60)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO4, 0F))

                                                //75%以下额外派生
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1).health(0.75F, HealthCheck.Comparator.LESS_RATIO)
                                                        .withinDistance(0, 5).withinAngle(0, 120)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1, 0.35F)
                                                        .addExBehavior(mobPatch -> {
                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 60), mobPatch.getOriginal());
                                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                                            if(mobPatch.getTarget() != null) {
                                                                LivingEntity target = mobPatch.getTarget();
                                                                Vec3 pos = target.position();
                                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                                            }
                                                        })
                                                        .addTimeEvent(new TimeEvent(1F, mobPatch -> {
                                                            Level world = mobPatch.getOriginal().level();
                                                            LivingEntity caster = mobPatch.getOriginal();
                                                            double spawnX = caster.getX();
                                                            double spawnY = caster.getY();
                                                            double spawnZ = caster.getZ();
                                                            float rot = caster.getYRot() + 90;
                                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5F;
                                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 35,12,1,damage,caster);
                                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 15, 12,1,damage,caster);
                                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 0, 30,2,damage,caster);
                                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 15,12,1,damage,caster);
                                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 35,12,1,damage,caster);
                                                        }))))

                                        //跳攻派生
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .priority(1).weight(1)
                                                .withinDistance(0, 6).withinAngle(0, 60)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AIRSLASH, 0F)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO4, 0.25F)))

                                        //疾跑派生
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .priority(1).weight(1)
                                                .withinDistance(5, 12).withinAngle(0, 60)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, 0.25F)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .wander(8, 0, 0)

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO4, 0.25F)))))))

                //额外新增远距离攻击
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(400)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .health(0.75F, HealthCheck.Comparator.LESS_RATIO)
                                .withinDistance(6, 12).withinAngle(0, 60)
                                .interruptedByTime(2.1F,2.2F)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_FINISHER, 0.25F)
                                .addTimeEvent(
                                        new TimeEvent(0F,0.2F,mobPatch -> {
                                            if(mobPatch.getTarget() != null){
                                                mobPatch.rotateTo(mobPatch.getTarget(),360,false);
                                            }
                                        }),
                                        new TimeEvent(0.5F, mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float rot = caster.getYRot() + 90;
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F;
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 30,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 0, 30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 30,30,1,damage,caster);
                                        }),
                                        new TimeEvent(0.85F, mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float rot = caster.getYRot() + 90;
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F;
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 35,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot - 15,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 15,30,1,damage,caster);
                                            spawnJetsLine(world,spawnX,spawnY,spawnZ,rot + 35,30,1,damage,caster);
                                        })
                                )
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(1)
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.5F)
                                        .addTimeEvent(
                                                new TimeEvent(0.25F,mobPatch -> {
                                                    if(mobPatch.getTarget() != null){
                                                        teleportInFrontAlongLine(mobPatch.getOriginal(),mobPatch.getTarget(),1,0);
                                                    }
                                                })
                                        )
                                )
                                //50%以下概率出火球
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(0.5)
                                        .addCooldown(100)
                                        .health(0.5F, HealthCheck.Comparator.LESS_RATIO)
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.5F)
                                        .addTimeEvent(
                                                new TimeEvent(0.05F,mobPatch -> {
                                                    int random = mobPatch.getOriginal().getRandom().nextInt(5);
                                                    summonFireBall(mobPatch,random);
                                                }),
                                                new TimeEvent(0.25F,mobPatch -> {
                                                    if(mobPatch.getTarget() != null){
                                                        teleportInFrontAlongLine(mobPatch.getOriginal(),mobPatch.getTarget(),1,0);
                                                    }
                                                })
                                        )
                                )
                                //25%以下出烈焰打击环
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(1)
                                        .addCooldown(100)
                                        .health(0.25F, HealthCheck.Comparator.LESS_RATIO)
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.5F)
                                        .addTimeEvent(
                                                new TimeEvent(0.25F,mobPatch -> {
                                                    if(mobPatch.getTarget() != null){
                                                        teleportInFrontAlongLine(mobPatch.getOriginal(),mobPatch.getTarget(),1,0);
                                                    }
                                                }),
                                                new TimeEvent(1.4F,mobPatch -> {
                                                    Level world = mobPatch.getOriginal().level();
                                                    LivingEntity caster = mobPatch.getOriginal();
                                                    double spawnX = caster.getX();
                                                    double spawnY = caster.getY();
                                                    double spawnZ = caster.getZ();
                                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.35F;
                                                    spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,7,12,20,0,1.15F,damage,caster);
                                                })
                                        )
                                )
                        ))





                /*-----------90%血以下，新增人车------------**/

                //人车连段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1)
                        .maxCooldown(800)
                        .rootName("ArteriusDashing")
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .health(0.9F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .health(0.4F, HealthCheck.Comparator.GREATER_RATIO)
                                .withinDistance(4.5, 8).withinAngle(0, 60).randomChance(0.35F)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams().transitionTime(0.5F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG))
                                .addExBehavior(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                    mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                })
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams().transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG))
                                        .addExBehavior(mobPatch -> {
                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                        })
                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams().transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG))
                                                .addExBehavior(mobPatch -> {
                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                })
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams().transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG))
                                                        .addExBehavior(mobPatch -> {
                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                        })
                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams().transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG))
                                                                .addExBehavior(mobPatch -> {
                                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                                })
                                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams().transitionTime(-0.45F).damageMultiplier(0.65F).impactMultiplier(0.5F).stunType(StunType.LONG))
                                                                        .addExBehavior(mobPatch -> {
                                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                                        })
                                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, new AnimationParams().transitionTime(-0.45F).damageMultiplier(1.3F).impactMultiplier(2.5F).stunType(StunType.LONG))
                                                                                .addExBehavior(mobPatch -> {
                                                                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 40), mobPatch.getOriginal());
                                                                                })
                                                                        ))))))))


                /*---------------80%血以下，新增连段------------------*/
                //抓握尖塔
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(10)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .health(0.8F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .withinDistance(0, 4).withinAngle(0, 60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams().transitionTime(0.35F).stunType(StunType.SHORT))

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0, 6).withinAngle(0, 120)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO4, 0.25F)

                                                //50%血以下额外派生
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1)
                                                        .health(0.5F, HealthCheck.Comparator.LESS_RATIO)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2, new AnimationParams().transitionTime(0.45F).damageMultiplier(0.65F))
                                                        .addExBehavior(mobPatch -> {
                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                            summonFireBall(mobPatch,7);
                                                            if(mobPatch.getTarget() != null) {
                                                                LivingEntity target = mobPatch.getTarget();
                                                                Vec3 pos = target.position();
                                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                                            }
                                                        }))

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1)
                                                        .animationBehavior(Animations.GRASPING_SPIRAL_FIRST, 0.35F)))))


                /*---------------70%血以下，新增连段------------------*/
                //跳攻起手
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(10)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .health(0.7F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .withinDistance(0, 4).withinAngle(0, 60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AIRSLASH, 0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 8).withinAngle(0, 60)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, 0.25F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .wander(8, 0, 0)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams().transitionTime(0.25F).stunType(StunType.SHORT)))

                                                //50%血以下额外派生
                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .priority(1).weight(1.5)
                                                        .health(0.5F, HealthCheck.Comparator.LESS_RATIO)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2, new AnimationParams().transitionTime(0.45F).damageMultiplier(0.65F))
                                                        .addExBehavior(mobPatch -> {
                                                            mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                                            summonFireBall(mobPatch,7);
                                                            if(mobPatch.getTarget() != null) {
                                                                LivingEntity target = mobPatch.getTarget();
                                                                Vec3 pos = target.position();
                                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                                            }
                                                        }))))))


                //拼一点大剑的招
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(10)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .health(0.7F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .withinDistance(0, 4).withinAngle(0, 60)
                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO1, 0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0, 8).withinAngle(0, 120)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO2, 0.1F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0, 8).withinAngle(0, 120)
                                                .animationBehavior(EFNLanceAnimations.NF_MEEN_AUTO3, new AnimationParams().transitionTime(0.15F).stunType(StunType.SHORT))

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .withinDistance(0, 8).withinAngle(0, 120)
                                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_DASH, 0.25F))))))

                /*---------------65%血以下，新增连段------------------*/
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1).maxCooldown(400)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .health(0.65F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .withinDistance(6, 12).withinAngle(0, 60)
                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_FIRST, new AnimationParams().transitionTime(0.4F).damageMultiplier(0.6F))
                                .addExBehavior(mobPatch -> {
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                })

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(1).weight(1)
                                        .health(0.25F, HealthCheck.Comparator.GREATER_RATIO)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.25F))

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .priority(2).weight(1)
                                        .health(0.25F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_SECOND, 0.25F)
                                        .addTimeEvent(
                                                new TimeEvent(1.4F,mobPatch -> {
                                                    Level world = mobPatch.getOriginal().level();
                                                    LivingEntity caster = mobPatch.getOriginal();
                                                    double spawnX = caster.getX();
                                                    double spawnY = caster.getY();
                                                    double spawnZ = caster.getZ();
                                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.5F;
                                                    spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,8,14,20,0,1.25F,damage,caster);
                                                })
                                        )

                                )
                        )
                )



                /*--------------首次低于75%血以下时，释放蓄力一；后续防御反击替换为蓄力一段，并能够在普攻中出现-----------------*/
                //正常蓄力一段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(7).weight(1000)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .custom(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    return arterius.getSkillReleased() == 0 && arterius.getBossPhase() == 1;
                                })
                                .interruptedByTime(1.85F,1.95F)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGING_MOB, 0.15F)
                                .addExBehavior(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    arterius.setSkillReleased(1);
                                    arterius.setInvulnerableTimer(60);
                                    arterius.setCanSetDeltaMovement(false);
                                    arterius.setCanSetDeltaMovementTimer(60);
                                    mobPatch.playSound(SoundEvents.FIRECHARGE_USE, 0, 0);
                                    if (!mobPatch.isLogicalClient()) {
                                        mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                        ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();
                                        Vec3 pos = mobPatch.getOriginal().position();
                                        serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 200, 0, 1, 0, 0.5);
                                    }
                                })
                                .addTimeEvent(
                                        new TimeEvent(0F,1F,mobPatch -> {
                                            if(mobPatch.getTarget() != null) {
                                                mobPatch.rotateTo(mobPatch.getTarget(),360,false);
                                            }
                                        })
                                )
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE1, 0.1F)
                                        .addExBehavior(mobPatch -> {
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                            if(mobPatch.getTarget() != null) {
                                                LivingEntity target = mobPatch.getTarget();
                                                Vec3 pos = target.position();
                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                            }
                                        })
                                        .addTimeEvent(
                                                new TimeEvent(1F, mobPatch -> {
                                                    Level world = mobPatch.getOriginal().level();
                                                    LivingEntity caster = mobPatch.getOriginal();
                                                    double spawnX = caster.getX();
                                                    double spawnY = caster.getY();
                                                    double spawnZ = caster.getZ();
                                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.0F;
                                                    spawnEightDirectionJets(world,spawnX,spawnY,spawnZ,caster.getYRot(),30,2,damage,caster);
                                                    spawnEightDirectionJets(world,spawnX,spawnY,spawnZ,caster.getYRot() + 22.5F,30,1,damage,caster);
                                                })
                                        )
                                )
                        )
                )

                /*---------------首次低于50%血以下时，释放蓄力二；后续闪避能够派生蓄力二段，并能够在普攻中出现--------------------*/
                //正常蓄力二段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(6).weight(1000)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .custom(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    return arterius.getSkillReleased() == 1 && arterius.getBossPhase() == 2;
                                })
                                .interruptedByTime(3.0F,3.1F)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGING_MOB, 0.15F)
                                .addExBehavior(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    arterius.setSkillReleased(2);
                                    arterius.setInvulnerableTimer(80);
                                    arterius.setCanSetDeltaMovement(false);
                                    arterius.setCanSetDeltaMovementTimer(80);
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 70), mobPatch.getOriginal());
                                    mobPatch.playSound(SoundEvents.FIRECHARGE_USE, 0, 0);
                                    if (!mobPatch.isLogicalClient()) {
                                        ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();
                                        Vec3 pos = mobPatch.getOriginal().position();
                                        serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 200, 0, 1, 0, 0.5);
                                    }
                                })
                                .addTimeEvent(
                                        new TimeEvent(0F,1F,mobPatch -> {
                                            if(mobPatch.getTarget() != null) {
                                                mobPatch.rotateTo(mobPatch.getTarget(),360,false);
                                            }
                                        })
                                )
                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGE2, new AnimationParams().transitionTime(0.45F).damageMultiplier(0.65F))
                                        .addExBehavior(mobPatch -> {
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                            int random = mobPatch.getOriginal().getRandom().nextInt(5);
                                            summonFireBall(mobPatch,random);
                                            if(mobPatch.getTarget() != null) {
                                                LivingEntity target = mobPatch.getTarget();
                                                Vec3 pos = target.position();
                                                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                                ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                            }
                                        }))))

                /*----------------首次低于25%血以下时，释放蓄力三；后续能够独立出现蓄力三段攻击--------------------*/
                //正常蓄力三段
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(5).weight(1000)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .custom(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    return arterius.getSkillReleased() == 2 && arterius.getBossPhase() == 3;
                                })
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGING_MOB, 0.15F)
                                .addExBehavior(mobPatch -> {
                                    Arterius arterius = (Arterius) mobPatch.getOriginal();
                                    arterius.setSkillReleased(3);
                                    arterius.setInvulnerableTimer(100);
                                    arterius.setCanSetDeltaMovement(false);
                                    arterius.setCanSetDeltaMovementTimer(100);
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 90), mobPatch.getOriginal());
                                    mobPatch.playSound(SoundEvents.FIRECHARGE_USE, 0, 0);
                                    if (!mobPatch.isLogicalClient()) {
                                        ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();
                                        Vec3 pos = mobPatch.getOriginal().position();
                                        serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 200, 0, 1, 0, 0.5);
                                    }
                                })
                                .addTimeEvent(
                                        new TimeEvent(0F,1F,mobPatch -> {
                                            if(mobPatch.getTarget() != null) {
                                                mobPatch.rotateTo(mobPatch.getTarget(),360,false);
                                            }
                                        }),
                                        new TimeEvent(4.25F,mobPatch -> {
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
                                            summonFireBall(mobPatch,6);
                                        }),
                                        new TimeEvent(4.5F,mobPatch -> {
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(EFNMobEffectRegistry.MEEN_LANCE.get(), Integer.MAX_VALUE,1));
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3F;
                                            spawnSixDirectionFlameStrike(world,spawnX,spawnY,spawnZ,caster.getYRot() + 22.5F,20,1.25F,damage,caster);
                                        }),
                                        new TimeEvent(4.75F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.2F;
                                            spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,8,12,4,1.2F,damage,caster);
                                        }),
                                        new TimeEvent(5.0F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.2F;
                                            spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,20,24,12,1.2F,damage,caster);
                                        })
                                )))


                //三段派生大招释放 + 焰魂斩 八向
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(10).maxCooldown(600)
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .health(0.25F, HealthCheck.Comparator.LESS_RATIO_CONTAIN)
                                .withinDistance(0, 5).withinAngle(0, 60)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_FINISHER, new AnimationParams().transitionTime(0.8F).damageMultiplier(2F).impactMultiplier(2F).damageSource(Set.of(EpicFightDamageTypeTags.BYPASS_DODGE,EpicFightDamageTypeTags.UNBLOCKALBE)))
                                .addExBehavior(mobPatch -> {
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(EFNMobEffectRegistry.BYPASS_DODGE_EFFECT.get(), 100));
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(EFNMobEffectRegistry.GUARD_PUNCTURE_EFFECT.get(), 100));
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 100));
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                    mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);

                                    if(mobPatch.getTarget() != null) {
                                        LivingEntity target = mobPatch.getTarget();
                                        Vec3 pos = target.position();
                                        ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_DODGE_WARNING.get(),target.getId(),new Vec3(0,1.5,0));
                                        ((ServerLevel)target.level()).sendParticles(options,pos.x, pos.y, pos.z,0,0, 0,0,0);
                                    }

                                    summonFireBall(mobPatch,5);

                                    Level world = mobPatch.getOriginal().level();
                                    LivingEntity caster = mobPatch.getOriginal();
                                    double spawnX = caster.getX();
                                    double spawnY = caster.getY();
                                    double spawnZ = caster.getZ();
                                    float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 10F;
                                    spawnFlameStrike(
                                            world,spawnX,spawnZ,
                                            spawnY-2, spawnY + 2,caster.getYRot(),
                                            40, 40, 0,
                                            3F,damage,true,caster
                                    );
                                })
                                .addTimeEvent(
                                        new TimeEvent(mobPatch -> {
                                            if(mobPatch.getTarget() != null){
                                                mobPatch.rotateTo(mobPatch.getTarget(),360,false);
                                            }
                                        }),
                                        new TimeEvent(1F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.2F;
                                            spawnEightDirectionJets(world,spawnX,spawnY,spawnZ,caster.getYRot(),15,2,damage,caster);
                                        }),
                                        new TimeEvent(1.25F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.2F;
                                            spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,6,12,2,1.0F,damage,caster);
                                        }),
                                        new TimeEvent(1.5F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.2F;
                                            spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,14,22,6,1.0F,damage,caster);
                                        }),
                                        new TimeEvent(1.75F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.2F;
                                            spawnEightDirectionJets(world,spawnX,spawnY,spawnZ,caster.getYRot() + 22.5F,15,2,damage,caster);
                                        }),
                                        new TimeEvent(1.75F,mobPatch -> {
                                            Level world = mobPatch.getOriginal().level();
                                            LivingEntity caster = mobPatch.getOriginal();
                                            double spawnX = caster.getX();
                                            double spawnY = caster.getY();
                                            double spawnZ = caster.getZ();
                                            float damage = (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3.2F;
                                            spawnFlameStrikeRing(world,spawnX,spawnY,spawnZ,22,32,10,1.5F,damage,caster);
                                        })
                                )
                        ))



                /*--------------------------------特殊连段-------------------------------------------*/


                //对于距离过远，或者Y轴差距过大的敌人，使用特殊招式
                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1000).cooldown(400)

                        //距离远时
                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(4.5,100)
                                .custom(mobPatch -> ((Arterius) mobPatch.getOriginal()).getFarAwayFromTargetTime() > 600)
                                .animationBehavior(EFNLanceAnimations.NF_MEEN_CHARGING_MOB, new AnimationParams().transitionTime(0.05F).damageMultiplier(2F).impactMultiplier(2F))
                                .addExBehavior(mobPatch -> {
                                    //重置时间
                                    ((Arterius) mobPatch.getOriginal()).setFarAwayFromTargetTime(0);
                                    ((Arterius) mobPatch.getOriginal()).setInvulnerableTimer(100);
                                    //药水效果和声音粒子
                                    mobPatch.getOriginal().forceAddEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 100), mobPatch.getOriginal());
                                    mobPatch.playSound(SoundEvents.FIRECHARGE_USE, 0, 0);
                                    if (!mobPatch.isLogicalClient()) {
                                        ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();
                                        Vec3 pos = mobPatch.getOriginal().position();
                                        serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 200, 0, 1, 0, 0.5);
                                    }
                                })
                                .addTimeEvent(
                                        new TimeEvent(0.4F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(-5.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(0.8F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(-3.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(1.2F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(0.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(1.6F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(3.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(2.0F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(5.0F, 2.0F, 0.0F), 40);
                                        }),

                                        new TimeEvent(2.4F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootAbyssFireball(caster, new Vec3(5.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(2.8F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootAbyssFireball(caster, new Vec3(3.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(3.2F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootAbyssFireball(caster, new Vec3(0.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(3.6F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootAbyssFireball(caster, new Vec3(-3.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(4.0F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootAbyssFireball(caster, new Vec3(-5.0F, 2.0F, 0.0F), 40);
                                            
                                            //提前0.8秒释放提示
                                            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
                                            mobPatch.getOriginal().playSound(SoundEvents.ANVIL_LAND, 5, 1);
                                            mobPatch.getOriginal().playSound(SoundEvents.FIRECHARGE_USE, 5, 1);
                                        }),

                                        new TimeEvent(4.2F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(-5.0F, 2.0F, 0.0F), 40);
                                            shootFireball(caster, new Vec3(5.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(4.4F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(3.0F, 2.0F, 0.0F), 40);
                                            shootFireball(caster, new Vec3(-3.0F, 2.0F, 0.0F), 40);
                                        }),
                                        new TimeEvent(4.6F,mobPatch -> {
                                            LivingEntity caster = mobPatch.getOriginal();
                                            shootFireball(caster, new Vec3(1.5F, 2.0F, 0.0F), 40);
                                            shootFireball(caster, new Vec3(-1.5F, 2.0F, 0.0F), 40);
                                        }),


                                        new TimeEvent(4.8F,mobPatch -> {
                                            Arterius caster = (Arterius) mobPatch.getOriginal();
                                            if(caster.getTarget() != null) {
                                                caster.getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60,7));
                                                //传送前后生成粒子
                                                if (!mobPatch.isLogicalClient()) {
                                                    ServerLevel serverLevel = (ServerLevel) mobPatch.getOriginal().level();

                                                    Vec3 pos = caster.getTarget().position();
                                                    serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 200, 0, 1, 0, 0.5);

                                                    teleportInFrontAlongLine(caster.getTarget(),caster,1.5F,0);

                                                    pos = caster.getTarget().position();
                                                    serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 200, 0, 1, 0, 0.5);
                                                }

                                            }
                                        })
                                )
                        ))
        ;
    }

    private static @NotNull Consumer<MobPatch<?>> lookAtTarget() {
        return mobPatch -> {
            if (mobPatch.getTarget() != null) {
                mobPatch.getOriginal().lookAt(EntityAnchorArgument.Anchor.FEET, mobPatch.getTarget().position());
            }
        };
    }

    private static @NotNull Consumer<MobPatch<?>> bypassGuardWarning() {
        return mobPatch -> {
            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
            if (mobPatch.getTarget() != null) {
                LivingEntity target = mobPatch.getTarget();
                Vec3 pos = target.position();
                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_GUARD_WARNING.get(), target.getId(), new Vec3(0, 1.5, 0));
                ((ServerLevel) target.level()).sendParticles(options, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
            }
        };
    }

    private static @NotNull Consumer<MobPatch<?>> bypassDodgeWarning() {
        return mobPatch -> {
            mobPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
            mobPatch.playSound(SoundEvents.ANVIL_LAND, 0, 0);
            if (mobPatch.getTarget() != null) {
                LivingEntity target = mobPatch.getTarget();
                Vec3 pos = target.position();
                ParticleOptions options = new CEFollowParticleOptions(CEParticles.BYPASS_DODGE_WARNING.get(), target.getId(), new Vec3(0, 1.5, 0));
                ((ServerLevel) target.level()).sendParticles(options, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
            }
        };
    }

    private static @NotNull Consumer<MobPatch<?>> applyStunImmunity(int time) {
        return mobPatch -> mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), time));
    }

    public static void teleportInFrontAlongLine(Entity from, Entity to, double distance, double yOffset) {
        if (from == null || to == null) return;

        Vec3 fromPos = from.position();
        Vec3 toPos = to.position();

        Vec3 dir = toPos.subtract(fromPos);
        if (dir.lengthSqr() < 1e-6) {
            dir = to.position();
        } else {
            dir = dir.normalize();
        }
        Vec3 dest = toPos.subtract(dir.scale(distance));
        dest = new Vec3(dest.x, dest.y + yOffset, dest.z);
        from.teleportTo(dest.x, dest.y, dest.z);
    }


    public static void spawnEightDirectionJets(Level level, double x, double y, double z,float baseRot, int rune, double time,float damage, @Nullable LivingEntity owner) {
        for (int i = 0; i < 8; ++i) {
            // 角度转弧度
            float throwAngle = baseRot + 22.5F + i * 45.0F;

            spawnJetsLine(level, x, y, z, throwAngle, rune, time, damage, owner);
        }
    }

    public static void spawnJetsLine(Level level, double x, double y, double z, float baseRot, int rune, double time, float damage, @Nullable LivingEntity owner) {
        baseRot = (float) Math.toRadians(baseRot);
        for (int k = 0; k < rune; ++k) {
            double d2 = 0.8 * (k + 1);
            int d3 = (int) (time * (k + 1));

            spawnJet(level,
                    x + Mth.cos(baseRot) * 1.25F * d2,
                    z + Mth.sin(baseRot) * 1.25F * d2,
                    y - 2.0F, y + 2.0F,
                    baseRot, d3,damage, owner);
        }
    }

    public static void spawnJetsLine(Level level, double x, double y, double z, float baseRot, int rune,int baseDelay, double time, float damage, @Nullable LivingEntity owner) {
        baseRot = (float) Math.toRadians(baseRot);
        for (int k = 0; k < rune; ++k) {
            double d2 = 0.8 * (k + 1);
            int d3 = (int) (time * (k + 1));

            spawnJet(level,
                    x + Mth.cos(baseRot) * 1.25F * d2,
                    z + Mth.sin(baseRot) * 1.25F * d2,
                    y - 2.0F, y + 2.0F,
                    baseRot,baseDelay, d3,damage, owner);
        }
    }


    private static void spawnJet(Level level, double x, double z, double minY, double maxY, float rotation,int baseDelay, int delay,float damage, @Nullable LivingEntity owner) {
        BlockPos pos = BlockPos.containing(x, maxY, z);
        boolean found = false;
        double offsetY = 0.0;

        // 向下寻找坚固地面
        do {
            BlockPos below = pos.below();
            BlockState state = level.getBlockState(below);
            if (state.isFaceSturdy(level, below, Direction.UP)) {
                if (!level.isEmptyBlock(pos)) {
                    BlockState topState = level.getBlockState(pos);
                    VoxelShape shape = topState.getCollisionShape(level, pos);
                    if (!shape.isEmpty()) {
                        offsetY = shape.max(Direction.Axis.Y);
                    }
                }
                found = true;
                break;
            }
            pos = pos.below();
        } while (pos.getY() >= Mth.floor(minY) - 1);

        // 找到地面则生成实体
        if (found) {
            Flame_Jet_Entity jet = new Flame_Jet_Entity(level, x, pos.getY() + offsetY, z,rotation, baseDelay + delay, damage, owner);
            level.addFreshEntity(jet);
        }
    }

    private static void spawnJet(Level level, double x, double z, double minY, double maxY, float rotation, int delay,float damage, @Nullable LivingEntity owner) {
        BlockPos pos = BlockPos.containing(x, maxY, z);
        boolean found = false;
        double offsetY = 0.0;

        do {
            BlockPos below = pos.below();
            BlockState state = level.getBlockState(below);
            if (state.isFaceSturdy(level, below, Direction.UP)) {
                if (!level.isEmptyBlock(pos)) {
                    BlockState topState = level.getBlockState(pos);
                    VoxelShape shape = topState.getCollisionShape(level, pos);
                    if (!shape.isEmpty()) {
                        offsetY = shape.max(Direction.Axis.Y);
                    }
                }
                found = true;
                break;
            }
            pos = pos.below();
        } while (pos.getY() >= Mth.floor(minY) - 1);

        if (found) {
            Flame_Jet_Entity jet = new Flame_Jet_Entity(level, x, pos.getY() + offsetY, z,rotation, delay, damage, owner);
            level.addFreshEntity(jet);
        }
    }

    public static void spawnFlameStrikeRing(Level level, double x, double y, double z,float radius,int count, int delay, float size,float damage, @Nullable LivingEntity entity) {


        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            double dx = x + offsetX;
            double dz = z + offsetZ;

            double minY = y - 2.0F;
            double maxY = y + 2.0F;


            spawnFlameStrike(level,
                    dx,dz, minY, maxY,0,
                    40, delay, delay,size, damage,false, entity
            );
        }
    }

    public static void spawnFlameStrikeRing(Level level, double x, double y, double z,float radius,int count,int duration, int delay, float size,float damage, @Nullable LivingEntity entity) {


        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            double dx = x + offsetX;
            double dz = z + offsetZ;

            double minY = y - 2.0F;
            double maxY = y + 2.0F;


            spawnFlameStrike(level,
                    dx,dz, minY, maxY,0,
                    duration, delay, delay,size, damage,false, entity
            );
        }
    }

    public static void spawnSixDirectionFlameStrike(Level level, double x, double y, double z, float baseRot, int rune, float size, float damage, @Nullable LivingEntity entity) {
        for (int i = 0; i < 6; ++i) {
            // 每 60 度一个方向
            float yawRadians = (float) Math.toRadians(baseRot + i * 60.0F);

            for (int k = 0; k < rune; ++k) {
                double d2 = 2.25 * (k + 1);
                int delay = (int) (1.5 * k);
                double minY = y - 2.0F;
                double maxY = y + 2.0F;

                spawnFlameStrike(level,
                        x + Mth.cos(yawRadians) * d2,
                        z + Mth.sin(yawRadians) * d2,
                        minY, maxY,yawRadians,
                        40, delay, delay,
                        size, damage,false, entity
                );
            }
        }
    }

    public static void spawnEightDirectionFlameStrike(Level level, double x, double y, double z, float baseRot, int rune, float size, float damage, @Nullable LivingEntity entity) {
        for (int i = 0; i < 8; ++i) {
            // 每 45 度一个方向
            float yawRadians = (float) Math.toRadians(baseRot + i * 45.0F);

            for (int k = 0; k < rune; ++k) {
                double d2 = 2.25 * (k + 1);
                int delay = (int) (1.5 * k);
                double minY = y - 2.0F;
                double maxY = y + 2.0F;

                spawnFlameStrike(level,
                        x + Mth.cos(yawRadians) * d2,
                        z + Mth.sin(yawRadians) * d2,
                        minY, maxY,yawRadians,
                        40, delay, delay,
                        size, damage,false, entity
                );
            }
        }
    }

    private static void spawnFlameStrike(Level world, double x, double z, double minY, double maxY, float rotation, int duration, int wait, int delay, float radius, float damage, boolean soul, @Nullable LivingEntity player) {
        BlockPos blockpos = BlockPos.containing(x, maxY, z);
        boolean flag = false;
        double d0 = 0.0;

        // 向下检测地面
        do {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = world.getBlockState(blockpos1);
            if (blockstate.isFaceSturdy(world, blockpos1, Direction.UP)) {
                if (!world.isEmptyBlock(blockpos)) {
                    BlockState blockstate1 = world.getBlockState(blockpos);
                    VoxelShape voxelshape = blockstate1.getCollisionShape(world, blockpos);
                    if (!voxelshape.isEmpty()) {
                        d0 = voxelshape.max(Direction.Axis.Y);
                    }
                }
                flag = true;
                break;
            }
            blockpos = blockpos.below();
        } while (blockpos.getY() >= minY);

        // 生成火焰打击实体
        if (flag) {
            world.addFreshEntity(new Flame_Strike_Entity(world,x,blockpos.getY() + d0,z,
                    rotation,duration, wait,delay,radius,damage,2.0F,soul,player));
        }
    }

    private static void shootFireball(LivingEntity entity, Vec3 shotAt, int timer) {
        shotAt = shotAt.yRot(-entity.getYRot() * ((float) Math.PI / 180F));
        Ignis_Fireball_Entity shot = new Ignis_Fireball_Entity(entity.level(), entity);
        shot.setPos(entity.getX() - (double) (entity.getBbWidth() + 1.0F) * 0.15 * (double) Mth.sin(entity.yBodyRot * ((float) Math.PI / 180F)), entity.getY() + (double) 1.0F, entity.getZ() + (double) (entity.getBbWidth() + 1.0F) * 0.15 * (double) Mth.cos(entity.yBodyRot * ((float) Math.PI / 180F)));
        double d0 = shotAt.x;
        double d1 = shotAt.y;
        double d2 = shotAt.z;
        float f = Mth.sqrt((float) (d0 * d0 + d2 * d2)) * 0.35F;
        shot.shoot(d0, d1 + (double) f, d2, 0.25F, 3.0F);
        shot.setUp(timer + 1);

        entity.level().addFreshEntity(shot);
    }

    private static void shootAbyssFireball(LivingEntity entity, Vec3 shotAt, int timer) {
        shotAt = shotAt.yRot(-entity.getYRot() * ((float) Math.PI / 180F));
        Ignis_Abyss_Fireball_Entity shot = new Ignis_Abyss_Fireball_Entity(entity.level(), entity);
        shot.setPos(entity.getX() - (double) (entity.getBbWidth() + 1.0F) * 0.15 * (double) Mth.sin(entity.yBodyRot * ((float) Math.PI / 180F)), entity.getY() + (double) 1.0F, entity.getZ() + (double) (entity.getBbWidth() + 1.0F) * 0.15 * (double) Mth.cos(entity.yBodyRot * ((float) Math.PI / 180F)));
        double d0 = shotAt.x;
        double d1 = shotAt.y;
        double d2 = shotAt.z;
        float f = Mth.sqrt((float) (d0 * d0 + d2 * d2)) * 0.35F;
        shot.shoot(d0, d1 + (double) f, d2, 0.25F, 3.0F);
        shot.setUp(timer + 1);
        entity.level().addFreshEntity(shot);
    }

    public static void summonFireBall(LivingEntityPatch<?> entityPatch, int type) {
        if(entityPatch.isLogicalClient()) return;
        LivingEntity entity = entityPatch.getOriginal();
        entityPatch.playSound(ModSounds.ABYSS_BLAST_ONLY_CHARGE.get(), 0, 0);
        entityPatch.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 0,0);

        switch (type) {
            case 0 ->{
                shootAbyssFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 50);
                shootFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 40);
                shootFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 30);
                shootFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 20);
            }
            case 1 ->{
                shootFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 60);
                shootAbyssFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 50);
                shootFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 40);
                shootFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 30);
                shootFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 20);
            }
            case 2 ->{
                shootFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 50);
                shootAbyssFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 40);
                shootFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 30);
                shootFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 20);
            }
            case 3 ->{
                shootFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 50);
                shootFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 40);
                shootAbyssFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 30);
                shootFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 20);
            }
            case 4 ->{
                shootFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 50);
                shootFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 40);
                shootFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 30);
                shootAbyssFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 20);
            }
            case 5 ->{
                shootAbyssFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 20);
                shootAbyssFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 20);
                shootAbyssFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 60);
            }
            case 6 ->{
                shootFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 40);
                shootAbyssFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 20);
                shootAbyssFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 40);
            }
            case 7 ->{
                shootFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 40);
                shootFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 20);
                shootFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 60);
            }
            default -> {
                shootFireball(entity, new Vec3(-5.0F, 3.0F, 0.0F), 60);
                shootFireball(entity, new Vec3(-2.0F, 3.0F, 0.0F), 50);
                shootFireball(entity, new Vec3(0.0F, 3.0F, 0.0F), 40);
                shootFireball(entity, new Vec3(2.0F, 3.0F, 0.0F), 30);
                shootFireball(entity, new Vec3(5.0F, 3.0F, 0.0F), 20);
            }
        }
    }
}
