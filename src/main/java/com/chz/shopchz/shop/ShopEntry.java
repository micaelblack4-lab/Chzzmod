package com.chz.shopchz.shop;

import net.minecraft.item.ItemStack;

public class ShopEntry {
	private final ItemStack stack;
	private final int buyPrice;
	private final int sellPrice;

	public ShopEntry(ItemStack stack, int buyPrice, int sellPrice) {
		this.stack = stack.copy();
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
	}

	public ItemStack getStack() {
		return stack.copy();
	}

	public int getBuyPrice() {
		return buyPrice;
	}

	public int getSellPrice() {
		return sellPrice;
	}
}
