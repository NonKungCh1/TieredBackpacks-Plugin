package com.nonkungch.tieredbackpacks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class TieredBackpacks extends JavaPlugin {

    // สร้างไอเทมกระเป๋าไว้ใช้ในส่วนต่างๆ ของปลั๊กอิน
    public static ItemStack smallBackpack;
    public static ItemStack mediumBackpack;
    public static ItemStack largeBackpack;

    @Override
    public void onEnable() {
        getLogger().info("TieredBackpacks Plugin has been enabled!");

        // สร้างไอเทมกระเป๋าทั้ง 3 ระดับ
        createBackpackItems();

        // ลงทะเบียนสูตรคราฟและสูตรเผาทั้งหมด
        registerRecipes();

        // ลงทะเบียน Listener เพื่อให้ปลั๊กอินทำงานกับผู้เล่นได้
        Bukkit.getServer().getPluginManager().registerEvents(new BackpackListener(this), this);
    }

    private void createBackpackItems() {
        // --- กระเป๋าขั้นที่ 1 (เล็ก) ---
        smallBackpack = new ItemStack(Material.LEATHER_HORSE_ARMOR); // ใช้ไอเทมที่ไม่ค่อยมีคนใช้เพื่อไม่ให้ซ้ำ
        ItemMeta smallMeta = smallBackpack.getItemMeta();
        smallMeta.setDisplayName("§aกระเป๋าเป้ขั้นที่ 1 (เล็ก)");
        smallMeta.setLore(Arrays.asList("§7ความจุ: 9 ช่อง", "§7คลิกขวาเพื่อเปิด"));
        smallMeta.setCustomModelData(1); // สำหรับ Resource Pack (ถ้ามี)
        smallBackpack.setItemMeta(smallMeta);

        // --- กระเป๋าขั้นที่ 2 (กลาง) ---
        mediumBackpack = new ItemStack(Material.IRON_HORSE_ARMOR);
        ItemMeta mediumMeta = mediumBackpack.getItemMeta();
        mediumMeta.setDisplayName("§eกระเป๋าเป้ขั้นที่ 2 (กลาง)");
        mediumMeta.setLore(Arrays.asList("§7ความจุ: 27 ช่อง", "§7คลิกขวาเพื่อเปิด"));
        mediumMeta.setCustomModelData(2);
        mediumBackpack.setItemMeta(mediumMeta);

        // --- กระเป๋าขั้นที่ 3 (ใหญ่) ---
        largeBackpack = new ItemStack(Material.DIAMOND_HORSE_ARMOR);
        ItemMeta largeMeta = largeBackpack.getItemMeta();
        largeMeta.setDisplayName("§bกระเป๋าเป้ขั้นที่ 3 (ใหญ่)");
        largeMeta.setLore(Arrays.asList("§7ความจุ: 54 ช่อง", "§7คลิกขวาเพื่อเปิด"));
        largeMeta.setCustomModelData(3);
        largeBackpack.setItemMeta(largeMeta);
    }

    private void registerRecipes() {
        // --- สูตรเผา ---
        // เผาเนื้อซอมบี้ -> หนังวัว
        FurnaceRecipe fleshToLeather = new FurnaceRecipe(new NamespacedKey(this, "flesh_to_leather"),
                new ItemStack(Material.LEATHER), Material.ROTTEN_FLESH, 0.5f, 200); // 200 ticks = 10 วินาที
        Bukkit.addRecipe(fleshToLeather);

        // เผาหนังวัว -> หนังกระต่าย
        FurnaceRecipe leatherToHide = new FurnaceRecipe(new NamespacedKey(this, "leather_to_hide"),
                new ItemStack(Material.RABBIT_HIDE), Material.LEATHER, 0.3f, 200);
        Bukkit.addRecipe(leatherToHide);


        // --- สูตรคราฟกระเป๋า ---
        // กระเป๋าขั้นที่ 1
        ShapedRecipe smallRecipe = new ShapedRecipe(new NamespacedKey(this, "small_backpack"), smallBackpack);
        smallRecipe.shape(
                "LLL",
                "LSL",
                "LLL"
        );
        smallRecipe.setIngredient('L', Material.LEATHER);
        smallRecipe.setIngredient('S', Material.STRING);
        Bukkit.addRecipe(smallRecipe);

        // กระเป๋าขั้นที่ 2 (อัปเกรดจากขั้น 1)
        ShapedRecipe mediumRecipe = new ShapedRecipe(new NamespacedKey(this, "medium_backpack"), mediumBackpack);
        mediumRecipe.shape(
                "III",
                "IBI",
                "III"
        );
        mediumRecipe.setIngredient('I', Material.IRON_INGOT);
        // ใช้ RecipeChoice เพื่อให้แน่ใจว่าต้องใช้กระเป๋าที่เราสร้างขึ้นเท่านั้น
        mediumRecipe.setIngredient('B', new RecipeChoice.ExactChoice(smallBackpack));
        Bukkit.addRecipe(mediumRecipe);

        // กระเป๋าขั้นที่ 3 (อัปเกรดจากขั้น 2)
        ShapedRecipe largeRecipe = new ShapedRecipe(new NamespacedKey(this, "large_backpack"), largeBackpack);
        largeRecipe.shape(
                "DDD",
                "DBD",
                "DDD"
        );
        largeRecipe.setIngredient('D', Material.DIAMOND);
        largeRecipe.setIngredient('B', new RecipeChoice.ExactChoice(mediumBackpack));
        Bukkit.addRecipe(largeRecipe);
    }
}
