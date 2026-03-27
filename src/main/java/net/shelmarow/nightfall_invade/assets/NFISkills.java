package net.shelmarow.nightfall_invade.assets;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.item.NFICreativeTab;
import net.shelmarow.nightfall_invade.skill.BloodBoomSkill;
import net.shelmarow.nightfall_invade.skill.BloodSlashSkill;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NFISkills {

    public static Skill BLOOD_SLASH;
    public static Skill BLOOD_BOOM;

    @SubscribeEvent
    public static void buildSkillEvent(SkillBuildEvent build) {
        SkillBuildEvent.ModRegistryWorker modRegistry = build.createRegistryWorker(NightFallInvade.MOD_ID);

        BLOOD_SLASH = modRegistry.build("blood_slash", BloodSlashSkill::new,
                BloodSlashSkill.createBuilder().setCreativeTab(NFICreativeTab.NIGHTFALL_INVADE_TAB.get()));

        BLOOD_BOOM = modRegistry.build("blood_boom", BloodBoomSkill::new,
                BloodBoomSkill.createBuilder().setCreativeTab(NFICreativeTab.NIGHTFALL_INVADE_TAB.get()));
    }
}
