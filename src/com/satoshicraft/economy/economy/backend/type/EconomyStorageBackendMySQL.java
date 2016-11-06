package com.satoshicraft.economy.economy.backend.type;

import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.economy.economable.Economable;
import com.satoshicraft.economy.utils.DatabaseCredentials;
import com.satoshicraft.economy.utils.MySQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendMySQL extends EconomyStorageBackendCaching {
    private final MySQLConnection dbConn;

    public EconomyStorageBackendMySQL(DatabaseCredentials dbCredentials) {
        this.dbConn = new MySQLConnection(dbCredentials);
    }

    @Override
    public void reloadDatabase() {
        try (Connection conn = dbConn.openConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `usuarios`");
            
            ResultSet rs = ps.executeQuery();

            balances.clear();

            while (rs.next()) {
                balances.put(rs.getString("username"), rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reload data from SQL.", e);
        }
    }

    @Override
    public synchronized void setBalance(final Economable economable, final double newBalance) {
        final double oldBalance = getBalance(economable);
        balances.put(economable.getUniqueIdentifier(), newBalance);

        dbConn.executeAsyncOperation((conn) -> {
            try {
                ensureAccountExists(economable, conn);
                PreparedStatement statement = conn.prepareStatement("UPDATE `usuarios` SET balance = ? WHERE `username` = ?");
                statement.setDouble(1, newBalance);
                statement.setString(2, economable.getUniqueIdentifier());
                statement.executeUpdate();
            } catch (Exception e) {
                balances.put(economable.getUniqueIdentifier(), oldBalance);
                throw new RuntimeException("SQL error has occurred.", e);
            }
        });
    }

    private synchronized void ensureAccountExists(Economable economable, Connection conn) throws SQLException {
        if (!accountExists(economable, conn)) {
            
        }
    }

    private synchronized boolean accountExists(Economable economable, Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT 1 FROM `usuarios` WHERE `username` = ?");
        statement.setString(1, economable.getUniqueIdentifier());

        ResultSet rs = statement.executeQuery();

        return rs.next();
    }

    @Override
    public void waitUntilFlushed() {
        dbConn.waitUntilFlushed();
    }

    public MySQLConnection getConnection() {
        return dbConn;
    }
}
