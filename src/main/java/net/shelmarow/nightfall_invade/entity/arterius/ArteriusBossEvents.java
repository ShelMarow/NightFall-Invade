package net.shelmarow.nightfall_invade.entity.arterius;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.config.boss.BossConfig;
import net.shelmarow.nightfall_invade.effect.NFIMobEffects;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArteriusBossEvents {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if(event.getEntity() instanceof Arterius){
            for(ItemEntity drop : event.getDrops()){
                drop.setGlowingTag(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer && !event.isCanceled()) {
            if(event.getSource().getEntity() instanceof Arterius arterius) {
                arterius.resetBossStatus(true);
            }
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        Entity target = event.getEntity();
        if (target instanceof Arterius) {
            event.setStrength(0.0F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        Entity target = event.getEntity();
        if(target instanceof Arterius arterius && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && arterius.isAlive()) {

            //非战斗模式下免疫伤害
            if(!arterius.isInBattle()){
                event.setCanceled(true);
            }

            //免疫远距离伤害
            if (source.is(DamageTypeTags.IS_PROJECTILE)){
                event.setCanceled(true);
            }

            //无敌时间不受伤害
            if(arterius.getInvulnerableTimer() > 0){
                int bossPhase = arterius.getBossPhase();
                if(bossPhase == 1 && arterius.getHealth() < arterius.getMaxHealth() * 0.75F){
                    arterius.setHealth(arterius.getMaxHealth()*0.75F);
                }
                else if(bossPhase == 2 && arterius.getHealth() < arterius.getMaxHealth() * 0.50F){
                    arterius.setHealth(arterius.getMaxHealth()*0.50F);
                }
                else if(bossPhase == 3 && arterius.getHealth() < arterius.getMaxHealth() * 0.25F){
                    arterius.setHealth(arterius.getMaxHealth()*0.25F);
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurtEvent(LivingHurtEvent event){
        DamageSource source = event.getSource();
        float damage = event.getAmount();
        Entity target = event.getEntity();
        Entity trueAttacker = source.getEntity();

        //BOSS对目标造成伤害时，施加异常效果，并对带有异常效果的目标额外造成最大生命百分比的伤害
        if(trueAttacker instanceof Arterius && target instanceof LivingEntity livingTarget){
            int level = BossConfig.ARTERIUS_DEBUFF_LEVEL.get();
            int duration = BossConfig.ARTERIUS_DEBUFF_DURATION.get();
            float exBuffDamage = BossConfig.ARTERIUS_DEBUFF_EX_DAMAGE.get().floatValue();
            float exBuffHPDamage = BossConfig.ARTERIUS_DEBUFF_EX_HP_DAMAGE.get().floatValue();
            float exHPDamage = BossConfig.ARTERIUS_EX_HP_DAMAGE.get().floatValue();

            if(livingTarget.hasEffect(NFIMobEffects.SOUL_OF_FLAME.get())) {
                //额外debuff倍率伤害
                damage *= (1F + exBuffDamage);
                damage += livingTarget.getMaxHealth() * exBuffHPDamage;
            }
            livingTarget.forceAddEffect(new MobEffectInstance(NFIMobEffects.SOUL_OF_FLAME.get(),duration,level),livingTarget);
            //额外百分比伤害
            damage += livingTarget.getMaxHealth() * exHPDamage;
        }


        event.setAmount(damage);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHealEvent(LivingHealEvent event){
        LivingEntity entity = event.getEntity();
        //禁疗
        if(entity.hasEffect(NFIMobEffects.SOUL_OF_FLAME.get())) {
            event.setCanceled(true);
        }
    }

}
