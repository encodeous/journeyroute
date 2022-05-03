package ca.encodeous.journeyroute.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;

public class RouteChunk implements DataStorable {
    public int chunkX, chunkZ;
    public HashMap<Vec3i, RouteNode> NodeMap;
    public RouteChunk(int x, int z){
        chunkX = x;
        chunkZ = z;
        NodeMap = new HashMap<>();
    }
    public RouteChunk(){

    }
    @Override
    public void write(NbtCompound out) {
        out.putInt("x", chunkX);
        out.putInt("z", chunkZ);
        var nodes = NodeMap.values().stream().toList();
        out.putInt("rl", nodes.size());
        for(int i = 0; i < nodes.size(); i++){
            var cmp = new NbtCompound();
            nodes.get(i).write(cmp);
            out.put("rd" + i, cmp);
        }
    }

    @Override
    public void read(NbtCompound in) {
        chunkX = in.getInt("x");
        chunkZ = in.getInt("z");
        int len = in.getInt("rl");
        NodeMap = new HashMap<>(len);
        for(int i = 0; i < len; i++){
            var nn = new RouteNode();
            nn.read((NbtCompound) in.get("rd" + i));
            NodeMap.put(new Vec3i(nn.worldX, nn.worldY, nn.worldZ), nn);
        }
    }
}
