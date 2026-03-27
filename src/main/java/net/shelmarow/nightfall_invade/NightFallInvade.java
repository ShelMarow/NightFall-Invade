package net.shelmarow.nightfall_invade;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.shelmarow.nightfall_invade.assets.NFIArmatures;
import net.shelmarow.nightfall_invade.assets.NFISkillCategories;
import net.shelmarow.nightfall_invade.assets.NFISkillSlots;
import net.shelmarow.nightfall_invade.config.boss.BossConfig;
import net.shelmarow.nightfall_invade.effect.NFIMobEffects;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.item.NFICreativeTab;
import net.shelmarow.nightfall_invade.item.NFIItems;
import net.shelmarow.nightfall_invade.network.server.S2CPushEntityAwayPacket;
import net.shelmarow.nightfall_invade.particle.NFIParticles;
import net.shelmarow.nightfall_invade.structure.NFIStructureType;
import org.slf4j.Logger;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;

@Mod(NightFallInvade.MOD_ID)
public class NightFallInvade {
    public static final String MOD_ID = "nightfall_invade";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public NightFallInvade(FMLJavaModLoadingContext context){
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        NFIEntities.ENTITY_TYPES.register(modEventBus);
        NFIMobEffects.EFFECTS.register(modEventBus);
        
        NFIItems.ITEMS.register(modEventBus);
        NFICreativeTab.CREATIVE_TABS.register(modEventBus);
        NFIParticles.PARTICLE_TYPES.register(modEventBus);

        NFIStructureType.STRUCTURE_TYPES.register(modEventBus);
        NFIStructureType.STRUCTURE_PIECE.register(modEventBus);

        NFISkillSlots.ENUM_MANAGER.registerEnumCls(MOD_ID, NFISkillSlots.class);
        NFISkillCategories.ENUM_MANAGER.registerEnumCls(MOD_ID, NFISkillCategories.class);

        context.registerConfig(ModConfig.Type.COMMON, BossConfig.FORGE_CONFIG_SPEC, "nightfall_invade-common.toml");

        registerPackets();
    }

    private void commonSetup(final FMLCommonSetupEvent event){
        event.enqueueWork(NightFallInvade::registerArmatures);
    }

    public static void registerArmatures() {
        Armatures.registerEntityTypeArmature(NFIEntities.ARTERIUS.get(), Armatures.BIPED);
        Armatures.registerEntityTypeArmature(NFIEntities.SCARLET_HUNTER.get(), Armatures.BIPED);
        Armatures.registerEntityTypeArmature(NFIEntities.BLOOD_SLASH.get(), NFIArmatures.BLOOD_SLASH_ARMATURE);
    }

    private void registerPackets() {
        int packetId = 0;
        CHANNEL.registerMessage(++packetId, S2CPushEntityAwayPacket.class, S2CPushEntityAwayPacket::encode, S2CPushEntityAwayPacket::decode, S2CPushEntityAwayPacket::handle);
    }
}
