package de.happybavarian07.api;
 
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;


public class Fireworkgenerator {
 
    Location location;
    Location spawnlocation;
    Firework firework;
    FireworkMeta fireworkmeta;
    FireworkEffect fireworkeffect;
    int LifeTime = 0;
    int Power;
    Plugin plugin;
    
    public Fireworkgenerator(Plugin plugin)
    {
        this.plugin = plugin;
    }
    
    public void setLocation(Location location){
        this.location = location;
    }
    
    public Location getLocation(){
        return firework.getLocation();
    }
    
    public Location getSpawnLocaton(){
        return this.spawnlocation;
    }
    
    public void setEffect(FireworkEffect fireworkEffect){
        this.fireworkeffect = fireworkEffect;
    }
    
    public void addEffect(FireworkEffect fireworkEffect){
            if(this.fireworkeffect == null){
                this.fireworkeffect = fireworkEffect;
            }else{
                if(this.fireworkmeta != null){
            this.fireworkmeta.addEffect(fireworkEffect);
                }
            }
    }
    
    public void setPower(int Power){
        this.Power = Power;
    }
    
    public void setLifeTime(int LifeTime){
        this.LifeTime = LifeTime;
    }
    
    public void clearEffect(){
        if(fireworkmeta != null){
        this.fireworkmeta.clearEffects();
        }
    }
    
    public void teleport(Location location){
        this.location = location;
        if(firework != null){
        this.firework.teleport(this.location);
        }
    }
    
    public void detonate(){
        if(firework != null){
        this.firework.detonate();
        }
    }
    
    public void spawn(){
        this.firework = (Firework)this.location.getWorld().spawnEntity(this.location, EntityType.FIREWORK);
        this.spawnlocation = this.location;
        this.fireworkmeta = (FireworkMeta)this.firework.getFireworkMeta();
        if(this.fireworkeffect != null){
        this.fireworkmeta.addEffect(this.fireworkeffect);
        }
        this.fireworkmeta.setPower(this.Power);
        this.firework.setFireworkMeta(this.fireworkmeta);
        
        if(this.LifeTime != 0){
            Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
                
                @Override
                public void run() {
                    detonate(); 
                }
            }, this.LifeTime);
        }
    }
 
 
    
}