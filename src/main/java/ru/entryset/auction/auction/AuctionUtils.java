package ru.entryset.auction.auction;

import org.bukkit.entity.Player;
import ru.entryset.auction.item.Convector;
import ru.entryset.auction.main.Main;

import java.util.UUID;

public class AuctionUtils {

	public static void removeAuctionItem(UUID uuid){
		if(!Main.auction.containsKey(uuid)){
			return;
		}
		AuctionUtils.remove(uuid);
	}

	public static String format(long time) {
		long days = time / 86400;
		long hours = (time % 86400) / 3600;
		long minutes = (time % 3600) / 60;
		long seconds = time % 60;

		StringBuilder sb = new StringBuilder();

		String format = "settings.time.";

		if (days != 0)
			sb.append(Main.config.getString(format + "days").replace("%size%", "" + days)).append(" ");
		if (hours != 0)
			sb.append(Main.config.getString(format + "hours").replace("%size%", "" + hours)).append(" ");
		if (minutes != 0)
			sb.append(Main.config.getString(format + "minute").replace("%size%", "" + minutes)).append(" ");
		if (seconds != 0)
			sb.append(Main.config.getString(format + "seconds").replace("%size%", "" + seconds)).append(" ");

		String str = sb.toString().trim();

		if (str.isEmpty())
			str = Main.config.getString(format + "now");
		return str;
	}

	public static void update(UUID uuid, AuctionItem item){
		Main.redis.push("EntryAuction", "update=" + uuid.toString() + "=" + Convector.serialize(item));
	}

	public static void update2(UUID uuid, AuctionItem item){
		Main.redis.push("EntryAuction", "update2=" + uuid.toString() + "=" + Convector.serialize(item));
	}

	public static void remove(UUID uuid){
		Main.redis.push("EntryAuction", "remove=" + uuid.toString());
	}

	public static void msg(String player, String msg){
		Main.redis.push("EntryAuctionMsg", player + "=" + msg);
	}

	public static void clear(){
		Main.redis.push("EntryAuction", "clear=1");
	}

	public static int getSizeProducts(Player player){
		int size = 0;
		if(!Main.auction.isEmpty()){
			for(AuctionItem key : Main.auction.values()){
				if(key.getSeller().equalsIgnoreCase(player.getName())){
					size++;
				}
			}
		}
		return size;
	}

	public static int getMaxSizeForPlayer(Player player){
		int perm = 0;
		for(int x = 0; x <= 100; x++){
			if(player.hasPermission(Main.config.getPermission("limits").replace("<x>", x + ""))){
				perm = x;
			}
		}
		return perm;
	}

}
