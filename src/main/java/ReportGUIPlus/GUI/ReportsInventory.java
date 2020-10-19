package ReportGUIPlus.GUI;

import ReportGUIPlus.ReportGUIPlus;
import ReportGUIPlus.Utils.Heads;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class ReportsInventory implements Listener {
    private Inventory inventory;
    private Player player;
    private String reasons;

    public ReportsInventory(Player player) {
        this.player = player;
        inventory = Bukkit.createInventory(null, 5 * 9, ReportGUIPlus.getStringLangConfig("ReportsInventory.name"));

        getPlayers();

        player.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(this, ReportGUIPlus.getInstance());
    }

    private void getPlayers(){
        try {
            ReportGUIPlus.getMysql().openConnection();
            Connection connection = ReportGUIPlus.getMysql().getConnection();

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM reports");
            ResultSet resultSet = ps.executeQuery();

            int slot = 0;

            while (resultSet.next()){
                String userUUID = resultSet.getString("user");
                String reportedUUID = resultSet.getString("reportedUUID");
                reasons = resultSet.getString("reasons");
                Timestamp timestamp = resultSet.getTimestamp("time");

                reasons = reasons.replace("[", "").replace("]", "");

                Player playerSender = Bukkit.getPlayer(UUID.fromString(userUUID));
                Player playerReported = Bukkit.getPlayer(UUID.fromString(reportedUUID));

                String isOnline = "";

                if(playerReported.isOnline()){
                    isOnline = ReportGUIPlus.getStringLangConfig("ReportsInventory.playerIsON");
                }

                ItemStack item = Heads.createSkullByNickname(playerReported.getName());
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName("§a" + playerReported.getName());
                itemMeta.setLore(Arrays.asList(
                        ReportGUIPlus.getStringLangConfig("ReportsInventory.ReportedBy") + playerSender.getName(),
                        ReportGUIPlus.getStringLangConfig("ReportsInventory.SendOn") + new SimpleDateFormat("dd/MM/yyyy k:mm:ss").format(new Date(timestamp.getTime())),
                        ReportGUIPlus.getStringLangConfig("ReportsInventory.Reasons") + reasons,
                        "",
                        ReportGUIPlus.getStringLangConfig("ReportsInventory.UseLeftClick"),
                        ReportGUIPlus.getStringLangConfig("ReportsInventory.UseMiddleClick"),
                        "",
                        isOnline
                ));
                item.setItemMeta(itemMeta);

                inventory.setItem(slot, item);

                slot++;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        if(!e.getInventory().getName().equalsIgnoreCase(ReportGUIPlus.getStringLangConfig("ReportsInventory.name"))) return;

        if(inventory.getItem(e.getSlot()) == null) return;

        String tag = ReportGUIPlus.getInstance().getConfig().getString("configs.tag").replace("&", "§");

        e.setCancelled(true);

        ItemStack item = inventory.getItem(e.getSlot());
        ItemMeta itemMeta = item.getItemMeta();

        Player playerReported = Bukkit.getPlayer(itemMeta.getDisplayName().replace("§a", ""));
        Player playerSender = Bukkit.getPlayer(((String) itemMeta.getLore().toArray()[0]).replace(ReportGUIPlus.getStringLangConfig("ReportsInventory.ReportedBy"), ""));

        if(e.isRightClick()){
            if(playerReported.isOnline()){
                player.teleport(playerReported);
            }else{
                player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("ReportsInventory.OfflinePlayer"));
            }
            return;
        }
        if(e.getClick() == ClickType.MIDDLE){
            player.closeInventory();
            new OptionsReportedInventory(player, playerReported, playerSender, reasons).getInventory();
        }
        if(e.isLeftClick()){

            if(playerReported == null || playerSender == null){
                player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("ReportsInventory.error"));
                return;
            }

            try {
                ReportGUIPlus.getMysql().openConnection();
                Connection connection = ReportGUIPlus.getMysql().getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM reports WHERE user = '"+playerSender.getUniqueId().toString()+"' AND reportedUUID = '"+playerReported.getUniqueId().toString()+"'");
                ResultSet resultSet = ps.executeQuery();
                if(resultSet.next()){
                    ps = connection.prepareStatement("DELETE FROM reports WHERE user = '"+playerSender.getUniqueId().toString()+"' AND reportedUUID = '"+playerReported.getUniqueId().toString()+"'");
                    ps.execute();
                    ps.close();

                    player.closeInventory();
                    player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("ReportsInventory.SuccessfullyDelete"));
                    return;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}
