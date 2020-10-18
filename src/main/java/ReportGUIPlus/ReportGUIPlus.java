package ReportGUIPlus;


import ReportGUIPlus.Commands.ReportCommand;
import ReportGUIPlus.Commands.ReportsCommand;
import ReportGUIPlus.Utils.MYSQL;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportGUIPlus extends JavaPlugin {

    public static ReportGUIPlus instance;
    public static MYSQL mysql;

    public void onEnable() {
        instance = this;
        LoadConfiguration();

        String host = getConfig().getString("mysql.host");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        String database = getConfig().getString("mysql.database");
        int port = getConfig().getInt("mysql.port");
        String table = getConfig().getString("mysql.table");

        mysql = new MYSQL(host, database, username, password, port, table);

        RegisterCommands();
        RegisterEvents();
    }

    private void LoadConfiguration(){
        getConfig().options().copyDefaults(false);
        saveDefaultConfig();
    }

    private void RegisterCommands(){
        getCommand("report").setExecutor(new ReportCommand());
        getCommand("reports").setExecutor(new ReportsCommand());
    }

    private void RegisterEvents(){

    }

    public static ReportGUIPlus getInstance() {
        return instance;
    }

    public static MYSQL getMysql() {
        return mysql;
    }
}
