package net.shelmarow.nightfall_invade.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.shelmarow.nightfall_invade.utils.EntityUtils;

import java.util.function.Supplier;

public class S2CPushEntityAwayPacket {
    private final int entityID;
    private final float strengthX;
    private final float strengthZ;
    private final float airBorne;

    public S2CPushEntityAwayPacket(int entityID, float strengthX, float airBorne, float strengthZ) {
        this.entityID = entityID;
        this.strengthX = strengthX;
        this.airBorne = airBorne;
        this.strengthZ = strengthZ;
    }

    public static void encode(S2CPushEntityAwayPacket msg, FriendlyByteBuf buffer){
        buffer.writeInt(msg.entityID);
        buffer.writeFloat(msg.strengthX);
        buffer.writeFloat(msg.airBorne);
        buffer.writeFloat(msg.strengthZ);
    }

    public static S2CPushEntityAwayPacket decode(FriendlyByteBuf buffer){
        return new S2CPushEntityAwayPacket(buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
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

    public float getStrengthX() {
        return strengthX;
    }

    public float getStrengthZ() {
        return strengthZ;
    }

    public float getAirBorne() {
        return airBorne;
    }
}
