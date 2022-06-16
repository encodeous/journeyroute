package ca.encodeous.journeyroute.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.NbtComponent;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * An interface that enforces an object to be able to be written or read to/from a file
 */
public interface DataStorable {
    void write(ByteBuf out);
    void read(ByteBuf in);
}
