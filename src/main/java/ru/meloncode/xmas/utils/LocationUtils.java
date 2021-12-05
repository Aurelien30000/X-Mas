package ru.meloncode.xmas.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class LocationUtils {

    public static long getChunkKey(Chunk chunk) {
        return getChunkKey(chunk.getX(), chunk.getZ());
    }

    public static long getChunkKey(Location location) {
        return getChunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static long getChunkKey(int x, int z) {
        x = floor(x);
        z = floor(z);
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    public static int floor(double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

}