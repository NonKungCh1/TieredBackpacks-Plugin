package com.nonkungch.tieredbackpacks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InventorySerializer {

    /**
     * แปลง Inventory ให้เป็น Base64 String
     * @param inventory Inventory ที่ต้องการแปลง
     * @return Base64 String
     * @throws IllegalStateException
     */
    public static String inventoryToBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            // บันทึกขนาดของ Inventory
            dataOutput.writeInt(inventory.getSize());
            
            // บันทึกแต่ละไอเทมใน Inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }
            
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
            
        } catch (Exception e) {
            throw new IllegalStateException("ไม่สามารถบันทึก Inventory เป็น Base64 ได้", e);
        }
    }

    /**
     * แปลง Base64 String กลับเป็น ItemStack[]
     * @param data Base64 String
     * @return ItemStack[]
     * @throws IOException
     */
    public static ItemStack[] inventoryFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            
            // อ่านขนาดของ Inventory
            int size = dataInput.readInt();
            ItemStack[] contents = new ItemStack[size];
            
            // อ่านแต่ละไอเทม
            for (int i = 0; i < size; i++) {
                contents[i] = (ItemStack) dataInput.readObject();
            }
            
            dataInput.close();
            return contents;
            
        } catch (ClassNotFoundException e) {
            throw new IOException("ไม่สามารถอ่าน ItemStack[] จาก Base64 ได้", e);
        }
    }
}
