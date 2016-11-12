package ru.meloncode.xmas.utils;

import java.io.File;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.meloncode.xmas.Main;

public class ConfigUtils {

	public static FileConfiguration loadConfig(String fileName) {
		File file = new File(Main.getInstance().getDataFolder(), fileName);
		FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

		// Looks for defaults in the jar
		InputStream defConfigStream = Main.getInstance().getResource(fileName);
		if (defConfigStream != null) {
			@SuppressWarnings("deprecation")
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			fileConfiguration.setDefaults(defConfig);
		}
		return fileConfiguration;
	}

	public static FileConfiguration loadConfig(File file) {
		YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
		return defConfig;
	}

	// Save default secondary config
	public static void saveDefaultConfig(String fileName) {
		File file = new File(Main.getInstance().getDataFolder(), fileName);
		if (!file.exists()) {
			Main.getInstance().saveResource(fileName, false);
		}
	}
}
