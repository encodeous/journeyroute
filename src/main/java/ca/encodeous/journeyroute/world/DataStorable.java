package ca.encodeous.journeyroute.world;

import net.minecraft.nbt.NbtCompound;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface DataStorable {
    public void write(NbtCompound out);
    public void read(NbtCompound in);
}
