package net.shelmarow.nightfall_invade.client.key;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NFIKeyMappings {

    public static final KeyMapping NFI_SKILL = creatKey("nfi_skill", GLFW.GLFW_KEY_X);


    private static KeyMapping creatKey(String name, int key){
        return new KeyMapping("key."+ NightFallInvade.MOD_ID + "." + name, key, "key."+ NightFallInvade.MOD_ID +".category");
    }


    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(NFI_SKILL);
    }
}
