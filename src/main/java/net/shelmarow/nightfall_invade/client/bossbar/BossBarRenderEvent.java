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

    @SubscribeEvent
    public static void onBossBarRender(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = event.getX();
        int y = event.getY();
        Component displayName = boss.getName();
        float progress = Mth.clamp(boss.getProgress(),0,1);

        BossData bossData = ClientBossData.getBossData(boss.getId());
        if(bossData.displayType.equals("[NightFallInvade:Arterius]")) {
            event.setCanceled(true);
            float stamina = Mth.clamp(ClientBossData.getStaminaProgress(boss.getId()),0,1);

            //绘制血量进度 13 + 230 + 13
            guiGraphics.blit(
                    BossBarTextures.ArteriusBossBar.BOSS_BAR_HEALTH,
                    x + 91 - 256/2 + 13, y - mc.font.lineHeight,
                    13,0,
                    Math.round(230*progress),32,
                    256,32
            );

            //绘制耐力进度 11 + 234 + 11
            if(bossData.staminaStatus != StaminaStatus.BREAK) {
                guiGraphics.blit(
                        BossBarTextures.ArteriusBossBar.BOSS_BAR_STAMINA,
                        x + 91 - 256 / 2 + 11, y - mc.font.lineHeight,
                        11, 0,
                        Math.round(234 * stamina), 32,
                        256, 32
                );
            }
            else{
                guiGraphics.blit(
                        BossBarTextures.ArteriusBossBar.BOSS_BAR_STAMINA_BREAK,
                        x + 91 - 256 / 2 , y - mc.font.lineHeight,
                        0, 0,
                        256, 32,
                        256, 32
                );
            }
            //绘制背景
            guiGraphics.blit(
                    BossBarTextures.ArteriusBossBar.BOSS_BAR_BG,
                    x + 91 - 256/2, y - mc.font.lineHeight,
                    0,0,
                    256,32,
                    256,32
            );

            //绘制名称
            guiGraphics.drawString(
                    mc.font, displayName,
                    x + 91 - mc.font.width(displayName) / 2,
                    y - mc.font.lineHeight + 23,
                    0xFFFFFF,false
            );

            event.setIncrement(16 + 32 - mc.font.lineHeight);
        }
        else if (bossData.displayType.equals("[NightFallInvade:ScarletHunter]")) {
            event.setCanceled(true);
            float stamina = Mth.clamp(ClientBossData.getStaminaProgress(boss.getId()),0,1);

            y += 7;

            //绘制背景
            guiGraphics.blit(
                    BossBarTextures.ScarletHunterBossBar.BOSS_BAR_BG,
                    x + 91 - 330/2, y - mc.font.lineHeight,
                    0,0,
                    330,21,
                    330,21
            );

            //绘制血量进度 4 + 322 + 4
            guiGraphics.blit(
                    BossBarTextures.ScarletHunterBossBar.BOSS_BAR_HEALTH,
                    x + 91 - 330/2 + 4, y - mc.font.lineHeight,
                    4,0,
                    Math.round(322*progress),21,
                    330,21
            );

            if(bossData.displayType.equals("[NightFallInvade:ScarletHunter_BloodShield]")){
                long t = System.currentTimeMillis() % 6400;
                //绘制血盾图层
                guiGraphics.blit(
                        BossBarTextures.ScarletHunterBossBar.SHIELD,
                        x + 91 - 330/2, y - mc.font.lineHeight + 5,
                        t * 0.01F,t * 0.01F,
                        330,7,
                        64,32
                );
            }




            //耐力 38 + 254 + 38
            if(bossData.staminaStatus != StaminaStatus.BREAK) {
                guiGraphics.blit(
                        BossBarTextures.ScarletHunterBossBar.BOSS_BAR_STAMINA,
                        x + 91 - 330 / 2 + 38, y - mc.font.lineHeight,
                        38, 0,
                        Math.round(254 * stamina), 21,
                        330, 21
                );
            }
            else{
                guiGraphics.blit(
                        BossBarTextures.ScarletHunterBossBar.BOSS_BAR_STAMINA_BREAK,
                        x + 91 - 330 / 2, y - mc.font.lineHeight,
                        0, 0,
                        330, 21,
                        330, 21
                );
            }

            //绘制名称
            guiGraphics.drawString(
                    mc.font, displayName,
                    x + 91 - mc.font.width(displayName) / 2,
                    y - 7 - mc.font.lineHeight ,
                    0xFFFFFF,true
            );

            event.setIncrement(16 + 21 - mc.font.lineHeight);

        }
    }
}
