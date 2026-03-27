package net.shelmarow.nightfall_invade.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;

public class NFIItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NightFallInvade.MOD_ID);

    public static final RegistryObject<SpawnEggItem> ARTERIUS_SPAWN_EGG = ITEMS.register("arterius_spawn_egg", () ->
            new ForgeSpawnEggItem(NFIEntities.ARTERIUS, 0xFF6A00, 0xFFD166, new Item.Properties()));

    public static final RegistryObject<SpawnEggItem> SCARLET_HUNTER_SPAWN_EGG = ITEMS.register("scarlet_hunter_spawn_egg", () ->
            new ForgeSpawnEggItem(NFIEntities.SCARLET_HUNTER, 0x6E0B14, 0xC1121F, new Item.Properties()));

    public static final RegistryObject<Item> DISASTER_FRAGMENT_RED = ITEMS.register("disaster_fragment_red", () ->
            new Item(new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
}
