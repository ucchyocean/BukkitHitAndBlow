/*
 * Copyright ucchy 2012
 */
package org.github.ucchyocean.misc;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * @author ucchy
 */
public class AccountHandler {

    public static Economy econ = null;
    public static Permission perms = null;

    public AccountHandler() throws Exception {

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        if ( plugin == null ) {
            throw new Exception("Vault was not loaded!");
        }
        econ = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }

    public boolean hasFunds(String name, double value)
    {
        if( !econ.hasAccount(name) ) {
            econ.createBank(name, name);
        }

        double account = econ.getBalance(name);
        return Double.compare(account, value) >= 0;
    }

    public boolean chargeMoney(String name, double value)
    {
        return econ.withdrawPlayer(name, value).transactionSuccess();
    }

    public boolean addMoney(String name, double value)
    {
        return econ.depositPlayer(name, value).transactionSuccess();
    }

    public String getUnitsPlural()
    {
        return econ.currencyNamePlural();
    }

    public String getUnitsSingular()
    {
        return econ.currencyNameSingular();
    }
}
