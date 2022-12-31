package ru.entryset.auction.auction;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.entryset.api.tools.Messager;
import ru.entryset.auction.item.Controller;
import ru.entryset.auction.item.Convector;
import ru.entryset.auction.main.Main;

import java.util.*;

public class Auction {

    private Inventory inventory;

    private Player player;

    private String target = null;

    private String sort;

    private String category;

    private List<AuctionItem> list;

    private List<AuctionItem> sortList;

    private List<AuctionItem> expired;

    private List<AuctionItem> sale;

    private int page = 1;

    private int max_page;

    private AuctionType type;

    public Auction(Player player){
        setPlayer(player);
        setSort(Main.config.getSettings("select_sort").toLowerCase());
        setCategory(Main.config.getSettings("select_category").toLowerCase());
        player.closeInventory();
        update();
        updateInventory();
        Main.auction_menus.put(player, this);
    }

    public Auction(Player player, String target){
        setPlayer(player);
        setTarget(target);
        setSort(Main.config.getSettings("select_sort").toLowerCase());
        setCategory(Main.config.getSettings("select_category").toLowerCase());
        player.closeInventory();
        update();
        updateInventory();
        Main.auction_menus.put(player, this);
    }

    public void nextCategory(){
        List<String> arr = new ArrayList<>(Main.config.getConfigurationSection("settings.categories").getKeys(false));
        int i = arr.indexOf(getCategory());
        if(i < (arr.size()-1)){
            i++;
        } else {
            i = 0;
        }
        setCategory(arr.get(i));
    }

    public void lastCategory(){
        List<String> arr = new ArrayList<>(Main.config.getConfigurationSection("settings.categories").getKeys(false));
        int i = arr.indexOf(getCategory());
        if(i > 0){
            i--;
        } else {
            i = (arr.size() - 1);
        }
        setCategory(arr.get(i));
    }

    public void nextSort(){
        List<String> arr = Arrays.asList("fresh", "old", "cheap", "expensive");
        int i = arr.indexOf(getSort());
        if(i < (arr.size()-1)){
            i++;
        } else {
            i = 0;
        }
        setSort(arr.get(i));
    }

    public void lastSort(){
        List<String> arr = Arrays.asList("fresh", "old", "cheap", "expensive");
        int i = arr.indexOf(getSort());
        if(i > 0){
            i--;
        } else {
            i = (arr.size() - 1);
        }
        setSort(arr.get(i));
    }

    public void sort(){
        sortOfCategory();
        sortOfSort();
        updateExpired();
        updateSale();
    }


    public void updateInventory(){
        int pages = 1;
        int inner2 = 0;
        HashMap<Integer, List<AuctionItem>> map = new HashMap<>();
        for(int x = 0; x < getSortList().size(); x++){
            if(inner2 == 45) {
                inner2 = 0;
                pages++;
            } else {
                inner2++;
            }
            if(!map.containsKey(pages)){
                map.put(pages, new ArrayList<>());
            }
            map.get(pages).add(getSortList().get(x));
        }
        setMaxPage(pages);
        if(getPage() > getMaxPage()){
            setPage(getMaxPage());
        }
        setInventory(Bukkit.createInventory(getPlayer(), 9*6, Messager.color(Main.config.getSettings("auction_title"))
        .replace("<page>", getPage() + "").replace("<max>", getMaxPage() + "")));
        for(int inner = 0; inner <= 44; inner++){
            if(map.containsKey(getPage())){
                if(map.get(getPage()).size() > inner){
                    getInventory().setItem(inner, map.get(getPage()).get(inner).getAuctionItem());
                } else {
                    break;
                }
            }
        }
        if(getPage() < getMaxPage()){
            getInventory().setItem(50, Controller.getItem("nextPage"));
        }
        if(getPage() > 1){
            getInventory().setItem(48, Controller.getItem("lastPage"));
        }
        getInventory().setItem(53, getCategoryItem());
        getInventory().setItem(52, getSortItem());
        getInventory().setItem(51, Controller.getItem("info"));
        getInventory().setItem(49, Controller.getItem("help"));
        getInventory().setItem(47, Controller.getItem("update"));
        getInventory().setItem(46, Controller.getItem("sellExpired"));
        getInventory().setItem(45, Controller.getItem("sellItems"));
        getPlayer().openInventory(getInventory());
        setType(AuctionType.DEFAULT);
        Main.auction_menus.put(player, this);
    }

    private ItemStack getCategoryItem(){
        ItemStack sort = Controller.getItem("category");
        ItemMeta meta = sort.getItemMeta();
        List<String> lore = new ArrayList<>();
        if(meta != null && meta.hasLore()){
            lore = meta.getLore();
        }
        for(String sec : Main.config.getConfigurationSection("settings.categories").getKeys(false)){
            if(getCategory().equalsIgnoreCase(sec)){
                lore.add(Messager.color(Main.config.getSettings("select").replace("<arg>", Main.config.getSettings("categories." + sec + ".name"))));
            } else {
                lore.add(Messager.color(Main.config.getSettings("no_select").replace("<arg>", Main.config.getSettings("categories." + sec + ".name"))));
            }
        }
        meta.setLore(lore);
        sort.setItemMeta(meta);
        return sort;
    }

    public ItemStack getSortItem(){
        ItemStack sort = Controller.getItem("sort");
        ItemMeta meta = sort.getItemMeta();
        List<String> lore = new ArrayList<>();
        if(meta != null && meta.hasLore()){
            lore = meta.getLore();
        }
        String select = Main.config.getSettings("select");
        String no_select = Main.config.getSettings("no_select");
        if(getSort().equalsIgnoreCase("fresh")){
            lore.add(Messager.color(select.replace("<arg>", Main.config.getSettings("sorting.fresh"))));
        } else {
            lore.add(Messager.color(no_select.replace("<arg>", Main.config.getSettings("sorting.fresh"))));
        }
        if(getSort().equalsIgnoreCase("old")){
            lore.add(Messager.color(select.replace("<arg>", Main.config.getSettings("sorting.old"))));
        } else {
            lore.add(Messager.color(no_select.replace("<arg>", Main.config.getSettings("sorting.old"))));
        }
        if(getSort().equalsIgnoreCase("cheap")){
            lore.add(Messager.color(select.replace("<arg>", Main.config.getSettings("sorting.cheap"))));
        } else {
            lore.add(Messager.color(no_select.replace("<arg>", Main.config.getSettings("sorting.cheap"))));
        }
        if(getSort().equalsIgnoreCase("expensive")){
            lore.add(Messager.color(select.replace("<arg>", Main.config.getSettings("sorting.expensive"))));
        } else {
            lore.add(Messager.color(no_select.replace("<arg>", Main.config.getSettings("sorting.expensive"))));
        }
        meta.setLore(lore);
        sort.setItemMeta(meta);
        return sort;
    }

    public void updateInventorySell(){
        int pages = 1;
        int inner = 0;
        HashMap<Integer, List<AuctionItem>> map2 = new HashMap<>();
        for(int x = 0; x < getSale().size(); x++){
            if(inner == 45) {
                inner = 0;
                pages++;
            } else {
                inner++;
            }
            if(!map2.containsKey(pages)){
                map2.put(pages, new ArrayList<>());
            }
            map2.get(pages).add(getSale().get(x));
        }
        setMaxPage(pages);
        if(getPage() > getMaxPage()){
            setPage(getMaxPage());
        }
        setInventory(Bukkit.createInventory(getPlayer(), 9*6, Messager.color(Main.config.getSettings("auction_title_sell"))
                .replace("<page>", getPage() + "").replace("<max>", getMaxPage() + "")));

        for(int inner2 = 0; inner2 <= 44; inner2++){
            if(map2.containsKey(getPage())){
                if(map2.get(getPage()).size() > inner2){
                    getInventory().setItem(inner2, map2.get(getPage()).get(inner2).getAuctionItemSell());
                } else {
                    break;
                }
            }
        }

        if(getPage() < getMaxPage()){
            getInventory().setItem(50, Controller.getItem("nextPage"));
        }
        if(getPage() > 1){
            getInventory().setItem(48, Controller.getItem("lastPage"));
        }
        getInventory().setItem(45, Controller.getItem("back"));
        getInventory().setItem(53, Controller.getItem("infoSell"));
        setType(AuctionType.SELL);
        getPlayer().openInventory(getInventory());
        Main.auction_menus.put(player, this);
    }

    public void updateInventoryExpired(){
        int pages = 1;
        int inner = 0;
        HashMap<Integer, List<AuctionItem>> map2 = new HashMap<>();
        for(int x = 0; x < getExpired().size(); x++){
            if(inner == 45) {
                inner = 0;
                pages++;
            } else {
                inner++;
            }
            if(!map2.containsKey(pages)){
                map2.put(pages, new ArrayList<>());
            }
            map2.get(pages).add(getExpired().get(x));
        }
        setMaxPage(pages);
        if(getPage() > getMaxPage()){
            setPage(getMaxPage());
        }
        setInventory(Bukkit.createInventory(getPlayer(), 9*6, Messager.color(Main.config.getSettings("auction_title_expired"))
                .replace("<page>", getPage() + "").replace("<max>", getMaxPage() + "")));
        for(int inner2 = 0; inner2 <= 44; inner2++){
            if(map2.containsKey(getPage())){
                if(map2.get(getPage()).size() > inner2){
                    getInventory().setItem(inner2, map2.get(getPage()).get(inner2).getAuctionItemExpired());
                } else {
                    break;
                }
            }
        }

        if(getPage() < getMaxPage()){
            getInventory().setItem(50, Controller.getItem("nextPage"));
        }
        if(getPage() > 1){
            getInventory().setItem(48, Controller.getItem("lastPage"));
        }
        getInventory().setItem(45, Controller.getItem("back"));
        getInventory().setItem(53, Controller.getItem("infoExpired"));
        getPlayer().openInventory(getInventory());
        setType(AuctionType.EXPIRED);
        Main.auction_menus.put(player, this);
    }

    public void setType(AuctionType type) {
        this.type = type;
    }

    public AuctionType getType() {
        return type;
    }

    public void update(){
        if(getTarget() == null){
            setList(new ArrayList<>(Main.auction.values()));
        } else {
            List<AuctionItem> list = new ArrayList<>();
            for(AuctionItem key : new ArrayList<>(Main.auction.values())){
                if(getTarget().equalsIgnoreCase(key.getSeller())){
                    list.add(key);
                }
            }
            setList(list);
        }
        sort();
    }

    public void updateSale(){
        List<AuctionItem> list = new ArrayList<>();
        for(AuctionItem item : getList()){
            if(item.getSeller().equalsIgnoreCase(getPlayer().getName())){
                if(item.isTime()){
                    list.add(item);
                }
            }
        }
        setSale(list);
    }

    public void updateExpired(){
        List<AuctionItem> list = new ArrayList<>();
        for(AuctionItem item : getList()){
            if(item.getSeller().equalsIgnoreCase(getPlayer().getName())){
                if(!item.isTime()){
                    list.add(item);
                }
            }
        }
        setExpired(list);
    }

    public void sortOfCategory(){
        List<AuctionItem> items = new ArrayList<>();
        List<String> categoryList = Main.config.getStringList("settings.categories." + getCategory() + ".items");
        for(AuctionItem item : getList()){
            if(item.isTime()){
                if(getCategory().equalsIgnoreCase("all")){
                    items.add(item);
                } else {
                    if(categoryList.contains(Convector.deserializeItemStack(item.getStack()).getType().name())){
                        items.add(item);
                    }
                }
            }
        }
        setSortList(items);
    }

    public void sortOfSort(){
        switch (getSort()) {
            case "expensive":
                List<AuctionItem> reversed = ImmutableList.copyOf(sortCheap()).reverse();
                setSortList(reversed);
                break;
            case "old":
                setSortList(sortFresh());
                break;
            case "cheap":
                setSortList(sortCheap());
                break;
            default:
                List<AuctionItem> reversed2 = ImmutableList.copyOf(sortFresh()).reverse();
                setSortList(reversed2);
                break;
        }
    }

    private List<AuctionItem> sortCheap(){
        Comparator<AuctionItem> comparator = Comparator.comparing(AuctionItem::getPriceSort);
        List<AuctionItem> list = new ArrayList<>(getSortList());
        list.sort(comparator);
        return list;
    }

    private List<AuctionItem> sortFresh(){
        Comparator<AuctionItem> comparator = Comparator.comparing(AuctionItem::getTime);
        List<AuctionItem> list = new ArrayList<>(getSortList());
        list.sort(comparator);
        return list;
    }

    public void setSortList(List<AuctionItem> sortList) {
        this.sortList = sortList;
    }

    public void setExpired(List<AuctionItem> expired) {
        this.expired = expired;
    }

    public void setSale(List<AuctionItem> sale) {
        this.sale = sale;
    }

    public List<AuctionItem> getExpired() {
        return expired;
    }

    public int getMaxPage() {
        return max_page;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setMaxPage(int max_page) {
        this.max_page = max_page;
    }

    public List<AuctionItem> getSale() {
        return sale;
    }

    public List<AuctionItem> getSortList() {
        return sortList;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public String getTarget() {
        return target;
    }

    public String getCategory() {
        return category;
    }

    public String getSort() {
        return sort;
    }

    public void setList(List<AuctionItem> list) {
        this.list = list;
    }

    public List<AuctionItem> getList() {
        return list;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
