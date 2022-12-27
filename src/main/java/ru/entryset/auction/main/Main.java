package ru.entryset.auction.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.persistence.PersistentDataContainer;
//import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;
import ru.entryset.api.configuration.Config;
import ru.entryset.api.configuration.Configuration;
import ru.entryset.api.database.Database;
import ru.entryset.api.sync.redis.Redis;
import ru.entryset.api.tools.Logger;
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

    public static String AUCTION_ITEM = "AUCTION_ITEM";

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
                    Logger logger = new Logger(Main.getInstance());
                    logger.enable(channel + "|" + message);
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

//    public static void addTag(ItemStack stack, String type){
//        if(Messager.getVersion() > 13){
//            ItemMeta meta = stack.getItemMeta();
//            Objects.requireNonNull(meta).getPersistentDataContainer().set(Main.getInstance().AUCTION_ITEM, PersistentDataType.STRING, type);
//            stack.setItemMeta(meta);
//            return;
//        }
//        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
//        NBTTagCompound compound = (nms.hasTag()) ? nms.getTag() : new NBTTagCompound();
//        Objects.requireNonNull(compound).set("AUCTION_ITEM", new NBTTagString(type));
//        nms.setTag(compound);
//        stack = CraftItemStack.asBukkitCopy(nms);
//    }
//
//
//    //C:\Program Files\Eclipse Adoptium\jdk-17.0.1.12-hotspot
//    public static String getTag(ItemStack stack){
//        if(Messager.getVersion() > 13){
//            ItemMeta meta = stack.getItemMeta();
//            PersistentDataContainer container = Objects.requireNonNull(meta).getPersistentDataContainer();
//            if(container.has(Main.getInstance().AUCTION_ITEM, PersistentDataType.STRING)){
//                return container.get(Main.getInstance().AUCTION_ITEM, PersistentDataType.STRING);
//            }
//            return null;
//        }
//        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
//        NBTTagCompound compound = (nms.hasTag()) ? nms.getTag() : new NBTTagCompound();
//        return Objects.requireNonNull(compound).getString("yourTagHere");
//    }
//
//    public static void addTag(JavaPlugin plugin, ItemStack stack, String key, String source){
//        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(stack);
//        NBTTagCompound compound = (nmsItemStack.hasTag()) ? nmsItemStack.getTag() : new NBTTagCompound();
//        compound.set(key, new NBTTagString(source));
//        nmsItemStack.setTag(compound);
//        stack = CraftItemStack.asBukkitCopy(nmsItemStack);
//    }
//
//    public static ItemStack addTag2(JavaPlugin plugin, ItemStack stack, String key, String source){
//        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(stack);
//        NBTTagCompound compound = (nmsItemStack.hasTag()) ? nmsItemStack.getTag() : new NBTTagCompound();
//        compound.set(key, new NBTTagString(source));
//        nmsItemStack.setTag(compound);
//        return CraftItemStack.asBukkitCopy(nmsItemStack);
//    }
//
//    public static String getTag(JavaPlugin plugin, ItemStack stack, String key){
//        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(stack);
//        NBTTagCompound compound = (nmsItemStack.hasTag()) ? nmsItemStack.getTag() : new NBTTagCompound();
//        return compound.getString(key);
//    }

//    public static void addTag(JavaPlugin plugin, ItemStack stack, String key, String source)
//            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
//
//        if(Messager.getVersion() > 13){
//            ItemMeta meta = stack.getItemMeta();
//            Objects.requireNonNull(meta).getPersistentDataContainer().set(Objects.requireNonNull(NamespacedKey.fromString(key, plugin))
//                    , PersistentDataType.STRING, source);
//            stack.setItemMeta(meta);
//            return;
//        }
//        Logger logger = new Logger(getInstance());
//        logger.warn(key + "|" + source);
//        logger.warn("+");
//        Class<?> nmsCraftItemStack = NMSUtils.getOBC("inventory.CraftItemStack");
//        Class<?> nmsItemStack = NMSUtils.getNMS("ItemStack");
//        Class<?> nmsNBTTagCompound = NMSUtils.getNMS("NBTTagCompound");
//        Class<?> nmsNBTBase = NMSUtils.getNMS("NBTBase");
//        Class<?> nmsNBTTagString = NMSUtils.getNMS("NBTTagString");
//
//        //get object for class net.minecraft.server.<version>.ItemStack
//        Method asNMSCopyMethod = nmsCraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
//        Object objectNMSItemStack = asNMSCopyMethod.invoke(null, stack);
//
//        //get object for class net.minecraft.server.<version>.NBTTagCompound
//        Method hasTagMethod = nmsItemStack.getDeclaredMethod("hasTag", null);
//        Method getTagMethod = nmsItemStack.getDeclaredMethod("getTag", null);
//        Object objectNMSNBTTagCompound = ((boolean)hasTagMethod.invoke(objectNMSItemStack, null))
//                ? getTagMethod.invoke(objectNMSItemStack, null) : nmsNBTTagCompound.newInstance();
//
//        //set tag in net.minecraft.server.<version>.NBTTagCompound
//        Method setMethod = nmsNBTTagCompound.getDeclaredMethod("set", String.class, nmsNBTBase);
//        setMethod.invoke(objectNMSNBTTagCompound, key, nmsNBTTagString.getDeclaredConstructor(String.class).newInstance(source));
//
//        //set net.minecraft.server.<version>.NBTTagCompound in net.minecraft.server.<version>.ItemStack
//        Method setTagMethod = nmsItemStack.getDeclaredMethod("setTag", nmsNBTTagCompound);
//        setTagMethod.invoke(objectNMSItemStack, objectNMSNBTTagCompound);
//
//        //assign net.minecraft.server.<version>.ItemStack to stack
//        Method asBukkitCopyMethod = nmsCraftItemStack.getDeclaredMethod("asBukkitCopy", nmsItemStack);
//        stack = (ItemStack) asBukkitCopyMethod.invoke(null, objectNMSItemStack);
//        logger.warn("++");
//    }
//
//    public static String getTag(JavaPlugin plugin, ItemStack stack, String key)
//            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
//
//        if(Messager.getVersion() > 13){
//            ItemMeta meta = stack.getItemMeta();
//            PersistentDataContainer container = Objects.requireNonNull(meta).getPersistentDataContainer();
//            if(container.has(Objects.requireNonNull(NamespacedKey.fromString(key, plugin)), PersistentDataType.STRING)){
//                return container.get(Objects.requireNonNull(NamespacedKey.fromString(key, plugin)), PersistentDataType.STRING);
//            }
//            return null;
//        }
//        Logger logger = new Logger(getInstance());
//        logger.warn(key);
//        logger.warn("+");
//
//        Class<?> nmsCraftItemStack = NMSUtils.getOBC("inventory.CraftItemStack");
//        Class<?> nmsItemStack = NMSUtils.getNMS("ItemStack");
//        Class<?> nmsNBTTagCompound = NMSUtils.getNMS("NBTTagCompound");
//
//        //get object for class net.minecraft.server.<version>.ItemStack
//        Method asNMSCopyMethod = nmsCraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
//        Object objectNMSItemStack = asNMSCopyMethod.invoke(null, stack);
//
//        //get object for class net.minecraft.server.<version>.NBTTagCompound
//        Method hasTagMethod = nmsItemStack.getDeclaredMethod("hasTag", null);
//        Method getTagMethod = nmsItemStack.getDeclaredMethod("getTag", null);
//        Object objectNMSNBTTagCompound = ((boolean)hasTagMethod.invoke(objectNMSItemStack, null))
//                ? getTagMethod.invoke(objectNMSItemStack, null) : nmsNBTTagCompound.newInstance();
//
//        //get tag from net.minecraft.server.<version>.NBTTagCompound
//        Method getStringMethod = nmsNBTTagCompound.getDeclaredMethod("getString", String.class);
//        String s = (String) getStringMethod.invoke(objectNMSNBTTagCompound, key);
//        logger.warn("++ | " + s);
//        return s;
//    }

}
