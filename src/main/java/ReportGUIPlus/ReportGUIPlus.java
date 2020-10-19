package ReportGUIPlus;


import ReportGUIPlus.Commands.ReportCommand;
import ReportGUIPlus.Commands.ReportsCommand;
import ReportGUIPlus.Utils.MYSQL;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReportGUIPlus extends JavaPlugin {

    public static ReportGUIPlus instance;
    public static MYSQL mysql;
    public static File langFile;
    public static FileConfiguration langConfig;

    public void onEnable() {
        instance = this;
        LoadConfiguration();

        String host = getConfig().getString("mysql.host");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        String database = getConfig().getString("mysql.database");
        Boolean mysqlEnabled = getConfig().getBoolean("mysql.enabled");
        int port = getConfig().getInt("mysql.port");
        String table = getConfig().getString("mysql.table");

        mysql = new MYSQL(host, database, username, password, port, table, mysqlEnabled);

        RegisterCommands();
        RegisterEvents();
    }

    private void LoadConfiguration(){
        getConfig().options().copyDefaults(false);
        saveDefaultConfig();
        langFile = new File(getDataFolder(), "/lang.yml");
        if(!langFile.exists()){
            try{
                /*langFile.createNewFile();
                langConfig = YamlConfiguration.loadConfiguration(langFile);*/
                langFile.getParentFile().mkdirs();
                saveResource("lang.yml", false);

                langConfig = new YamlConfiguration();
                try{
                    langConfig.load(langFile);
                }catch (Exception err){

                }

            }catch(Exception e){
                Bukkit.getConsoleSender().sendMessage("§4[ReportGUIPlus] Um erro ocorreu ao iniciar a §6lang.yml");
            }
        }else{
            langConfig = YamlConfiguration.loadConfiguration(langFile);
        }
    }

    public static FileConfiguration getLangConfig() {
        return langConfig;
    }

    public static String getStringLangConfig(String path){
        return langConfig.getString(path).replace("&", "§");
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
