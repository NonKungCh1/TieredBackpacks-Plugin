package com.nonkungch.tieredbackpacks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
// *** เพิ่ม InventoryCloseEvent ***
import org.bukkit.event.inventory.InventoryCloseEvent; 
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
// *** เพิ่ม PersistentDataContainer & ItemMeta ***
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

// *** Custom InventoryHolder: ใช้สำหรับระบุว่า Inventory นี้เป็นของกระเป๋าไหน ***
// *วิธีนี้ดีกว่าการใช้แค่ชื่อ Title และช่วยให้เราเข้าถึงไอเทมกระเป๋าที่เปิดอยู่ได้โดยตรง*
class BackpackHolder implements org.bukkit.inventory.InventoryHolder {
    private final ItemStack backpackItem;
    private final Inventory inventory;

    public BackpackHolder(ItemStack backpackItem, int size, String title) {
        this.backpackItem = backpackItem;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack getBackpackItem() {
        return backpackItem;
    }
}


public class BackpackListener implements Listener {

    private final TieredBackpacks plugin;

    public BackpackListener(TieredBackpacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBackpackOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItem();

        // เช็คว่าผู้เล่นคลิกขวาหรือไม่ และถือไอเทมอยู่
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (itemInHand == null || !itemInHand.hasItemMeta()) return;

        // ดึง ItemMeta และ PDC
        ItemMeta meta = itemInHand.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        // เช็คว่าเป็นกระเป๋าที่เราสร้างหรือไม่ โดยเช็คจาก CustomModelData
        int customModelData = meta.getCustomModelData();
        int inventorySize = 0;
        String backpackTitle = null;

        if (customModelData == 1 && meta.getDisplayName().equals(TieredBackpacks.smallBackpack.getItemMeta().getDisplayName())) {
            inventorySize = 9;
            backpackTitle = "§aกระเป๋าเป้ขั้นที่ 1 (เล็ก)";
        } else if (customModelData == 2 && meta.getDisplayName().equals(TieredBackpacks.mediumBackpack.getItemMeta().getDisplayName())) {
            inventorySize = 27;
            backpackTitle = "§eกระเป๋าเป้ขั้นที่ 2 (กลาง)";
        } else if (customModelData == 3 && meta.getDisplayName().equals(TieredBackpacks.largeBackpack.getItemMeta().getDisplayName())) {
            inventorySize = 54;
            backpackTitle = "§bกระเป๋าเป้ขั้นที่ 3 (ใหญ่)";
        }

        // ถ้าเป็นกระเป๋า ให้เปิด GUI
        if (backpackTitle != null) {
            event.setCancelled(true); // ยกเลิก event ปกติของไอเทม

            // *** 1. สร้าง BackpackHolder และ Inventory ***
            // เราต้อง clone ไอเทมมาใช้ใน holder เพื่อไม่ให้เกิดปัญหาตอน InventoryCloseEvent
            ItemStack backpackClone = itemInHand.clone();
            BackpackHolder holder = new BackpackHolder(backpackClone, inventorySize, backpackTitle);
            Inventory backpackInv = holder.getInventory(); 

            // *** 2. โหลดข้อมูลจาก PDC และใส่ใน Inventory ***
            
            // ดึงข้อมูล Base64 String ที่เก็บ Inventory มาจาก PDC ของ itemInHand
            String invData = dataContainer.get(TieredBackpacks.inventoryKey, PersistentDataType.STRING);
            
            if (invData != null) {
                try {
                    // แปลง Base64 String กลับเป็น ItemStack[]
                    ItemStack[] contents = InventorySerializer.inventoryFromBase64(invData);
                    backpackInv.setContents(contents);
                } catch (Exception e) {
                    player.sendMessage("§cเกิดข้อผิดพลาดในการโหลดข้อมูลกระเป๋า!");
                    e.printStackTrace();
                }
            }
            
            player.openInventory(backpackInv);
        }
    }
    
    // *** 3. Event สำหรับการปิด Inventory เพื่อบันทึกข้อมูล ***
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // เช็คว่าเป็น Inventory ที่ใช้ BackpackHolder หรือไม่
        if (event.getInventory().getHolder() instanceof BackpackHolder) {
            
            BackpackHolder holder = (BackpackHolder) event.getInventory().getHolder();
            ItemStack backpackItem = holder.getBackpackItem();
            
            // ดึง ItemMeta และ PDC จากไอเทมกระเป๋าใน holder
            ItemMeta meta = backpackItem.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            
            // *** 4. บันทึกข้อมูล Inventory กลับไปที่ PDC ***
            try {
                // แปลง Inventory Contents เป็น Base64 String
                String invData = InventorySerializer.inventoryToBase64(event.getInventory());
                
                // บันทึก String ลงใน PDC
                dataContainer.set(TieredBackpacks.inventoryKey, PersistentDataType.STRING, invData);
                
                // เซ็ต ItemMeta กลับไปที่ ItemStack
                backpackItem.setItemMeta(meta);
                
                // *** 5. อัปเดตไอเทมกระเป๋าในมือผู้เล่น (สำคัญมาก!) ***
                Player player = (Player) event.getPlayer();
                
                // หาไอเทมกระเป๋าเดิมใน Inventory ผู้เล่น
                int slot = -1;
                
                // เช็คในมือหลักและมือรอง
                if (isBackpack(player.getInventory().getItemInMainHand(), holder.getBackpackItem())) {
                    slot = player.getInventory().getHeldItemSlot();
                } else if (isBackpack(player.getInventory().getItemInOffHand(), holder.getBackpackItem())) {
                    slot = 40; // ช่องมือรอง
                }
                
                // หากระเป๋าเจอในมือหลัก/รอง หรือใน Inventory ผู้เล่น
                if (slot != -1) {
                    player.getInventory().setItem(slot, backpackItem);
                } else {
                    // กรณีที่กระเป๋าถูกย้ายไปช่องอื่นใน Inventory ผู้เล่น (ไม่ได้อยู่ในมือ)
                    for (int i = 0; i < player.getInventory().getSize(); i++) {
                        if (isBackpack(player.getInventory().getItem(i), holder.getBackpackItem())) {
                            player.getInventory().setItem(i, backpackItem);
                            break;
                        }
                    }
                }
                
                player.updateInventory();

            } catch (Exception e) {
                ((Player) event.getPlayer()).sendMessage("§cเกิดข้อผิดพลาดในการบันทึกข้อมูลกระเป๋า!");
                e.printStackTrace();
            }
        }
    }

    // *** 6. แก้ไข onInventoryClick เพื่อป้องกันการใส่กระเป๋าซ้อนกัน และป้องกันการลากกระเป๋าเป้ออก ***
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        
        // ป้องกันการใส่กระเป๋าซ้อนกันใน GUI กระเป๋า
        if (event.getView().getTopInventory().getHolder() instanceof BackpackHolder) {
            ItemStack cursorItem = event.getCursor();
            ItemStack currentItem = event.getCurrentItem();
            
            // ป้องกันการนำกระเป๋าเป้มาวางในกระเป๋าเป้ที่เปิดอยู่ (ไม่ว่าจะเป็น cursor หรือ item ที่คลิก)
            if (isBackpack(cursorItem, null) || isBackpack(currentItem, null)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player) {
                    event.getWhoClicked().sendMessage("§cคุณไม่สามารถนำกระเป๋าใส่ในกระเป๋าได้!");
                }
                return;
            }
            
            // ป้องกันไม่ให้คลิกที่ตัวกระเป๋าในช่อง Inventory ของผู้เล่น (ถ้าคลิกที่ช่องของ Player's Inventory)
            if (clickedInventory != null && clickedInventory.equals(event.getView().getBottomInventory())) {
                ItemStack itemOnCursor = event.getCursor();
                
                if (isBackpack(itemOnCursor, null)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // ฟังก์ชันช่วยเช็คว่าไอเทมเป็นกระเป๋าหรือไม่ (แบบง่าย)
    private boolean isBackpack(ItemStack item, ItemStack referenceItem) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return false;
        }
        int modelData = item.getItemMeta().getCustomModelData();
        
        // ถ้ามี referenceItem ให้เช็คว่า PDC เหมือนกันหรือไม่ (เพื่อยืนยันว่าเป็นกระเป๋าใบเดียวกัน)
        if (referenceItem != null) {
            ItemMeta itemMeta = item.getItemMeta();
            ItemMeta referenceMeta = referenceItem.getItemMeta();
            
            if (!itemMeta.getPersistentDataContainer().has(TieredBackpacks.inventoryKey, PersistentDataType.STRING) && 
                !referenceMeta.getPersistentDataContainer().has(TieredBackpacks.inventoryKey, PersistentDataType.STRING)) {
                 // ถ้าทั้งสองไม่มี PDC เลย อาจจะใช้การเปรียบเทียบแค่ CustomModelData และชื่อ
                 return modelData >= 1 && modelData <= 3 && itemMeta.getDisplayName().equals(referenceMeta.getDisplayName());
            }

            // ในทางปฏิบัติ การเปรียบเทียบ PDC ที่ซับซ้อนกว่านี้จะดีกว่า แต่สำหรับโค้ดนี้ใช้แค่ CustomModelData ก็เพียงพอ 
            // เพราะเราใช้ BackpackHolder เพื่ออ้างอิงถึงไอเทมกระเป๋าที่เปิดอยู่แล้ว 
        }

        return modelData >= 1 && modelData <= 3;
    }
}
