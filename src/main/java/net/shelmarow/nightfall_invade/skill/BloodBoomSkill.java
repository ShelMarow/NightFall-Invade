package net.shelmarow.nightfall_invade.skill;

import com.hm.efn.gameasset.animations.EFNClawAnimations_N;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shelmarow.combat_evolution.effect.CEMobEffects;
import net.shelmarow.nightfall_invade.assets.NFISkillCategories;
import net.shelmarow.nightfall_invade.entity.misc.blood_bomb.BloodBoom;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class BloodBoomSkill extends Skill {

    public BloodBoomSkill(SkillBuilder<? extends Skill> builder) {
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
        PlayerPatch<?> executor = container.getExecutor();
        if(!executor.getOriginal().isCreative() && container.getStack() > 0){
            setStackSynchronize(container,container.getStack() - 1);
        }
        executor.playAnimationSynchronized(EFNClawAnimations_N.NF_CLAW_BEASTROAR, 0F);
        executor.getOriginal().addEffect(new MobEffectInstance(CEMobEffects.FULL_STUN_IMMUNITY.get(), 30, 0, false, false, true));

        double verticalSpeed = 0.2 + Math.random() * 0.2;
        float damage = (float)executor.getOriginal().getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.20F;
        spawnBloodBoom(executor, 70, 10, 30, 4, verticalSpeed, damage);
    }

    private static void spawnBloodBoom(LivingEntityPatch<?> caster, double maxAngle, int count, int waitTime, float delay, double verticalSpeed, float damage) {
        Level level = caster.getOriginal().level();
        if(level instanceof ServerLevel serverLevel){

            double maxAngleRad = Math.toRadians(maxAngle);
            double cosMax = Math.cos(maxAngleRad);

            for (int i = 0; i < count; i++) {
                BloodBoom bloodBoom = new BloodBoom(serverLevel);
                bloodBoom.setPos(caster.getOriginal().getEyePosition());
                bloodBoom.setOwnerPatch(caster);
                bloodBoom.setWaitTick(waitTime + (int)(delay * i));
                bloodBoom.setDamage(damage);

                double u = Math.random();
                double v = Math.random();
                double theta = 2 * Math.PI * u;
                double cosPhi = Mth.lerp(v, cosMax, 1.0);
                double sinPhi = Math.sqrt(1 - cosPhi * cosPhi);

                double x = sinPhi * Math.cos(theta);
                double z = sinPhi * Math.sin(theta);

                Vec3 dir = new Vec3(x, cosPhi, z).normalize();
                bloodBoom.setDeltaMovement(dir.scale(verticalSpeed));

                serverLevel.addFreshEntity(bloodBoom);
            }
        }
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
