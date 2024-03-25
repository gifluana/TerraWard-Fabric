package com.lunazstudios.terraward;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerraWard implements ModInitializer {
	public static final String MOD_NAME = "TerraWard";
	public static final String MOD_ID = "terraward";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}
}