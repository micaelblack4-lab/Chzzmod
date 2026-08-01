package com.chz.shopchz.command;

import com.chz.shopchz.economy.EconomyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MoneyCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
								CommandRegistryAccess registryAccess,
								CommandManager.RegistrationEnvironment environment) {

		dispatcher.register(CommandManager.literal("saldo")
				.executes(MoneyCommand::checkBalance)
		);

		dispatcher.register(CommandManager.literal("dinheiro")
				.executes(MoneyCommand::checkBalance)
				.then(CommandManager.literal("dar")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("jogador", EntityArgumentType.player())
								.then(CommandManager.argument("quantidade", LongArgumentType.longArg(1))
										.executes(MoneyCommand::giveMoney)
								)
						)
				)
				.then(CommandManager.literal("setar")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("jogador", EntityArgumentType.player())
								.then(CommandManager.argument("quantidade", LongArgumentType.longArg(0))
										.executes(MoneyCommand::setMoney)
								)
						)
				)
				.then(CommandManager.literal("remover")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("jogador", EntityArgumentType.player())
								.then(CommandManager.argument("quantidade", LongArgumentType.longArg(1))
										.executes(MoneyCommand::removeMoney)
								)
						)
				)
		);

		dispatcher.register(CommandManager.literal("money")
				.executes(MoneyCommand::checkBalance)
				.then(CommandManager.literal("give")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("jogador", EntityArgumentType.player())
								.then(CommandManager.argument("quantidade", LongArgumentType.longArg(1))
										.executes(MoneyCommand::giveMoney)
								)
						)
				)
				.then(CommandManager.literal("set")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("jogador", EntityArgumentType.player())
								.then(CommandManager.argument("quantidade", LongArgumentType.longArg(0))
										.executes(MoneyCommand::setMoney)
								)
						)
				)
		);
	}

	private static int checkBalance(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
			source.sendError(Text.literal("Apenas jogadores."));
			return 0;
		}

		long balance = EconomyManager.getBalance(player);

		player.sendMessage(Text.literal(""), false);
		player.sendMessage(Text.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
		player.sendMessage(Text.literal("§6§l           ✦ SEU SALDO ✦"), false);
		player.sendMessage(Text.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
		player.sendMessage(Text.literal(""), false);
		player.sendMessage(Text.literal("  §e§l$ " + balance), false);
		player.sendMessage(Text.literal(""), false);
		player.sendMessage(Text.literal("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"), false);
		return 1;
	}

	private static int giveMoney(CommandContext<ServerCommandSource> context) throws Exception {
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "jogador");
		long amount = LongArgumentType.getLong(context, "quantidade");

		EconomyManager.addMoney(target, amount);

		context.getSource().sendFeedback(() ->
				Text.literal("§aVocê deu §e$" + amount + " §apara §e" + target.getName().getString()), true);
		target.sendMessage(Text.literal("§a§l+ \( " + amount + " §arecebido! Saldo: §e \)" + EconomyManager.getBalance(target)), false);
		return 1;
	}

	private static int setMoney(CommandContext<ServerCommandSource> context) throws Exception {
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "jogador");
		long amount = LongArgumentType.getLong(context, "quantidade");

		EconomyManager.setBalance(target, amount);

		context.getSource().sendFeedback(() ->
				Text.literal("§aSaldo de §e" + target.getName().getString() + " §adefinido para §e$" + amount), true);
		target.sendMessage(Text.literal("§aSeu saldo foi definido para §e$" + amount), false);
		return 1;
	}

	private static int removeMoney(CommandContext<ServerCommandSource> context) throws Exception {
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "jogador");
		long amount = LongArgumentType.getLong(context, "quantidade");

		boolean success = EconomyManager.removeMoney(target, amount);

		if (success) {
			context.getSource().sendFeedback(() ->
					Text.literal("§aRemovido §e$" + amount + " §ade §e" + target.getName().getString()), true);
			target.sendMessage(Text.literal("§c§l- \( " + amount + " §cremovido. Saldo: §e \)" + EconomyManager.getBalance(target)), false);
		} else {
			context.getSource().sendError(Text.literal("O jogador não tem saldo suficiente."));
		}
		return success ? 1 : 0;
	}
}
