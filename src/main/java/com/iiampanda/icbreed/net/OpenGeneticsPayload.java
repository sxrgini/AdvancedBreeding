package com.iiampanda.icbreed.net;

import com.iiampanda.icbreed.IcBreed;
import com.iiampanda.icbreed.genetics.Genome;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenGeneticsPayload(int entityId, Genome genome, boolean canOpenInventory,
                                  int pregnantTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenGeneticsPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IcBreed.MOD_ID, "open_genetics"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGeneticsPayload> CODEC =
            StreamCodec.ofMember(OpenGeneticsPayload::write, OpenGeneticsPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        genome.write(buf);
        buf.writeBoolean(canOpenInventory);
        buf.writeVarInt(pregnantTicks + 1);
    }

    private static OpenGeneticsPayload read(RegistryFriendlyByteBuf buf) {
        return new OpenGeneticsPayload(buf.readVarInt(), Genome.read(buf), buf.readBoolean(), buf.readVarInt() - 1);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
