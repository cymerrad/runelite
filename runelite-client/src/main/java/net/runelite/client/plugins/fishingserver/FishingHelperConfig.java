package net.runelite.client.plugins.fishingserver;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("fishingserver")
public interface FishingHelperConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "serverPort",
		name = "Port on localhost",
		description = "Local server's port to which to send data."
	)
	default int serverPort()
	{
		return 8042;
	}

	@ConfigItem(
			position = 4,
			keyName = "statTimeout",
			name = "Reset stats (minutes)",
			description = "The time until fishing session data is reset in minutes."
	)
	default int statTimeout()
	{
		return 5;
	}

}
