package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.utils.MapUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendMySQL implements EconomyStorageBackend {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final HashMap<UUID, Double> playerBalances = new HashMap<>();
    private Map<UUID, Double> topBalances = new LinkedHashMap<>();


    public EconomyStorageBackendMySQL(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No MySQL driver found.");
        }
    }

    private Connection openConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Database unavailable.");
        }
    }

    public boolean testConnection() {
        try {
            openConnection().close();
            createTable();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void createTable() {
        Connection conn = openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `player_balances` (player_uuid CHAR(38), balance DECIMAL(18, 2))");
            ps.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables!", e);
        }
    }

    @Override
    public void reloadDatabase() {
        Connection conn = openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `player_balances`");
            ResultSet rs = ps.executeQuery();

            playerBalances.clear();

            while (rs.next()) {
                playerBalances.put(UUID.fromString(rs.getString("player_uuid")), rs.getDouble("balance"));
            }

            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reload data from SQL.", e);
        }
    }

    @Override
    public void reloadTopBalances() {
        topBalances = MapUtil.sortByValue(playerBalances);
    }

    @Override
    public boolean accountExists(OfflinePlayer player) {
        return playerBalances.containsKey(player.getUniqueId());
    }

    @Override
    public synchronized double getBalance(OfflinePlayer player) {
        if (!accountExists(player)) {
            return 0.0D;
        }

        return playerBalances.get(player.getUniqueId());
    }

    @Override
    public synchronized void setBalance(final OfflinePlayer player, final double newBalance) {
        final double oldBalance = getBalance(player);
        playerBalances.put(player.getUniqueId(), newBalance);

        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(SaneEconomy.getInstance(), () -> {
            Connection conn = openConnection();
            ensureAccountExists(player, conn);
            try {
                PreparedStatement statement = conn.prepareStatement("UPDATE `player_balances` SET balance = ? WHERE `player_uuid` = ?");
                statement.setDouble(1, newBalance);
                statement.setString(2, player.getUniqueId().toString());
                statement.executeUpdate();
                conn.close();
            } catch (SQLException e) {
                /* Roll it back */
                playerBalances.put(player.getUniqueId(), oldBalance);
                throw new RuntimeException("SQL error has occurred.", e);
            }
        });
    }

    @Override
    public Map<UUID, Double> getTopBalances(int amount) {
        return MapUtil.takeFromMap(topBalances, amount);
    }

    private void ensureAccountExists(OfflinePlayer player, Connection conn) {
        if (!accountExists(player, conn)) {
            try {
                PreparedStatement statement = conn.prepareStatement("INSERT INTO `player_balances` (player_uuid, balance) VALUES (?, 0.0)");
                statement.setString(1, player.getUniqueId().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("SQL error has occurred.", e);
            }
        }
    }

    private boolean accountExists(OfflinePlayer player, Connection conn) {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT 1 FROM `player_balances` WHERE `player_uuid` = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException("SQL error has occurred.", e);
        }

        return false;
    }
}
