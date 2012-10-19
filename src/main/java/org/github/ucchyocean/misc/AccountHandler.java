package org.github.ucchyocean.misc;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.github.ucchyocean.hitandblow.HitAndBlowException;

public class AccountHandler {

    public static Economy econ = null;
    public static Permission perms = null;

    public AccountHandler() throws HitAndBlowException {

    	Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
    	if ( !(plugin instanceof Vault) ) {
    		throw new HitAndBlowException("Vault was not loaded!");
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
