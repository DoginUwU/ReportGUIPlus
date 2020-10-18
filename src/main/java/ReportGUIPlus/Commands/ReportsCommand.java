package ReportGUIPlus.Commands;

import ReportGUIPlus.GUI.ReportsInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ReportsCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)) return false;

        Player player = (Player)sender;

        Inventory inventory = new ReportsInventory(player).getInventory();

        return true;
    }
}
