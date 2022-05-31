package ca.encodeous.journeyroute.utils;

import net.minecraft.core.Vec3i;

public class TextUtils {
    public static String formatCoordinate(Vec3i pos){
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
