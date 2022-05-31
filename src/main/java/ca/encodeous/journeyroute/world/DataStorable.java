package ca.encodeous.journeyroute.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.NbtComponent;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface DataStorable {
    public void write(ByteBuf out);
    public void read(ByteBuf in);
}
