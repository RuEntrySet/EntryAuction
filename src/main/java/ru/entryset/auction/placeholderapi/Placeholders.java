package ru.entryset.auction.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import ru.entryset.auction.main.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Placeholders extends PlaceholderExpansion {

    public String onPlaceholderRequest(Player player, String params) {
        if (params.equalsIgnoreCase("kdr")) {
            double kdr = 0;
            if(Main.getInstance().getKdr().containsKey(player.getName())){
                kdr = Main.getInstance().getKdr().get(player.getName());
            }
            return kdr + "";
        }
        if(params.startsWith("size_")){
            int top = (Integer.parseInt(params.replace("size_", "")) - 1);
            Set<String> set = Main.getInstance().getKdr().keySet();
            List<String> list = new ArrayList<>(set);
            if(list.size() <= top){
                return "Не найден";
            }
            String key = list.get(top);
            return Main.getInstance().getKdr().get(key) + "";
        }
        if(params.startsWith("name_")){
            int top = (Integer.parseInt(params.replace("name_", "")) - 1);
            Set<String> set = Main.getInstance().getKdr().keySet();
            List<String> list = new ArrayList<>(set);
            if(list.size() <= top){
                return "Не найден";
            }
            return list.get(top);
        }
        return null;
    }

    public String getIdentifier() {
        return "entryauction";
    }

    public String getAuthor() {
        return "EntrySet";
    }

    public String getVersion() {
        return "1.0.0";
    }
}