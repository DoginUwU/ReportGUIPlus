package ReportGUIPlus.Commands;

import ReportGUIPlus.GUI.ReportInventory;
import ReportGUIPlus.ReportGUIPlus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportCommand implements CommandExecutor {

    private ArrayList<Player> players = new ArrayList<Player>();

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)) return false;

        Player player = (Player)sender;



        if(args.length == 0){
            player.sendMessage(ReportGUIPlus.getInstance().getConfig().getString("texts.ReportUsage").replace("&", "§"));
            return true;
        }

        Player reportedPlayer = Bukkit.getPlayer(args[0]);

        if(reportedPlayer == null){
            player.sendMessage(ReportGUIPlus.getInstance().getConfig().getString("texts.UserNotFound").replace("&", "§"));
            return true;
        }
        if(reportedPlayer.getUniqueId() == player.getUniqueId()){
            player.sendMessage(ReportGUIPlus.getInstance().getConfig().getString("texts.ReportMe").replace("&", "§"));
            return true;
        }

        //if(players.contains(player)){
            Inventory inventory = new ReportInventory(player, reportedPlayer).getInventory();
        /*}else{
            Inventory inventory = new ReportInventory(player, reportedPlayer, true).getInventory();
            players.add(player);
        }*/

        return true;
    }
}
