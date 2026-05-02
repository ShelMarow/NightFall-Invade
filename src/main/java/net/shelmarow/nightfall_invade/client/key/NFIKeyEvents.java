package net.shelmarow.nightfall_invade.client.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.assets.NFISkillSlots;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;


@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID,bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NFIKeyEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer != null) {
            if(NFIKeyMappings.NFI_SKILL.isDown()){
                LocalPlayerPatch playerPatch = EpicFightCapabilities.getLocalPlayerPatch(localPlayer);
                if(playerPatch != null && playerPatch.getSkill(NFISkillSlots.NFI_COMBAT_ART1).hasSkill()){
                    CPSkillRequest packet = new CPSkillRequest(SkillSlot.ENUM_MANAGER.get(NFISkillSlots.NFI_COMBAT_ART1.universalOrdinal()));
                    EpicFightNetworkManager.sendToServer(packet);
                }
            }
        }
    }
}
