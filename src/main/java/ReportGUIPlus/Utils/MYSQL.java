package ReportGUIPlus.Utils;

import ReportGUIPlus.ReportGUIPlus;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MYSQL {
    private Connection connection;
    private String host, database, username, password, table;
    private int port;

    public MYSQL(String host, String database, String username, String password, int port, String table) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;
        this.table = table;

        try {
            if(openConnection()){
                Statement statement = connection.createStatement();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initDB();
    }

    public Boolean openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return true;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return true;
            }
            try{
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            }catch (Exception err){
                Bukkit.getConsoleSender().sendMessage("§4[ReportGUIPlus] Desculpe, nao foi possivel se conectar com o mysql! Cheque o arquivo config.yml.");
                Bukkit.getPluginManager().disablePlugin(ReportGUIPlus.getInstance());
                return false;
            }
        }

        return false;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void initDB(){
        CreateTable(table, "user text(200), reportedUUID text(200), reasons text(100), time timestamp");
    }

    private void CreateTable(String table, String coluns){
        try{
            if((connection != null) && (!connection.isClosed())){
                Statement stm = connection.createStatement();
                stm.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (" + coluns + ");");
            }
        }catch (Exception err){
            System.out.println("Erro ao criar uma nova tabela: " + err);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
