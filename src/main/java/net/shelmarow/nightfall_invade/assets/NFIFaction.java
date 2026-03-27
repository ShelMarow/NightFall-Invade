package net.shelmarow.nightfall_invade.assets;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.capabilities.entitypatch.Faction;

public enum NFIFaction implements Faction {

    NFII_NO_ALIVE(ResourceLocation.parse(""), MathUtils.packColor(255, 255, 0, 100), 0);

    final ResourceLocation healthBar;
    final int healthBarIndex;
    final int damageColor;
    final int id;

    NFIFaction(ResourceLocation healthBar, int healthBarIndex, int damageColor) {
        this.id = Faction.ENUM_MANAGER.assign(this);
        this.healthBar = healthBar;
        this.healthBarIndex = healthBarIndex;
        this.damageColor = damageColor;
    }

    @Override
    public ResourceLocation healthBarTexture() {
        return healthBar;
    }

    @Override
    public int healthBarIndex() {
        return healthBarIndex;
    }

    @Override
    public int damageColor() {
        return damageColor;
    }

    @Override
    public int universalOrdinal() {
        return id;
    }
}
