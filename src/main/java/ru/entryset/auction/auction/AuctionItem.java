package ru.entryset.auction.auction;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import ru.entryset.api.tag.ItemTag;
import ru.entryset.api.tools.Messager;
import ru.entryset.auction.hook.Money;
import ru.entryset.auction.item.Convector;
import ru.entryset.auction.main.Main;
import ru.entryset.auction.mysql.MySQLExecutor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class AuctionItem implements Serializable {

    private UUID uuid;

    private String seller;

    private String stack;

    private SellType sellType;

    private boolean expired;

    private double price;

    private double priceOfOne;

    private int size;

    private Instant dateOfSale;

    public AuctionItem(Player player, ItemStack stack, int price, SellType type){
        setSeller(player.getName());
        setSellType(type);
        setPrice(price);
        messagePlayer(player, stack);
        setSize(stack.getAmount());
        if(getSellType() == SellType.DEFAULT){
            if(stack.getAmount() == 1){
                setSellType(SellType.FULL);
            } else {
                setPriceOfOne(getPrice()/getSize());
            }
        }
        setStack(Convector.serializeItemStack(stack));
        setDateOfSale(new Date());
        goToChannel();
    }

    public ItemStack getAuctionItem(){
        ItemStack des = Convector.deserializeItemStack(getStack());
        ItemStack st = null;
        try {
            st = ItemTag.setTag(Main.getInstance(), des, Main.AUCTION_ITEM, getUuid().toString());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        ItemMeta meta = st.getItemMeta();
        List<String> lore = new ArrayList<>();
        if(Objects.requireNonNull(meta).hasLore()){
            lore = st.getItemMeta().getLore();
        }
        if(getSellType() == SellType.FULL){
            for(String str : Main.config.getStringList("settings.auction_item_full")){
                lore.add(Messager.color(str
                .replace("<seller>", getSeller()).replace("<max>", getSize() + "")
                        .replace("<price>", getPrice() + "")
                                .replace("<time>", AuctionUtils.format(getTime()) + "")));
            }
        } else {
            for(String str : Main.config.getStringList("settings.auction_item")){
                lore.add(Messager.color(str
                        .replace("<seller>", getSeller()).replace("<max>", getSize() + "")
                        .replace("<price>", getPrice() + "")
                        .replace("<time>", AuctionUtils.format(getTime()) + ""))
                        .replace("<price_one>", getPriceOfOne() + ""));
            }
        }
        meta.setLore(lore);
        st.setItemMeta(meta);
        return st;
    }

    public ItemStack getAuctionItemSell(){
        ItemStack st = getAuctionItem();
        ItemMeta meta = st.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add(Messager.color(Main.config.getSettings("return_item")));
        meta.setLore(lore);
        st.setItemMeta(meta);
        return st;
    }

    public ItemStack getAuctionItemExpired(){
        ItemStack des = Convector.deserializeItemStack(getStack());
        ItemStack st = null;
        try {
            st = ItemTag.setTag(Main.getInstance(), des, Main.AUCTION_ITEM, getUuid().toString());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        ItemMeta meta = st.getItemMeta();
        List<String> lore = new ArrayList<>();
        if(Objects.requireNonNull(meta).hasLore()){
            lore = st.getItemMeta().getLore();
        }
        lore.add(Messager.color("&f"));
        lore.add(Messager.color(Main.config.getSettings("return_item")));
        meta.setLore(lore);
        st.setItemMeta(meta);
        return st;
    }

    public boolean hasMoney(Player bayer){
        Money money = new Money(bayer);
        return money.has(getPrice());
    }

    public boolean hasMoneyOne(Player bayer){
        Money money = new Money(bayer);
        return money.has(getPriceOfOne());
    }

    public long getTime(){
        Instant last = getDateOfSale().toInstant();
        Instant now = Instant.now();
        Duration duration = Duration.between(last, now);
        return ((60L *60*Main.config.getInt("settings.time_item")) - duration.getSeconds());
    }

    public boolean isTime(){
        Instant last = getDateOfSale().toInstant();
        Instant now = Instant.now();
        Duration duration = Duration.between(last, now);
        if (duration.getSeconds() <= (3600L * Main.config.getInt("settings.time_item"))) {
            return true;
        }
        setExpired(true);
        AuctionUtils.update2(getUuid(), this);
        return false;
    }

    public void bay(Player bayer){
        if(!Main.auction.containsKey(getUuid())){
            return;
        }
        if(!isTime()){
            return;
        }
        ItemStack stack = Convector.deserializeItemStack(getStack()).clone();
        stack.setAmount(1);

        giveItem(bayer, stack);
        Main.messager.sendMessage(bayer, Main.config.getMessage("bay_item").replace("<item>", stack.getType().name())
                        .replace("<size>", "1").replace("<price>", getPriceOfOne() + ""));

        AuctionUtils.msg(getSeller(),
                Main.config.getMessage("prefix")
                        + Main.config.getMessage("sold_item").replace("<item>", stack.getType().name())
                        .replace("<size>", "1").replace("<price>", getPriceOfOne() + "")
                        .replace("<bayer>", bayer.getName())
                );

        Money money = new Money(bayer);
        money.take(getPriceOfOne());
        if(Bukkit.getPlayer(getSeller()) != null){
            Money money1 = new Money(Bukkit.getPlayer(getSeller()));
            money1.give(getPriceOfOne());
        } else {
            MySQLExecutor.addMoney(getSeller(), getPriceOfOne());
            MySQLExecutor.addKdr(getSeller(), getPriceOfOne());
        }

        if(getSize() == 1){
            AuctionUtils.remove(getUuid());
        } else {
            setSize(getSize() - 1);
            ItemStack stack1 = Convector.deserializeItemStack(getStack());
            stack1.setAmount(getSize());
            setStack(Convector.serializeItemStack(stack1));
            setPrice(getPrice() - getPriceOfOne());
            setPriceOfOne(getPrice()/getSize());
            AuctionUtils.update(getUuid(),this);
        }
    }

    public void bayOfFull(Player bayer){
        if(!Main.auction.containsKey(getUuid())){
            return;
        }
        AuctionItem item = Main.auction.get(getUuid());
        if(item.isExpired()){
            return;
        }
        AuctionUtils.remove(getUuid());
        ItemStack stack = Convector.deserializeItemStack(getStack());
        giveItem(bayer, stack);
        Main.messager.sendMessage(bayer, Main.config.getMessage("bay_item").replace("<item>", stack.getType().name())
                .replace("<size>", stack.getAmount() + "").replace("<price>", getPrice() + ""));
        AuctionUtils.msg(getSeller(),
                Main.config.getMessage("prefix")
                + Main.config.getMessage("sold_item")
                .replace("<item>", stack.getType().name()).replace("<price>", getPrice() + "")
                .replace("<bayer>", bayer.getName())
                .replace("<size>", stack.getAmount() + "")
        );

        Money money = new Money(bayer);
        money.take(getPrice());

        if(Bukkit.getPlayer(getSeller()) != null){
            Money money1 = new Money(Bukkit.getPlayer(getSeller()));
            money1.give(getPrice());
        } else {
            MySQLExecutor.addMoney(getSeller(), getPrice());
        }
        MySQLExecutor.addKdr(getSeller(), getPrice());
    }

    public void returnItem(Player player){
        if(!Main.auction.containsKey(getUuid())){
            return;
        }
        AuctionUtils.remove(getUuid());
        giveItem(player, Convector.deserializeItemStack(getStack()));
        Main.messager.sendMessage(player, Main.config.getMessage("return_item"));
    }

    private void giveItem(Player player, ItemStack stack){
        PlayerInventory inventory = player.getInventory();
        boolean hasSlot = false;
        for(ItemStack sec : inventory.getContents()){
            if(sec == null || sec.getType() == Material.AIR){
                hasSlot = true;
                break;
            }
        }
        if(hasSlot){
            player.getInventory().addItem(stack);
            return;
        }
        Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(), stack);
    }

    private void goToChannel(){
        setUuid(UUID.randomUUID());
        AuctionUtils.update(getUuid(),this);
    }

    private void messagePlayer(Player player, ItemStack stack){
        Main.messager.sendMessage(player, Main.config.getMessage("sell_item").replace("<item>", stack.getType().name())
        .replace("<size>", stack.getAmount() + "").replace("<price>", getPrice() + ""));
    }

    public int getSize() {
        return size;
    }

    public UUID getUuid() {
        return uuid;
    }

    private void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getStack() {
        return stack;
    }

    public String getSeller() {
        return seller;
    }

    public Date getDateOfSale() {
        return Date.from(dateOfSale);
    }

    public double getPrice() {
        return price;
    }

    public double getPriceOfOne() {
        return priceOfOne;
    }

    public SellType getSellType() {
        return sellType;
    }

    private void setSize(int size) {
        this.size = size;
    }

    private void setStack(String stack) {
        this.stack = stack;
    }

    private void setSeller(String seller) {
        this.seller = seller;
    }

    private void setDateOfSale(Date dateOfSale) {
        this.dateOfSale = dateOfSale.toInstant();
    }

    private void setPrice(double price) {
        this.price = round(price);
    }

    private void setPriceOfOne(double priceOfOne) {
        this.priceOfOne = round(priceOfOne);
    }

    private void setSellType(SellType sellType) {
        this.sellType = sellType;
    }

    private static double round(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Double getPriceSort(){
        double price;
        if(getSellType() == SellType.FULL){
            price = (getPrice()/getSize());
        } else {
            price = getPriceOfOne();
        }
        return price;
    }

}
