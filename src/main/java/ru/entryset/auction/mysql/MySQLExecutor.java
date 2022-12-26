package ru.entryset.auction.mysql;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.entryset.auction.hook.Money;
import ru.entryset.auction.item.Convector;
import ru.entryset.auction.main.Main;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MySQLExecutor {

    public static void createTableProducts() {
        Main.base.execute(Main.getInstance().getResource("deploy.sql"));
    }

    public static void check() {

        Main.base.select("SELECT * FROM `auction`", rs -> {
            while (rs.next()) {
                String hash = rs.getString(2);
                Main.auction = Convector.deserializeMap(hash);
            }
        });
    }

    public static void update(){
        Main.base.select("SELECT * FROM `auction`", rs -> {
            int x = 0;
            while (rs.next()) {
                x++;
            }
            if(x > 0){
                Main.base.update("UPDATE `auction` SET `hash` = ? WHERE `id` = 1", Convector.serializeMap(Main.auction));
            } else {
                Main.base.update("INSERT INTO `auction` (`id`, `hash`) VALUES (?, ?)", 1, Convector.serializeMap(Main.auction));
            }
        });
    }

    public static void update2(){
        int x = Main.base.select("SELECT * FROM `auction`", rs -> {
            int z = 0;
            while (rs.next()) {
                z++;
            }
            return z;
        }).join();
        String serialize = Convector.serializeMap(Main.auction);
        if(x > 0){
            Main.base.update("UPDATE `auction` SET `hash` = ? WHERE `id` = 1", Convector.serializeMap(Main.auction)).join();
        } else {
            Main.base.update("INSERT INTO `auction` (`id`, `hash`) VALUES (?, ?)", 1, Convector.serializeMap(Main.auction)).join();
        }
    }

    public static void addMoney(String player, double money) {
        Main.base.update("INSERT INTO `auction_money` (`id`, `player`, `money`) VALUES (NULL, ?, ?)", player, money);
    }

    public static void checkAll() {

        Main.base.select("SELECT * FROM `auction_money`", rs -> {
            while (rs.next()) {
                int id = rs.getInt(1);
                String user = rs.getString(2);
                double money = rs.getDouble(3);
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {

                    if (Bukkit.getPlayerExact(user) != null) {

                        Player player = Bukkit.getPlayerExact(user);

                        Money money1 = new Money(player);
                        money1.give(money);

                        remove(id);
                    }

                });
            }
        });
    }

    public static void checkTop() {

        String query = "SELECT player, revenue FROM auction_top ORDER BY revenue DESC LIMIT 20";

        HashMap<String, Double> map2 = null;
        try {
            map2 = Main.base.select(query, rs -> {
                HashMap<String, Double> map = new HashMap<>();
                while (rs.next()) {
                    String name = rs.getString(1);
                    double kdr = rs.getDouble(2);
                    map.put(name, kdr);
                }
                return map;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Main.getInstance().setKrd(map2);
    }

    public static void addKdr(String player, double revenue) {
        try {
            double x = Main.base.select("SELECT * FROM `auction_top` WHERE player = ?", rs -> {

                double reuslt = 0;

                while (rs.next()){
                    reuslt++;
                }
                return reuslt;
            }, player).get();

            if(x == 0){
                Main.base.update("INSERT INTO `auction_top` (`player`, `revenue`) VALUES (?, ?)", player, revenue);
                return;
            }
            Main.base.update("UPDATE `auction_top` SET `revenue` = '"+ (getKdr(player) + revenue) + "' WHERE `auction_top`.`player` = ?;", player);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static double getKdr(String player){
        double x = 0;
        try {
            x = Main.base.select("SELECT * FROM `auction_top` WHERE player = ?", rs -> {

                double reuslt = 0;

                while (rs.next()){
                    reuslt = rs.getFloat(2);
                }
                return reuslt;
            }, player).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return x;
    }

    public static void clear(){
        Main.base.update("DELETE FROM `auction_money`");
        Main.base.update("DELETE FROM `auction_top`");
    }

    public static void remove(int id) {
        Main.base.update("DELETE FROM `auction_money` WHERE id = ?", id);
    }
}
