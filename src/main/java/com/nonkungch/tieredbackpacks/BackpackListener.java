package com.nonkungch.tieredbackpacks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BackpackListener implements Listener {

    private final TieredBackpacks plugin;

    public BackpackListener(TieredBackpacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBackpackOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // เช็คว่าผู้เล่นคลิกขวาหรือไม่ และถือไอเทมอยู่
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (item == null || !item.hasItemMeta()) return;

        // เช็คว่าเป็นกระเป๋าที่เราสร้างหรือไม่ โดยเช็คจากชื่อและ CustomModelData
        String itemName = item.getItemMeta().getDisplayName();
        int customModelData = item.getItemMeta().getCustomModelData();
        Inventory backpackInv = null;

        if (customModelData == 1 && itemName.equals(TieredBackpacks.smallBackpack.getItemMeta().getDisplayName())) {
            backpackInv = Bukkit.createInventory(null, 9, "§aกระเป๋าเป้ขั้นที่ 1 (เล็ก)");
        } else if (customModelData == 2 && itemName.equals(TieredBackpacks.mediumBackpack.getItemMeta().getDisplayName())) {
            backpackInv = Bukkit.createInventory(null, 27, "§eกระเป๋าเป้ขั้นที่ 2 (กลาง)");
        } else if (customModelData == 3 && itemName.equals(TieredBackpacks.largeBackpack.getItemMeta().getDisplayName())) {
            backpackInv = Bukkit.createInventory(null, 54, "§bกระเป๋าเป้ขั้นที่ 3 (ใหญ่)");
        }

        // ถ้าเป็นกระเป๋า ให้เปิด GUI
        if (backpackInv != null) {
            // (ส่วนนี้ยังไม่ได้เพิ่มระบบเซฟของ จะเพิ่มในเวอร์ชั่นถัดไป)
            // ในเวอร์ชั่นนี้ ของจะหายเมื่อปิดกระเป๋า
            player.openInventory(backpackInv);
            event.setCancelled(true); // ยกเลิก event ปกติของไอเทม
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // ป้องกันการเอากระเป๋าใส่ในกระเป๋าด้วยกันเอง
        String viewTitle = event.getView().getTitle();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // เช็คว่ากำลังเปิด GUI กระเป๋าของเราอยู่หรือไม่
        if (viewTitle.contains("กระเป๋าเป้ขั้นที่")) {
            // เช็คว่าไอเทมที่กำลังจะเอาเข้ามาเป็นกระเป๋าหรือไม่
            if (isBackpack(cursorItem) || isBackpack(clickedItem)) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage("§cคุณไม่สามารถนำกระเป๋าใส่ในกระเป๋าได้!");
            }
        }
    }

    // ฟังก์ชันช่วยเช็คว่าไอเทมเป็นกระเป๋าหรือไม่
    private boolean isBackpack(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return false;
        }
        int modelData = item.getItemMeta().getCustomModelData();
        return modelData >= 1 && modelData <= 3;
    }
}
