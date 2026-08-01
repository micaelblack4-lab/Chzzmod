package com.chz.shopchz.command;

import com.chz.shopchz.economy.EconomyManager;
import com.chz.shopchz.shop.ShopCategory;
import com.chz.shopchz.shop.ShopEntry;
import com.chz.shopchz.shop.ShopManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ShopCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
								CommandRegistryAccess registryAccess,
								CommandManager.RegistrationEnvironment environment) {

		dispatcher.register(CommandManager.literal("loja")
				.executes(ShopCommand::openMainShop)
				.then(CommandManager.argument("categoria", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (ShopCategory cat : ShopManager.getCategories()) {
								builder.suggest(cat.getId());
							}
							return builder.buildFuture();
						})
						.executes(ShopCommand::openCategory)
						.then(CommandManager.literal("comprar")
								.then(CommandManager.argument("index", IntegerArgumentType.integer(0))
										.executes(ShopCommand::buyItem)
								)
						)
						.then(CommandManager.literal("vender")
								.then(CommandManager.argument("index", IntegerArgumentType.integer(0))
										.executes(ShopCommand::sellItem)
								)
						)
				)
		);
	}

	private static int openMainShop(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();

		if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
			source.sendError(Text.literal("Este comando só pode ser usado por jogadores."));
			return 0;
		}

		List<ShopCategory> categories = ShopManager.getCategories();

		player.sendMessage(Text.literal("§8§m                                          "), false);
		player.sendMessage(Text.literal("§6§l           ✦ LOJA SHOPCHZ ✦"), false);
		player.sendMessage(Text.literal("§7     Dinheiro virtual do Shop"), false);
		player.sendMessage(Text.literal("§8§m                                          "), false);
		player.sendMessage(Text.literal(""), false);

		for (ShopCategory category : categories) {
			int count = category.getEntries().size();

			MutableText line = Text.literal(" §e▶ ")
					.append(category.getName().copy())
					.append(Text.literal(" §8(" + count + " itens)").formatted(Formatting.DARK_GRAY));

			line.setStyle(Style.EMPTY
					.withClickEvent(new ClickEvent.RunCommand("/loja " + category.getId()))
					.withHoverEvent(new HoverEvent.ShowText(
							Text.literal("§aClique para abrir: ").append(category.getName())
					)));

			player.sendMessage(line, false);
		}

		player.sendMessage(Text.literal(""), false);
		player.sendMessage(Text.literal("§7Use §e/saldo §7para ver seu dinheiro"), false);
		player.sendMessage(Text.literal("§8§m                                          "), false);

		return 1;
	}

	private static int openCategory(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		String categoryId = StringArgumentType.getString(context, "categoria");

		if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
			source.sendError(Text.literal("Este comando só pode ser usado por jogadores."));
			return 0;
		}

		ShopCategory category = ShopManager.getCategory(categoryId);

		if (category == null) {
			player.sendMessage(Text.literal("§cCategoria não encontrada: " + categoryId), false);
			return 0;
		}

		player.sendMessage(Text.literal("§8§m                                          "), false);
		player.sendMessage(Text.literal("§6§l").append(category.getName())
				.append(Text.literal(" §8(" + category.getEntries().size() + " itens)")), false);
		player.sendMessage(Text.literal("§7Dinheiro virtual ($)"), false);
		player.sendMessage(Text.literal("§8§m                                          "), false);

		int shown = 0;
		for (int i = 0; i < category.getEntries().size(); i++) {
			if (shown >= 12) {
				player.sendMessage(Text.literal("§7... e mais " + (category.getEntries().size() - 12) + " itens."), false);
				player.sendMessage(Text.literal("§7Use §e/loja " + categoryId + " comprar <nº>"), false);
				break;
			}

			ShopEntry entry = category.getEntries().get(i);
			ItemStack stack = entry.getStack();

			MutableText line = Text.literal("§e[" + i + "] §f" + stack.getName().getString())
					.append(Text.literal(" §a$" + entry.getBuyPrice()).formatted(Formatting.GREEN))
					.append(Text.literal(" §7/ §c$" + entry.getSellPrice()).formatted(Formatting.RED));

			final int index = i;
			line.setStyle(Style.EMPTY
					.withClickEvent(new ClickEvent.RunCommand("/loja " + categoryId + " comprar " + index))
					.withHoverEvent(new HoverEvent.ShowText(
							Text.literal("§aClique para COMPRAR por " + entry.getBuyPrice() + " dinheiro\n§cShift+clique ou use vender para vender")
					)));

			player.sendMessage(line, false);
			shown++;
		}

		player.sendMessage(Text.literal(""), false);
		player.sendMessage(Text.literal("§7Voltar: §e/loja  §8|  §7Comprar: §a/loja " + categoryId + " comprar <nº>"), false);
		player.sendMessage(Text.literal("§7Vender: §c/loja " + categoryId + " vender <nº>"), false);
		player.sendMessage(Text.literal("§8§m                                          "), false);

		return 1;
	}

	private static int buyItem(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		String categoryId = StringArgumentType.getString(context, "categoria");
		int index = IntegerArgumentType.getInteger(context, "index");

		if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
			source.sendError(Text.literal("Apenas jogadores."));
			return 0;
		}

		ShopCategory category = ShopManager.getCategory(categoryId);
		if (category == null || index < 0 || index >= category.getEntries().size()) {
			player.sendMessage(Text.literal("§cItem inválido."), false);
			return 0;
		}

		ShopEntry entry = category.getEntries().get(index);
		Text result = EconomyManager.tryBuy(player, entry.getStack(), entry.getBuyPrice());
		player.sendMessage(result, false);
		return 1;
	}

	private static int sellItem(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		String categoryId = StringArgumentType.getString(context, "categoria");
		int index = IntegerArgumentType.getInteger(context, "index");

		if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
			source.sendError(Text.literal("Apenas jogadores."));
			return 0;
		}

		ShopCategory category = ShopManager.getCategory(categoryId);
		if (category == null || index < 0 || index >= category.getEntries().size()) {
			player.sendMessage(Text.literal("§cItem inválido."), false);
			return 0;
		}

		ShopEntry entry = category.getEntries().get(index);
		Text result = EconomyManager.trySell(player, entry.getStack(), entry.getSellPrice());
		player.sendMessage(result, false);
		return 1;
	}
}
