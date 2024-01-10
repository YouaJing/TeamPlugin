package tcc.youajing.teamplugin;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TeamTabCompleter implements TabCompleter {

    private TeamPlugin plugin;
    private TeamManager teamManager;

    public TeamTabCompleter(TeamPlugin plugin) {
        this.plugin = plugin;
        this.teamManager = plugin.getTeamManager();
    }
    static HashMap<String, String> colors = new HashMap<>();
    static HashMap<String, String> abbrs = new HashMap<>();
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, String alias, String[] args) {
        // 判断命令发送者是否是玩家
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender; // 强制转换为玩家对象

        // 判断命令参数的长度
        if (args.length == 1) {
            // 如果只有一个参数，返回所有子命令的列表
            return Arrays.asList("new", "set副手", "unset副手", "del", "sethome", "rename", "invite" , "accept" , "reject" , "kick", "color", "abbr","list", "members" ,"home","online", "quit" );
        } else if (args.length == 2) {
            // 如果有两个参数，根据第一个参数返回不同的补全列表
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {

//                case "help" :
//                    return null;

                case "new":
                    // 创建团队的子命令，不需要补全
                    return null;

                case "del":
                    // 删除团队的子命令，返回所有团队名，令其选择
                    Team team = teamManager.getTeamByPlayer(player);
                    if (team == null || !team.isLeader(player)) {
                        return null;
                    }
                    List<String> teams = new ArrayList<>();
                    for (Team team1 : teamManager.getTeams()) {
                        teams.add(team1.getName());
                    }
                    return teams;
//                    return Collections.singletonList(team.getName());
                case "rename":
                    //无需补全
                    return null;
                case "sethome":
                    // 设定团队传送点的子命令，不需要补全
                    return null;
                case "home":
                    // 传送到团队传送点的子命令，不需要补全
                    return null;

                case "invite":
                    team = teamManager.getTeamByPlayer(player);
                    // 邀请玩家加入团队的子命令，返回所有在线玩家的名字
                    if (team == null) {
                        return null;
                    }
                    if (!team.isLeader(player) && !team.isFushou(player)) {
                        return null;
                    }
                    List<String> players = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!team.isInTeam(p)) {
                            players.add(p.getName());
                        }
                    }
                    return players;



                case "kick":
                    // 从团队中剔除玩家的子命令，返回团队中所有队员的名字（除了自己）
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        return null;
                    }
                    if (!team.isLeader(player) && !team.isFushou(player)) {
                        return null;
                    }
                    List<String> members = new ArrayList<>();
                    for (UUID uuid : team.getMembers()) {
                        String playerkicked = Bukkit.getOfflinePlayer(uuid).getName();
                        if (playerkicked != null && !playerkicked.equals(player.getName())) {
                            members.add(playerkicked);
                        }
                    }
                    return members;

                case "set副手":
                    //返回团队中所有队员的名字（除了自己）
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null || !team.isLeader(player)) {
                        return null;
                    }
                    members = new ArrayList<>();
                    for (UUID uuid : team.getMembers()) {
                        String playerkicked = Bukkit.getOfflinePlayer(uuid).getName();
                        if (playerkicked != null && !playerkicked.equals(player.getName())) {
                            members.add(playerkicked);
                        }
                    }
                    return members;

                case "unset副手" :
                    return null;

                case "color":
                    // 选择团队颜色的子命令，返回所有颜色代码的列表
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        return null;
                    }
                    if (!team.isLeader(player) && !team.isFushou(player)) {
                        return null;
                    }
                    colors.put("正白", "#FFFFFF");
                    colors.put("正黑", "#000000");
                    colors.put("正红", "#FF0000");
                    colors.put("正蓝", "#0000FF");
                    colors.put("正黄", "#FFFF00");
                    colors.put("青", "#00FFFF");
                    colors.put("紫红", "#FF00FF");
                    colors.put("灰", "#808080");
                    colors.put("橙", "#FFA500");
                    colors.put("棕", "#A52A2A");
                    colors.put("正粉红", "#FFC0CB");
                    colors.put("正浅蓝", "#ADD8E6");
                    colors.put("浅绿", "#90EE90");
                    colors.put("深蓝", "#00008B");
                    colors.put("暗绿", "#006400");
                    colors.put("暗紫", "#800080");
                    colors.put("暗红", "#8B0000");
                    colors.put("土黄", "#FFD700");
                    colors.put("三色堇紫", "#7400a1");
                    colors.put("中岩蓝", "#7b68ee");
                    colors.put("中缘松石", "#48d1cc");
                    colors.put("亮天蓝", "#87cefa");
                    colors.put("亚麻色", "#faf0e6");
                    colors.put("酒红", "#470024");
                    colors.put("亮青", "#e0ffff");
                    colors.put("皇家蓝", "#4169e1");
                    colors.put("品红", "#f400a1");
                    colors.put("卡其色", "#996b1f");
                    colors.put("古铜", "#b87333");
                    colors.put("含羞草黄", "#e6d933");
                    colors.put("孔雀蓝", "#00808c");
                    colors.put("小麦色", "#f5deb3");
                    colors.put("黄绿", "#66ff00");
                    colors.put("暗橙", "#ff8c00");
                    colors.put("暗海绿", "#8fbc8f");
                    colors.put("梅紅色", "#dda0dd");
                    colors.put("水蓝", "#66ffe6");
                    colors.put("淡紫丁香", "#e6cfe6");
                    colors.put("灰土色", "#ccb38c");
                    colors.put("热带橙", "#ff8033");
                    colors.put("灰绿", "#98fb98");
                    colors.put("番茄红", "#ff6347");
                    colors.put("萨克斯蓝", "#4798b3");
                    colors.put("雪色", "#fffafa");
                    colors.put("银色", "#c0c0c0");
                    return new ArrayList<>(colors.keySet());

                case "abbr":
                    // 选择团队缩写的子命令，返回团队名称中的每一个字
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        return null;
                    }
                    if (!team.isLeader(player) && !team.isFushou(player)) {
                        return null;
                    }
                    char[] chars = team.getName().toCharArray();
                    for (char c : chars) {
                        abbrs.put(String.valueOf(c), String.valueOf(c));
                    }
                    return new ArrayList<>(abbrs.keySet());

                case "list":
                    // 查看团队列表的子命令，不需要补全
                    return null;

                case "quit":
                    //
                    return null;
                case "accept":
                    return null;
                case "reject":
                    return null;

                case "members":
                    // 查看团队成员的子命令，返回所有团队的名称
                    List<String> teamNames = new ArrayList<>();
                    for (Team t : teamManager.getTeams()) {
                        teamNames.add(t.getName());
                    } return teamNames;

                case "tp":
                    // 查看团队成员的子命令，返回所有团队的名称
                    if (!player.hasPermission("teamplugin.op")) {
                        return null;
                    }
                     teamNames = new ArrayList<>();
                    for (Team t : teamManager.getTeams()) {
                        teamNames.add(t.getName());
                    } return teamNames;

                default:
                    return null;
            }
        } else {
            // 如果有三个或以上的参数，不需要补全
            return null;
        }
    }

}
