package net.shelmarow.nightfall_invade.assets;

import yesman.epicfight.skill.SkillSlot;

public enum NFISkillSlots implements SkillSlot {
    NFI_COMBAT_ART1(NFISkillCategories.NFI_COMBAT_ART)
    ;

    final NFISkillCategories category;
    final int id;

    NFISkillSlots(NFISkillCategories category) {
        this.category = category;
        this.id = SkillSlot.ENUM_MANAGER.assign(this);
    }

    @Override
    public NFISkillCategories category() {
        return category;
    }

    @Override
    public int universalOrdinal() {
        return id;
    }
}
