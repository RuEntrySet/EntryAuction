package ru.entryset.auction.item;

import org.bukkit.inventory.ItemStack;

public class Controller {

	public static ItemStack getItem(String info) {
		return new Item("items." + info + ".").getItem();
	}

}
