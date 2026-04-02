package net.shelmarow.nightfall_invade.assets;

import net.shelmarow.nightfall_invade.NightFallInvade;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.gameasset.Armatures;

public class NFIArmatures {

    public static Armatures.ArmatureAccessor<Armature> BLOOD_SLASH_ARMATURE =
            Armatures.ArmatureAccessor.create(NightFallInvade.MOD_ID,"entity/blood_slash", Armature::new);

}
