package net.runelite.client.plugins.fishingserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class FishingSpotPositionData {
	public final int spot_id;
	public final int npoints;
	public final int[] xpoints;
	public final int[] ypoints;
	public final int screenx;
	public final int screeny;

	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		return gson.toJson(this);
	}
}
