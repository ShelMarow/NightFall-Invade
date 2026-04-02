package net.shelmarow.nightfall_invade.assets;

import com.hm.efn.client.sound.EFNSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.BloodSlashEntity;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.BloodSlashPatch;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Set;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class NFIAnimations {

    public static AnimationManager.AnimationAccessor<LongHitAnimation> SCARLET_HUNTER_BROKEN;

    public static AnimationManager.AnimationAccessor<StaticAnimation> IDLE;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SHOOT;

    public static AnimationManager.AnimationAccessor<AttackAnimation> BLOOD_SLASH_ATTACK;


    public static final Collider BLOOD_SLASH =
            new MultiOBBCollider(3,
                    0.2D, 1.0D, 0.75D,
                    0D, 0D, -0.75D
            );

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(NightFallInvade.MOD_ID, NFIAnimations::build);
    }

    public static void build(AnimationManager.AnimationBuilder builder) {
        SCARLET_HUNTER_BROKEN = builder.nextAccessor("scarlet_hunter/scarlet_hunter_broken", accessor ->
                new LongHitAnimation(0.05F,accessor, Armatures.BIPED));

        IDLE = builder.nextAccessor("blood_slash/idle", accessor->
                new StaticAnimation(0F, true, accessor, NFIArmatures.BLOOD_SLASH_ARMATURE));

        SHOOT = builder.nextAccessor("blood_slash/shoot", accessor->
                new AttackAnimation(0F,0F,0F,0.625F,0.625F,
                        BLOOD_SLASH, NFIArmatures.BLOOD_SLASH_ARMATURE.get().rootJoint, accessor, NFIArmatures.BLOOD_SLASH_ARMATURE)
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.BLADE_RUSH_SKILL)
                        .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
                        .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1F))
                        .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1F))
                        .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.setter(100.0F))
                        .addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(
                                EpicFightDamageTypeTags.FINISHER, EpicFightDamageTypeTags.BYPASS_DODGE,
                                EpicFightDamageTypeTags.GUARD_PUNCTURE, EpicFightDamageTypeTags.UNBLOCKALBE)
                        )
                        .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get())
                        .addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true)
                        .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
                        .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, ROOT_X_MODIFIER)
                        .addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD_WITH_X_ROT)
                        .addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER,(self, entityPatch, speed, prevElapsedTime, elapsedTime) -> 1F)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create((entityPatch, animation, params) -> {
                            entityPatch.getOriginal().discard();
                        }, AnimationEvent.Side.SERVER))
        );

        BLOOD_SLASH_ATTACK = builder.nextAccessor("biped/skill/blood_slash", accessor->
                new AttackAnimation(0.15F, 1F, 1F, 1.16F, 1.8F, null, Armatures.BIPED.get().toolR, accessor, Armatures.BIPED)
                        .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.LONG)
                        .addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND,EFNSounds.MORTAL_BLADE_CHARGE2.get())
                        .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER,(self, entityPatch, speed, prevElapsedTime, elapsedTime) -> 1F)
                        .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.SimpleEvent.create((entitypatch, animation, params) -> {
                            entitypatch.playSound(EFNSounds.MORTAL_BLADE_CHARGE1.get(),0,0);
                        }, AnimationEvent.Side.SERVER))
                        .addEvents(AnimationEvent.InTimeEvent.create(1.08F, (entitypatch, animation, params) -> {
                            float yaw = entitypatch.getYRot();
                            double damage = entitypatch.getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5;
                            entitypatch.playSound(EpicFightSounds.WHOOSH_SHARP.get(), 0,0);
                            spawnBloodSlash(entitypatch, yaw + 24,0,0, damage, 0.02F, 0.04F);
                            spawnBloodSlash(entitypatch, yaw + 12,0,0, damage, 0.02F, 0.04F);
                            spawnBloodSlash(entitypatch, yaw +  0,0,0, damage, 0.02F, 0.04F);
                            spawnBloodSlash(entitypatch, yaw - 12,0,0, damage, 0.02F, 0.04F);
                            spawnBloodSlash(entitypatch, yaw - 24,0,0, damage, 0.02F, 0.04F);
                        }, AnimationEvent.Side.SERVER))

        );
    }

    public static final AnimationProperty.PoseModifier ROOT_X_MODIFIER = (self, pose, entitypatch, time, partialTicks) -> {
        float pitch = -entitypatch.getOriginal().getXRot();
        JointTransform chest = pose.orElseEmpty("Root");
        chest.frontResult(JointTransform.rotation(QuaternionUtils.XP.rotationDegrees(-pitch)), OpenMatrix4f::mulAsOriginInverse);

        if(entitypatch instanceof BloodSlashPatch bloodSlashPatch){
            float roll = bloodSlashPatch.getOriginal().getSlashZRot();
            chest.frontResult(JointTransform.rotation(QuaternionUtils.ZP.rotationDegrees(roll)), OpenMatrix4f::mulAsOriginInverse);
        }
    };

    private static void spawnBloodSlash(LivingEntityPatch<?> mobPatch, float yaw, float pitch, float roll, double basicDamage, float maxHpDamage, float hpDamage) {
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
}
