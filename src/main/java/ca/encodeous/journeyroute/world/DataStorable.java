package ca.encodeous.journeyroute.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.NbtComponent;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface DataStorable {
    public void write(CompoundTag out);
    public void read(CompoundTag in);
}
