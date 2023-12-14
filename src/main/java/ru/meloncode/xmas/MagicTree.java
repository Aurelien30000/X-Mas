package ru.meloncode.xmas;

import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.*;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MagicTree {
    private static final ConcurrentHashMap<Block, UUID> blockAssociation = new ConcurrentHashMap<>();
    private final UUID owner;
    private final Location location;
    private final UUID treeuid;
    TreeLevel level;
    private Map<Material, Integer> levelupRequirements;
    private Set<Block> blocks;
    private long presentCounter;
    private int scheduledPresents;

    public MagicTree(UUID owner, TreeLevel level, Location location) {
        this.treeuid = UUID.randomUUID();
        this.owner = owner;
        this.level = level;
        this.location = location;
        this.levelupRequirements = new HashMap<>(level.getLevelupRequirements());
        if (Main.inProgress)
            build();
        this.presentCounter = 0;
        this.scheduledPresents = 0;
    }

    public MagicTree(UUID owner, UUID uid, TreeLevel level, Location location, Map<Material, Integer> levelupRequirements, long presentCounter, int scheduledPresents) {
        this.owner = owner;
        this.treeuid = uid;
        this.level = level;
        this.location = location;
        this.levelupRequirements = new HashMap<>(levelupRequirements);
        this.presentCounter = presentCounter;
        if (Main.inProgress)
            build();
        this.scheduledPresents = scheduledPresents;
    }

    public static MagicTree getTreeByBlock(Block block) {
        return XMas.getTree(blockAssociation.get(block));
    }

    public static boolean isBlockBelongs(Block block) {
        return blockAssociation.containsKey(block);
    }

    public UUID getOwner() {
        return owner;
    }

    public TreeLevel getLevel() {
        return level;
    }

    public Location getLocation() {
        return location;
    }

    public Map<Material, Integer> getLevelupRequirements() {
        return levelupRequirements;
    }

    public boolean grow(Material material) {
        if (levelupRequirements.containsKey(material)) {
            float levelReq = getLevel().getLevelupRequirements().get(material);
            int treeReq;
            if (levelupRequirements.get(material) <= 1) {
                levelupRequirements.remove(material);
                treeReq = 0;
            } else {
                treeReq = levelupRequirements.get(material) - 1;
                levelupRequirements.put(material, treeReq);
            }
            for (Block block : blocks) {
                if (block.getType() == Material.SPRUCE_LEAVES || block.getType() == Material.SPRUCE_SAPLING) {
                    Effects.GROW.playEffect(block.getLocation());
                }
            }
            location.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.2f + ((levelReq - treeReq) / levelReq));
            save();
            return true;
        }
        return false;
    }

    public void update() {
        if (Main.inProgress) {
            if (level.getGiftDelay() > 0) {
                if (presentCounter == 0) {
                    spawnPresent();
                    presentCounter = (long) (Math.random() * (level.getGiftDelay() / 2) + level.getGiftDelay());
                }
                presentCounter--;
            }
        }
    }

    public void playParticles() {
        if (blocks != null && !blocks.isEmpty()) {
            if (!location.isChunkLoaded())
                return;
            for (Block block : blocks) {
                if (block.getType() == Material.SPRUCE_LEAVES) {
                    if (level.getSwagEffect() != null) {
                        level.getSwagEffect().playEffect(block.getLocation());
                    }
                }
                if (block.getType() == Material.SPRUCE_LOG) {
                    if (level.getBodyEffect() != null) {
                        level.getBodyEffect().playEffect(block.getLocation());
                    }
                }
                if (level.getAmbientEffect() != null) {
                    level.getAmbientEffect().playEffect(location.clone().add(0, level.getTreeHeight(), 0));
                }
            }
        }
    }

    public boolean tryLevelUp() {
        if (level.hasNext()) {
            if (level.nextLevel.getStructureTemplate().canGrow(location)) {
                levelUp();
                return true;
            }
        }
        return false;
    }

    private void levelUp() {
        unbuild();
        this.level = level.nextLevel;
        this.levelupRequirements = new HashMap<>(level.getLevelupRequirements());
        for (int i = 0; i <= 3; i++) {
            final Firework fw = location.getWorld().spawn(location.clone().add(new Vector(-3 + Main.RANDOM.nextInt(6), 3, -3 + Main.RANDOM.nextInt(6))), Firework.class);
            final FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().trail(true).withColor(Color.RED).withFade(Color.LIME).withFlicker().with(Type.BURST).build());
            fw.setFireworkMeta(meta);
            fw.setMetadata("nodamage", new FixedMetadataValue(Main.getInstance(), true));
        }
        build();
        save();
    }

    public void unbuild() {
        Block block;
        Location loc;
        for (Entry<Block, UUID> cBlock : blockAssociation.entrySet()) {
            if (cBlock.getValue().equals(treeuid)) {
                block = cBlock.getKey();
                loc = block.getLocation();
                loc.getWorld().playEffect(loc, Effect.STEP_SOUND, block.getType());
                block.setType(Material.AIR);
                blockAssociation.remove(block);
            }
        }
        location.clone().add(0, -1, 0).getBlock().setType(Material.GRASS_BLOCK);
    }

    public void build() {
        if (level.getStructureTemplate().canGrow(location)) {
            blocks = level.getStructureTemplate().build(location);
            for (Block block : blocks) {
                blockAssociation.put(block, getTreeUID());
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void spawnPresent() {
        if (!location.isChunkLoaded()) {
            if (scheduledPresents + 1 <= 8)
                scheduledPresents++;
            return;
        }

        location.getWorld().getChunkAtAsync(location, false).thenRun(() -> {
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if (x == 0 && y == 0)
                        continue;

                    final Block block = location.clone().add(x, 0, y).getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.PLAYER_HEAD);
                        final BlockState state = block.getState();
                        final Skull skull = (Skull) state;
                        BlockFace face;
                        do {
                            face = BlockFace.values()[Main.RANDOM.nextInt(BlockFace.values().length)];
                        }
                        while (face == BlockFace.DOWN || face == BlockFace.UP || face == BlockFace.SELF);
                        //skull.setRotation(face);
                        final Rotatable skullRotatable = (Rotatable) skull.getBlockData();
                        skullRotatable.setRotation(face);
                        //skull.setSkullType(SkullType.PLAYER);
                        skull.setType(Material.PLAYER_HEAD);
                        //skull.setOwner();
                        skull.setOwningPlayer(Bukkit.getOfflinePlayer(Main.getHeads().get(Main.RANDOM.nextInt(Main.getHeads().size()))));
                        skull.update(true);
                        return;
                    }
                }
            }
        });
    }

    public boolean canLevelUp() {
        return getLevelupRequirements().isEmpty();
    }

    public UUID getTreeUID() {
        return treeuid;
    }

    public void save() {
        TreeSerializer.saveTree(this);
    }

    public void end() {
        unbuild();
        // Bad code. Need it fast.
        Block bl;
        if ((bl = location.clone().add(1, 0, 0).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);
        if ((bl = location.clone().add(-1, 0, 0).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);
        if ((bl = location.clone().add(0, 0, 1).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);
        if ((bl = location.clone().add(0, 0, -1).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);

        if ((bl = location.clone().add(1, 0, 1).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);
        if ((bl = location.clone().add(-1, 0, -1).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);

        if ((bl = location.clone().add(-1, 0, 1).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);
        if ((bl = location.clone().add(1, 0, -1).getBlock()).getType() == Material.PLAYER_HEAD)
            bl.setType(Material.AIR);
        if (Main.resourceBack) {
            bl = location.getBlock();
            bl.setType(Material.CHEST);
            final Chest chest = (Chest) bl.getState();
            final Inventory inv = chest.getInventory();

            inv.addItem(new ItemStack(Material.DIAMOND, 4));
            inv.addItem(new ItemStack(Material.EMERALD, 1));
            TreeLevel cLevel = TreeLevel.SAPLING;
            while (cLevel != level) {
                if (cLevel.getLevelupRequirements() != null && !cLevel.getLevelupRequirements().isEmpty()) {
                    for (Entry<Material, Integer> currItem : cLevel.getLevelupRequirements().entrySet()) {
                        inv.addItem(new ItemStack(currItem.getKey(), currItem.getValue()));
                    }
                }

                if (cLevel.nextLevel == null)
                    break;
                cLevel = cLevel.nextLevel;
            }

            int count = 0;
            for (Entry<Material, Integer> currItem : level.getLevelupRequirements().entrySet()) {
                if (getLevelupRequirements().containsKey(currItem.getKey()))
                    count = getLevelupRequirements().get(currItem.getKey());
                if (currItem.getValue() - count > 0)
                    inv.addItem(new ItemStack(currItem.getKey(), currItem.getValue() - count));
                count = 0;
            }
        }
        XMas.removeTree(this);
    }

    public long getPresentCounter() {
        return presentCounter;
    }

    public int getScheduledPresents() {
        return scheduledPresents;
    }

    public boolean hasScheduledPresents() {
        return scheduledPresents > 0;
    }

    public void spawnScheduledPresents() {
        for (int i = scheduledPresents; i > 0; i--)
            spawnPresent();
        scheduledPresents = 0;
    }

}
