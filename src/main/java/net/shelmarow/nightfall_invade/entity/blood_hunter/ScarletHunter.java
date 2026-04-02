package net.shelmarow.nightfall_invade.entity.blood_hunter;

import com.asanginxst.epicfightx.gameassets.animations.AnimationsX;
import com.hm.efn.registries.EFNItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.shelmarow.combat_evolution.ai.StaminaStatus;
import net.shelmarow.combat_evolution.ai.util.BehaviorUtils;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.bgm.network.CEMusicNetworkHandler;
import net.shelmarow.combat_evolution.bgm.network.CEMusicPacket;
import net.shelmarow.combat_evolution.bossbar.CEBossEvent;
import net.shelmarow.combat_evolution.damage_source.CEDamageTypeTags;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.api.event.NFIFinalDamageEvent;
import net.shelmarow.nightfall_invade.effect.NFIMobEffects;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScarletHunter extends PathfinderMob {

    private final CEBossEvent ceBossEvent = new CEBossEvent(
            "[NightFallInvade:ScarletHunter]",
            Component.empty().append(getDisplayName()).withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
    );

    private final UUID bgmRequestUUID = UUID.randomUUID();
    private final CEMusicPacket ceMusicPacket;
    private boolean shouldPlayBGM = false;
    private int removeBGMWaitTime = 0;

    protected LivingEntityPatch<?> cePatch = null;

    private static final EntityDataAccessor<String> TRUE_HEALTH = SynchedEntityData.defineId(ScarletHunter.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BOSS_PHASE = SynchedEntityData.defineId(ScarletHunter.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BLOOD_SHIELD = SynchedEntityData.defineId(ScarletHunter.class, EntityDataSerializers.BOOLEAN);
    private int phaseChangeCounter = -1;
    private int bloodShieldCooldown = 0;
    protected int bloodShieldTimer = 0;

    //脱战回血
    private int noTargetTime = 0;

    //状态保护
    private boolean canBypassSpeedLimit = false;
    private boolean canBypassStunImmunity = false;

    //受伤保护
    protected int totalHitCounter = 0;
    protected float totalDamageTaken = 0;
    //单次限伤
    private final float DAMAGE_CAP = 0.05F;
    //累计伤害达到多少时，减少达到最大值
    private final float MAX_DAMAGE_TAKEN = 0.07F;
    //减伤完全衰减时间
    private final int RESISTANCE_REDUCE_TIME = 160;

    public ScarletHunter(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setTrueHealth(getMaxHealth());
        this.setEquipment();
        ceMusicPacket = new CEMusicPacket(
                false, bgmRequestUUID,
                ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID,"scarlet_hunter.bgm.blood_for_blood"),
                SoundSource.RECORDS, 0.7F, 4680, true, true, 40, 40
        );
        CompoundTag tag = ceBossEvent.getCustomData();
        tag.putBoolean("bloodShield", false);
        ceBossEvent.updateCustomData(tag);
    }

    private void setEquipment() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(EFNItem.CRIMSON_MOON.get()));
        this.setDropChance(EquipmentSlot.MAINHAND,0F);
        this.setDropChance(EquipmentSlot.OFFHAND,0F);
        this.setDropChance(EquipmentSlot.HEAD,0F);
        this.setDropChance(EquipmentSlot.CHEST,0F);
        this.setDropChance(EquipmentSlot.LEGS,0F);
        this.setDropChance(EquipmentSlot.FEET,0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1D)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1D)

                .add(EpicFightAttributes.IMPACT.get(),0.5D)
                .add(EpicFightAttributes.ARMOR_NEGATION.get(),0D)
                .add(EpicFightAttributes.STUN_ARMOR.get(),20.0D)
                .add(EpicFightAttributes.MAX_STRIKES.get(),100.0D)
                .add(EpicFightAttributes.MAX_STAMINA.get(),60.0D)
                .add(EpicFightAttributes.STAMINA_REGEN.get(),1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class,12));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TRUE_HEALTH, "[1]");
        this.entityData.define(BOSS_PHASE, 0);
        this.entityData.define(BLOOD_SHIELD, false);
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("trueHealth", getTrueHealth());
        tag.putInt("bossPhase", getBossPhase());
        tag.putBoolean("bloodShield", hasBloodShield());
        tag.putInt("phaseChangeCounter", this.phaseChangeCounter);
        tag.putInt("bloodShieldCooldown", this.bloodShieldCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.contains("trueHealth")) {
            setTrueHealth(tag.getFloat("trueHealth"));
        }
        setBossPhase(tag.getInt("bossPhase"));
        setBloodShield(tag.getBoolean("bloodShield"));
        this.phaseChangeCounter = tag.contains("phaseChangeCounter") ? tag.getInt("phaseChangeCounter") : -1;
        this.bloodShieldCooldown = tag.getInt("bloodShieldCooldown");
    }

    @Override
    public boolean canRide(@NotNull Entity entity) {
        return false;
    }

    @Override
    public void rideTick(){
        this.unRide();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if(!level().isClientSide){

            if(getTarget() != null && !shouldPlayBGM){
                removeBGMWaitTime = 0;
                shouldPlayBGM = true;
                for(ServerPlayer serverPlayer : ceBossEvent.getPlayers()){
                    CEMusicNetworkHandler.sendRequestPlayPacket(serverPlayer, ceMusicPacket);
                }
            }
            else if(getTarget() == null && shouldPlayBGM){
                removeBGMWaitTime = 0;
                shouldPlayBGM = false;
            }

            if(getTarget() == null && !shouldPlayBGM && removeBGMWaitTime >= 0){
                if(removeBGMWaitTime++ >= 20){
                    removeBGMWaitTime = -1;
                    for(ServerPlayer serverPlayer : ceBossEvent.getPlayers()){
                        CEMusicNetworkHandler.sendRemoveMusicPacket(serverPlayer, bgmRequestUUID, false);
                    }
                }
            }

            if(getTarget() == null){
                if(noTargetTime < 160) {
                    noTargetTime++;
                }
                else if(tickCount % 5 == 0){
                    if(getHealth() == getMaxHealth()){
                        setBossPhase(0);
                        setBloodShield(false);
                        setPhaseChangeCounter(-1);
                    }
                    if(getHealth() < getMaxHealth()){
                        heal(getMaxHealth() * 0.05F);
                    }

                }
            } else if(noTargetTime != 0){
                noTargetTime = 0;
            }


            //切换阶段计时器
            if(phaseChangeCounter != -1){
                if(phaseChangeCounter-- <= 0){
                    phaseChangeCounter = -1;
                    setBossPhase(getBossPhase() + 1);
                    if(getBossPhase() == 2){
                        onPhaseChange();
                    }
                }
            }

            //超时自动破盾
            if(hasBloodShield() && getTarget() != null){
                bloodShieldTimer++;
            }
            else if((!hasBloodShield() && bloodShieldTimer != 0) || (hasBloodShield() && bloodShieldTimer != 0 && noTargetTime >= 20)){
                bloodShieldTimer = 0;
            }

            //血盾
            if(getBossPhase() > 0 && !hasBloodShield() && bloodShieldCooldown == 0){
                EpicFightCapabilities.getUnparameterizedEntityPatch(this, ScarletHunterPatch.class).ifPresent(entityPatch -> {
                    if(!entityPatch.isStunned() && CEPatchUtils.getStaminaStatus(entityPatch) != StaminaStatus.BREAK){
                        setBloodShield(true);
                        CEPatchUtils.setStaminaStatus(entityPatch, StaminaStatus.COMMON);
                        CEPatchUtils.setStamina(entityPatch,CEPatchUtils.getMaxStamina(entityPatch));
                    }
                });
            }
            else if(bloodShieldCooldown > 0){
                bloodShieldCooldown--;
            }

            //伤害减免计算
            if(totalDamageTaken > 0){
                totalDamageTaken = Mth.approach(totalDamageTaken, 0, getMaxHealth() * MAX_DAMAGE_TAKEN / RESISTANCE_REDUCE_TIME);
            }

            if(tickCount % 4 == 0 && totalHitCounter > 0){
                totalHitCounter = Mth.clamp(totalHitCounter-1, 0, 5);
            }

            //BOSS血条信息
            this.setBossBarHealth();
            this.setBossBarStamina();

            //防止锁尸体（怪物大乱斗）
            if(getTarget() != null && !getTarget().isAlive()){
                setTarget(null);
            }
        }
    }

    protected void setBossBarHealth() {
        this.ceBossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    protected void setBossBarStamina() {
        if (this.cePatch == null) {
            this.cePatch = EpicFightCapabilities.getEntityPatch(this, LivingEntityPatch.class);
        }

        if (this.cePatch != null) {
            this.ceBossEvent.setStaminaStatus(CEPatchUtils.getStaminaStatus(this.cePatch));
            this.ceBossEvent.setStamina(CEPatchUtils.getStaminaPercent(this.cePatch));
        }

    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
        return false;
    }

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.ceBossEvent.addPlayer(pPlayer);
        if(getTarget() != null){
            CEMusicNetworkHandler.sendRequestPlayPacket(pPlayer, ceMusicPacket);
        }
    }

    @Override
    public void stopSeenByPlayer(@NotNull ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.ceBossEvent.removePlayer(pPlayer);
        CEMusicNetworkHandler.sendRemoveMusicPacket(pPlayer, bgmRequestUUID, false);
    }

    @Override
    public float getScale(){
        return 1F;
    }


    @Override
    public void setDeltaMovement(@NotNull Vec3 pDeltaMovement) {
        double length = pDeltaMovement.length();
        if(length > 1 && !canBypassSpeedLimit){
            pDeltaMovement = pDeltaMovement.normalize();
        }
        else if(canBypassSpeedLimit){
            canBypassSpeedLimit = false;
        }
        super.setDeltaMovement(pDeltaMovement);
    }

    public boolean isCanBypassSpeedLimit() {
        return canBypassSpeedLimit;
    }

    public void setCanBypassSpeedLimit(boolean canBypassSpeedLimit) {
        this.canBypassSpeedLimit = canBypassSpeedLimit;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        return (pEffectInstance.getEffect().getCategory() == MobEffectCategory.BENEFICIAL ||
                pEffectInstance.getEffect() == MobEffects.GLOWING ||
                pEffectInstance.getEffect() == NFIMobEffects.SOUL_OF_FLAME.get()) &&
                super.canBeAffected(pEffectInstance);
    }


    @Override
    protected void tickEffects() {
        Set<MobEffectInstance> remove = new HashSet<>();
        for (MobEffectInstance instance : getActiveEffects()){
            if (!canBeAffected(instance)) {
                remove.add(instance);
            }
        }
        for (MobEffectInstance instance : remove){
            removeEffect(instance.getEffect());
        }
        super.tickEffects();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return hasBloodShield() ? SoundEvents.GLASS_BREAK : SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return super.getDeathSound();
    }

    @Override
    public boolean killedEntity(@NotNull ServerLevel pLevel, @NotNull LivingEntity pEntity) {
        if(cePatch != null){
            BehaviorUtils.setRootCooldown(cePatch,"远距离惩罚剑气", 300, false);
            BehaviorUtils.resetRootCooldown(cePatch,"远距离追击",  false);
        }
        return true;
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource damageSource) {
        Entity attacker = damageSource.getEntity();
        if(damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
            if(attacker != null){
                return true;

            }
        }
        //免疫远距离的攻击
        if(attacker != null){
            Vec3 attackerPos = attacker.position();
            Vec3 selfPos = position();
            if(attackerPos.distanceToSqr(selfPos) >= 16 * 16){
                return true;
            }
        }
        return super.isInvulnerableTo(damageSource);
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if(!pSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && phaseChangeCounter != -1){
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public void actuallyHurt(@NotNull DamageSource pDamageSource, float originalDamage) {
        if (!this.isInvulnerableTo(pDamageSource)) {
            noTargetTime = 0;
            //Hurt事件伤害修改
            originalDamage = ForgeHooks.onLivingHurt(this, pDamageSource, originalDamage);
            if (originalDamage <= 0) return;

            //计算伤害减免
            originalDamage = this.getDamageAfterArmorAbsorb(pDamageSource, originalDamage);
            originalDamage = this.getDamageAfterMagicAbsorb(pDamageSource, originalDamage);

            //计算伤害吸收
            float damage = Math.max(originalDamage - this.getAbsorptionAmount(), 0.0F);
            //被吸收的伤害
            float absorbed = originalDamage - damage;
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (originalDamage - damage));
            if (absorbed > 0.0F && absorbed < Float.MAX_VALUE) {
                Entity entity = pDamageSource.getEntity();
                if (entity instanceof ServerPlayer serverplayer) {
                    serverplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbed * 10.0F));
                }
            }

            //damage事件额外修改伤害
            damage = ForgeHooks.onLivingDamage(this, pDamageSource, damage);


            //进行限伤计算
            if(!pDamageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !pDamageSource.is(CEDamageTypeTags.EXECUTION)) {
                //单次限伤
                float damageCap = getMaxHealth() * DAMAGE_CAP;
                damage = Mth.clamp(damage, 0, damageCap);

                //受击保护
                float damageProtect = 1 - Mth.clamp(totalDamageTaken / (getMaxHealth() * MAX_DAMAGE_TAKEN), 0F, 0.90F);
                float hitCountProtect = 1 - Mth.clamp(totalHitCounter / 5F, 0F, 0.90F);

                //远程武器伤害衰减
                float rangeAttackProtect = 0;
                Entity attacker = pDamageSource.getEntity();
                if(attacker != null){
                    Vec3 attackerPos = attacker.position();
                    Vec3 selfPos = position();
                    double distanceSqr = attackerPos.distanceToSqr(selfPos);
                    if(distanceSqr >= 6 * 6){
                        rangeAttackProtect = (float) Mth.clamp((distanceSqr - 6 * 6) / (12 * 12),0,1F);
                    }
                }

                damage *= damageProtect;
                damage *= 1 - rangeAttackProtect;
                if(totalHitCounter >= 5){
                    damage = 0.1F;
                }
                else{
                    damage *= hitCountProtect;
                }

                if(!hasBloodShield()){
                    totalHitCounter++;
                    totalDamageTaken += damage;
                }
            }

            //限制最高处决伤害
            if(pDamageSource.is(CEDamageTypeTags.EXECUTION) && !pDamageSource.is(CEDamageTypeTags.EXECUTION_FINISHED)){
                damage = Math.min(damage, getMaxHealth() * 0.08F);
            }
            else if(pDamageSource.is(CEDamageTypeTags.EXECUTION_FINISHED)){
                damage = Math.min(damage, getMaxHealth() * 0.17F);
            }

            //转阶段锁血
            if(!pDamageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
                //拥有血盾的情况下伤害降低100%
                if(hasBloodShield() || phaseChangeCounter != -1){
                    damage = 0;
                }

                int bossPhase = getBossPhase();
                //65%血量转阶段
                if(bossPhase == 0 && getHealth() - damage < getMaxHealth() * 0.65F){
                    //限制伤害不超过血量65%
                    damage = Math.max(getHealth() - getMaxHealth() * 0.65F, 0);
                    if(getHealth() < getMaxHealth() * 0.65F){
                        setTrueHealth(getMaxHealth() * 0.65F);
                    }
                    if(getPhaseChangeCounter() == -1){
                        //等待转阶段
                        onPhaseChange();
                    }
                }
            }

            NFIFinalDamageEvent event = new NFIFinalDamageEvent(this, pDamageSource, damage);
            damage = MinecraftForge.EVENT_BUS.post(event) ? 0 : event.getAmount();

            if (damage != 0.0F) {
                this.getCombatTracker().recordDamage(pDamageSource, damage);

                this.setTrueHealth(this.getTrueHealth() - damage);

                //this.setAbsorptionAmount(this.getAbsorptionAmount() - damage);
                this.gameEvent(GameEvent.ENTITY_DAMAGE);
            }
        }
    }

    private void onPhaseChange() {
        if(getBossPhase() == 0){
            this.phaseChangeCounter = 6000;
            this.bloodShieldCooldown = 0;
            BehaviorUtils.stopCurrentBehavior(this);
            if(cePatch != null && CEPatchUtils.getStaminaStatus(cePatch) != StaminaStatus.BREAK){
                setCanBypassStunImmunity(true);
                cePatch.playAnimationSynchronized(AnimationsX.BIPED_COMMON_NEUTRALIZED, 0F);
                BehaviorUtils.resetRootCooldown(cePatch, "赤色末路", false);
            }
            if(getBossPhase() == 0 && getTarget() != null){
                broadcastToNearbyPlayers(
                        Component.literal("[")
                                .append(this.getDisplayName())
                                .append(Component.literal("] "))
                                .append(Component.translatable("message.combat_evolution.scarlet_hunter_phase2")),
                        64
                );
            }
        }
        else if(getBossPhase() == 2){
            if(getTarget() != null){
                broadcastToNearbyPlayers(
                        Component.literal("[")
                                .append(this.getDisplayName())
                                .append(Component.literal("] "))
                                .append(Component.translatable("message.combat_evolution.scarlet_hunter_phase3")),
                        64
                );
            }
        }
    }
    public void broadcastToNearbyPlayers(Component message, double radius) {
        Level level = this.level();
        AABB area = this.getBoundingBox().inflate(radius);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, area);

        for (Player player : nearbyPlayers) {
            player.sendSystemMessage(message);
        }
    }

    @Override
    public void heal(float pHealAmount) {
        pHealAmount = ForgeEventFactory.onLivingHeal(this, pHealAmount);
        if (pHealAmount <= 0) return;
        float f = this.getHealth();
        if (f > 0.0F) {
            this.setTrueHealth(f + pHealAmount);
        }
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getTrueHealth() > 0.0F;
    }

    @Override
    public boolean isDeadOrDying() {
        return getTrueHealth() <= 0.0F;
    }

    @Override
    public void die(@NotNull DamageSource pDamageSource) {
        playSound(EpicFightSounds.EVISCERATE.get(), 1.0F, 1.0F);
        super.die(pDamageSource);
        if(!level().isClientSide()){
            broadcastToNearbyPlayers(
                    Component.literal("[")
                            .append(this.getDisplayName())
                            .append(Component.literal("] "))
                            .append(Component.translatable("message.combat_evolution.scarlet_hunter_death")),
                    64
            );
        }
    }

    @Override
    public float getHealth() {
        return getTrueHealth();
    }

    @Override
    public void setHealth(float pHealth) {
        super.setHealth(pHealth);
    }

    public void setTrueHealth(float pHealth) {
        this.entityData.set(TRUE_HEALTH, "[" +
                Float.toString(Mth.clamp(pHealth, 0.0F, this.getMaxHealth()) * 100 + this.getMaxHealth() * 100)
                        .replace('.', 'x')
                + "]"
        );
        this.setHealth(pHealth);
    }

    public float getTrueHealth() {
        return (Float.parseFloat(this.entityData.get(TRUE_HEALTH)
                .replace('x','.').replace('[',' ').replace(']',' '))
                - this.getMaxHealth() * 100) / 100;
    }

    public void setBossPhase(int phase) {
        entityData.set(BOSS_PHASE, phase);
    }

    public int getBossPhase() {
        return entityData.get(BOSS_PHASE);
    }

    public void setBloodShield(boolean bloodShield) {
        entityData.set(BLOOD_SHIELD, bloodShield);
        CompoundTag tag = ceBossEvent.getCustomData();
        tag.putBoolean("bloodShield", bloodShield);
        ceBossEvent.updateCustomData(tag);
    }

    public boolean hasBloodShield() {
        return entityData.get(BLOOD_SHIELD);
    }

    public int getBloodShieldCooldown() {
        return bloodShieldCooldown;
    }

    public void setBloodShieldCooldown(int bloodShieldCooldown) {
        this.bloodShieldCooldown = bloodShieldCooldown;
    }

    public int getPhaseChangeCounter() {
        return phaseChangeCounter;
    }

    public void setPhaseChangeCounter(int phaseChangeCounter) {
        this.phaseChangeCounter = phaseChangeCounter;
    }

    public boolean isCanBypassStunImmunity() {
        return canBypassStunImmunity;
    }

    public void setCanBypassStunImmunity(boolean canBypassStunImmunity) {
        this.canBypassStunImmunity = canBypassStunImmunity;
    }

    public float getDamageProtectPercent(){
        float maxHealth = this.getMaxHealth();
        return Mth.clamp(totalDamageTaken / (maxHealth * MAX_DAMAGE_TAKEN), 0, 1);
    }
}
