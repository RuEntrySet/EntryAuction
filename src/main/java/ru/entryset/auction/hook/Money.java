package ru.entryset.auction.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Money {
    private Player player;

    private Economy economy;

    private RegisteredServiceProvider<Economy> rsp;

    public Money(Player player) {
        setPlayer(player);
        this.economy = null;
        this.rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (this.rsp != null)
            this.economy = (Economy)this.rsp.getProvider();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public boolean has(Double amont) {
        return getEconomy().has((OfflinePlayer)getPlayer(), amont);
    }

    public boolean has(Integer amont) {
        return getEconomy().has((OfflinePlayer)getPlayer(), amont.doubleValue());
    }

    public Double get() {
        return getEconomy().getBalance((OfflinePlayer) getPlayer());
    }

    public void set(Double amont) {
        take(get());
        give(amont);
    }

    public void set(int amont) {
        take(get());
        give(amont);
    }

    public void give(Double amont) {
        getEconomy().depositPlayer((OfflinePlayer)getPlayer(), amont);
    }

    public void give(Integer amont) {
        getEconomy().depositPlayer((OfflinePlayer)getPlayer(), amont.doubleValue());
    }

    public void take(Double amont) {
        getEconomy().withdrawPlayer((OfflinePlayer)getPlayer(), amont);
    }

    public void take(int amont) {
        getEconomy().withdrawPlayer((OfflinePlayer)getPlayer(), amont);
    }
}
