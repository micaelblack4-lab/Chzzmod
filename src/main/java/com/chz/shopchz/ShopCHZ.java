package com.chz.shopchz;

import com.chz.shopchz.command.MoneyCommand;
import com.chz.shopchz.command.ShopCommand;
import com.chz.shopchz.shop.ShopManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopCHZ implements ModInitializer {
	public static final String MOD_ID = "shopchz";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("ShopCHZ carregando... Versão para Minecraft 26.1.2");
		ShopManager.init();
		ServerLifecycleEvents.SERVER_STARTED.register(ShopManager::loadEnchantments);
		CommandRegistrationCallback.EVENT.register(ShopCommand::register);
		CommandRegistrationCallback.EVENT.register(MoneyCommand::register);
		LOGGER.info("ShopCHZ carregado com sucesso!");
	}
}
