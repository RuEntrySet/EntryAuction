package ru.entryset.auction.item;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.entryset.api.tools.Messager;
import ru.entryset.auction.main.Main;

@SuppressWarnings("deprecation")
public class Item {

	private final ItemStack i;
	
	private ItemMeta meta;
	
	private String name;
	
	private List<String> lore;
	
	private final List<ItemFlag> flags = new ArrayList<ItemFlag>();
	 
	private final List<Enchantment> enchantments = new ArrayList<Enchantment>();
	
	private Boolean unbreakable;

	public Item(String info) {
		this.i = new ItemStack(Material.valueOf(Main.items.getString(info + "material"))
				, Main.items.getInt( info + "amount"), (short) Main.items.getInt(info + "data"));
		ItemMeta im = i.getItemMeta();
		load(info, im);
		i.setItemMeta(im);
	}

	public ItemStack getItem() {
		return this.i;
	}

	public ItemMeta getMeta() {
		return this.meta;
	}

	public String getDisplayName() {
		return this.name;
	}

	public List<String> getLore() {
		return this.lore;
	}

	public List<ItemFlag> getFlags() {
		return this.flags;
	}

	public List<Enchantment> getEnchantments() {
		return this.enchantments;
	}

	public boolean isUnbreakable() {
		return this.unbreakable;
	}

	private void load(String info, ItemMeta im) {
		this.meta = im;
		if(Main.items.getConfiguration().get(info + "name") != null) {
			this.name = Messager.color(Main.items.getString(info + "name"));
			im.setDisplayName(getDisplayName());
		}
		if(Main.items.getConfiguration().get(info + "lore") != null) {
			List<String> lore = new ArrayList<String>();
			for(String s : Main.items.getStringList(info + "lore")) {
				lore.add(Messager.color(s));
			}
			this.lore = lore;
			im.setLore(lore);
		}
	}
	
	public void setItemMeta(ItemMeta meta) {
		getItem().setItemMeta(meta);
		this.meta = meta;
	}
}
