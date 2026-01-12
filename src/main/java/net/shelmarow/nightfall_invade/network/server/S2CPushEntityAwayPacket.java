package net.shelmarow.nightfall_invade.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shelmarow.nightfall_invade.utils.EntityUtils;

import java.util.function.Supplier;

public class S2CPushEntityAwayPacket {
    private final int entityID;
    private final float range;
    private final float strength;
    private final float airBorne;
    private final int type;

    public S2CPushEntityAwayPacket(int entityID, float range, float strength, float airBorne,int type) {
        this.entityID = entityID;
        this.range = range;
        this.strength = strength;
        this.airBorne = airBorne;
        this.type = type;
    }

    public static void encode(S2CPushEntityAwayPacket msg, FriendlyByteBuf buffer){
        buffer.writeInt(msg.entityID);
        buffer.writeFloat(msg.range);
        buffer.writeFloat(msg.strength);
        buffer.writeFloat(msg.airBorne);
        buffer.writeInt(msg.type);
    }

    public static S2CPushEntityAwayPacket decode(FriendlyByteBuf buffer){
        return new S2CPushEntityAwayPacket(buffer.readInt(), buffer.readFloat(), buffer.readFloat(),buffer.readFloat(),buffer.readInt());
    }

    public static void handle(S2CPushEntityAwayPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EntityUtils.handlePushEntityAway(msg);
        });
        ctx.get().setPacketHandled(true);
    }

    public int getEntityID() {
        return entityID;
    }

    public float getRange() {
        return range;
    }

    public float getStrength() {
        return strength;
    }

    public float getAirBorne() {
        return airBorne;
    }

    public int getType() {
        return type;
    }
}
