package net.shelmarow.nightfall_invade.entity.scarlet_hunter;

import com.hm.efn.registries.EFNMobEffectRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScarletHunterEvent {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if(event.getEntity() instanceof ScarletHunter){
            for(ItemEntity drop : event.getDrops()){
                drop.setGlowingTag(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurtEvent(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        float damage = event.getAmount();
        Entity target = event.getEntity();
        Entity trueAttacker = source.getEntity();

        if(trueAttacker instanceof ScarletHunter scarletHunter && target instanceof LivingEntity livingTarget){
            damage += livingTarget.getMaxHealth() * 0.015F;
            if(livingTarget.hasEffect(EFNMobEffectRegistry.CURSE_OF_BLOOD.get())){
                scarletHunter.heal(scarletHunter.getMaxHealth() * 0.01F);
                damage += livingTarget.getMaxHealth() * 0.015F;
            }
        }

        event.setAmount(damage);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamageEvent(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        float damage = event.getAmount();
        Entity trueAttacker = source.getEntity();
        Entity attacker = source.getDirectEntity();

        if(trueAttacker instanceof ScarletHunter scarletHunter && attacker instanceof ScarletHunter){
            //残血增伤
            float healthPercent = scarletHunter.getHealth() / scarletHunter.getMaxHealth();
            damage *= 1 + (1 - healthPercent) * 0.2F;
            //减伤过高增伤
            damage *= 1 + scarletHunter.getDamageProtectPercent() * 0.2F;
        }

        event.setAmount(damage);
    }
}
