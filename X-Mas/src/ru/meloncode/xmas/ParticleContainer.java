package ru.meloncode.xmas;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import ru.meloncode.xmas.ParticleEffect;

public class ParticleContainer {

	ParticleEffect type;
	float offsetX, offsetY, offsetZ, speed;
	int count;

	public ParticleContainer(ParticleEffect type, float offsetX, float offsetY, float offsetZ, float speed, int count) {
		this.type = type;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.speed = speed;
		this.count = count;
	}

	public void playEffect(Location location) {
		location = location.clone(); // prevent chaning pos of object
		location.add(0.5, 0.5, 0.5); // A small fix
		for (Player player : location.getWorld().getPlayers())
			if (player.getLocation().distance(location) < 16) {
				try {
					type.sendToPlayer(player, location, offsetX, offsetY, offsetZ, speed, count);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
	}
}
