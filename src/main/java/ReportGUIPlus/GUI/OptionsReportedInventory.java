package ReportGUIPlus.GUI;

import ReportGUIPlus.ReportGUIPlus;
import fr.neatmonster.nocheatplus.NCPAPIProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OptionsReportedInventory implements Listener {
    private Inventory inventory;
    private Player player;
    private Player reportedPlayer;
    private Player playerSender;
    private String reasons;

    public OptionsReportedInventory(Player player, Player reportedPlayer, Player playerSender, String reasons) {
        this.player = player;
        this.reportedPlayer = reportedPlayer;
        this.playerSender = playerSender;
        this.reasons = reasons;
        inventory = Bukkit.createInventory(null, 3 * 9, ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.name") + reportedPlayer.getName());

        player.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(this, ReportGUIPlus.getInstance());

        createOptions(Material.ENDER_PEARL, (short)0,ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.teleportPlayer"), Collections.singletonList(""),10);
        createOptions(Material.CHEST, (short)0,ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.openInventory"), Collections.singletonList(""),12);
        createOptions(Material.STAINED_CLAY, (short)14,ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.BanUser"), Arrays.asList(""),14);
        createOptions(Material.STAINED_CLAY, (short)13,ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.VerifedUser"), Arrays.asList(""),16);
    }

    private void createOptions(Material material, short type, String name, List<String> lore, int slot){
        ItemStack item = new ItemStack(material, 1,type);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(name);
        itemMeta.setLore(lore);

        item.setItemMeta(itemMeta);

        insertItens(item, slot);
    }

    public OptionsReportedInventory insertItens(ItemStack item, int slot){
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
    public void ReportGUI(InventoryClickEvent e) {
        if (!e.getInventory().getName().equalsIgnoreCase(ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.name") + reportedPlayer.getName())) return;

        if (inventory.getItem(e.getSlot()) == null) return;

        e.setCancelled(true);

        ItemStack item = inventory.getItem(e.getSlot());
        ItemMeta itemMeta = item.getItemMeta();

        String tag = ReportGUIPlus.getInstance().getConfig().getString("configs.tag").replace("&", "§");

        if(String.valueOf(itemMeta.getDisplayName()).contains(ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.teleportPlayer"))){
            if(reportedPlayer == null){
                player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.error"));
                return;
            }
            if(reportedPlayer.isOnline()){
                reportedPlayer.teleport(player);
                player.closeInventory();
            }
        }else if(String.valueOf(itemMeta.getDisplayName()).contains(ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.openInventory"))){
            if(reportedPlayer == null){
                player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.error"));
                return;
            }
            if(reportedPlayer.isOnline()){
                player.closeInventory();
                player.openInventory(reportedPlayer.getInventory());
            }
        }else if(String.valueOf(itemMeta.getDisplayName()).contains(ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.BanUser"))){
            if(reportedPlayer == null || playerSender == null){
                player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.error"));
                return;
            }

            try {
                ReportGUIPlus.getMysql().openConnection();
                Connection connection = ReportGUIPlus.getMysql().getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM reports WHERE user = '"+playerSender.getUniqueId().toString()+"' AND reportedUUID = '"+reportedPlayer.getUniqueId().toString()+"'");
                ResultSet resultSet = ps.executeQuery();
                if(resultSet.next()){
                    ps = connection.prepareStatement("DELETE FROM reports WHERE user = '"+playerSender.getUniqueId().toString()+"' AND reportedUUID = '"+reportedPlayer.getUniqueId().toString()+"'");
                    ps.execute();
                    ps.close();

                    reportedPlayer.kickPlayer(tag + " " + ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.BanReason") + reasons);
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "ban " + reportedPlayer.getName());
                    player.closeInventory();
                    player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.banSuccessfully"));
                    if(playerSender.isOnline()){
                        for (String text : ReportGUIPlus.getLangConfig().getStringList("OptionsReportedInventory.adminReport")){
                            if(text.contains("%recompense%") && ReportGUIPlus.getInstance().getConfig().getString("configs.recompense") != null && ReportGUIPlus.getInstance().getConfig().getString("configs.recompense") != ""){
                                player.sendMessage(text.replace("&", "§").replace("%ban_user%", reportedPlayer.getName()));
                            }else{
                                player.sendMessage(text.replace("&", "§").replace("%ban_user%", reportedPlayer.getName()));
                            }
                        }
                    }
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ReportGUIPlus.getInstance().getConfig().getString("configs.recompense").replace("%nick%", playerSender.getName()));
                    return;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }

           }else if(String.valueOf(itemMeta.getDisplayName()).contains(ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.VerifedUser"))){
            if(reportedPlayer == null || playerSender == null){
                player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.error"));
                return;
            }

            try {
                ReportGUIPlus.getMysql().openConnection();
                Connection connection = ReportGUIPlus.getMysql().getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM reports WHERE user = '"+playerSender.getUniqueId().toString()+"' AND reportedUUID = '"+reportedPlayer.getUniqueId().toString()+"'");
                ResultSet resultSet = ps.executeQuery();
                if(resultSet.next()){
                    ps = connection.prepareStatement("DELETE FROM reports WHERE user = '"+playerSender.getUniqueId().toString()+"' AND reportedUUID = '"+reportedPlayer.getUniqueId().toString()+"'");
                    ps.execute();
                    ps.close();

                    player.closeInventory();
                    player.sendMessage(tag + " " + ReportGUIPlus.getStringLangConfig("OptionsReportedInventory.VerifedUserSuccessfully"));
                    if(playerSender.isOnline()){
                        for (String text : ReportGUIPlus.getLangConfig().getStringList("OptionsReportedInventory.wrongReport")){
                            player.sendMessage(text.replace("&", "§"));
                        }
                    }
                    return;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        }
    }
}
