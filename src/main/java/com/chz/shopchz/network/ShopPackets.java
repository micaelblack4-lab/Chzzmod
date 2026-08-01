package com.chz.shopchz.network;

import com.chz.shopchz.ShopCHZ;
import net.minecraft.util.Identifier;

public class ShopPackets {
	public static final Identifier BUY_PACKET = Identifier.of(ShopCHZ.MOD_ID, "buy");
	public static final Identifier SELL_PACKET = Identifier.of(ShopCHZ.MOD_ID, "sell");
}
