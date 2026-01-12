package net.shelmarow.nightfall_invade.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;

public class NFIItems {
    public static final DeferredRegister<Item> ITEMS;
    public static final RegistryObject<SpawnEggItem> ARTERIUS_SPAWN_EGG;

    static {
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NightFallInvade.MOD_ID);

        ARTERIUS_SPAWN_EGG = ITEMS.register("arterius_spawn_egg",() ->
                new ForgeSpawnEggItem(NFIEntities.ARTERIUS, 0xFF3333, 0xFF0000,new Item.Properties()));
    }
}
