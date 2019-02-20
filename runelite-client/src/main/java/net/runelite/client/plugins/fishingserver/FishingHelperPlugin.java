package net.runelite.client.plugins.fishingserver;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.HashMap;

@PluginDescriptor(
	name = "Fishing server",
	description = "Run a simple server exposing on-screen position of fishing spots.",
	tags = {"overlay", "skilling"}
)
@Singleton
@Slf4j
public class FishingHelperPlugin extends Plugin
{
	private HashMap<Integer, NPC> minnowSpots = new HashMap<>();

	private HashMap<Integer, NPC> fishingSpots = new HashMap<>();

	@Inject
	private FishingHelperServer server;

	@Inject
	private Client client;

	@Inject
	private Notifier notifier;

	@Inject
	private FishingHelperConfig config;

	@Provides
	FishingHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FishingHelperConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Fishing server started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		fishingSpots.clear();
		minnowSpots.clear();
		server.clear();
	}


	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.FILTERED)
		{
			return;
		}

		if (event.getMessage().contains("You catch a") || event.getMessage().contains("You catch some") ||
			event.getMessage().equals("Your cormorant returns with its catch."))
		{
			server.setLastFishCaught(Instant.now());
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		final Actor target = event.getTarget();

		if (!(target instanceof NPC))
		{
			return;
		}

		final NPC npc = (NPC) target;
		FishingHelperSpot spot = FishingHelperSpot.getSPOTS().get(npc.getId());

		if (spot == null)
		{
			return;
		}

		log.info("We are fishing at spot {}", npc);
		server.setTarget(npc);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		int id = npc.getIndex();
		NPC minnowSpot = minnowSpots.remove(id);
		if (minnowSpot != null)
		{
			log.info("Minnow spot {} despawned", minnowSpot);
			server.setMinnowSpots(minnowSpots);
		}
		NPC fishingSpot = fishingSpots.remove(id);
		if (fishingSpot != null)
		{
			log.info("Generic fishing spot {} despawned", fishingSpot);
			server.setFishingSpots(fishingSpots);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
//		log.info("NPC {} spawned", npc);
		int id = npc.getId();
		FishingHelperSpot spot = FishingHelperSpot.getSPOTS().get(id);
		if (spot != null) {
			if (spot == FishingHelperSpot.MINNOW) {
//				minnowSpots.put(id, new MinnowSpot(npc.getWorldLocation(), Instant.now()));
				minnowSpots.put(id, npc);
				log.info("Minnow spot {} spawned", npc);
			} else {
				fishingSpots.put(id, npc);
				log.info("Generic fishing spot {} spawned", npc);
			}

			server.setMinnowSpots(minnowSpots);
			server.setFishingSpots(fishingSpots);

		} else {
			return;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// TODO: which component should be responsible for what?
		// every tick, if we have a designated target, check if it has moved
		NPC target = server.getTarget();
		if (target != null) {
			WorldPoint oldPos = server.getLastPosition();
			WorldPoint newPos = target.getWorldLocation();
			if (!newPos.equals(oldPos)) {
				log.info("Our target {} moved {} -> {}", target, oldPos, newPos);
				server.setLastPosition(newPos);
			}

			if (target.getGraphic() == GraphicID.FLYING_FISH) {
				log.info("Target minnow spot is stealing fish");
			}

			server.ping();
		}
	}
}
