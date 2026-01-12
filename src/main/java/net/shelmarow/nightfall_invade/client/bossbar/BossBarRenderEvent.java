package net.shelmarow.nightfall_invade.client.bossbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.combat_evolution.ai.StaminaStatus;
import net.shelmarow.combat_evolution.bossbar.BossData;
import net.shelmarow.combat_evolution.bossbar.ClientBossData;
import net.shelmarow.nightfall_invade.NightFallInvade;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
public class BossBarRenderEvent {

    private static final ResourceLocation BOSS_BAR_HEALTH = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/gui/bossbar/boss_bar_health.png");
    private static final ResourceLocation BOSS_BAR_STAMINA = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/gui/bossbar/boss_bar_stamina.png");
    private static final ResourceLocation BOSS_BAR_STAMINA_BREAK = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/gui/bossbar/boss_bar_stamina_break.png");
    private static final ResourceLocation BOSS_BAR_BG = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/gui/bossbar/boss_bar_bg.png");

    @SubscribeEvent
    public static void onBossBarRender(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();

        BossData bossData = ClientBossData.getBossData(boss.getId());
        if (bossData.displayType.equals("[NightFallInvade:Arterius]")) {
            event.setCanceled(true);

            Minecraft mc = Minecraft.getInstance();
            GuiGraphics guiGraphics = event.getGuiGraphics();

            int x = event.getX();
            int y = event.getY();
            Component displayName = boss.getName();
            float progress = Mth.clamp(boss.getProgress(),0,1);
            float stamina = Mth.clamp(ClientBossData.getStaminaProgress(boss.getId()),0,1);

            //绘制血量进度 13 + 230 + 13
            guiGraphics.blit(BOSS_BAR_HEALTH,x + 91 - 256/2 + 13,y - mc.font.lineHeight,13,0,Math.round(230*progress),32,256,32);

            //绘制耐力进度 11 + 234 + 11
            if(bossData.staminaStatus != StaminaStatus.BREAK) {
                guiGraphics.blit(BOSS_BAR_STAMINA, x + 91 - 256 / 2 + 11, y - mc.font.lineHeight, 11, 0, Math.round(234 * stamina), 32, 256, 32);
            }
            else{
                guiGraphics.blit(BOSS_BAR_STAMINA_BREAK, x + 91 - 256 / 2 + 11, y - mc.font.lineHeight, 11, 0, Math.round(234 * stamina), 32, 256, 32);
            }
            //绘制背景
            guiGraphics.blit(BOSS_BAR_BG,x + 91 - 256/2, y - mc.font.lineHeight, 0,0,256,32,256,32);

            //绘制名称
            guiGraphics.drawString(mc.font, displayName, x + 91 - mc.font.width(displayName) / 2, y - mc.font.lineHeight + 23, 0xFFFFFF,false);

            event.setIncrement(16 + 32 - mc.font.lineHeight);
        }
    }
}
