package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.genetics.Genome;
import net.minecraft.network.FriendlyByteBuf;
import com.iiampanda.icbreed.platform.StreamCodec;
import com.iiampanda.icbreed.platform.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenGeneticsPayload(int entityId, Genome genome, boolean canOpenInventory,
                                  int pregnantTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenGeneticsPayload> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(IcBreed.MOD_ID, "open_genetics"));

    public static final StreamCodec<FriendlyByteBuf, OpenGeneticsPayload> CODEC =
            StreamCodec.ofMember(OpenGeneticsPayload::write, OpenGeneticsPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        genome.write(buf);
        buf.writeBoolean(canOpenInventory);
        buf.writeVarInt(pregnantTicks + 1);
    }

    private static OpenGeneticsPayload read(FriendlyByteBuf buf) {
        return new OpenGeneticsPayload(buf.readVarInt(), Genome.read(buf), buf.readBoolean(), buf.readVarInt() - 1);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
