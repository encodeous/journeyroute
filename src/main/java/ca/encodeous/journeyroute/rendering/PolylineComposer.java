package ca.encodeous.journeyroute.rendering;

import net.minecraft.world.phys.Vec3;

public class PolylineComposer {
    public static Vec3 getNormalVectorPlane(Vec3 a, Vec3 b, float mag){
        var vec = a.subtract(b);
        return new Vec3(vec.z, 0, -vec.x).normalize().scale(mag);
    }
}
