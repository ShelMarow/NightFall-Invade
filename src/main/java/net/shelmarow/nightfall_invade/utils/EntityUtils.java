package net.shelmarow.nightfall_invade.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.network.server.S2CPushEntityAwayPacket;

import java.util.List;

public class EntityUtils {

    public static void handlePushEntityAway(S2CPushEntityAwayPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(msg.getEntityID());
        if (entity == null) return;

        pushEntityAway(entity, new Vec3(msg.getStrengthX(), msg.getAirBorne(), msg.getStrengthZ()));
    }

    public static void pushEntitiesAway(Entity entity, Level level, float range, float strength, float airBorne) {
        if (level != null) {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(range))
                    .stream().filter(target -> target != entity).toList();
            Vec3 pos = entity.position();
            for (Entity target : entities) {
                Vec3 targetPos = target.position();
                Vec3 pushStrength = targetPos.subtract(new Vec3(pos.x, targetPos.y, pos.z)).normalize().scale(strength).add(0, airBorne, 0);
                pushEntityAway(target, pushStrength);
            }
        }
    }

    public static void pushEntitiesAwayByDistance(Entity entity, Level level, float range, float strength, float airBorne) {
        if (level != null) {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(range))
                    .stream().filter(target -> target != entity).toList();
            Vec3 pos = entity.position();
            for (Entity target : entities) {
                Vec3 targetPos = target.position();
                double ratio = 1 - (Mth.clamp(pos.distanceToSqr(targetPos), 0, range * range) / (range * range));
                Vec3 pushStrength = targetPos.subtract(new Vec3(pos.x, targetPos.y, pos.z)).normalize()
                        .scale(strength * ratio).add(0, airBorne * ratio, 0);
                pushEntityAway(target, pushStrength);
            }
        }
    }


    public static void pushEntityAway(Entity target, Vec3 pushStrength) {
        target.push(pushStrength.x, pushStrength.y, pushStrength.z);
        if (target instanceof ServerPlayer serverPlayer && !serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
            NightFallInvade.CHANNEL.send(PacketDistributor.PLAYER.with(()-> serverPlayer),
                    new S2CPushEntityAwayPacket(target.getId(), (float) pushStrength.x, (float) pushStrength.y, (float) pushStrength.z));
        }
    }
}
