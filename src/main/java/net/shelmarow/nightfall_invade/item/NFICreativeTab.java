package net.shelmarow.nightfall_invade.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.assets.NFISkills;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;

public class NFICreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NightFallInvade.MOD_ID);

    public static final RegistryObject<CreativeModeTab> NIGHTFALL_INVADE_TAB = CREATIVE_TABS.register("nightfall_invade_items", () ->
            CreativeModeTab.builder()
            .title(Component.translatable("creativetab.nightfall_invade.items"))
            .icon(() -> new ItemStack(NFIItems.ARTERIUS_SPAWN_EGG.get()))
            .displayItems((params, output) -> {

                output.accept(NFIItems.ARTERIUS_SPAWN_EGG.get());
                output.accept(NFIItems.SCARLET_HUNTER_SPAWN_EGG.get());
                output.accept(NFIItems.DISASTER_FRAGMENT_RED.get());

            }).build());

}
