package ru.entryset.auction.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.entryset.api.tools.Messager;
import ru.entryset.auction.auction.Auction;
import ru.entryset.auction.auction.AuctionType;
import ru.entryset.auction.item.Convector;
import ru.entryset.auction.mysql.MySQLExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Subject{

    synchronized void msg(String user, String base){
        if(Bukkit.getPlayerExact(user) == null){
            return;
        }
        Player player = Bukkit.getPlayerExact(user);
        Main.messager.sendMessage(player, Messager.color(base));
    }

    synchronized void updateItem2(String uuid, String base){
        Main.auction.put(UUID.fromString(uuid), Convector.deserialize(base));
    }

    synchronized void updateItem(String uuid, String base){
        Main.auction.put(UUID.fromString(uuid), Convector.deserialize(base));
        Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () ->{
            List<Auction> list = new ArrayList<>(Main.auction_menus.values());
            for(Auction auction : list){
                if(auction.getType() == AuctionType.DEFAULT){
                    auction.update();
                    auction.updateInventory(); //Тут Main.auction_menus.put();
                }
            }
            MySQLExecutor.update();
            return null;
        });
    }

    synchronized void removeItem(String uuid){
        Main.auction.remove(UUID.fromString(uuid));
        Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () ->{
            List<Auction> list = new ArrayList<>(Main.auction_menus.values());
            for(Auction auction : list){
                if(auction.getType() == AuctionType.DEFAULT){
                    auction.update();
                    auction.updateInventory(); //Тут Main.auction_menus.put();
                }
            }
            MySQLExecutor.update();
            return null;
        });
    }

    synchronized void clear(){
        Main.auction.clear();
        List<Auction> list = new ArrayList<>(Main.auction_menus.values());
        for(Auction auction : list){
            if(auction.getType() == AuctionType.DEFAULT){
                auction.update();
                auction.updateInventory(); //Тут Main.auction_menus.put();
            }
        }
        MySQLExecutor.clear();
    }

}
