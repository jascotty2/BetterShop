/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: handler for economy events
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.jascotty2.bettershop;

import java.util.Map.Entry;
import me.jascotty2.bettershop.enums.EconMethod;
import me.jascotty2.bettershop.utils.BSPermissions;
import me.jascotty2.bettershop.utils.BetterShopLogger;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class BSEcon implements Listener {

	protected static String methodName = null;
	protected static Economy econ = null;
	static BetterShop plugin;
	final PluginManager pm;

	public BSEcon(BetterShop plugin) {
		BSEcon.plugin = plugin;
		pm = plugin.getServer().getPluginManager();
		if (setupEconomy()) {
			methodName = econ.getName();
			BetterShopLogger.Log("Using " + methodName + " (via Vault) for economy");
		}
		else {
			BetterShopLogger.Severe("[BetterShop] Error: Vault not found or Vault failed to register economy. Disabling plugin.");
			pm.disablePlugin(plugin);
		}
	}

	private boolean setupEconomy() {
		Plugin v = plugin.getServer().getPluginManager().getPlugin("Vault");
		if (!(v instanceof Vault)) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static boolean active() {
		return BetterShop.config.econ != EconMethod.AUTO || econ != null;
	}

	public static String getMethodName() {
		if (BetterShop.config.econ == EconMethod.AUTO) {
			return methodName;
		}
		if (BetterShop.config.econ == EconMethod.BULTIN) {
			return "BettershopEcon";
		}
		return "Experience";
	}

	public static boolean hasAccount(Player pl) {
		return pl != null && (BetterShop.config.econ != EconMethod.AUTO
				|| (econ != null && econ.hasAccount(pl.getName())));
	}

	public static boolean canAfford(Player pl, double amt) {
		if (BetterShop.config.econ != EconMethod.AUTO) {
			return pl != null ? getBalance(pl) >= amt : false;
		}
		return pl != null ? getBalance(pl.getName()) >= amt : false;
	}

	public static double getBalance(Player pl) {
		if (pl == null) {
			return 0;
		} else if (BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if (BetterShop.config.econ == EconMethod.EXP) {
			return pl.getExp();
		} else if (BetterShop.config.econ == EconMethod.TOTAL) {
			return pl.getTotalExperience();
		}
		return pl == null ? 0 : getBalance(pl.getName());
	}

	public static double getBalance(String playerName) {
		if (playerName == null) {
			return 0;
		} else if (BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if (BetterShop.config.econ == EconMethod.EXP) {
			Player p = plugin.getServer().getPlayerExact(playerName);
			return p == null ? 0 : p.getExp();
		} else if (BetterShop.config.econ == EconMethod.TOTAL) {
			Player p = plugin.getServer().getPlayerExact(playerName);
			return p == null ? 0 : p.getTotalExperience();
		}
		try {
			if (econ != null && econ.hasAccount(playerName)) {
				return econ.getBalance(playerName);
			}
		} catch (Exception e) {
				BetterShopLogger.Severe("Error looking up player balance.", e, false);
		}
		return 0;
	}

	public static void addMoney(Player pl, double amt) {
		if (BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if (BetterShop.config.econ == EconMethod.EXP) {
			pl.setExp(pl.getExp() + (float) amt);
		} else if (BetterShop.config.econ == EconMethod.TOTAL) {
			pl.setTotalExperience(pl.getTotalExperience() + (int) amt);
		} else {
			addMoney(pl.getName(), amt);
		}
	}

	public static void addMoney(String playerName, double amt) {
		if (BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if (BetterShop.config.econ == EconMethod.EXP) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if (pl != null) {
				pl.setExp(pl.getExp() + (float) amt);
			}
		} else if (BetterShop.config.econ == EconMethod.TOTAL) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if (pl != null) {
				pl.setTotalExperience(pl.getTotalExperience() + (int) amt);
			}
		} else if (econ != null) {
			if (!econ.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			econ.depositPlayer(playerName, amt);
		}
	}

	public static void subtractMoney(Player pl, double amt) {
		if (pl != null) {
			if (BetterShop.config.econ == EconMethod.BULTIN) {
				throw new UnsupportedOperationException("Bultin Not supported yet.");
			} else if (BetterShop.config.econ == EconMethod.EXP) {
				if (pl.getExp() > (int) amt) {
					pl.setExp(pl.getExp() - (float) amt);
				} else {
					pl.setExp(0);
				}
			} else if (BetterShop.config.econ == EconMethod.TOTAL) {
				if (pl.getTotalExperience() > (int) amt) {
					pl.setTotalExperience(pl.getTotalExperience() - (int) amt);
				} else {
					pl.setTotalExperience(0);
				}
			} else {
				subtractMoney(pl.getName(), amt);
			}
		}
	}

	public static void subtractMoney(String playerName, double amt) {
		if (BetterShop.config.econ == EconMethod.BULTIN) {
			throw new UnsupportedOperationException("Bultin Not supported yet.");
		} else if (BetterShop.config.econ == EconMethod.EXP) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if (pl != null) {
				if (pl.getExp() > (int) amt) {
					pl.setExp(pl.getExp() - (float) amt);
				} else {
					pl.setExp(0);
				}
			}
		} else if (BetterShop.config.econ == EconMethod.TOTAL) {
			Player pl = plugin.getServer().getPlayerExact(playerName);
			if (pl != null) {
				if (pl.getTotalExperience() > (int) amt) {
					pl.setTotalExperience(pl.getTotalExperience() - (int) amt);
				} else {
					pl.setTotalExperience(0);
				}
			}
		} else if (econ != null) {
			if (!econ.hasAccount(playerName)) {
				// TODO? add methods for creating an account
				return;
			}
			econ.withdrawPlayer(playerName, amt);
		}
	}

	public static double getPlayerDiscount(Player p) {
		if (p != null && !BSPermissions.has(p, "BetterShop.discount.none")) {
			double discount = Double.NEGATIVE_INFINITY;
			for (Entry<String, Double> g : BetterShop.getSettings().groups.entrySet()) {
				if (BSPermissions.has(p, "BetterShop.discount." + g.getKey())) {
					if(g.getValue() > discount) discount = g.getValue();
				}
			}
			if(discount > Double.NEGATIVE_INFINITY) return discount;
		}
		return 0;
	}

	public static boolean credit(Player player, double amount) {
		return execTransaction(player,amount);
	}

	public static boolean debit(Player player, double amount) {
		return execTransaction(player,-amount);
	}

	private static boolean execTransaction(Player player, double amount) {
		if (econ == null) return false;
		if ((BetterShop.config.econ == EconMethod.AUTO
				&& BetterShop.getSettings().BOSBank != null
				&& !BetterShop.getSettings().BOSBank.trim().isEmpty()
				&& hasBank(BetterShop.getSettings().BOSBank))) {
			return bankTransaction(amount);
		}
		if (amount < 0) {
			if (econ.has(player.getName(), amount)) return econ.withdrawPlayer(player.getName(), -amount).transactionSuccess();
			return false;
		}
		return econ.depositPlayer(player.getName(), amount).transactionSuccess();
	}

	private static boolean bankTransaction(double amount) {
		if (amount < 0) {
			if (econ.bankHas(BetterShop.getSettings().BOSBank,amount).transactionSuccess()) {
				return econ.bankWithdraw(BetterShop.getSettings().BOSBank, -amount).transactionSuccess();
			}
			return false;
		}
		return econ.bankDeposit(BetterShop.getSettings().BOSBank, amount).transactionSuccess();
	}

	public static String format(double amt) {
		return econ.format(amt);
	}

	public static boolean hasBank(String bank) {
		if (econ != null && econ.hasBankSupport()) {
			return econ.bankBalance(bank).transactionSuccess();
		}
		return false;
	}
}