package tcc.youajing.teamplugin;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;





public class Team {
    private TeamManager teamManager;
    private String name;
    private final UUID leader;
    private UUID fushou;
    private List<UUID> members;
    private Location home;
    private String color;
    public Team(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.fushou = null;
        this.members = new ArrayList<>();
        this.home = null;
        this.color = "<#FFFFFF>";
    }

    public String getName() {
        return name;
    }
    public UUID getLeader() {
        return leader;
    }
    public UUID getFushou() { return fushou;}
    public void setFushou(UUID fushou) {
        if (this.fushou != null) {
            members.add(this.fushou);
        }
        this.fushou = fushou;
        members.remove(fushou);
    }
    public List<UUID> getMembers() {
        return members;
    }
    public Location getHome() {
        return home;
    }
    public void setHome(Location home) {
        this.home = home;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void reName(String newName) {
        this.name = newName;
    }
    public boolean isLeader(Player player) {
        return player.getUniqueId().equals(leader);
    }
    public boolean isFushou(Player player) { return player.getUniqueId().equals(fushou); }
    public boolean isInTeam(Player player) {
        return isLeader(player) || isMember(player) || isFushou(player);
    }
    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }
    public void addMember(UUID memberUUID) {
        members.add(memberUUID);
    }
    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
    }
    public void quitFushou() {
        this.fushou = null;
    }
    public void kickMember(UUID memberUUID) {
        members.remove(memberUUID);
    }
    public boolean hasFushou() {
        return this.fushou != null;
    }
}
