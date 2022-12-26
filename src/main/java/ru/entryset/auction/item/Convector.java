package ru.entryset.auction.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import ru.entryset.auction.auction.AuctionItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class Convector {

    public static HashMap<UUID, AuctionItem> deserializeMap(String item) {
        HashMap<UUID, AuctionItem> stack = null;
        try {
            byte[] serializedObject = Base64.getDecoder().decode(item);
            ByteArrayInputStream io = new ByteArrayInputStream(serializedObject);
            BukkitObjectInputStream os = new BukkitObjectInputStream(io);
            stack = (HashMap<UUID, AuctionItem>) os.readObject();
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        if(stack == null){
            return new HashMap<>();
        }

        return stack;
    }

    public static String serializeMap(HashMap<UUID, AuctionItem> item) {

        String encodedObject = null;

        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(item);
            os.flush();
            byte[] serializedObject = io.toByteArray();
            encodedObject = Base64.getEncoder().encodeToString(serializedObject);
        } catch (IOException e){
            e.printStackTrace();
        }
        return encodedObject;
    }

    public static AuctionItem deserialize(String item) {

        AuctionItem stack = null;
        try {
            byte[] serializedObject = Base64.getDecoder().decode(item);
            ByteArrayInputStream io = new ByteArrayInputStream(serializedObject);
            BukkitObjectInputStream os = new BukkitObjectInputStream(io);
            stack = (AuctionItem) os.readObject();
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return stack;
    }

    public static String serialize(AuctionItem item) {
        String encodedObject = null;

        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(item);
            os.flush();
            byte[] serializedObject = io.toByteArray();
            encodedObject = Base64.getEncoder().encodeToString(serializedObject);
        } catch (IOException e){
            e.printStackTrace();
        }
        return encodedObject;
    }

    public static ItemStack deserializeItemStack(String item) {
        ItemStack stack = null;
        try {
            byte[] serializedObject = Base64.getDecoder().decode(item);
            ByteArrayInputStream io = new ByteArrayInputStream(serializedObject);
            BukkitObjectInputStream os = new BukkitObjectInputStream(io);
            stack = (ItemStack) os.readObject();
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return stack;
    }

    public static String serializeItemStack(ItemStack item) {
        String encodedObject = null;

        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(item);
            os.flush();
            byte[] serializedObject = io.toByteArray();
            encodedObject = Base64.getEncoder().encodeToString(serializedObject);
        } catch (IOException e){
            e.printStackTrace();
        }
        return encodedObject;
    }
}
