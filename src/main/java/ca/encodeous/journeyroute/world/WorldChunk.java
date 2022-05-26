package ca.encodeous.journeyroute.world;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class WorldChunk implements DataStorable {
    public int chunkX, chunkZ;
    public HashMap<Vec3i, WorldNode> NodeMap;
    public WorldChunk(int x, int z){
        chunkX = x;
        chunkZ = z;
        NodeMap = new HashMap<>();
    }
    public WorldChunk(){

    }
    @Override
    public void write(CompoundTag out) {
        out.putInt("x", chunkX);
        out.putInt("z", chunkZ);
        var nodes = NodeMap.values().stream().toList();
        out.putInt("rl", nodes.size());
        for(int i = 0; i < nodes.size(); i++){
            var cmp = new CompoundTag();
            nodes.get(i).write(cmp);
            out.put("rd" + i, cmp);
        }
    }

    @Override
    public void read(CompoundTag in) {
        chunkX = in.getInt("x");
        chunkZ = in.getInt("z");
        int len = in.getInt("rl");
        NodeMap = new HashMap<>(len);
        for(int i = 0; i < len; i++){
            var nn = new WorldNode();
            nn.read((CompoundTag) in.get("rd" + i));
            NodeMap.put(new Vec3i(nn.worldX, nn.worldY, nn.worldZ), nn);
        }
    }
}
