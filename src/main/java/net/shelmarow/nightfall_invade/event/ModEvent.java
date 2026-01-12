package net.shelmarow.nightfall_invade.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.combat_evolution.api.event.RegisterCustomExecutionEvent;
import net.shelmarow.combat_evolution.execution.ExecutionTypeManager;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.entity.spear_knight.Arterius;
import net.shelmarow.nightfall_invade.entity.spear_knight.ArteriusPatch;
import yesman.epicfight.api.forgeevent.EntityPatchRegistryEvent;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event){
        event.put(NFIEntities.ARTERIUS.get(), Arterius.createAttributes().build());
    }

    @SubscribeEvent
    public static void setPatch(EntityPatchRegistryEvent event) {
        event.getTypeEntry().put(NFIEntities.ARTERIUS.get(), (entity) -> ArteriusPatch::new);
    }
}
