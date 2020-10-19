package ReportGUIPlus.GUI;

import ReportGUIPlus.ReportGUIPlus;
import ReportGUIPlus.Utils.Heads;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class ReportInventory implements Listener {
    private Inventory inventory;

    private Player player;
    private Player reportedPlayer;

    private ArrayList<String> reports = new ArrayList();

    public ReportInventory(Player player, Player reportedPlayer){
        this.player = player;
        this.reportedPlayer = reportedPlayer;

        inventory = Bukkit.createInventory(null, 5 * 9, ReportGUIPlus.getStringLangConfig("ReportInventory.name"));

        AddReportsType();

        ItemStack continueItem = Heads.createSkullByNickname(reportedPlayer.getName());
        ItemMeta continueItemMeta = continueItem.getItemMeta();
        continueItemMeta.setDisplayName(ReportGUIPlus.getStringLangConfig("ReportInventory.playerInfo"));
        continueItemMeta.setLore(Arrays.asList(ReportGUIPlus.getStringLangConfig("ReportInventory.reported") + reportedPlayer.getName(), "", "", ReportGUIPlus.getStringLangConfig("ReportInventory.ClickToReport")));
        continueItem.setItemMeta(continueItemMeta);

        inventory.setItem(40, continueItem);

        Bukkit.getPluginManager().registerEvents(this, ReportGUIPlus.getInstance());


        player.openInventory(inventory);
    }

    private void AddReportsType(){
        ConfigurationSection reports =  ReportGUIPlus.getInstance().getConfig().getConfigurationSection("reports");

        for (String key : reports.getKeys(false)) {
            ItemStack item = Heads.createSkullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZkZTNiZmNlMmQ4Y2I3MjRkZTg1NTZlNWVjMjFiN2YxNWY1ODQ2ODRhYjc4NTIxNGFkZDE2NGJlNzYyNGIifX19");
            ItemMeta itemMeta = item.getItemMeta();

            itemMeta.setDisplayName("§4" + key);
            itemMeta.setLore(Arrays.asList(ReportGUIPlus.getInstance().getConfig().getString("reports." + key + ".description").replace("&", "§"), ReportGUIPlus.getStringLangConfig("ReportInventory.Deselected")));


            item.setItemMeta(itemMeta);
            inventory.setItem(ReportGUIPlus.getInstance().getConfig().getInt("reports." + key + ".slot") - 1, item);
        }
    }

    public ReportInventory insertItens(ItemStack item, int slot){
        inventory.setItem(slot, item);

        return this;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void OnClose(InventoryCloseEvent e){
        try {
            PlayerInteractEvent.getHandlerList().unregisterAll(this);
        }catch (Exception err){

        }
    }

    @EventHandler
    public void ReportGUI(InventoryClickEvent e){
        if(!e.getInventory().getName().equalsIgnoreCase(ReportGUIPlus.getStringLangConfig("ReportInventory.name"))) return;

        if(inventory.getItem(e.getSlot()) == null) return;

        e.setCancelled(true);

        ItemStack item = inventory.getItem(e.getSlot());
        ItemMeta itemMeta = item.getItemMeta();

        String tag = ReportGUIPlus.getInstance().getConfig().getString("configs.tag").replace("&", "§");

        if(String.valueOf(itemMeta.getDisplayName()).contains(ReportGUIPlus.getStringLangConfig("ReportInventory.playerInfo"))){
            try {
                ReportGUIPlus.getMysql().openConnection();
                Connection connection = ReportGUIPlus.getMysql().getConnection();

                UUID UUID = reportedPlayer.getUniqueId();

                PreparedStatement ps = connection.prepareStatement("SELECT * FROM reports WHERE user = '"+player.getUniqueId().toString()+"' AND reportedUUID = '"+UUID.toString()+"'");
                ResultSet resultSet = ps.executeQuery();

                if(resultSet.next()){
                    player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("ReportInventory.AlreadySend"));
                    return;
                }else if(reports.isEmpty() || reports.get(0) == ""){
                    player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("ReportInventory.ReasonsNotFound"));
                    return;
                }else{
                    ps = connection.prepareStatement("INSERT INTO reports VALUES ('"+player.getUniqueId().toString()+"', '"+UUID.toString()+"', '"+reports.toString()+"', '"+new Timestamp(System.currentTimeMillis())+"')");
                    ps.execute();
                    ps.close();

                    player.closeInventory();

                    player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("ReportInventory.SuccessfullySend"));
                    sendMessageForAllStaff();
                    return;
                }


            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return;
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
                return;
            }

        }

        if(String.valueOf(itemMeta.getLore()).contains(ReportGUIPlus.getStringLangConfig("ReportInventory.Deselected"))){
            item = Heads.updateSkullByBase64(item, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19");
            itemMeta = item.getItemMeta();
            String key = item.getItemMeta().getDisplayName().replace("§4", "");

            itemMeta.setLore(Arrays.asList(ReportGUIPlus.getInstance().getConfig().getString("reports." + key + ".description").replace("&", "§"), ReportGUIPlus.getStringLangConfig("ReportInventory.Selected")));
            item.setItemMeta(itemMeta);

            reports.add(key);
            return;
        }else if(String.valueOf(itemMeta.getLore()).contains(ReportGUIPlus.getStringLangConfig("ReportInventory.Selected"))){
            item = Heads.updateSkullByBase64(item, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZkZTNiZmNlMmQ4Y2I3MjRkZTg1NTZlNWVjMjFiN2YxNWY1ODQ2ODRhYjc4NTIxNGFkZDE2NGJlNzYyNGIifX19");
            itemMeta = item.getItemMeta();
            String key = item.getItemMeta().getDisplayName().replace("§4", "");

            itemMeta.setLore(Arrays.asList(ReportGUIPlus.getInstance().getConfig().getString("reports." + key + ".description").replace("&", "§"), ReportGUIPlus.getStringLangConfig("ReportInventory.Deselected")));
            item.setItemMeta(itemMeta);

            reports.remove(key);
            return;
        }
    }

    public void sendMessageForAllStaff(){
        for (Player player : Bukkit.getOnlinePlayers()){
            if(player.hasPermission("reportguiplus.admin")){
                for (String text : ReportGUIPlus.getLangConfig().getStringList("ReportInventory.adminReport")){
                    player.sendMessage(text.replace("&", "§"));
                }
            }
        }
    }
}
