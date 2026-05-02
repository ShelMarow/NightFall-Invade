package net.shelmarow.nightfall_invade.entity.fallen_knight.ai;

import com.asanginxst.epicfightx.gameassets.animations.AnimationsX;
import com.asanginxst.epicfightx.gameassets.animations.ExtraAnimations;
import com.hm.efn.gameasset.animations.EFNDodgeAnimations;
import com.hm.efn.gameasset.animations.EFNGreatSwordAnimations;
import net.minecraft.world.effect.MobEffectInstance;
import net.shelmarow.combat_evolution.ai.CECombatBehaviors;
import net.shelmarow.combat_evolution.effect.CEMobEffects;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class FallenKnightAI {
    public static CECombatBehaviors.Builder<MobPatch<?>> createNormal() {
        return CECombatBehaviors.builder()

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1)
                        .maxCooldown(40)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0,4)
                                .animationBehavior(AnimationsX.GREATSWORD_AUTO1, 0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0,4)
                                        .animationBehavior(AnimationsX.GREATSWORD_AUTO2, 0.25F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0,4)
                                                .animationBehavior(ExtraAnimations.GREATSWORD_AUTO3, 0.25F)

                                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                        .randomChance(0.35F)
                                                        .animationBehavior(ExtraAnimations.GREATSWORD_AUTO4, 0.25F)

                                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                                .animationBehavior(ExtraAnimations.GREATSWORD_AUTO5, 0.25F)
                                                        )
                                                )
                                        )
                                )
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0,4)
                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO1, 0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0,4)
                                        .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO2, 0.25F)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .withinDistance(0,4)
                                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_AUTO3, 0.25F)
                                        )
                                )
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0,4)
                                .animationBehavior(Animations.GREATSWORD_AUTO1, 0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .withinDistance(0,4)
                                        .animationBehavior(Animations.GREATSWORD_AUTO2, 0.25F)
                                )
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1)
                        .maxCooldown(60)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .withinDistance(0,4)
                                .animationBehavior(Animations.GREATSWORD_DASH, 0.25F)
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(10)
                        .maxCooldown(100)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .guard(100)
                                .withinDistance(0,6)
                                .attackLevel(1,2)
                                .counterType(CECombatBehaviors.CounterType.END)
                                .maxGuardHit(3)
                                .counterAnimation(EFNGreatSwordAnimations.NG_GREATSWORD_DASH, 0.25F)
                                .onCounterStart(mobPatch -> {
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.HIGH_STUN_IMMUNITY.get(), 30, 0, false, false, false));
                                })
                        )
                )

                .newBehaviorRoot(CECombatBehaviors.BehaviorRoot.builder()
                        .priority(1).weight(1)
                        .maxCooldown(10)

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(2)
                                .withinDistance(0,6)
                                .guardWithWander(60, 0.25F, 0, false)
                                .counterType(CECombatBehaviors.CounterType.RANDOM)
                                .counterChance(0.4F)
                                .counterAnimation(EFNGreatSwordAnimations.NG_GREATSWORD_DASH, 0.25F)
                                .onCounterStart(mobPatch -> {
                                    mobPatch.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.HIGH_STUN_IMMUNITY.get(), 30, 0, false, false, false));
                                })
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0,6)
                                .stopByStun(5)
                                .interruptedByTime(1.5F,3F)
                                .wander(60, 0F, 0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .attackLevel(1,2)
                                        .animationBehavior(Animations.BIPED_ROLL_BACKWARD, 0)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .randomChance(0.45F)
                                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_FIRST, 0.25F)
                                        )
                                )
                        )

                        .addFirstBehavior(CECombatBehaviors.Behavior.builder()
                                .priority(1).weight(1)
                                .withinDistance(0,6)
                                .stopByStun(5)
                                .interruptedByTime(1.5F,3F)
                                .wander(60, 0F, -0.25F)

                                .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                        .canInterruptParent(true)
                                        .attackLevel(1,2)
                                        .animationBehavior(Animations.BIPED_ROLL_BACKWARD, 0)

                                        .addNextBehavior(CECombatBehaviors.Behavior.builder()
                                                .randomChance(0.45F)
                                                .animationBehavior(EFNGreatSwordAnimations.NG_GREATSWORD_CHARG1MAX_FIRST, 0.25F)
                                        )
                                )
                        )
                )
                ;
    }
}
