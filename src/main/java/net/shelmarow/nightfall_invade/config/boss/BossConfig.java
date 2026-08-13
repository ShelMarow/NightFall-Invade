package net.shelmarow.nightfall_invade.config.boss;

import net.minecraftforge.common.ForgeConfigSpec;

public class BossConfig {
    public static final ForgeConfigSpec FORGE_CONFIG_SPEC;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_ATTACK_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_MAX_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_EX_HP_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ARTERIUS_DEBUFF_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Integer> ARTERIUS_DEBUFF_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_DEBUFF_EX_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_DEBUFF_EX_HP_DAMAGE;

    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_EX_FIRE_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_DAMAGE_CAP;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_DAMAGE_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_DAMAGE_MAX_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Double> ARTERIUS_MAX_REDUCTION;
    public static final ForgeConfigSpec.ConfigValue<Integer> ARTERIUS_DECAY_SECOND;

    public static final ForgeConfigSpec.ConfigValue<Double> SCARLET_HUNTER_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SCARLET_HUNTER_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SCARLET_HUNTER_STAMINA_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> SCARLET_HUNTER_DAMAGE_CAP;
    public static final ForgeConfigSpec.ConfigValue<Double> SCARLET_HUNTER_DAMAGE_MAX_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Double> SCARLET_HUNTER_MAX_REDUCTION;
    public static final ForgeConfigSpec.ConfigValue<Integer> SCARLET_HUNTER_DECAY_SECOND;
    public static final ForgeConfigSpec.ConfigValue<Integer> SCARLET_HUNTER_BLOOD_SHIELD_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> SCARLET_HUNTER_BLOOD_SHIELD_COOLDOWN;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("BOSS Config");

        builder.push("Arterius Config");

        ARTERIUS_ATTACK_DAMAGE_MULTIPLIER = builder
                .comment("攻击力倍率 （基础伤害 + 武器伤害）*倍率")
                .defineInRange("arteriusAttackDamageMultiplier",1D,0D,Double.MAX_VALUE);

        ARTERIUS_MAX_HEALTH_MULTIPLIER = builder
                .comment("最大生命值倍率")
                .defineInRange("arteriusMaxHealthMultiplier",1D,0D,Double.MAX_VALUE);

        ARTERIUS_EX_HP_DAMAGE = builder
                .comment("命中敌人时额外造成的伤害（基于目标最大生命值的一定百分比）")
                .defineInRange("arteriusExHPDamage",0.012D,0D,Double.MAX_VALUE);

        ARTERIUS_DEBUFF_LEVEL = builder
                .comment("命中时施加的debuff等级（从0开始）")
                .defineInRange("arteriusDebuffLevel",5,0,255);

        ARTERIUS_DEBUFF_DURATION = builder
                .comment("命中时施加的debuff时长（tick）")
                .defineInRange("arteriusDebuffDuration",60,0,Integer.MAX_VALUE);

        ARTERIUS_DEBUFF_EX_DAMAGE = builder
                .comment("命中拥有debuff敌人时额外造成的伤害（基于自身攻击力的一定百分比）")
                .defineInRange("arteriusDebuffExDamage",0.3D,0D,Double.MAX_VALUE);

        ARTERIUS_DEBUFF_EX_HP_DAMAGE = builder
                .comment("命中拥有debuff敌人时额外造成的伤害（基于目标最大生命值的一定百分比）")
                .defineInRange("arteriusDebuffExHPDamage",0.05D,0D,Double.MAX_VALUE);


        ARTERIUS_EX_FIRE_DAMAGE = builder
                .comment("攻击额外附带基于攻击力一定百分比的火焰伤害")
                .defineInRange("arteriusExFireDamage",1D,0D,Double.MAX_VALUE);

        ARTERIUS_DAMAGE_CAP = builder
                .comment("受到的单次伤害限制，不超过最大生命值的一定百分比")
                .defineInRange("arteriusDamageCap",0.08D,0D,1D);

        ARTERIUS_DAMAGE_THRESHOLD = builder
                .comment("累计的伤害达到最大生命值一定比例后，减伤率开始提升")
                .defineInRange("arteriusDamageThreshold",0.04D,0D,1D);

        ARTERIUS_DAMAGE_MAX_THRESHOLD = builder
                .comment("累计的伤害达到最大生命值一定比例后，减伤率达到最大值")
                .defineInRange("arteriusDamageMaxThreshold",0.12D,0D,1D);

        ARTERIUS_MAX_REDUCTION = builder
                .comment("减伤率能达到的最大值")
                .defineInRange("arteriusMaxReduction",0.9D,0D,1D);

        ARTERIUS_DECAY_SECOND = builder
                .comment("减伤率从最大值减少到0所需要的时间（tick）")
                .defineInRange("arteriusDecaySecond",160,0,Integer.MAX_VALUE);


        builder.pop();

        builder.push("Scarlet Hunter Config");

        SCARLET_HUNTER_DAMAGE_MULTIPLIER = builder
                .comment("攻击力倍率")
                .defineInRange("scarletHunterDamageMultiplier",1D,0D,Double.MAX_VALUE);

        SCARLET_HUNTER_HEALTH_MULTIPLIER = builder
                .comment("最大生命值倍率")
                .defineInRange("scarletHunterHealthMultiplier",1D,0D,Double.MAX_VALUE);

        SCARLET_HUNTER_STAMINA_MULTIPLIER = builder
                .comment("耐力倍率")
                .defineInRange("scarletHunterStaminaMultiplier",1D,0D,Double.MAX_VALUE);

        SCARLET_HUNTER_DAMAGE_CAP = builder
                .comment("单次伤害上限（最大生命值百分比）")
                .defineInRange("scarletHunterDamageCap",0.04D,0D,1D);

        SCARLET_HUNTER_DAMAGE_MAX_THRESHOLD = builder
                .comment("达到最大减伤所需的伤害比例")
                .defineInRange("scarletHunterDamageMaxThreshold",0.06D,0D,1D);

        SCARLET_HUNTER_MAX_REDUCTION = builder
                .comment("最大减伤比例")
                .defineInRange("scarletHunterMaxReduction",0.9D,0D,1D);

        SCARLET_HUNTER_DECAY_SECOND = builder
                .comment("减伤从最大衰减到0的时间（tick）")
                .defineInRange("scarletHunterDecaySecond",160,0,Integer.MAX_VALUE);

        SCARLET_HUNTER_BLOOD_SHIELD_DURATION = builder
                .comment("血盾持续时间（tick）")
                .defineInRange("scarletHunterBloodShieldDuration",2400,0,Integer.MAX_VALUE);

        SCARLET_HUNTER_BLOOD_SHIELD_COOLDOWN = builder
                .comment("血盾冷却时间（tick）")
                .defineInRange("scarletHunterBloodShieldCooldown",2400,0,Integer.MAX_VALUE);

        builder.pop();



        builder.pop();

        FORGE_CONFIG_SPEC = builder.build();
    }
}
