package tcc.youajing.teamplugin;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.*;




public class TeamManager {
    private TeamPlugin plugin;
    private File file;
    private FileConfiguration config;
    private Map<String, Team> teams;
    public TeamManager(TeamPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "teams.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.teams = new HashMap<>();
    }

    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }
    public Team getTeam(String name) {
        return teams.get(name);
    }
    public boolean hasTeam(String name) {
        return teams.containsKey(name);
    }
    public boolean createTeam(String name, Player leader) {
        if (hasTeam(name)) {
            return false;
        }
        Team team = new Team(name, leader.getUniqueId());
        teams.put(name, team);
        saveTeams();
        return true;
    }

    public boolean deleteTeam(String name) {
        if (!hasTeam(name)) {
            return false;
        }
        Team team = getTeam(name);
        for (UUID uuid: team.getMembers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage("你所在的队伍已被队长删除！");
            }
        }
        config.set(name, null);
        teams.remove(name);
        // 从配置文件中移除这个团队的数据

        saveTeams();
        return true;
    }

    public boolean replaceTeam(String newKey, String oldKey, Team newTeam) {

        teams.put(newKey, newTeam);
        teams.remove(oldKey);
        saveTeams();
        return true;
    }
    public boolean deleteTeamFromConfig(String name) {
        config.set(name, null);
        saveTeams();
        return true;
    }

    public Team getTeamByPlayer(Player player) {
        for (Team team: getTeams() ){
            if (team.isInTeam(player)) {
                return team;
            }
        }
        return null;
    }

    public void loadTeams() {
        for (String key: config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            String name = key;
            String leaderId = section.getString("leader");
            String fushouId = section.getString("fushou");
            List<String> membersId = section.getStringList("members");
            Location home = section.getLocation("home");
            String color = section.getString("color");
            String abbr = section.getString("abbr");

            String[] leaderSplit;
            UUID leaderUUID = null;
            if (leaderId != null) {
                leaderSplit = leaderId.split(",");
                leaderUUID = UUID.fromString(leaderSplit[1]);
            }
            String[] fushouSplit;
            UUID fushouUUID = null;
            if (fushouId != null) {
                fushouSplit = fushouId.split(",");
                fushouUUID = UUID.fromString(fushouSplit[1]);
            }


            Team team = new Team(name, leaderUUID);
            team.setHome(home);
            team.setColor(color);
            team.setAbbr(abbr);
            team.setFushou(fushouUUID);

            for (String memberId: membersId) {
                String[] memberSplit = memberId.split(",");
                UUID memberUUID = UUID.fromString(memberSplit[1]);
                if (memberUUID != null) {
                    team.addMember(memberUUID);
                }
            }

            teams.put(name, team);
        }
    }

//    public void reloadTeams() {
//        teams.clear();
//        loadTeams();
//        saveTeams();
//        System.out.println("团队数据保存成功！...");
//    }
//    public void clearTeams() {
//        teams.clear();
//        System.out.println("团队数据清除成功！...");
//    }

    public void saveTeams() {
        System.out.println("开始保存团队数据...");
        for (Team team: getTeams()) {
            String name = team.getName();
            UUID leader = team.getLeader();
            UUID fushou = team.getFushou();
            List<UUID> members = team.getMembers();
            Location home = team.getHome();
            String color = team.getColor();
            String abbr = team.getAbbr();
            String leaderName;
            String leaderId = null;
            if (leader != null) {
                leaderName = Bukkit.getOfflinePlayer(leader).getName();
                leaderId = leaderName + "," + leader;
            }
            String fushouName;
            String fushouId = null;
            if (fushou != null) {
                fushouName = Bukkit.getOfflinePlayer(fushou).getName();
                fushouId = fushouName + "," + fushou;
            }
            ConfigurationSection section = config.createSection(name);
            if (leader != null) {
                section.set("leader", leaderId);
            }
            if (fushou != null) {
                section.set("fushou", fushouId);
            }
            List<String> membersId = new ArrayList<>();
            for (UUID uuid : members) {
                String memberName = Bukkit.getOfflinePlayer(uuid).getName();
                String memberId = memberName + "," + uuid;
                membersId.add(memberId);
            }
            section.set("members", membersId);
            section.set("home", home);
            section.set("color", color);
            section.set("abbr", abbr);
        }
        try {
            config.save(file);
            System.out.println("团队数据保存成功！...");
        } catch (IOException e) {
            System.out.println("团队数据保存失败！...");
            e.printStackTrace();
        }

    }

    public void teleport(Player player, Location location) {
        //传送模块
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    plugin.getPlatform().teleportPlayer(player, location);
    }
}











