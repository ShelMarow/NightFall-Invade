package net.shelmarow.nightfall_invade.assets;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.skill.SkillCategory;

public enum NFISkillCategories implements SkillCategory {

    NFI_COMBAT_ART(true, true, true);

    final boolean shouldSave;
    final boolean shouldSyncronize;
    final boolean modifiable;
    final int id;
    final ResourceLocation bookIcon;

    NFISkillCategories(boolean shouldSave, boolean shouldSyncronizedAllPlayers, boolean modifiable) {
        this.shouldSave = shouldSave;
        this.shouldSyncronize = shouldSyncronizedAllPlayers;
        this.modifiable = modifiable;
        this.id = SkillCategory.ENUM_MANAGER.assign(this);
        this.bookIcon = SkillCategory.DEFAULT_BOOK_ICON;
    }

    @Override
    public boolean shouldSave() {
        return shouldSave;
    }

    @Override
    public boolean shouldSynchronize() {
        return shouldSyncronize;
    }

    @Override
    public boolean learnable() {
        return modifiable;
    }

    @Override
    public int universalOrdinal() {
        return id;
    }
}
