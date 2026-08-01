package com.chz.shopchz;

import com.chz.shopchz.gui.ShopScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public class ShopCHZClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ShopCHZ.LOGGER.info("ShopCHZ Client inicializado");

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("loja")
					.executes(context -> {
						MinecraftClient client = MinecraftClient.getInstance();
						if (client.player != null) {
							client.setScreen(new ShopScreen(client.currentScreen));
						}
						return 1;
					})
			);
		});
	}
}
