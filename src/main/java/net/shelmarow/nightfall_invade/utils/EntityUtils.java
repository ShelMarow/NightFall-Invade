package net.shelmarow.nightfall_invade.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.shelmarow.nightfall_invade.network.server.S2CPushEntityAwayPacket;

import java.util.List;

public class EntityUtils {

    public static void handlePushEntityAway(S2CPushEntityAwayPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(msg.getEntityID());
        if (entity == null) return;

        if (msg.getType() == 0) {
            EntityUtils.pushEntityAway(entity,mc.level,msg.getRange(),msg.getStrength(),msg.getAirBorne());
        } else {
            EntityUtils.pushEntityAwayByDistance(entity,mc.level,msg.getRange(),msg.getStrength(),msg.getAirBorne());
        }
    }

    public static void pushEntityAway(Entity entity, Level level, float range, float strength, float airBorne) {
        if (level != null) {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(range))
                    .stream().filter(target -> target != entity).toList();
            Vec3 pos = entity.position();
            for (Entity target : entities) {
                Vec3 targetPos = target.position();
                Vec3 pushStrength = targetPos.subtract(new Vec3(pos.x, targetPos.y,pos.z)).normalize().scale(strength);
                target.push(pushStrength.x, airBorne, pushStrength.z);
            }
        }
    }

    public static void pushEntityAwayByDistance(Entity entity, Level level, float range, float strength, float airBorne) {
        if (level != null) {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(range))
                    .stream().filter(target -> target != entity).toList();
            Vec3 pos = entity.position();
            for (Entity target : entities) {
                Vec3 targetPos = target.position();
                double ratio = 1 - (Mth.clamp(pos.distanceToSqr(targetPos),0, range * range) / (range * range));
                Vec3 pushStrength = targetPos.subtract(new Vec3(pos.x, targetPos.y,pos.z)).normalize().scale(strength * ratio);
                target.push(pushStrength.x, airBorne * ratio, pushStrength.z);
            }
        }
    }
}
