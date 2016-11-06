package com.satoshicraft.economy.utils;

import com.google.common.base.Strings;
import com.satoshicraft.economy.SatoshiEconomy;
import com.satoshicraft.economy.economy.Currency;
import com.satoshicraft.economy.economy.EconomyManager;
import com.satoshicraft.economy.economy.backend.EconomyStorageBackend;
import com.satoshicraft.economy.economy.backend.type.EconomyStorageBackendFlatfile;
import com.satoshicraft.economy.economy.backend.type.EconomyStorageBackendMySQL;
import com.satoshicraft.economy.economy.economable.EconomableGeneric;
import com.satoshicraft.economy.economy.logger.TransactionLogger;
import com.satoshicraft.economy.economy.logger.TransactionLoggerMySQL;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 */
public class SaneEconomyConfiguration {
    private final Logger logger;
    private final SatoshiEconomy saneEconomy;
    private final Configuration rootConfig;

    public SaneEconomyConfiguration(SatoshiEconomy saneEconomy) {
        this.saneEconomy = saneEconomy;
        this.rootConfig = saneEconomy.getConfig();
        this.logger = saneEconomy.getLogger();
    }

    public EconomyManager loadEconomyBackend() {
        logger.info("Initializing currency...");
        Currency currency = Currency.fromConfig(rootConfig.getConfigurationSection("currency"));
        logger.info("Initialized currency: " + currency.getPluralName());

        logger.info("Initializing economy storage backend...");

        EconomyStorageBackend backend = loadBackend(rootConfig.getConfigurationSection("backend"));

        if (backend == null) {
            logger.severe("Failed to load backend!");
            return null;
        }

        logger.info("Performing initial data load...");
        backend.reloadDatabase();
        logger.info("Data loaded!");

        if (!Strings.isNullOrEmpty(rootConfig.getString("old-backend.type", null))) {
            logger.info("Old backend detected, converting... (This may take a minute or two.)");
            EconomyStorageBackend oldBackend = loadBackend(rootConfig.getConfigurationSection("old-backend"));
            if (oldBackend == null) {
                logger.severe("Failed to load old backend!");
                return null;
            }

            oldBackend.reloadDatabase();
            convertBackends(oldBackend, backend);
            logger.info("Data converted, removing old config section.");
            rootConfig.set("old-backend", null);
        }

        return new EconomyManager(saneEconomy, currency, backend);
    }

    /**
     * Load an EconomyStorageBackend using the information in the given ConfigurationSection.
     * @param config ConfigurationSection to read connection parameters from
     * @return Constructed EconomyStorageBackend, or null if something inappropriate happened.
     */
    private EconomyStorageBackend loadBackend(ConfigurationSection config) {
        EconomyStorageBackend backend;
        String backendType = config.getString("type");

        if (backendType.equalsIgnoreCase("flatfile")) {
            String backendFileName = config.getString("file", "economy.db");
            File backendFile = new File(saneEconomy.getDataFolder(), backendFileName);
            backend = new EconomyStorageBackendFlatfile(backendFile);
            logger.info("Initialized flatfile backend with file " + backendFile.getAbsolutePath());
        } else if (backendType.equalsIgnoreCase("mysql")) {
            EconomyStorageBackendMySQL mySQLBackend = new EconomyStorageBackendMySQL(loadCredentials(config));

            backend = mySQLBackend;

            logger.info("Initialized MySQL backend.");
            logger.info("Testing connection...");
            if (!mySQLBackend.getConnection().testConnection()) {
                logger.severe("MySQL connection failed - cannot continue!");
                return null;
            }

            logger.info("Connection successful!");
        } else {
            logger.severe("Unknown storage backend " + backendType + "!");
            return null;
        }

        return backend;
    }

    /**
     * Convert one EconomyStorageBackend to another.
     * Right now, this just consists of converting all player balances. Data in the old backend is kept.
     * @param old Old backend
     * @param newer New backend
     */
    private void convertBackends(EconomyStorageBackend old, EconomyStorageBackend newer) {
        old.getAllBalances().forEach((uniqueId, balance) -> {
            newer.setBalance(new EconomableGeneric(uniqueId), balance);
        });
        newer.waitUntilFlushed();
    }

    public TransactionLogger loadLogger() {
        if (!rootConfig.getBoolean("log-transactions", false)) {
            return null;
        }

        logger.info("Attempting to load transaction logger...");

        if (rootConfig.getConfigurationSection("logger-database") == null) {
            logger.severe("No transaction logger database defined, cannot possibly connect!");
            return null;
        }

        DatabaseCredentials credentials = loadCredentials(rootConfig.getConfigurationSection("logger-database"));

        TransactionLoggerMySQL transactionLogger = new TransactionLoggerMySQL(credentials);

        if (transactionLogger.testConnection()) {
            logger.info("Initialized MySQL transaction logger.");
            return transactionLogger;
        }

        logger.severe("Failed to connect to MySQL database for transaction logger!");
        return null;
    }

    /**
     * Load database host, port, username, password, and db name from a ConfigurationSection
     * @param config ConfigurationSection containing the right fields.
     * @return DatabaseCredentials with the information from the config.
     */
    private DatabaseCredentials loadCredentials(ConfigurationSection config) {
        String backendHost = config.getString("host");
        int backendPort = config.getInt("port", 3306);
        String backendDb = config.getString("database");
        String backendUser = config.getString("username");
        String backendPass = config.getString("password");

        return new DatabaseCredentials(
                backendHost, backendPort, backendUser, backendPass, backendDb
        );
    }
}
