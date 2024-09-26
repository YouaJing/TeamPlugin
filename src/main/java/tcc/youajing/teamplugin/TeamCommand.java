package tcc.youajing.teamplugin;

import crypticlib.util.MsgUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import java.util.*;

public class TeamCommand implements CommandExecutor {
    private TeamPlugin plugin;
    private TeamManager teamManager;
    private HashMap<Player, Team> invitations = new HashMap<>();
    private HashMap<Player, String> deletingTeams = new HashMap<>();

    public TeamCommand(TeamPlugin plugin) {
        this.plugin = plugin;
        this.teamManager = plugin.getTeamManager();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                player.sendMessage(ChatColor.YELLOW + "什么！不会用Team插件？？");
                player.sendMessage(ChatColor.GOLD + "快给我去看《TCC社区游玩手册》！！！");
                return true;
            }


            String subcommand = args[0].toLowerCase();


            switch (subcommand) {
                case "get":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team get <玩家ID>");
                        return true;
                    }

                    String targetName = args[1];
                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
                    if (targetPlayer == null) {
                        sender.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "玩家不存在！");
                        return true;
                    }
                    if (teamManager.getTeamByOfflinePlayer(targetPlayer) == null) {
                        sender.sendMessage("你还没有加入组织捏");
                        return true;
                    }
                    String teamName2 = teamManager.getTeamByOfflinePlayer(targetPlayer).getName();
                    String teamColor = teamManager.getTeamByOfflinePlayer(targetPlayer).getColor().replaceAll("[<>]", "");

                    sender.sendMessage(String.format("{\"team_color\":\"%s\",\"team_name\":\"%s\"}", teamColor, teamName2));
                    return true;


                case "new":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team new <名称>");
                        return true;
                    }

                    int playTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000; // 获取玩家的总游玩时长，单位为小时

                    if (teamManager.getTeamByPlayer(player) != null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你已经在一个团队中了！请先退出你所在的团队才能使用此指令！");
                        return true;
                    }
                    String name = args[1];
                    if (!name.matches("[a-zA-Z0-9_\u4e00-\u9fa5]+")) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队名称只能包含字母、数字、下划线和中文！");
                        return true;
                    }
                    int length = 0;
                    for (char c : name.toCharArray()) {
                        if (c >= '\u4e00' && c <= '\u9fa5') {
                            length += 2;
                        } else {
                            length += 1;
                        }
                    }
                    if (length > 12) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队名称不得超过六个汉字（12个字符）！");
                        return true;
                    }

                    if (teamManager.hasTeam(name)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队名称已经被注册过了！");
                        return true;
                    }

                    if (player.getLevel() < 100) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你需要至少100级经验才能创建团队！谢谢");
                        return true;
                    }

                    if (playTime < 48) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你的总游玩时长不足48小时，不能创建团队！");
                        return true;
                    }
                    player.setLevel(player.getLevel() - 100);
                    teamManager.createTeam(name, player);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.GOLD + "``经验等级-100");
                    player.sendMessage(ChatColor.GOLD + "恭喜，你创建了一个名为" + name + "的团队");
                    player.sendMessage(ChatColor.GOLD + "当团队规模达到" + ChatColor.YELLOW + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.GOLD + "人，你可以使用 /team sethome 为团队设置传送点并且可以使用 /team color 选择团队名称颜色");

                    return true;

                case "color":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team color <颜色别称>");
                        return true;
                    }
                    Team team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }

                    if (!team.isLeader(player) & !team.isFushou(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长、副手才能选择团队颜色！");
                        return true;
                    }

                    int teamSize = team.getMembers().size() + 1;
                    if (team.hasFushou()) {
                        teamSize += 1;
                    }
                    if (teamSize < 5) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队规模小于5人无法选择颜色！");
                        return true;
                    }
                    String colorName = args[1];

                    if (!TeamTabCompleter.colors.containsKey(colorName)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "请选择列表中的颜色");
                        return true;
                    }
                    if (player.getLevel() < 3) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你需要至少3级经验才能更改团队颜色！");
                        return true;
                    }
                    String colorCode = "<" + TeamTabCompleter.colors.get(colorName) + ">";

                    player.setLevel(player.getLevel() - 3);
                    team.setColor(colorCode);
                    teamManager.saveTeams();
                    player.sendMessage(ChatColor.GOLD + "``经验等级-3");
                    player.sendMessage(ChatColor.GOLD + "你成功选择了团队" + MsgUtil.color("&" + TeamTabCompleter.colors.get(colorName)) + team.getName() + ChatColor.GOLD + "的颜色为" + MsgUtil.color("&" + TeamTabCompleter.colors.get(colorName)) + colorName + ChatColor.GOLD + "！");

                    return true;

                case "abbr":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team abbr <简称>");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }

                    if (!team.isLeader(player) & !team.isFushou(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长、副手才能设定团队简称！");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }

                    teamSize = team.getMembers().size() + 1;
                    if (team.hasFushou()) {
                        teamSize += 1;
                    }
                    if (teamSize < 5) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队规模小于5人无法设定团队简称！");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }
                    String abbr = args[1];

                    if (!TeamTabCompleter.abbrs.contains(abbr)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "请选择列表中的文字");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }
                    if (player.getLevel() < 3) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你需要至少3级经验才能改变团队简称！");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }
                    if (Objects.equals(abbr, team.getAbbr())) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "已经选定了这个字作为简称");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }
                    player.setLevel(player.getLevel() - 3);
                    team.setAbbr(abbr);
                    teamManager.saveTeams();
                    TeamTabCompleter.abbrs.clear();
                    player.sendMessage(ChatColor.GOLD + "``经验等级-3");
                    player.sendMessage(ChatColor.GOLD + "你成功为团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "设定了简称" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + " [" + abbr + "]" + ChatColor.GOLD + "！");

                    return true;

                case "enabbr":
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.YELLOW + "参数过少，用法: /team enabbr <组织名> <简称>");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }

                    // 判断玩家是否有teamplugin.op权限
                    if (!player.hasPermission("teamplugin.op")) {
                        player.sendMessage(ChatColor.DARK_RED + "你没有权限执行这个命令！");
                        return true;
                    }
                    // 获取第二个参数，作为团队名
                    String teamName = args[1];

                    // 判断团队是否存在
                    if (!teamManager.hasTeam(teamName)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队不存在！");
                        return true;
                    }

                    // 获取团队对象
                    team = teamManager.getTeam(teamName);

                    teamSize = team.getMembers().size() + 1;
                    if (team.hasFushou()) {
                        teamSize += 1;
                    }
                    if (teamSize < 5) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队规模小于5人无法设定团队简称！");
                        TeamTabCompleter.abbrs.clear();
                        return true;
                    }
                    abbr = args[2];

                    team.setAbbr(abbr);
                    teamManager.saveTeams();
                    player.sendMessage(ChatColor.GOLD + "你成功为团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "设定了简称" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + " [" + abbr + "]" + ChatColor.GOLD + "！");

                    return true;

                case "del":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team del <名称>");
                        return true;
                    }
                    name = args[1];
                    // 获取团队对象
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null || !Objects.equals(team.getName(), name)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "请输入正确的你的团队名！");
                        return true;
                    }

                    // 判断玩家是否是队长
                    if (!team.isLeader(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "啊？？只有队长才能删除团队！");
                        return true;
                    }


                    // 判断玩家是否有足够的经验等级
                    if (player.getLevel() < 30) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你需要至少30级经验才能删除团队！");
                        return true;
                    }

                    player.setLevel(player.getLevel() - 30);
                    teamManager.deleteTeam(team.getName());
                    player.sendMessage(ChatColor.GREEN + "你成功删除了团队" + name + "!");
                    return true;

                case "sethome":

                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }

                    // 判断玩家是否是队长
                    if (!team.isLeader(player) & !team.isFushou(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长、副手才能设定团队传送点！");
                        return true;
                    }

                    teamSize = team.getMembers().size() + 1;
                    if (team.hasFushou()) {
                        teamSize += 1;
                    }
                    if (teamSize < 5) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队规模小于5人无法设置传送点！");
                        return true;
                    }

                    // 获取玩家的脚下坐标
                    Location location = player.getLocation();

                    // 设置团队传送点
                    team.setHome(location);

                    // 发送成功消息
                    player.sendMessage(ChatColor.GOLD + "你成功为团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "设定了传送点！");
                    teamManager.saveTeams();
                    return true;

                case "enforcesethome":
                    // 判断玩家是否有teamplugin.op权限
                    if (!player.hasPermission("teamplugin.op")) {
                        player.sendMessage(ChatColor.DARK_RED + "你没有权限执行这个命令！");
                        return true;
                    }
                    // 获取第二个参数，作为团队名
                    String teamName1 = args[1];
                    // 判断团队是否存在
                    if (!teamManager.hasTeam(teamName1)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队不存在！");
                        return true;
                    }
                    // 获取团队对象
                    team = teamManager.getTeam(teamName1);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }

                    // 获取玩家的脚下坐标
                    location = player.getLocation();

                    // 设置团队传送点
                    team.setHome(location);

                    // 发送成功消息
                    player.sendMessage(ChatColor.GOLD + "你成功为团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "设定了传送点！");
                    teamManager.saveTeams();
                    return true;

                case "enforcesetcolor":

                    // 判断玩家是否有teamplugin.op权限
                    if (!player.hasPermission("teamplugin.op")) {
                        player.sendMessage(ChatColor.DARK_RED + "你没有权限执行这个命令！");
                        return true;
                    }
                    // 获取第二个参数，作为团队名
                    teamName1 = args[1];
                    // 判断团队是否存在
                    if (!teamManager.hasTeam(teamName1)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队不存在！");
                        return true;
                    }
                    // 获取团队对象
                    team = teamManager.getTeam(teamName1);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }
                    colorName = args[2];
                    colorCode = "<" + colorName + ">";

                    team.setColor(colorCode);
                    teamManager.saveTeams();
                    player.sendMessage(ChatColor.GOLD + "``经验等级-3");
                    player.sendMessage(ChatColor.GOLD + "你成功选择了团队" + MsgUtil.color("&" + TeamTabCompleter.colors.get(colorName)) + team.getName() + ChatColor.GOLD + "的颜色为" + MsgUtil.color("&" + colorName + ChatColor.GOLD + "！"));
                    return true;


                case "home":
                    // 传送到团队传送点的子命令
                    // 判断玩家是否在一个团队中
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }

                    // 判断玩家是否是队员或队长
                    if (!team.isMember(player) && !team.isLeader(player) && !team.isFushou(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有团队成员才能传送到团队传送点！");
                        return true;
                    }

                    // 获取团队传送点
                    location = team.getHome();

                    // 判断团队是否有设定传送点
                    if (location == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你的团队还没有设定传送点！请让团队的队长使用 /team sethome 来设定传送点");
                        return true;
                    }
                    teamManager.teleport(player, location);
                    player.sendMessage(ChatColor.GOLD + "你成功传送到了团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "的传送点！");

                    return true;

                case "tp":
                    // 传送到团队传送点的子命令
                    // 判断参数是否足够
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team tp <团队名>");
                        return true;
                    }
                    // 判断玩家是否有teamplugin.op权限
                    if (!player.hasPermission("teamplugin.op")) {
                        player.sendMessage(ChatColor.DARK_RED + "你没有权限执行这个命令！");
                        return true;
                    }
                    // 获取第二个参数，作为团队名
                    teamName = args[1];
                    // 判断团队是否存在
                    if (!teamManager.hasTeam(teamName)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队不存在！");
                        return true;
                    }

                    // 获取团队对象
                    team = teamManager.getTeam(teamName);

                    // 获取团队传送点
                    location = team.getHome();

                    // 判断团队是否有设定传送点
                    if (location == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队还没有设定传送点！请让团队的队长使用 /team sethome 来设定传送点");
                        return true;
                    }

                    // 传送玩家到团队传送点
                    teamManager.teleport(player, location);
                    player.sendMessage(ChatColor.GOLD + "你成功传送到了团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "的传送点！");

                    return true;


                case "invite":

                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team invite <玩家>");
                        return true;
                    }
                    // 获取第二个参数，作为目标玩家的名字
                    targetName = args[1];

                    // 获取目标玩家对象
                    Player target = Bukkit.getPlayer(targetName);

                    // 判断目标玩家是否在线
                    if (target == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你邀请的玩家不在线！");
                        return true;
                    }

                    // 判断目标玩家游玩时长是否满足24h
                    playTime = target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000;
                    if (playTime < 24) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你邀请的玩家的总游玩时长不足24小时，无法邀请他加入团队！");
                        return true;
                    }
                    // 判断玩家是否在团队中
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你还没有拥有团队，请先使用/team new <名称>创建一个团队！");
                        return true;
                    }
                    // 判断玩家是否是队长
                    if (!team.isLeader(player) & !team.isFushou(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长、副手才能邀请玩家！");
                        return true;
                    }

                    // 判断目标玩家是否是自己
                    if (target.equals(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "NONONONO不能邀请自己！");
                        return true;
                    }

                    // 判断目标玩家是否已经在一个团队中
                    if (teamManager.getTeamByPlayer(target) != null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个玩家已经在一个团队中了！");
                        return true;
                    }

                    //目标玩家提示音
                    player.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    //发起人提示音
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    // 发送邀请消息给目标玩家
                    target.sendMessage(ChatColor.GOLD + "你收到了来自" + player.getName() + "的团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "的邀请，接受请输入/team accept，拒绝请输入/team reject。");

                    //发送成功消息给玩家
                    player.sendMessage(ChatColor.GOLD + "你成功邀请了" + target.getName() + "加入你的团队！请等待" + target.getName() + "的回复!");
                    //将玩家与队伍添加到等待列表
                    invitations.put(target, team);


                    return true;

                case "kick":

                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team kick <玩家>");
                        return true;
                    }

                    // 判断玩家是否在一个团队中
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }

                    // 判断玩家是否是队长
                    if (!team.isLeader(player) & !team.isFushou(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长、副手才能剔除玩家！");
                        return true;
                    }
                    // 获取第二个参数，作为目标玩家的名字
                    targetName = args[1];
                    String playerKickName = null;
                    UUID playerKickUUID = null;
                    // 获取目标玩家对象
                    for (UUID uuid : team.getMembers()) {
                        playerKickName = Bukkit.getOfflinePlayer(uuid).getName();
                        playerKickUUID = uuid;
                        if (playerKickName != null) {
                            if (playerKickName.equals(targetName)) {
                                break; // 跳出循环，不再继续遍历
                            } else playerKickName = null;
                        }
                    }
                    if (playerKickName == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个玩家不在你的团队中！");
                        return true;
                    }

                    // 判断目标玩家是否是自己
                    if (playerKickName.equals(player.getName())) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不能剔除自己！");
                        return true;
                    }

                    // 从团队中移除目标玩家

                    team.kickMember(playerKickUUID);
                    teamManager.saveTeams();
                    // 发送被剔除消息给目标玩家
                    if (Bukkit.getPlayer(playerKickName) != null) {
                        Bukkit.getPlayer(playerKickName).sendMessage(ChatColor.DARK_RED + "你被" + player.getName() + "从团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.DARK_RED + "中剔除了！");
                    }

                    // 发送成功消息给玩家
                    player.sendMessage(ChatColor.GOLD + "你成功剔除了" + playerKickName + "从你的团队！");

                    return true;

                case "set副手":

                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team setdeputy <玩家>");
                        return true;
                    }

                    // 判断玩家是否在一个团队中
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }

                    // 判断玩家是否是队长
                    if (!team.isLeader(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长才能设置团队副手！");
                        return true;
                    }
                    // 获取第二个参数，作为目标玩家的名字
                    targetName = args[1];
                    String fushouName = null;
                    UUID fushouUUID = null;
                    // 获取目标玩家对象
                    for (UUID uuid : team.getMembers()) {
                        fushouName = Bukkit.getOfflinePlayer(uuid).getName();
                        fushouUUID = uuid;
                        if (fushouName != null) {
                            if (fushouName.equals(targetName)) {
                                break; // 跳出循环，不再继续遍历
                            } else fushouName = null;
                        }
                    }

                    if (fushouName == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个玩家不在你的团队中！");
                        return true;
                    }
                    // 判断目标玩家是否是自己
                    if (fushouName.equals(player.getName())) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不能将自己设置为团队副手！");
                        return true;
                    }

                    // 将目标玩家设为团队副手
                    team.setFushou(fushouUUID);
                    teamManager.saveTeams();

                    if (Bukkit.getPlayer(fushouName) != null) {
                        Bukkit.getPlayer(fushouName).sendMessage(ChatColor.GOLD + "你被" + player.getName() + "设置为团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "的副手！");
                    }

                    // 发送成功消息给玩家
                    player.sendMessage(ChatColor.GOLD + "你成功将" + fushouName + "设置为你的团队副手！");

                    return true;

                case "unset副手":
                    // 判断玩家是否在一个团队中
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }
                    // 判断玩家是否是队长
                    if (!team.isLeader(player) && !team.isFushou(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长以及副手本人才能取消团队副手！");
                        return true;
                    }
                    fushouName = Bukkit.getOfflinePlayer(team.getFushou()).getName();
                    team.addMember(team.getFushou());
                    team.quitFushou();
                    teamManager.saveTeams();

                    if (player.getName().equals(fushouName)) {
                        player.sendMessage(ChatColor.GOLD + "你不再担任团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "的副手！");
                    } else {
                        player.sendMessage(fushouName + ChatColor.GOLD + "不再担任你的团队副手");
                    }
                    return true;

                case "rename":
                    // 更改团队名的子命令
                    // 判断参数是否足够
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team rename <新名称>");
                        return true;
                    }
                    // 判断玩家是否在一个团队中
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }

                    // 判断玩家是否是队长
                    if (!team.isLeader(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "只有队长才能更改团队名！");
                        return true;
                    }
                    // 获取第二个参数，作为新的团队名
                    String newName = args[1];

                    // 判断新的团队名是否合法
                    if (!newName.matches("[a-zA-Z0-9_\u4e00-\u9fa5]+")) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队名称只能包含字母、数字、下划线和中文！");
                        return true;
                    }

                    // 判断新的团队名的长度是否超过六个汉字（12个字符）
                    length = 0;
                    for (char c : newName.toCharArray()) {
                        if (c >= '\u4e00' && c <= '\u9fa5') {
                            length += 2; // 每个汉字占两个字符
                        } else {
                            length += 1; // 其他字符占一个字符
                        }
                    }
                    if (length > 12) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队名称不得超过六个汉字（12个字符）！");
                        return true;
                    }

                    // 判断新的团队名是否已存在
                    if (teamManager.hasTeam(newName)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队名称已经被注册过了！");
                        return true;
                    }

                    // 判断玩家是否有足够的经验等级
                    if (player.getLevel() < 100) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你需要至少100级经验才能更改团队名！");
                        return true;
                    }

                    // 扣除玩家100级经验
                    player.setLevel(player.getLevel() - 100);

                    // 获取原来的团队名
                    String oldName = team.getName();

                    // 更改团队名
                    team.reName(newName);
                    teamManager.replaceTeam(newName, oldName, team);
                    teamManager.deleteTeamFromConfig(oldName);


                    // 发送成功消息给玩家
                    player.sendMessage(ChatColor.GOLD + "``经验等级-100");
                    player.sendMessage("你成功将你的团队名从" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + oldName + ChatColor.RESET + "更改为" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + newName + ChatColor.RESET + "!");

                    // 发送通知消息给团队中的其他玩家和队长
                    for (UUID uuid : team.getMembers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GOLD + "你们的团队名已经被" + player.getName() + "更改为" + newName + "!");
                        }
                    }

                    return true;

                case "list":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "如果要翻页的话: /team list <页码>");
                    }
                    // 查看团队列表的子命令
                    // 获取所有团队对象的列表
                    List<Team> teams = teamManager.getTeams();

                    // 判断是否有团队存在
                    if (teams.isEmpty()) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "目前没有任何团队！");
                        return true;
                    }

                    // 按照团队规模排序，人数大的排在最上面
                    teams.sort(Comparator.comparingInt(t -> t.getMembers().size() + 1)); // 加一是为了包括队长
                    Collections.reverse(teams); // 反转顺序

                    // 获取第2个参数，作为页码，默认为1
                    int page = 1;

                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "页码必须是一个整数！");
                            return true;
                        }
                    }

                    // 定义每页显示的团队数
                    int pageSize = 10;

                    // 计算总页数
                    int totalPages = (teams.size() - 1) / pageSize + 1;

                    // 判断页码是否合法
                    if (page < 1 || page > totalPages) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "页码必须在1到" + totalPages + "之间！");
                        return true;
                    }

                    // 发送团队列表的标题
                    player.sendMessage(ChatColor.GOLD + "已注册的团队列表（第" + page + "页/共" + totalPages + "页）：");

                    // 发送团队列表的内容，每行显示一个团队的名称、颜色、规模和队长
                    for (int i = (page - 1) * pageSize; i < page * pageSize && i < teams.size(); i++) {
                        Team t = teams.get(i);
                        String listName = t.getName();
                        int size = t.getMembers().size();
                        if (t.hasLeader()) {
                            size += 1;
                        }
                        String leaderName;
                        if (t.hasFushou()) {
                            size += 1;
                            fushouName = Bukkit.getOfflinePlayer(t.getFushou()).getName();
                            leaderName = Bukkit.getOfflinePlayer(t.getLeader()).getName();
                            player.sendMessage(ChatColor.GRAY + "- " + ChatColor.AQUA + ChatColor.BOLD + listName);
                            player.sendMessage(ChatColor.GRAY + "      [" + size + "人] 队长: " + leaderName + "  副手：" + fushouName);
                        } else {
                            if (t.hasLeader()) {
                                leaderName = Bukkit.getOfflinePlayer(t.getLeader()).getName();
                                player.sendMessage(ChatColor.GRAY + "- " + ChatColor.AQUA + ChatColor.BOLD + listName);
                                player.sendMessage(ChatColor.GRAY + "      [" + size + "人] 队长: " + leaderName);
                            } else {
                                player.sendMessage(ChatColor.GRAY + "- " + ChatColor.AQUA + ChatColor.BOLD + listName);
                            }
                        }
                    }

                    return true;

                case "members":
                    // 查看团队成员的子命令
                    // 判断参数是否足够
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team members <队伍名>");
                        return true;
                    }
                    // 获取第二个参数，作为团队名
                    teamName = args[1];
                    // 判断团队是否存在
                    if (!teamManager.hasTeam(teamName)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队不存在！");
                        return true;
                    }

                    // 获取团队对象
                    team = teamManager.getTeam(teamName);

                    // 创建一个字符串缓冲区，用于存储团队成员的名字
                    StringBuilder members = new StringBuilder();

                    // 获取团队的队长的名字，将其添加到字符串缓冲区的最前面，用[队长]的标签标识
                    String leaderName = null;
                    if (team.hasLeader()) {
                        leaderName = Bukkit.getOfflinePlayer(team.getLeader()).getName();
                        members.append(ChatColor.AQUA).append(leaderName).append("[队长], ").append(ChatColor.GRAY);
                    }
                    if (team.hasFushou()) {
                        fushouName = Bukkit.getOfflinePlayer(team.getFushou()).getName();
                        members.append(ChatColor.AQUA).append(fushouName).append("[副手], ").append(ChatColor.GRAY);
                    }

                    // 遍历团队中的所有成员，将他们的名字添加到字符串缓冲区中，用逗号分隔
                    for (UUID uuid : team.getMembers()) {
                        String memberName = Bukkit.getOfflinePlayer(uuid).getName();
                        if (memberName != null) {
                            members.append(memberName).append(", ");
                        }
                    }

                    // 发送团队成员的标题
                    player.sendMessage(ChatColor.GOLD + "团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "的成员列表：");

                    // 发送团队成员的内容
                    player.sendMessage(ChatColor.GRAY + "- " + members);
                    return true;

                case "online":
                    // 查看在线团队成员的子命令
                    // 获取团队对象
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！");
                        return true;
                    }
                    // 创建一个字符串缓冲区，用于存储团队成员的名字
                    StringBuilder onlineMembers = new StringBuilder();

                    // 获取团队的队长的名字，将其添加到字符串缓冲区的最前面，用[队长]的标签标识
                    if (team.hasLeader()) {
                        leaderName = Bukkit.getOfflinePlayer(team.getLeader()).getName();
                        if (leaderName != null && Bukkit.getPlayer(leaderName) != null) {
                            onlineMembers.append(ChatColor.AQUA).append(leaderName).append("[队长], ").append(ChatColor.GRAY);
                        }
                    }
                    if (team.hasFushou()) {
                        fushouName = Bukkit.getOfflinePlayer(team.getFushou()).getName();
                        if (fushouName != null && Bukkit.getPlayer(fushouName) != null) {
                            onlineMembers.append(ChatColor.AQUA).append(fushouName).append("[副手], ").append(ChatColor.GRAY);
                        }
                    }

                    // 遍历团队中的所有成员，将他们的名字添加到字符串缓冲区中，用逗号分隔
                    for (UUID uuid : team.getMembers()) {
                        String memberName = Bukkit.getOfflinePlayer(uuid).getName();
                        if (memberName != null && Bukkit.getPlayer(memberName) != null) {
                            onlineMembers.append(memberName).append(", ");
                        }
                    }

                    // 发送团队成员的标题
                    player.sendMessage(ChatColor.GOLD + "团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "在线的成员列表：");

                    // 发送团队成员的内容
                    player.sendMessage(ChatColor.GRAY + "- " + onlineMembers.toString());

                    return true;

                case "accept":
                    // 接受团队邀请的子命令


                    // 判断玩家是否已经在一个团队中
                    if (teamManager.getTeamByPlayer(player) != null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "哥，你已经有团队了！");
                        invitations.remove(player);
                        return true;
                    }
                    // 判断玩家是否收到了邀请
                    if (!invitations.containsKey(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "没有任何团队向你发出邀请！");
                        return true;
                    }

                    // 获取邀请的团队对象
                    team = invitations.get(player);

                    // 判断团队是否还存在
                    if (!teamManager.hasTeam(team.getName())) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队已经不存在了！");
                        invitations.remove(player);
                        return true;
                    }

                    // 判断目标玩家游玩时长是否满足24h
                    playTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000;
                    if (playTime < 24) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你的总游玩时长不足24小时，无法加入团队！");
                        return true;
                    }
                    // 将玩家加入团队
                    team.addMember(player.getUniqueId());
                    teamManager.saveTeams();
                    // 从邀请列表中移除玩家
                    invitations.remove(player, team);
                    // 被邀请者成功提示音
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    // 发送成功消息给玩家
                    player.sendMessage(ChatColor.GOLD + "你成功加入了团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName());
                    // 发送通知消息给团队中的其他玩家和队长
                    for (UUID uuid : team.getMembers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null && !p.equals(player)) {
                            player.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            p.sendMessage(ChatColor.GOLD + player.getName() + "加入了你们的团队！");
                        }
                    }
                    if (team.hasLeader()) {
                        Player leader = Bukkit.getPlayer(team.getLeader());
                        if (leader != null) {
                            player.playSound(leader, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            leader.sendMessage(ChatColor.GOLD + player.getName() + "加入了你的团队！");
                        }
                    }
                    if (team.hasFushou()) {
                        Player fushou1 = Bukkit.getPlayer(team.getFushou());
                        if (fushou1 != null) {
                            player.playSound(fushou1, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            fushou1.sendMessage(ChatColor.GOLD + player.getName() + "加入了你的团队！");
                        }
                    }

                    return true;


                case "reject":
                    // 判断玩家是否已经在一个团队中
                    if (teamManager.getTeamByPlayer(player) != null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "哥，你已经有团队了！");
                        invitations.remove(player);
                        return true;
                    }
                    if (!invitations.containsKey(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你没有收到任何团队邀请!");
                        return true;
                    }
                    // 获取邀请的团队对象
                    team = invitations.get(player);

                    if (!teamManager.hasTeam(team.getName())) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队已经不存在了！");
                        invitations.remove(player);
                        return true;
                    }
                    // 从邀请列表中移除玩家
                    invitations.remove(player);

                    // 发送成功消息给玩家
                    player.sendMessage("你拒绝了来自" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "的团队邀请！");

                    // 发送通知消息给团队的队长

                    if (team.hasLeader()) {
                        Player leader = Bukkit.getPlayer(team.getLeader());
                        if (leader != null) {
                            leader.sendMessage(ChatColor.GOLD + player.getName() + "加入了你的团队！");
                        }
                    }
                    if (team.hasFushou()) {
                        Player fushou1 = Bukkit.getPlayer(team.getFushou());
                        if (fushou1 != null) {
                            fushou1.sendMessage(ChatColor.GOLD + player.getName() + "加入了你的团队！");
                        }
                    }
                    return true;

                case "enforcerename":
                    // 判断玩家是否有teamplugin.op权限
                    if (!player.hasPermission("teamplugin.op")) {
                        player.sendMessage(ChatColor.DARK_RED + "你没有权限执行这个命令！");
                        return true;
                    }
                    // 获取第二个参数，作为团队名
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "用法: /team rename <新名称>");
                        return true;
                    }
                    teamName1 = args[1];
                    // 判断团队是否存在
                    if (!teamManager.hasTeam(teamName1)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队不存在！");
                        return true;
                    }
                    // 获取团队对象
                    team = teamManager.getTeam(teamName1);
                    // 获取第二个参数，作为新的团队名
                    newName = args[2];

                    // 判断新的团队名是否合法
                    if (!newName.matches("[a-zA-Z0-9_\u4e00-\u9fa5]+")) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队名称只能包含字母、数字、下划线和中文！");
                        return true;
                    }

                    // 判断新的团队名的长度是否超过六个汉字（12个字符）
                    length = 0;
                    for (char c : newName.toCharArray()) {
                        if (c >= '\u4e00' && c <= '\u9fa5') {
                            length += 2; // 每个汉字占两个字符
                        } else {
                            length += 1; // 其他字符占一个字符
                        }
                    }
                    if (length > 12) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "团队名称不得超过六个汉字（12个字符）！");
                        return true;
                    }

                    // 判断新的团队名是否已存在
                    if (teamManager.hasTeam(newName)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "这个团队名称已经被注册过了！");
                        return true;
                    }


                    // 获取原来的团队名
                    oldName = team.getName();

                    // 更改团队名
                    team.reName(newName);
                    teamManager.replaceTeam(newName, oldName, team);
                    teamManager.deleteTeamFromConfig(oldName);


                    // 发送成功消息给玩家
                    player.sendMessage("你成功将你的团队名从" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + oldName + ChatColor.RESET + "更改为" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + newName + ChatColor.RESET + "!");

                    // 发送通知消息给团队中的其他玩家和队长
                    for (UUID uuid : team.getMembers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.GOLD + "你们的团队名已经被" + player.getName() + "更改为" + newName + "!");
                        }
                    }

                    return true;


                case "quit":
                    // 判断玩家是否在一个团队中
                    team = teamManager.getTeamByPlayer(player);
                    if (team == null) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "你不在一个团队中！大哥你没有团队咋退出啊！？");
                        return true;
                    }
                    // 判断玩家是否是队长
                    if (team.isLeader(player)) {
                        player.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "哼！想逃？如果你想解散团队，请使用/team del <名称>命令！");
                        return true;
                    }
                    // 从团队中移除玩家
                    if (team.isFushou(player)) {
                        team.quitFushou();
                    } else {
                        team.removeMember(player);
                    }
                    teamManager.saveTeams();
                    // 发送成功消息给玩家
                    player.sendMessage(ChatColor.GOLD + "你成功退出了团队" + MsgUtil.color("&" + team.getColor().replace("<", "").replace(">", "")) + team.getName() + ChatColor.GOLD + "！");

                    // 发送通知消息给团队中的其他玩家和队长
                    for (UUID uuid : team.getMembers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage(ChatColor.DARK_RED + "很遗憾" + player.getName() + "退出了你们的团队！");
                        }
                    }
                    if (team.hasLeader()) {
                        Player leader = Bukkit.getPlayer(team.getLeader());
                        if (leader != null) {
                            leader.sendMessage(ChatColor.DARK_RED + "很遗憾" + player.getName() + "退出了你们的团队！");
                        }
                    }
                    if (team.hasFushou()) {
                        Player fushou1 = Bukkit.getPlayer(team.getFushou());
                        if (fushou1 != null) {
                            fushou1.sendMessage(ChatColor.DARK_RED + "很遗憾" + player.getName() + "退出了你们的团队！");
                        }
                    }
                    return true;
            }


        } else {
            String subcommand = args[0].toLowerCase();


            switch (subcommand) {
                case "get":
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.YELLOW + "用法: /team get <玩家ID>");
                        return true;
                    }

                    String targetName = args[1];
                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
                    if (targetPlayer == null) {
                        sender.sendMessage(ChatColor.DARK_RED + "错误：" + ChatColor.GOLD + "玩家不存在！");
                        return true;
                    }
                    if (teamManager.getTeamByOfflinePlayer(targetPlayer) == null) {
                        sender.sendMessage("你还没有加入组织捏");
                        return true;
                    }
                    sender.sendMessage(teamManager.getTeamByOfflinePlayer(targetPlayer).getName());
                    return true;
            }

        }
        return false;
    }
}