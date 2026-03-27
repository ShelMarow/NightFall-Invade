package net.shelmarow.nightfall_invade.entity.spear_knight;

import com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Abyss_Fireball_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Fireball_Entity;
import com.hm.efn.registries.EFNItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.shelmarow.combat_evolution.ai.StaminaStatus;
import net.shelmarow.combat_evolution.ai.goal.CEAnimationAttackGoal;
import net.shelmarow.combat_evolution.ai.util.BehaviorUtils;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.bgm.network.CEMusicNetworkHandler;
import net.shelmarow.combat_evolution.bgm.network.CEMusicPacket;
import net.shelmarow.combat_evolution.bossbar.CEBossEvent;
import net.shelmarow.combat_evolution.damage_source.CEDamageTypeTags;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.api.event.NFIFinalDamageEvent;
import net.shelmarow.nightfall_invade.config.boss.BossConfig;
import net.shelmarow.nightfall_invade.entity.spear_knight.ai.ArteriusAI;
import net.shelmarow.nightfall_invade.entity.spear_knight.goal.AttackMonsterGoal;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

import java.util.UUID;

public class Arterius extends PathfinderMob {

    private final CEBossEvent bossEvent = new CEBossEvent(Component.translatable("boss_bar.nightfall_invade.arterius").withStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD));
    private final UUID bgmRequestUUID = UUID.randomUUID();
    private final CEMusicPacket ceMusicPacket;
    private boolean lastTickHasTarget = false;
    private int bgmStopDelay = 0;

    private boolean inBattle = true;
    private Vec3 homePos = null;
    private boolean difficultyHard = false;

    //脱战自回血
    private int noTargetTime = 0;

    /*通用数据记录*/
    private int bossPhase = 0;
    private int skillReleased = 0;
    private int invulnerableTimer = 0;
    /*困难数据记录*/

    private Vec3 lastDeltaMovement = Vec3.ZERO;
    private boolean canSetDeltaMovement = true;
    private int canSetDeltaMovementTimer = 0;

    private float damageReduction = 0F;
    private float totalDamageCount = 0;

    private int lastActuallyHurtTick = -1;

    private int bossBarVisibleTick = 0;

    //特殊计数器，用于检测距离过远或者Y轴差距过大的敌人
    private int farAwayFromTargetTime = 0;

    public Arterius(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setPersistenceRequired();
        this.setEquipment();
        bossEvent.setDisplayType("[NightFallInvade:Arterius]");
        bossEvent.setVisible(false);
        ceMusicPacket = new CEMusicPacket(
                false, bgmRequestUUID,
                ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID,"arterius.bgm.spear_of_flame"),
                SoundSource.RECORDS, 0.5F, 5200, true, true, 40, 40
        );
    }

    @Override
    public @NotNull Component getName() {
        return isDifficultyHard() ? Component.translatable("entity.nightfall_invade.arterius_hard").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD): super.getName();
    }


    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        setAttributesFromConfig();
        resetBossStatus(false);
        resetGoalsByInBattle();
        if(homePos == null){
            setHomePos(position());
        }
    }

    private void setAttributesFromConfig() {
        float healthConfig = BossConfig.ARTERIUS_MAX_HEALTH_MULTIPLIER.get().floatValue();
        float damageConfig = BossConfig.ARTERIUS_ATTACK_DAMAGE_MULTIPLIER.get().floatValue();

        UUID att = UUID.fromString("31898599-9b54-4600-a8c5-81845eb664f7");
        AttributeInstance healthInstance = getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damageInstance = getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeModifier healthModifier = new AttributeModifier(att,"health_modifier",healthConfig - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        AttributeModifier damageModifier = new AttributeModifier(att,"damage_modifier",damageConfig - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (healthInstance != null) {
            float ratio = getHealth() / getMaxHealth();
            if(healthInstance.hasModifier(healthModifier)) {
                healthInstance.removeModifier(healthModifier);
            }
            healthInstance.addPermanentModifier(healthModifier);
            this.setHealth(this.getMaxHealth() * ratio);
            if(getHealth() > getMaxHealth()) {
                setHealth(getMaxHealth());
            }
        }

        if (damageInstance != null) {
            if(damageInstance.hasModifier(damageModifier)) {
                damageInstance.removeModifier(damageModifier);
            }
            damageInstance.addPermanentModifier(damageModifier);
        }
    }

    private void setEquipment() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(EFNItem.MEEN_SPEAR.get()));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(EFNItem.DUSKFIRE_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(EFNItem.DUSKFIRE_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(EFNItem.DUSKFIRE_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(EFNItem.DUSKFIRE_BOOTS.get()));
        this.setDropChance(EquipmentSlot.MAINHAND,0F);
        this.setDropChance(EquipmentSlot.OFFHAND,0F);
        this.setDropChance(EquipmentSlot.HEAD,0F);
        this.setDropChance(EquipmentSlot.CHEST,0F);
        this.setDropChance(EquipmentSlot.LEGS,0F);
        this.setDropChance(EquipmentSlot.FEET,0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1D)

                .add(EpicFightAttributes.IMPACT.get(),4.0D)
                .add(EpicFightAttributes.ARMOR_NEGATION.get(),25D)
                .add(EpicFightAttributes.STUN_ARMOR.get(),16.0D)
                .add(EpicFightAttributes.MAX_STRIKES.get(),100.0D)
                .add(EpicFightAttributes.MAX_STAMINA.get(),50.0D)
                .add(EpicFightAttributes.STAMINA_REGEN.get(),1.5D);
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
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand){
        return super.mobInteract(pPlayer,pHand);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class,12));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(1, new AttackMonsterGoal(this, true,true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public void resetGoalByDifficulty(){
        ArteriusPatch arteriusPatch = EpicFightCapabilities.getEntityPatch(this,ArteriusPatch.class);
        if(arteriusPatch!=null){
            this.goalSelector.removeAllGoals(goal -> goal instanceof CEAnimationAttackGoal<?>);
            this.goalSelector.addGoal(0, new CEAnimationAttackGoal<>(arteriusPatch, (isDifficultyHard() ? ArteriusAI.HARD.get() : ArteriusAI.creatNormal()).build()));
        }
    }

    public void resetGoalsByInBattle() {
        for (WrappedGoal goal : this.targetSelector.getRunningGoals().toList()){
            goal.stop();
        }
        this.targetSelector.removeAllGoals((goal -> true));
        if(isInBattle()){
            this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
            this.targetSelector.addGoal(1, new AttackMonsterGoal(this, true,true));
            this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
            for (WrappedGoal goal : this.targetSelector.getAvailableGoals()){
                goal.start();
            }
        }
        else{
            this.setTarget(null);
        }
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(homePos != null){
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("x", homePos.x);
            posTag.putDouble("y", homePos.y);
            posTag.putDouble("z", homePos.z);
            tag.put("homePos", posTag);
        }
        tag.putBoolean("DifficultyHard", isDifficultyHard());
        tag.putBoolean("InBattle", isInBattle());
        //resetGoalsByInBattle();
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if(tag.contains("homePos",10)){
            CompoundTag posTag = tag.getCompound("homePos");
            homePos = new Vec3(
                    posTag.getDouble("x"),
                    posTag.getDouble("y"),
                    posTag.getDouble("z")
            );
        }
        if(tag.contains("DifficultyHard",1)){
            setDifficultyHard(tag.getBoolean("DifficultyHard"));
        }
        if(tag.contains("InBattle",1)){
            setInBattle(tag.getBoolean("InBattle"));
        }
        else {
            resetGoalsByInBattle();
        }
    }

    public Vec3 getHomePos() {
        return homePos;
    }

    public void setHomePos(Vec3 homePos) {
        this.homePos = homePos;
    }

    public void backToHomePos(){
        if(homePos != null){
            this.teleportTo(homePos.x, homePos.y, homePos.z);
        }
    }

    public boolean isDifficultyHard() {
        return difficultyHard;
    }

    public void setDifficultyHard(boolean difficultyHard) {
        if(this.difficultyHard != difficultyHard){
            this.difficultyHard = difficultyHard;
            bossEvent.setName(
                    isDifficultyHard() ?
                    Component.translatable("boss_bar.nightfall_invade.arterius_hard").withStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD) :
                    Component.translatable("boss_bar.nightfall_invade.arterius").withStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD)
            );
            resetGoalByDifficulty();
        }
    }

    public boolean isInBattle() {
        return this.inBattle;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
        resetGoalsByInBattle();
    }

    public void resetBossStatus(boolean resetHealth){
        //回满生命值
        if(resetHealth) setHealth(getMaxHealth());
        //回复所有耐力
        addEntityStamina((float) getAttributeValue(EpicFightAttributes.MAX_STAMINA.get()));

        //重新上装备
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(EFNItem.MEEN_SPEAR.get()));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(EFNItem.DUSKFIRE_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(EFNItem.DUSKFIRE_CHESTPLATE.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(EFNItem.DUSKFIRE_LEGGINGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(EFNItem.DUSKFIRE_BOOTS.get()));

        //重置phase
        setPhaseByHealth();
    }

    public int getInvulnerableTimer() {
        return invulnerableTimer;
    }

    public void setInvulnerableTimer(int invulnerableTimer) {
        this.invulnerableTimer = invulnerableTimer;
    }

    public int getSkillReleased() {
        return skillReleased;
    }

    public void setSkillReleased(int skillReleased) {
        this.skillReleased = skillReleased;
    }

    public int getBossPhase() {
        return bossPhase;
    }

    public void setBossPhase(int bossPhase) {
        this.bossPhase = bossPhase;
    }

    public void setPhaseByHealth(){
        invulnerableTimer = 0;
        float health = getHealth();
        float maxHealth = getMaxHealth();
        if(health > maxHealth * 0.75F){
            setBossPhase(0);
            setSkillReleased(0);

        } else if (health > maxHealth * 0.5F) {
            setBossPhase(1);
            setSkillReleased(1);
        }
        else if (health > maxHealth * 0.25F) {
            setBossPhase(2);
            setSkillReleased(2);
        }
        else{
            setBossPhase(3);
            setSkillReleased(3);
        }
    }

    public void adjustDamageReduction() {

        float maxRatio = BossConfig.ARTERIUS_DAMAGE_MAX_THRESHOLD.get().floatValue();
        float damageThreshold = BossConfig.ARTERIUS_DAMAGE_THRESHOLD.get().floatValue();
        int decaySecond = BossConfig.ARTERIUS_DECAY_SECOND.get();

        float speed = (maxRatio - damageThreshold) * getMaxHealth() / decaySecond;
        totalDamageCount = Math.max(totalDamageCount - speed,0);

        //伤害是否达到减伤阈值
        if(totalDamageCount <= this.getMaxHealth() * damageThreshold){
            damageReduction = 0;
        }
    }


    @Override
    public boolean killedEntity(@NotNull ServerLevel pLevel, @NotNull LivingEntity pEntity) {
        //击杀实体时，恢复自身生命值
        heal(getMaxHealth()/10);
        addEntityStamina((float)getAttributeValue(EpicFightAttributes.MAX_STAMINA.get())/10F);
        return true;
    }

    public void addEntityStamina(float value){
        ArteriusPatch arteriusPatch = EpicFightCapabilities.getEntityPatch(this, ArteriusPatch.class);
        if(arteriusPatch != null){
            CEPatchUtils.setStamina(arteriusPatch, CEPatchUtils.getStamina(arteriusPatch) + value);
        }
    }

    @Override
    public void tick(){
        super.tick();

        if(!level().isClientSide){
            if(getTarget() == null){
                if(noTargetTime < 200) {
                    noTargetTime++;
                }
                else if(getHealth() < getMaxHealth() && tickCount % 10 == 0){
                    heal(getMaxHealth() * 0.1F);
                    setPhaseByHealth();
                }
            } else if(noTargetTime != 0){
                noTargetTime = 0;
            }
        }


        this.bossEvent.setProgress(this.getHealth()/this.getMaxHealth());

        lastDeltaMovement = getDeltaMovement();

        handleBossBarAndBGM();

        if(getTarget() != null){
            LivingEntity target = getTarget();
            if(target.distanceToSqr(this) > 10 * 10 || Math.abs(target.getY() - this.getY()) >= 3){
                farAwayFromTargetTime++;
            }
        }
        else if(farAwayFromTargetTime > 0){
            farAwayFromTargetTime--;
        }

        if(invulnerableTimer > 0){
            invulnerableTimer--;
        }

        if(canSetDeltaMovementTimer > 0 && --canSetDeltaMovementTimer <= 0){
            canSetDeltaMovement = true;
        }

        if (!level().isClientSide()) {
            adjustDamageReduction();
        }
    }

    private void handleBossBarAndBGM() {
        if(level().isClientSide()) return;

        //记录当前是否有目标
        if (!lastTickHasTarget && getTarget() != null) {
            bgmStopDelay = 0;
            for (ServerPlayer player : bossEvent.getPlayers()) {
                CEMusicNetworkHandler.sendRequestPlayPacket(player, ceMusicPacket);
            }
        }
        else if (lastTickHasTarget && getTarget() == null) {
            bgmStopDelay = 40;
        }
        lastTickHasTarget = getTarget() != null;

        if(bgmStopDelay-- > 0){
            if(bgmStopDelay <= 0) {
                bgmStopDelay = 0;
                for (ServerPlayer player : bossEvent.getPlayers()) {
                    CEMusicNetworkHandler.sendRemoveMusicPacket(player, bgmRequestUUID, false);
                }
            }
        }

        //失去目标10秒后，如果没有玩家能看到该BOSS，则隐藏血条
        if(bossEvent.isVisible() && getTarget() == null){
            if(++bossBarVisibleTick >= 200){
                bossBarVisibleTick = 0;
                boolean disable = true;
                for (ServerPlayer player : bossEvent.getPlayers()) {
                    if (hasLineOfSight(player)) {
                        disable = false;
                        break;
                    }
                }

                if(disable){
                    bossEvent.setVisible(false);
                }
            }
        }
        else if (bossBarVisibleTick > 0) {
            bossBarVisibleTick = 0;
        }

        if(isInBattle() && !bossEvent.isVisible()) {
            for (ServerPlayer player : bossEvent.getPlayers()) {
                if (distanceToSqr(player) <= 64 * 64 && hasLineOfSight(player)) {
                    bossEvent.setVisible(true);
                    break;
                }
            }

        }
        else if(!isInBattle() && bossEvent.isVisible()){
            bossEvent.setVisible(false);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        //受到有来源且非创造模式玩家的真伤时免疫该伤害
        if(damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
            if(damageSource.getEntity() != null || damageSource.getDirectEntity() != null){
                if(damageSource.getEntity() instanceof Player player) {
                    return !player.isCreative();
                }
                else return true;
            }
        }
        return damageSource.getDirectEntity() instanceof Ignis_Fireball_Entity ||
                damageSource.getDirectEntity() instanceof Ignis_Abyss_Fireball_Entity ||
                damageSource.getDirectEntity() instanceof Flame_Strike_Entity ||
                damageSource.is(DamageTypeTags.IS_EXPLOSION) ||
                super.isInvulnerableTo(damageSource);
    }


    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }


    @Override
    public void push(double pX, double pY, double pZ) {
        Vec3 motion = new Vec3(pX,pY,pZ);
        if(motion.length() > 0.25F){
            motion = motion.normalize().scale(0.25F);
        }
        this.setDeltaMovement(this.getDeltaMovement().add(motion.x, motion.y, motion.z));
        this.hasImpulse = true;
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 pDeltaMovement) {
        if(!canSetDeltaMovement) return;
        double lastVec = lastDeltaMovement.length();
        double currentVec = pDeltaMovement.length();
        if(currentVec - lastVec < 2){
            super.setDeltaMovement(pDeltaMovement);
        }
        else{
            super.setDeltaMovement(lastDeltaMovement.add(pDeltaMovement.normalize().scale(2)));
        }
    }

    public void setCanSetDeltaMovementTimer(int tick){
        this.canSetDeltaMovementTimer = tick;
    }

    public void setCanSetDeltaMovement(boolean canSetDeltaMovement) {
        this.canSetDeltaMovement = canSetDeltaMovement;
    }


    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean causeFallDamage(float p_148711_, float p_148712_, @NotNull DamageSource p_148713_) {
        return false;
    }


    @Override
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        return (pEffectInstance.getEffect().getCategory() == MobEffectCategory.BENEFICIAL || pEffectInstance.getEffect() == MobEffects.GLOWING) &&
                super.canBeAffected(pEffectInstance);
    }

    @Override
    public void actuallyHurt(@NotNull DamageSource pDamageSource, float originalDamage) {
        //卡墙脱离
        if(pDamageSource.is(DamageTypes.IN_WALL)){
            backToHomePos();
            return;
        }

        if (!this.isInvulnerableTo(pDamageSource)) {

            lastActuallyHurtTick = this.tickCount;

            originalDamage = ForgeHooks.onLivingHurt(this, pDamageSource, originalDamage);
            if (originalDamage <= 0.0F) return;

            //处理护甲和伤害吸收
            originalDamage = this.getDamageAfterArmorAbsorb(pDamageSource, originalDamage);
            originalDamage = this.getDamageAfterMagicAbsorb(pDamageSource, originalDamage);

            float damage = Math.max(originalDamage - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (originalDamage - damage));
            float f = originalDamage - damage;
            if (f > 0.0F && f < 3.4028235E37F) {
                Entity entity = pDamageSource.getEntity();
                if (entity instanceof ServerPlayer serverplayer) {
                    serverplayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
                }
            }

            float damageCap = BossConfig.ARTERIUS_DAMAGE_CAP.get().floatValue();
            float maxRatio = BossConfig.ARTERIUS_DAMAGE_MAX_THRESHOLD.get().floatValue();
            float damageThreshold = BossConfig.ARTERIUS_DAMAGE_THRESHOLD.get().floatValue();
            float maxReduction = BossConfig.ARTERIUS_MAX_REDUCTION.get().floatValue();

            float bossDamageCap = this.getMaxHealth() * damageCap;

            damage = ForgeHooks.onLivingDamage(this, pDamageSource, damage);

            if (damage != 0.0F) {
                //计算减伤
                if(!(pDamageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || pDamageSource.is(CEDamageTypeTags.EXECUTION))) {

                    //如果受到的伤害超过了阈值，限制回范围内
                    if (damage > bossDamageCap) {
                        damage = bossDamageCap;
                    }

                    //计算减伤后的伤害
                    damageReduction = Mth.clamp((totalDamageCount - (this.getMaxHealth() * damageThreshold)) / (this.getMaxHealth() * (maxRatio - damageThreshold)),0, maxReduction);
                    damage *= (1 - damageReduction);

                    //困难模式下，受到的最终伤害降低50%
                    if(isDifficultyHard()){
                        damage *= 0.5F;
                    }

                    //合并总伤害
                    totalDamageCount = Math.min(totalDamageCount + damage, getMaxHealth() * maxRatio);

                }
                //如果首次低于75% 50% 25%血，转阶段前无敌（当然为了防止bug，还是设置30s）
                if(!pDamageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                    if (bossPhase == 0 && this.getHealth() - damage <= this.getMaxHealth() * 0.75F){
                        damage = 0;
                        bossPhase = 1;
                        invulnerableTimer = 600;
                        this.setHealth(this.getMaxHealth() * 0.75F);
                        //停止当前连段
                        BehaviorUtils.stopCurrentBehavior(this);

                    }
                    else if (bossPhase == 1 && this.getHealth() - damage <= this.getMaxHealth() * 0.5F) {
                        damage = 0;
                        bossPhase = 2;
                        invulnerableTimer = 600;
                        this.setHealth(this.getMaxHealth() * 0.5F);
                        //停止当前连段
                        BehaviorUtils.stopCurrentBehavior(this);
                    }
                    else if (bossPhase == 2 && this.getHealth() - damage <= this.getMaxHealth() * 0.25F) {
                        damage = 0;
                        bossPhase = 3;
                        invulnerableTimer = 600;
                        this.setHealth(this.getMaxHealth() * 0.25F);
                        //停止当前连段
                        BehaviorUtils.stopCurrentBehavior(this);
                    }
                }


                NFIFinalDamageEvent event = new NFIFinalDamageEvent(this, pDamageSource, damage);
                damage = MinecraftForge.EVENT_BUS.post(event) ? 0 : event.getAmount();

                this.getCombatTracker().recordDamage(pDamageSource, damage);
                this.setHealth(this.getHealth() - damage);

                this.setAbsorptionAmount(this.getAbsorptionAmount() - damage);
                this.gameEvent(GameEvent.ENTITY_DAMAGE);
            }
        }
    }

    @Override
    public void setHealth(float health) {
        //如果造成的伤害没有经过hurt，进行额外计算
        if(health < getHealth() && lastActuallyHurtTick != -1 && lastActuallyHurtTick != this.tickCount) {
            return;
        }
        super.setHealth(health);
    }

    @Override
    public SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    public SoundEvent getDeathSound(){
        return SoundEvents.BLAZE_DEATH;
    }


    @Override
    public float getScale(){
        return 1.4F;
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
        if(!level().isClientSide()) {
            for (ServerPlayer player : bossEvent.getPlayers()) {
                CEMusicNetworkHandler.sendRemoveMusicPacket(player, bgmRequestUUID, false);
            }
        }
    }

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
        if(getTarget() != null) {
            CEMusicNetworkHandler.sendRequestPlayPacket(pPlayer, ceMusicPacket);
        }
    }

    @Override
    public void stopSeenByPlayer(@NotNull ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
        CEMusicNetworkHandler.sendRemoveMusicPacket(pPlayer, bgmRequestUUID, false);
    }

    public void setStamina(float value, StaminaStatus staminaStatus) {
        this.bossEvent.setStaminaStatus(staminaStatus);
        this.bossEvent.setStamina(value);
    }

    public int getFarAwayFromTargetTime() {
        return farAwayFromTargetTime;
    }

    public void setFarAwayFromTargetTime(int farAwayFromTargetTime) {
        this.farAwayFromTargetTime = farAwayFromTargetTime;
    }
}
