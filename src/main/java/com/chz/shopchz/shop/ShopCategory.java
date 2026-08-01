package com.chz.shopchz.shop;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ShopCategory {
	private final String id;
	private final Text name;
	private final ItemStack icon;
	private final List<ShopEntry> entries = new ArrayList<>();

	public ShopCategory(String id, Text name, ItemStack icon) {
		this.id = id;
		this.name = name;
		this.icon = icon;
	}

	public String getId() {
		return id;
	}

	public Text getName() {
		return name;
	}

	public ItemStack getIcon() {
		return icon;
	}

	public List<ShopEntry> getEntries() {
		return entries;
	}

	public void addEntry(ShopEntry entry) {
		entries.add(entry);
	}

	public void addItem(ItemStack stack, int buyPrice, int sellPrice) {
		entries.add(new ShopEntry(stack, buyPrice, sellPrice));
	}
}
