package com.satoshicraft.economy.economy.backend.type;

import com.google.common.collect.ImmutableMap;
import com.satoshicraft.economy.economy.backend.EconomyStorageBackend;
import com.satoshicraft.economy.economy.economable.Economable;
import com.satoshicraft.economy.utils.MapUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public abstract class EconomyStorageBackendCaching implements EconomyStorageBackend {
    protected HashMap<String, Double> balances = new HashMap<>();
    private Map<UUID, Double> topPlayerBalances = new LinkedHashMap<>();

    @Override
    public boolean accountExists(Economable economable) {
        return balances.containsKey(economable.getUniqueIdentifier());
    }

    @Override
    public synchronized double getBalance(Economable economable) {
        if (!accountExists(economable)) {
            return 0.0D;
        }

        return balances.get(economable.getUniqueIdentifier());
    }

    @Override
    public Map<UUID, Double> getTopPlayerBalances(int amount, int offset) {
        return MapUtil.takeFromMap(topPlayerBalances, amount, offset);
    }

    @Override
    public void reloadTopPlayerBalances() {
        Map<UUID, Double> playerBalances = new HashMap<>();

        balances.forEach((identifier, balance) -> {
            if (identifier.startsWith("player:")) { // FIXME: Come on now...
                playerBalances.put(UUID.fromString(identifier.substring("player:".length())), balance);
            }
        });

        topPlayerBalances = MapUtil.sortByValue(playerBalances);
    }

    @Override
    public Map<String, Double> getAllBalances() {
        return ImmutableMap.copyOf(balances);
    }
}
