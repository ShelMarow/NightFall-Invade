package net.shelmarow.nightfall_invade.entity.misc.blood_slash;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.shelmarow.combat_evolution.ai.CEHumanoidPatch;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class BloodSlashEntity extends Mob {

    private static final EntityDataAccessor<Float> DATA_X_ROT = SynchedEntityData.defineId(BloodSlashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_Y_ROT = SynchedEntityData.defineId(BloodSlashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_Z_ROT = SynchedEntityData.defineId(BloodSlashEntity.class, EntityDataSerializers.FLOAT);
    private float hpDamage = 0;
    private float maxHpDamage = 0;

    private @Nullable LivingEntity owner;

    public BloodSlashEntity(EntityType<BloodSlashEntity> vacuumSliceEntityEntityType, Level level) {
        super(vacuumSliceEntityEntityType, level);
    }

    public BloodSlashEntity(@Nullable LivingEntity entity, Level level) {
        super(NFIEntities.BLOOD_SLASH.get(), level);
        this.owner = entity;
    }

    public void setSlashYRot(float yRot) {
        setYRot(yRot);
        yRotO = yRot;
        yBodyRot = yRot;
        yBodyRotO = yRot;
        yHeadRot = yRot;
        yHeadRotO = yRot;
        entityData.set(DATA_Y_ROT, yRot);
    }

    public void setSlashXRot(float xRot) {
        setXRot(xRot);
        xRotO = xRot;
        entityData.set(DATA_X_ROT, xRot);
    }

    public void setSlashZRot(float zRot) {
        entityData.set(DATA_Z_ROT, zRot);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(EpicFightAttributes.MAX_STRIKES.get(), Integer.MAX_VALUE)
                .add(EpicFightAttributes.IMPACT.get(), 1D)
                .add(EpicFightAttributes.ARMOR_NEGATION.get(), 100D);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_X_ROT, 0F);
        this.entityData.define(DATA_Y_ROT, 0F);
        this.entityData.define(DATA_Z_ROT, 0F);
    }

    @Override
    public boolean isPickable(){
        return false;
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 vec3){
        super.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity pEntity) {
        float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if(pEntity instanceof LivingEntity livingEntity) {
            damage += livingEntity.getMaxHealth() * maxHpDamage + livingEntity.getHealth() * hpDamage;
        }
        boolean flag = pEntity.hurt(this.damageSources().mobAttack(this), damage);
        if (flag) {
            this.doEnchantDamageEffects(this, pEntity);
            this.setLastHurtMob(pEntity);

            EpicFightCapabilities.getUnparameterizedEntityPatch(this, BloodSlashPatch.class).ifPresent(entitypatch -> {
                entitypatch.setLastAttackResult(AttackResult.success(0));
                entitypatch.setLastAttackEntity(pEntity);
            });

            if(owner != null){
                float healPercent = 0.01F;
                if(owner instanceof Player){
                    healPercent = 0.1F;
                }
                owner.heal(owner.getMaxHealth() * healPercent);
                CEHumanoidPatch<?> ceHumanoidPatch = EpicFightCapabilities.getEntityPatch(owner, CEHumanoidPatch.class);
                if(ceHumanoidPatch != null){
                    CEPatchUtils.addStamina(ceHumanoidPatch, 5F);
                }
            }
        }

        return flag;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        noPhysics = true;
        setNoGravity(true);
        setInvulnerable(true);
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance pEffectInstance) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            float pYRot = entityData.get(DATA_Y_ROT);
            setYRot(pYRot);
            yRotO = pYRot;
            yBodyRot = pYRot;
            yBodyRotO = pYRot;
            yHeadRot = pYRot;
            yHeadRotO = pYRot;

            float pXRot = entityData.get(DATA_X_ROT);
            setXRot(pXRot);
            xRotO = pXRot;

        }
        else if(owner == null){
            discard();
        }
    }

    public @Nullable LivingEntity getOwner() {
        return owner;
    }

    public float getSlashXRot() {
        return entityData.get(DATA_X_ROT);
    }

    public float getSlashZRot() {
        return entityData.get(DATA_Z_ROT);
    }

    public float getSlashYRot() {
        return entityData.get(DATA_Y_ROT);
    }

    public void setBasicDamage(double basicDamage) {
        AttributeInstance instance = getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance != null) {
            instance.setBaseValue(basicDamage);
        }
    }

    public void setHpDamage(float hpDamage) {
        this.hpDamage = hpDamage;
    }

    public void setMaxHpDamage(float maxHpDamage) {
        this.maxHpDamage = maxHpDamage;
    }

    public float getHpDamage() {
        return hpDamage;
    }

    public float getMaxHpDamage() {
        return maxHpDamage;
    }
}
