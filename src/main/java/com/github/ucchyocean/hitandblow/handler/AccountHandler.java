/*
 * Copyright ucchy 2012
 */
package com.github.ucchyocean.hitandblow.handler;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * @author ucchy
 */
public class AccountHandler {

    private static Economy econ = null;

    public AccountHandler() throws Exception {

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        if ( plugin == null ) {
            throw new Exception("Vault was not loaded!");
        }
        econ = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }

    public boolean hasFunds(String name, double value) {
        if( !econ.hasAccount(name) ) {
            econ.createBank(name, name);
        }

        double account = econ.getBalance(name);
        return Double.compare(account, value) >= 0;
    }

    public boolean chargeMoney(String name, double value) {
        return econ.withdrawPlayer(name, value).transactionSuccess();
    }

    public boolean addMoney(String name, double value) {
        return econ.depositPlayer(name, value).transactionSuccess();
    }

    public String getUnitsPlural() {
        return econ.currencyNamePlural();
    }

    public String getUnitsSingular() {
        return econ.currencyNameSingular();
    }
}
