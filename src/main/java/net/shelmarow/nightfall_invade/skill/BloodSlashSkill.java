package net.shelmarow.nightfall_invade.skill;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shelmarow.combat_evolution.effect.CEMobEffects;
import net.shelmarow.nightfall_invade.assets.NFIAnimations;
import net.shelmarow.nightfall_invade.assets.NFISkillCategories;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

public class BloodSlashSkill extends Skill {

    public BloodSlashSkill(SkillBuilder<? extends Skill> builder) {
        super(builder.setCategory(NFISkillCategories.NFI_COMBAT_ART).setActivateType(ActivateType.TOGGLE).setResource(Resource.COOLDOWN));
        this.consumption = 45;
        this.maxStackSize = 1;
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return container.getExecutor().getOriginal().isCreative() || container.getStack() > 0;
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        super.executeOnServer(container, args);
        if(!container.getExecutor().getOriginal().isCreative() && container.getStack() > 0){
            setStackSynchronize(container,container.getStack() - 1);
        }
        container.getExecutor().playAnimationSynchronized(NFIAnimations.BLOOD_SLASH_ATTACK, 0F);
        container.getExecutor().getOriginal().addEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 30, 0, false, false, true));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldDraw(SkillContainer container) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0.0F, gui.getSlidingProgression(), 0.0F);
        guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
        if(container.getStack() < this.maxStackSize) {
            String timeCount = String.format("%.1f", container.getMaxResource() - container.getResource());
            guiGraphics.drawString(gui.getFont(), timeCount, x + 12F - gui.getFont().width(timeCount) / 2F, y + 22F - gui.getFont().lineHeight / 2F, 0xFFFFFF, true);
        }
        poseStack.popPose();
    }
}
