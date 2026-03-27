package net.shelmarow.nightfall_invade.event.forge;

import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;

import javax.swing.text.html.parser.Entity;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEvent {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
    }
}
