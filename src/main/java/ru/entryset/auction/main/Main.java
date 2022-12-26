package ru.entryset.auction.main;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;
import ru.entryset.api.configuration.Config;
import ru.entryset.api.configuration.Configuration;
import ru.entryset.api.database.Database;
import ru.entryset.api.sync.redis.Redis;
import ru.entryset.api.tools.Messager;
import ru.entryset.auction.auction.Auction;
import ru.entryset.auction.auction.AuctionItem;
import ru.entryset.auction.commands.AuctionCommand;
import ru.entryset.auction.mysql.MySQLExecutor;
import ru.entryset.auction.placeholderapi.Placeholders;
import ru.entryset.auction.events.Events;

import java.util.*;

public class Main extends JavaPlugin {

    private static Main instance;

    public static Database base;

    public static Redis redis;

    public static Config config;

    public static Configuration items;

    public static Messager messager;

    public static HashMap<UUID, AuctionItem> auction = new HashMap<>();

    public static HashMap<Player, Auction> auction_menus = new HashMap<>();

    public NamespacedKey AUCTION_ITEM;

    public HashMap<String, Double> kdr = new HashMap<>();

    public final Subject subject = new Subject();

    @Override
    public void onEnable() {

        instance = this;
        config = new Config(getInstance(), "config.yml");
        items = new Configuration(getInstance(), "items.yml");
        messager = new Messager(config);
        redis = config.getRedis("redis");
        base = config.getMysqlDatabase("mysql");
        base.start();
        MySQLExecutor.createTableProducts();

        registerCommands();
        registerEvents();

        sub();
        check();
        AUCTION_ITEM = new NamespacedKey(this, "AUCTION_ITEM");
        MySQLExecutor.check();
        MySQLExecutor.checkTop();
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        }

    }

    public void check(){
        MySQLExecutor.check();
        new BukkitRunnable() {
            @Override
            public void run() {
                MySQLExecutor.update();
            }
        }.runTaskTimer(Main.getInstance(), 20*60*10, 20*60*10);
        new BukkitRunnable() {
            @Override
            public void run() {
                MySQLExecutor.checkAll();
            }
        }.runTaskTimer(Main.getInstance(), 20*30, 20*30);
        new BukkitRunnable() {
            @Override
            public void run() {
                MySQLExecutor.checkTop();
            }
        }.runTaskTimer(Main.getInstance(), 20*60*10, 20*60*10);
    }

    public void sub(){
        Runnable task = () -> {

            JedisPubSub jedisPubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (channel.equals("EntryAuction")) {
                        String[] parts = message.split("=");
                        switch (parts[0]){
                            case "remove":
                                subject.removeItem(parts[1]);
                                break;
                            case "update":
                                subject.updateItem(parts[1], parts[2]);
                                break;
                            case "clear":
                                subject.clear();
                                break;
                            case "update2":
                                subject.updateItem2(parts[1], parts[2]);
                                break;
                        }
                    }
                }
            };
            Main.config.getRedis("redis").getJedis().subscribe(jedisPubSub, "EntryAuction");
        };
        Thread thread = new Thread(task);
        thread.start();

        Runnable task2 = () -> {
            JedisPubSub jedisPubSub = new JedisPubSub() {

                @Override
                public void onMessage(String channel, String message) {
                    if(channel.equalsIgnoreCase("EntryAuctionMsg")){
                        String[] parts = message.split("=");
                        subject.msg(parts[0], parts[1]);
                    }
                }
            };
            Main.config.getRedis("redis").getJedis().subscribe(jedisPubSub, "EntryAuctionMsg");
        };
        Thread thread2 = new Thread(task2);
        thread2.start();
    }

    @Override
    public void onDisable(){
        MySQLExecutor.update2();
        base.close();
    }

    public HashMap<String, Double> getKdr() {
        return kdr;
    }

    public void setKrd(HashMap<String, Double>  kdr) {
        this.kdr = kdr;
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new Events(), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("ah")).setExecutor(new AuctionCommand());
        Objects.requireNonNull(getCommand("ah")).setTabCompleter(new AuctionCommand());
    }

    public static Main getInstance() {
        return Main.instance;
    }

    public static void addTag(ItemStack stack, String type){
        ItemMeta meta = stack.getItemMeta();
        Objects.requireNonNull(meta).getPersistentDataContainer().set(Main.getInstance().AUCTION_ITEM, PersistentDataType.STRING, type);
        stack.setItemMeta(meta);
    }

    public static String getTag(ItemStack stack){
        ItemMeta meta = stack.getItemMeta();
        PersistentDataContainer container = Objects.requireNonNull(meta).getPersistentDataContainer();
        if(container.has(Main.getInstance().AUCTION_ITEM, PersistentDataType.STRING)){
            return container.get(Main.getInstance().AUCTION_ITEM, PersistentDataType.STRING);
        }
        return null;
    }

}
