package ru.meloncode.xmas.utils;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.meloncode.xmas.LocaleManager;
import ru.meloncode.xmas.MagicTree;

public class TextUtils {

    public static String generateChatReqList(MagicTree tree) {
        if (tree == null)
            throw new NullArgumentException("tree");
        StringBuilder string = new StringBuilder();
        string.append(ChatColor.GOLD).append(LocaleManager.GROW_REQ_LIST_TITLE).append(" : ");
        if (tree.getLevel().getLevelupRequirements() != null && tree.getLevel().getLevelupRequirements().size() > 0)
            for (Material cMaterial : tree.getLevel().getLevelupRequirements().keySet()) {
                int levelReq = tree.getLevel().getLevelupRequirements().get(cMaterial);
                int treeReq = 0;
                if (tree.getLevelupRequirements().containsKey(cMaterial))
                    treeReq = tree.getLevelupRequirements().get(cMaterial);

                string.append(ChatColor.BOLD).append(treeReq == 0 ? ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH : ChatColor.RED).append(WordUtils.capitalizeFully(String.valueOf(cMaterial).replace('_', ' ') + " : " + (levelReq - treeReq + " / " + levelReq))).append(ChatColor.RESET).append(" ").append(ChatColor.GOLD).append("-").append(" ");
            }
        return string.toString();
    }

    public static void sendMessage(Player player, String message) {
        if (player != null && message != null)
            player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + LocaleManager.PLUGIN_NAME + ChatColor.DARK_RED + "] " + ChatColor.RESET + message);
    }

    public static void sendActionBarMessage(Player player, String message) {
        if (player != null && message != null)
            player.sendActionBar(ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + LocaleManager.PLUGIN_NAME + ChatColor.DARK_RED + "] " + ChatColor.RESET + message);
    }

    public static void sendConsoleMessage(String message) {
        if (message != null)
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + "X" + ChatColor.DARK_RED + "-" + ChatColor.DARK_GREEN + "MAS" + ChatColor.DARK_RED + "] " + ChatColor.DARK_GREEN + message);
    }
}
