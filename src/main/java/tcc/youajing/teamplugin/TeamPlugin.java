package tcc.youajing.teamplugin;

import crypticlib.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;


public class TeamPlugin extends BukkitPlugin {
    private TeamManager teamManager;

    private TeampluginExpansion expansion;
    @Override
    public void enable() {
        //TODO
        // 初始化团队管理器和监听器
        teamManager = new TeamManager(this);
        expansion = new TeampluginExpansion(this);

        // 注册命令和监听器
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("team").setTabCompleter(new TeamTabCompleter(this));
        expansion.register();


        // 加载团队数据
        teamManager.loadTeams();
        // team,启动！
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.GREEN + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.GREEN + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.GREEN + "#来自尤精的插件已启动#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.GREEN + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.GREEN + "######################");
    }

    @Override
    public void disable() {
        //TODO
        teamManager.saveTeams();
        if (expansion != null) {
            expansion.unregister();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.DARK_RED + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.DARK_RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.DARK_RED + "#来自尤精的插件已启动#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.DARK_RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "[Team]" + ChatColor.DARK_RED + "######################");
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

}