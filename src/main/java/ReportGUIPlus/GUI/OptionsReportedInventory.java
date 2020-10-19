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
        inventory = Bukkit.createInventory(null, 3 * 9, "§8Menu - " + reportedPlayer.getName());

        player.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(this, ReportGUIPlus.getInstance());

        createOptions(Material.ENDER_PEARL, (short)0,"§6§lTeleportar jogador", Collections.singletonList("§7Teleporte o player até você"),10);
        createOptions(Material.CHEST, (short)0,"§9§lVer inventario", Collections.singletonList("§7Veja o inventario do jogador selecionado"),12);
        createOptions(Material.STAINED_CLAY, (short)14,"§c§lBanir jogador", Arrays.asList("§7Dê ban permanente no jogador", " §7* A rasão do ban será a mesma do reporte"),14);
        createOptions(Material.STAINED_CLAY, (short)13,"§a§lUsuario verificado", Arrays.asList("§7Use caso você tenha certeza que ele está legitmo", " §7* Uma mensagem de rejeição será enviada para o jogador que o reportou"),16);
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
        if (!e.getInventory().getName().equalsIgnoreCase("§8Menu - " + reportedPlayer.getName())) return;

        if (inventory.getItem(e.getSlot()) == null) return;

        e.setCancelled(true);

        ItemStack item = inventory.getItem(e.getSlot());
        ItemMeta itemMeta = item.getItemMeta();

        String tag = ReportGUIPlus.getInstance().getConfig().getString("configs.tag").replace("&", "§");

        if(String.valueOf(itemMeta.getDisplayName()).contains("§6§lTeleportar jogador")){
            if(reportedPlayer == null){
                player.sendMessage(tag + " §4Desculpe, algo deu errado!");
                return;
            }
            if(reportedPlayer.isOnline()){
                reportedPlayer.teleport(player);
                player.closeInventory();
            }
        }else if(String.valueOf(itemMeta.getDisplayName()).contains("§9§lVer inventario")){
            if(reportedPlayer == null){
                player.sendMessage(tag + " §4Desculpe, algo deu errado!");
                return;
            }
            if(reportedPlayer.isOnline()){
                player.closeInventory();
                player.openInventory(reportedPlayer.getInventory());
            }
        }else if(String.valueOf(itemMeta.getDisplayName()).contains("§c§lBanir jogador")){
            if(reportedPlayer == null || playerSender == null){
                player.sendMessage(tag + " §4Desculpe, algo deu errado!");
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

                    reportedPlayer.kickPlayer(tag + " Você foi banido por uso de: §5" + reasons);
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "ban " + reportedPlayer.getName());
                    player.closeInventory();
                    player.sendMessage(tag + "§fUsuario banido com sucesso!");
                    if(playerSender.isOnline()){
                        playerSender.sendMessage("§3=-=-=-=-=-=-=-=-=-=-=-=-=");
                        playerSender.sendMessage("§fSua denuncia estava correta!");
                        playerSender.sendMessage("§fJogador banido: §6" + reportedPlayer.getName());
                        if(ReportGUIPlus.getInstance().getConfig().getString("configs.recompense") != null && ReportGUIPlus.getInstance().getConfig().getString("configs.recompense") != ""){
                            playerSender.sendMessage("§fVocê irá receber uma recompensa por isso");
                        }
                        playerSender.sendMessage("§3=-=-=-=-=-=-=-=-=-=-=-=-=");
                    }
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), ReportGUIPlus.getInstance().getConfig().getString("configs.recompense").replace("%nick%", playerSender.getName()));
                    return;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }

           }else if(String.valueOf(itemMeta.getDisplayName()).contains("§a§lUsuario verificado")){
            if(reportedPlayer == null || playerSender == null){
                player.sendMessage(tag + " §4Desculpe, algo deu errado!");
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
                    player.sendMessage(tag + " §fDenuncia verificada com sucesso!");
                    if(playerSender.isOnline()){
                        playerSender.sendMessage("§3=-=-=-=-=-=-=-=-=-=-=-=-=");
                        playerSender.sendMessage("§fSua denuncia foi negada!");
                        playerSender.sendMessage("§3=-=-=-=-=-=-=-=-=-=-=-=-=");
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
