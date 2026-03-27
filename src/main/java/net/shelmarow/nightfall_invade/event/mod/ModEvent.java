package net.shelmarow.nightfall_invade.event.mod;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunterPatch;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.BloodSlashEntity;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.BloodSlashPatch;
import net.shelmarow.nightfall_invade.entity.spear_knight.Arterius;
import net.shelmarow.nightfall_invade.entity.spear_knight.ArteriusPatch;
import yesman.epicfight.api.forgeevent.EntityPatchRegistryEvent;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event){
        event.put(NFIEntities.ARTERIUS.get(), Arterius.createAttributes().build());
        event.put(NFIEntities.SCARLET_HUNTER.get(), ScarletHunter.createAttributes().build());
        event.put(NFIEntities.BLOOD_SLASH.get(), BloodSlashEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void setPatch(EntityPatchRegistryEvent event) {
        event.getTypeEntry().put(NFIEntities.ARTERIUS.get(), (entity) -> ArteriusPatch::new);
        event.getTypeEntry().put(NFIEntities.SCARLET_HUNTER.get(), (entity) -> ScarletHunterPatch::new);
        event.getTypeEntry().put(NFIEntities.BLOOD_SLASH.get(),(entity -> BloodSlashPatch::new));
    }
}
