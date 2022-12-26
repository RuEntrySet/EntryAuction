package ru.entryset.auction.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.entryset.auction.auction.Auction;
import ru.entryset.auction.auction.AuctionItem;
import ru.entryset.auction.auction.AuctionUtils;
import ru.entryset.auction.auction.SellType;
import ru.entryset.auction.hook.Money;
import ru.entryset.auction.main.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuctionCommand implements CommandExecutor, TabCompleter {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender instanceof Player)){
            Main.messager.sendMessage(sender, Main.config.getMessage("console"));
            return false;
        }
        Player player = (Player) sender;


        if(!Main.messager.hasPermission(player, Main.config.getPermission("use"))){
            return false;
        }
        if(args.length == 0){
            new Auction(player);
            Main.messager.sendMessage(player, Main.config.getMessage("open"));
            return true;
        }
        if(args[0].equalsIgnoreCase("help")){
            for(String str : Main.config.getStringList("messages.help")){
                Main.messager.sendMessage(player, str, false);
            }
            return true;
        }
        if(args[0].equalsIgnoreCase("reload")){
            if(!Main.messager.hasPermission(player, Main.config.getPermission("reload"))){
                return false;
            }
            for(Player player1 : Bukkit.getOnlinePlayers()){
                player1.closeInventory();
            }
            Main.messager.sendMessage(player, Main.config.getMessage("reload"));
            return true;
        }
        if(args[0].equalsIgnoreCase("clear")){
            if(!Main.messager.hasPermission(player, Main.config.getPermission("clear"))){
                return false;
            }
            AuctionUtils.clear();
            Main.messager.sendMessage(player, Main.config.getMessage("clear"));
            return true;
        }
        if(args.length < 2){
            for(String str : Main.config.getStringList("messages.help")){
                Main.messager.sendMessage(player, str, false);
            }
            return false;
        }
        if(args[0].equalsIgnoreCase("search")){
            if(!Main.messager.hasPermission(player, Main.config.getPermission("search"))){
                return false;
            }
            new Auction(player, args[1]);
            Main.messager.sendMessage(player, Main.config.getMessage("search").replace("<target>", args[1]), true);
            return true;
        }
        if(args[0].equalsIgnoreCase("sell")){
            if(!Main.messager.hasPermission(player, Main.config.getPermission("sell"))){
                return false;
            }
            if(AuctionUtils.getSizeProducts(player) >= AuctionUtils.getMaxSizeForPlayer(player)){
                Main.messager.sendMessage(player, Main.config.getMessage("max_items"));
                return false;
            }
            boolean isSet = true;
            int price = 0;
            try {
                price = Integer.parseInt(args[1]);
            } catch (Exception e){
                isSet = false;
            }
            if(!isSet){
                Main.messager.sendMessage(player, Main.config.getMessage("wrong_number"));
                return false;
            }
            Money money = new Money(player);
            if(price < Main.config.getDouble("settings.min_price") || price > Main.config.getDouble("settings.max_price")){
                Main.messager.sendMessage(player, Main.config.getMessage("number_barrier"));
                return false;
            }
            double size = (price * Main.config.getDouble("settings.percent"))/100;
            if(!money.has(size)){
                Main.messager.sendMessage(player, Main.config.getMessage("no_money"));
                return false;
            }
            ItemStack stack = player.getInventory().getItemInMainHand();
            if(stack.getType() == Material.AIR){
                Main.messager.sendMessage(player, Main.config.getMessage("get_item"));
                return false;
            }
            if(Main.config.getStringList("settings.black_list").contains(stack.getType().name())){
                Main.messager.sendMessage(player, Main.config.getMessage("ban_item"));
                return false;
            }
            SellType type = SellType.DEFAULT;
            if(args.length > 2){
                if(args[2].equalsIgnoreCase("full")){
                    type = SellType.FULL;
                }
            }
            money.take(size);
            new AuctionItem(player, stack.clone(), price, type);
            player.getInventory().getItemInMainHand().setAmount(0);
            return true;
        }
        for(String str : Main.config.getStringList("messages.help")){
            Main.messager.sendMessage(player, str, false);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){
            return null;
        }
        Player player = (Player) sender;
        if (args.length == 1){
            List<String> allowed = new ArrayList<>();
            if(player.hasPermission(Main.config.getPermission("use"))){
                if(player.hasPermission(Main.config.getPermission("sell"))){
                    allowed.add("sell");
                }
                if(player.hasPermission(Main.config.getPermission("clear"))){
                    allowed.add("clear");
                }
                if(player.hasPermission(Main.config.getPermission("reload"))){
                    allowed.add("reload");
                }
                if(player.hasPermission(Main.config.getPermission("search"))){
                    allowed.add("search");
                }
                return filter(allowed, args);
            }
            return null;
        }
        if(args.length == 3 && args[0].equalsIgnoreCase("sell") && player.hasPermission(Main.config.getPermission("full"))){
            return filter(Collections.singletonList("full"), args);
        }
        if(args.length == 2 && args[0].equalsIgnoreCase("search") && player.hasPermission(Main.config.getPermission("search"))){
            List<String> list = new ArrayList<>();
            for(Player player1 : Main.getInstance().getServer().getOnlinePlayers()){
                list.add(player1.getName());
            }
            return filter(list, args);
        }
        return null;
    }

    private List<String> filter(List<String> text, String[] args) {
        String last = args[args.length - 1].toLowerCase();
        List<String> result = new ArrayList<>();

        for (String s : text)
            if (s.startsWith(last))
                result.add(s);
        return result;
    }
}
