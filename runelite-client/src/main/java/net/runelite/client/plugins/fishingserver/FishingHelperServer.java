package net.runelite.client.plugins.fishingserver;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;

@Slf4j
class FishingHelperServer
{
	private final Client client;

	@Inject
	FishingHelperServer(Client client) {
		lastPosition = new WorldPoint(0,0, 0);
		minnowSpots = new HashMap<>();
		fishingSpots = new HashMap<>();
		this.client = client;
	}

	@Getter
	@Setter
	private Instant lastFishCaught;

	@Getter
	@Setter
	private NPC target;

	@NonNull
	@Getter
	@Setter
	private HashMap<Integer, NPC> minnowSpots;

	@NonNull
	@Getter
	@Setter
	private HashMap<Integer, NPC> fishingSpots;

	@NonNull
	@Getter
	@Setter
	private WorldPoint lastPosition;

	private Point getOnscreenPosition(NPC npc) {
		if (npc == null) return null;

		LocalPoint localPoint = npc.getLocalLocation();
		return Perspective.localToCanvas(client, localPoint, client.getPlane());
	}

	private Polygon getConvexHull(NPC npc) {
		if (npc == null) return null;

		return npc.getConvexHull();
	}

	private FishingSpotPositionData getPositionData(NPC npc) {
		Polygon polygon = getConvexHull(npc);
		Point point = getOnscreenPosition(npc);

		if (polygon == null || point == null) return null;

		return new FishingSpotPositionData(npc.getId(), polygon.npoints, polygon.xpoints, polygon.ypoints, point.getX(), point.getY());
	}

	public void clear() {
		lastFishCaught = null;
		target = null;
		lastPosition = null;
	}

	public void ping() {
		List<FishingSpotPositionData> other = minnowSpots.values().stream()
				.filter(n -> n != target)
				.map(this::getPositionData)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		log.info("Target {} and other visible minnow spots: {}", target, other);

		other.add(getPositionData(target));
		log.info("JSONified:\n{}", gson.toJson(other));
	}
}
