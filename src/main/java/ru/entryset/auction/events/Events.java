package ru.entryset.auction.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.entryset.api.tag.ItemTag;
import ru.entryset.auction.auction.*;
import ru.entryset.auction.main.Main;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
public class Events implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(!(e.getPlayer() instanceof Player)){
            return;
        }
        Main.auction_menus.remove((Player) e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Main.auction_menus.remove(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e){
        Main.auction_menus.remove(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)){
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if(!Main.auction_menus.containsKey(player)){
            return;
        }
        e.setCancelled(true);
        Auction auction = Main.auction_menus.get(player);
        if(e.getClickedInventory() == null){
            return;
        }
        if(!e.getClickedInventory().equals(auction.getInventory())){
            return;
        }
        if(auction.getType() != AuctionType.DEFAULT){
            if(e.getSlot() > 44){
                if(e.getSlot() == 45){
                    auction.updateInventory();
                    return;
                }
                if(e.getSlot() == 48){
                    if(auction.getPage() > 1){
                        auction.setPage(auction.getPage()-1);
                        if(auction.getType() == AuctionType.EXPIRED){
                            auction.updateInventoryExpired();
                        } else {
                            auction.updateInventorySell();
                        }
                    }
                    return;
                }
                if(e.getSlot() == 50){
                    if(auction.getPage() < auction.getMaxPage()){
                        auction.setPage(auction.getPage()+1);
                        if(auction.getType() == AuctionType.EXPIRED){
                            auction.updateInventoryExpired();
                        } else {
                            auction.updateInventorySell();
                        }
                    }
                }
                return;
            }
            if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR){
                return;
            }
            UUID id = null;
            try {
                id = UUID.fromString(ItemTag.getTag(Main.getInstance(), e.getCurrentItem(), Main.AUCTION_ITEM));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException noSuchMethodException) {
                noSuchMethodException.printStackTrace();
            }
            if(!Main.auction.containsKey(id)){
                auction.update();
                if(auction.getType() == AuctionType.EXPIRED){
                    auction.updateInventoryExpired();
                } else {
                    auction.updateInventorySell();
                }
                return;
            }
            AuctionItem item = Main.auction.get(id);
            item.returnItem(player);
            if(auction.getType() == AuctionType.EXPIRED){
                auction.update();
                auction.updateInventoryExpired();
            } else {
                auction.update();
                auction.updateInventorySell();
            }
            return;
        }
        if(e.getSlot() > 44){
            switch (e.getSlot()){
                case 53:
                    if(e.getClick() == ClickType.LEFT){
                        auction.nextCategory();
                    } else {
                        auction.lastCategory();
                    }
                    auction.sort();
                    auction.updateInventory();
                    break;
                case 52:
                    if(e.getClick() == ClickType.LEFT){
                        auction.nextSort();
                    } else {
                        auction.lastSort();
                    }
                    auction.sort();
                    auction.updateInventory();
                    break;
                case 47:
                    auction.update();
                    auction.updateInventory();
                    break;
                case 46:
                    auction.updateInventoryExpired();
                    break;
                case 45:
                    auction.updateInventorySell();
                    break;
                case 48:
                    if(auction.getPage() > 1){
                        auction.setPage(auction.getPage()-1);
                        auction.updateInventory();
                    }
                    break;
                case 50:
                    if(auction.getPage() < auction.getMaxPage()){
                        auction.setPage(auction.getPage()+1);
                        auction.updateInventory();
                    }
                    break;
                default:
                    break;
            }
            return;
        }
        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR){
            return;
        }
        UUID id = null;
        try {
            id = UUID.fromString(ItemTag.getTag(Main.getInstance(), e.getCurrentItem(), Main.AUCTION_ITEM));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException noSuchMethodException) {
            noSuchMethodException.printStackTrace();
        }
        if(!Main.auction.containsKey(id)){
            Main.messager.sendMessage(player, Main.config.getMessage("no_item"));
            auction.update();
            auction.updateInventory();
            return;
        }
        AuctionItem item = Main.auction.get(id);
        if(e.getClick() == ClickType.MIDDLE){
            if(player.hasPermission("*")){
                AuctionUtils.removeAuctionItem(item.getUuid());
                return;
            }
        }
        if(e.getCurrentItem().getAmount() != item.getSize()){
            auction.update();
            auction.updateInventory();
        }
        if(!item.isTime()){
            auction.update();
            auction.updateInventory();
            Main.messager.sendMessage(player, Main.config.getMessage("istime"));
            return;
        }
        if(item.getSeller().equalsIgnoreCase(player.getName())){
            Main.messager.sendMessage(player, Main.config.getMessage("his"));
            return;
        }
        if(item.getSellType() == SellType.DEFAULT){
            if(e.getClick() == ClickType.LEFT) {
                if(!item.hasMoney(player)){
                    Main.messager.sendMessage(player, Main.config.getMessage("no_money_bay"));
                    return;
                }
                item.bayOfFull(player);
                auction.update();
                auction.updateInventory();
            } else if(e.getClick() == ClickType.RIGHT){
                if(!item.hasMoneyOne(player)){
                    Main.messager.sendMessage(player, Main.config.getMessage("no_money_bay"));
                    return;
                }
                item.bay(player);
                auction.update();
                auction.updateInventory();
            }
        } else {
            if (!item.hasMoney(player)) {
                Main.messager.sendMessage(player, Main.config.getMessage("no_money_bay"));
                return;
            }
            item.bayOfFull(player);
            auction.update();
            auction.updateInventory();
        }
    }
}
