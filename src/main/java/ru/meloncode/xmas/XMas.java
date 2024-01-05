package ru.meloncode.xmas;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.meloncode.xmas.utils.LocationUtils;
import ru.meloncode.xmas.utils.TextUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class XMas {

    private static final Map<UUID, MagicTree> TREES = new ConcurrentHashMap<>();
    private static final Map<Long, List<MagicTree>> TREES_BY_CHUNK = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    public static ItemStack XMAS_CRYSTAL;

    public static void createMagicTree(Player player, Location loc) {
        MagicTree tree = new MagicTree(player.getUniqueId(), TreeLevel.SAPLING, loc);
        TREES.put(tree.getTreeUID(), tree);
        TREES_BY_CHUNK.computeIfAbsent(LocationUtils.getChunkKey(tree.getLocation()), aLong -> new ArrayList<>()).add(tree);
        tree.save();
    }

    public static void addMagicTree(MagicTree tree) {
        TREES.put(tree.getTreeUID(), tree);
        TREES_BY_CHUNK.computeIfAbsent(LocationUtils.getChunkKey(tree.getLocation()), aLong -> new ArrayList<>()).add(tree);
        tree.build();
    }

    public static Collection<MagicTree> getAllTrees() {
        return TREES.values();
    }

    @Nullable
    public static Collection<MagicTree> getAllTreesInChunk(Chunk chunk) {
        return TREES_BY_CHUNK.get(LocationUtils.getChunkKey(chunk));
    }

    public static void removeTree(MagicTree tree) {
        tree.unbuild();
        TreeSerializer.removeTree(tree);
        TREES.remove(tree.getTreeUID());
        TREES_BY_CHUNK.remove(LocationUtils.getChunkKey(tree.getLocation()));
    }

    public static void processPresent(Block block, Player player) {
        if (block.getType() == Material.PLAYER_HEAD) {
            Skull skull = (Skull) block.getState();

            if (Main.getHeads().contains(skull.getOwner())) {
                Location loc = block.getLocation();
                World world = loc.getWorld();
                if (world != null) {
                    if (RANDOM.nextFloat() < Main.LUCK_CHANCE || !Main.LUCK_CHANCE_ENABLED) {
                        world.dropItemNaturally(loc, new ItemStack(Main.gifts.get(RANDOM.nextInt(Main.gifts.size()))));
                        Effects.TREE_SWAG.playEffect(loc);
                        TextUtils.sendActionBarMessage(player, LocaleManager.GIFT_LUCK);
                    } else {
                        Effects.SMOKE.playEffect(loc);
                        world.dropItemNaturally(loc, new ItemStack(Material.COAL));
                        TextUtils.sendActionBarMessage(player, LocaleManager.GIFT_FAIL);
                    }
                }
                block.setType(Material.AIR);
            }
        }
    }

    public static List<MagicTree> getTreesPlayerOwn(Player player) {
        List<MagicTree> own = new ArrayList<>();
        for (MagicTree cTree : getAllTrees())
            if (cTree.getOwner().equals(player.getUniqueId()))
                own.add(cTree);
        return own;
    }

    public static MagicTree getTree(UUID treeUID) {
        return TREES.get(treeUID);
    }
}
