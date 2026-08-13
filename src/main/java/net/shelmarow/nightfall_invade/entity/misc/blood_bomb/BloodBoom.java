package net.shelmarow.nightfall_invade.entity.misc.blood_bomb;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.particle.NFIParticles;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

public class BloodBoom extends Projectile {

    private LivingEntityPatch<?> ownerPatch;
    private boolean lunched = false;
    private int lifeTick = 0;
    private int waitTick = 30;
    private float damage = 1;


    public BloodBoom(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public BloodBoom(Level pLevel) {
        super(NFIEntities.BLOOD_BOOM.get(), pLevel);
    }

    @Override
    protected void defineSynchedData() {
    }

    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putFloat("damage", this.damage);
    }

    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("damage", Tag.TAG_ANY_NUMERIC)) {
            this.damage = pCompound.getFloat("damage");
        }
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();

        if(!level().isClientSide){
            if(entity == null || getOwnerPatch() == null || !entity.isAlive() || !getOwnerPatch().getOriginal().isAlive()){
                discard();
            }
        }

        if (this.level().isClientSide || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
            super.tick();

            if(lifeTick++ >= 200){
                this.discard();
                return;
            }

            //命中检测
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
                this.setPos(hitresult.getLocation());
                this.onHit(hitresult);
                return;
            }
            this.checkInsideBlocks();

            //设置运动逻辑
            Vec3 velocity = this.getDeltaMovement();
            //生成后等待一段时，期间逐渐减速
            if (lifeTick < waitTick) {
                velocity = velocity.scale(0.9);
            }
            //随后如果存在目标，进行追踪发射
            else if (ownerPatch != null && ownerPatch.getTarget() != null) {

                LivingEntity target = ownerPatch.getTarget();
                double speed = velocity.length();

                //启动时先索敌运动一次
                if (!lunched && (lifeTick == waitTick || speed < 0.01)) {
                    lunched = true;
                    Vec3 dir = target.getEyePosition().add(0,target.getY(0.5),0).subtract(this.getEyePosition()).normalize();
                    velocity = dir.scale(0.15);
                    speed = velocity.length();
                    level().playSound(null, blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, 1.0F);
                }

                //计算运动方向和目标的夹角
                Vec3 currentDir = velocity.normalize();
                Vec3 targetDir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
                Vec3 currentXZ = new Vec3(currentDir.x, 0, currentDir.z).normalize();
                Vec3 targetXZ = new Vec3(targetDir.x, 0, targetDir.z).normalize();
                double dot = currentXZ.dot(targetXZ);
                dot = Mth.clamp(dot, -1.0, 1.0);
                double angle = Math.acos(dot);

                //如果角度过大则不进行追踪
                if (angle <= Math.PI / 3 || lifeTick <= waitTick + 5) {
                    double maxTurn = Math.toRadians(2.0);
                    double t = Math.min(1.0, maxTurn / angle);

                    Vec3 newXZ = currentXZ.lerp(targetXZ, t).normalize();
                    double verticalLerp = 0.2;
                    double newY = Mth.lerp(verticalLerp, currentDir.y, targetDir.y);
                    Vec3 newDir = new Vec3(newXZ.x, newY, newXZ.z).normalize();

                    //速度设置
                    double accel = 1.1;
                    double maxSpeed = 1.15;
                    double newSpeed = Math.min(speed * accel, maxSpeed);
                    velocity = newDir.scale(newSpeed);
                }
                else {
                    velocity = velocity.scale(1.15F);
                }

            }
            this.setDeltaMovement(velocity);

            double d0 = this.getX() + velocity.x;
            double d1 = this.getY() + velocity.y;
            double d2 = this.getZ() + velocity.z;
            this.setPos(d0, d1, d2);
            ProjectileUtil.rotateTowardsMovement(this, 0.2F);

            if (this.level().isClientSide) {
                for (int i = 0; i < 8; ++i) {
                    this.level().addAlwaysVisibleParticle(
                            NFIParticles.BLOOD_A.get(),
                            d0 + 0 + Math.random() * 0.4 - 0.2,
                            d1 + 0.125 + Math.random() * 0.4 - 0.2,
                            d2 + 0 + Math.random() * 0.4 - 0.2,
                            0, 0, 0
                    );
                }
            }

        } else {
            this.discard();
        }
    }


    @Override
    protected boolean canHitEntity(@NotNull Entity pTarget) {
        return super.canHitEntity(pTarget) && (ownerPatch == null || pTarget != ownerPatch.getOriginal());
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    private Holder<DamageType> getDamageTypeHolder(ResourceKey<DamageType> damageTypeKey) {
        return level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageTypeKey);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        Entity target = pResult.getEntity();

        LivingEntity original = null;
        if ((ownerPatch == null || target != ownerPatch.getOriginal()) && target instanceof LivingEntity livingTarget) {

            DamageSource source;
            if(ownerPatch != null) {
                original = ownerPatch.getOriginal();
                source = new DamageSource(getDamageTypeHolder(DamageTypes.MAGIC), original, original, this.position());
            }
            else {
                source = new DamageSource(getDamageTypeHolder(DamageTypes.MAGIC), this, null, this.position());
            }

            //伤害源
            DamageSource efSource = new EpicFightDamageSource(source)
                    .addRuntimeTag(DamageTypeTags.BYPASSES_ARMOR)
                    .addRuntimeTag(EpicFightDamageTypeTags.GUARD_PUNCTURE)
                    .setStunType(StunType.SHORT).setBaseImpact(3.0F);

            //造成伤害
            int preInvul = livingTarget.invulnerableTime;
            livingTarget.invulnerableTime = 0;

            float hpDamage = 0.15F;
            if(ownerPatch instanceof PlayerPatch){
                hpDamage = 0.03F;
            }
            boolean success = livingTarget.hurt(efSource, this.damage + livingTarget.getHealth() * hpDamage);
            if(success && original != null) {
                float healPercent = 0.01F;
                if(original instanceof Player){
                    healPercent = 0.05F;
                }
                original.heal(original.getMaxHealth() * healPercent);
            }

            if(original != null){
                livingTarget.setLastHurtMob(original);
            }

            livingTarget.invulnerableTime = preInvul;

            //伪装爆炸效果
            if(level().isClientSide) {
                level().addParticle(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 0,0,0);
            }
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.0F);

            discard();
        }


    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        level().explode(
                this,
                getX(), getY(), getZ(),
                0f, Level.ExplosionInteraction.NONE
        );
        discard();
    }

    public int getWaitTick() {
        return waitTick;
    }

    public void setWaitTick(int waitTick) {
        this.waitTick = waitTick;
    }

    public LivingEntityPatch<?> getOwnerPatch() {
        return ownerPatch;
    }

    public void setOwnerPatch(LivingEntityPatch<?> ownerPatch) {
        this.ownerPatch = ownerPatch;
        super.setOwner(ownerPatch.getOriginal());
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
}
