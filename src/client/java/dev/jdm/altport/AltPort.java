package dev.jdm.altport;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;

public class AltPort implements ClientModInitializer {
	private static AltPort instance;

	private boolean useFailover = true;

	private ServerAddress serverAddress;
	private ServerInfo serverInfo;
	public static final String MOD_ID = "altport";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static AltPort getInstance() {
		return instance;
	}

	public static boolean canRetry(){
		return instance.useFailover;

	}

	public void retry() throws NoSuchFieldException, IllegalAccessException {
		int FailoverPort = 0;
		if (Arrays.stream(serverInfo.getClass().getFields()).anyMatch(f -> f.getName().equals("failoverPort"))) {
			System.out.println(ToStringBuilder.reflectionToString(instance));
			Field field = serverInfo.getClass().getDeclaredField("failoverPort");
			FailoverPort = (Integer) field.get(serverInfo);
			if (FailoverPort != -1 && StringUtils.countMatches(serverInfo.address, ":") == 1)

				serverInfo.address = serverAddress.getAddress()+":" + FailoverPort;
				serverAddress = ServerAddress.parse(serverInfo.address);
		}

		System.out.println("retry on port " + FailoverPort);
		ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), MinecraftClient.getInstance(), serverAddress, serverInfo,false);

		getInstance().useFailover = false;
	}


	public void tryConnect(ServerAddress address, ServerInfo info){
		getInstance().serverAddress = address;
		getInstance().serverInfo = info;
		try {
			Field field = info.getClass().getDeclaredField("failoverPort");
			int failoverPort = (Integer) field.get(info);
			if (failoverPort != -1 && address.getPort() != failoverPort)
				getInstance().useFailover = true;

		}catch(Exception ex){
			LOGGER.warn(ex.getMessage()); //TODO: better error handling!
		}
	}

	@Override
	public void onInitializeClient() {
		instance = this;
	}
}
