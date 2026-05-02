package net.shelmarow.nightfall_invade.config.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec FORGE_CONFIG_SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_CUSTOM_BOSS_BAR;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("BossBarConfig");

        ENABLE_CUSTOM_BOSS_BAR = builder.comment("Enable Custom BossBar Bar.")
                .define("EnableCustomBossBar", true);

        builder.pop();
        FORGE_CONFIG_SPEC = builder.build();
    }
}
