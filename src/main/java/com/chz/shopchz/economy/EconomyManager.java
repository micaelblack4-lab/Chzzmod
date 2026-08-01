package com.chz.shopchz.economy;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

	private static final Map<UUID, Long> balances = new HashMap<>();
	private static final long STARTING_MONEY = 1000;

	public static long getBalance(ServerPlayerEntity player) {
		return balances.computeIfAbsent(player.getUuid(), id -> STARTING_MONEY);
	}

	public static long getBalance(UUID uuid) {
		return balances.getOrDefault(uuid, STARTING_MONEY);
	}

	public static void setBalance(ServerPlayerEntity player, long amount) {
		balances.put(player.getUuid(), Math.max(0, amount));
	}

	public static void setBalance(UUID uuid, long amount) {
		balances.put(uuid, Math.max(0, amount));
	}

	public static void addMoney(ServerPlayerEntity player, long amount) {
		setBalance(player, getBalance(player) + amount);
	}

	public static boolean removeMoney(ServerPlayerEntity player, long amount) {
		long current = getBalance(player);
		if (current < amount) return false;
		setBalance(player, current - amount);
		return true;
	}

	public static boolean hasEnough(ServerPlayerEntity player, long amount) {
		return getBalance(player) >= amount;
	}

	public static Text tryBuy(ServerPlayerEntity player, ItemStack item, int price) {
		if (!hasEnough(player, price)) {
			return Text.literal("§cVocê não tem dinheiro suficiente! Precisa de §e\( " + price + "§c. Você tem §e \)" + getBalance(player) + "§c.");
		}

		if (!removeMoney(player, price)) {
			return Text.literal("§cErro ao debitar o dinheiro.");
		}

		ItemStack copy = item.copy();
		player.getInventory().insertStack(copy);
		if (!copy.isEmpty()) {
			player.dropItem(copy, false);
		}

		return Text.literal("§aCompra realizada! §e" + item.getName().getString() + " §apor §e\( " + price + "§a. Saldo: §e \)" + getBalance(player));
	}

	public static Text trySell(ServerPlayerEntity player, ItemStack itemToSell, int reward) {
		boolean found = false;

		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (ItemStack.areItemsAndComponentsEqual(stack, itemToSell) && stack.getCount() >= 1) {
				stack.decrement(1);
				found = true;
				break;
			}
		}

		if (!found) {
			return Text.literal("§cVocê não tem esse item no inventário para vender!");
		}

		addMoney(player, reward);
		return Text.literal("§aVenda realizada! §e" + itemToSell.getName().getString() + " §apor §e\( " + reward + "§a. Saldo: §e \)" + getBalance(player));
	}
}
