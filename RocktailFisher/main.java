package RocktailFisher;




import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.powerbot.event.PaintListener;
import org.powerbot.script.Manifest;
import org.powerbot.script.PollingScript;
import org.powerbot.script.lang.Filter;
import org.powerbot.script.methods.Hud;
import org.powerbot.script.methods.Menu;

import org.powerbot.script.methods.Skills;
import org.powerbot.script.util.GeItem;
import org.powerbot.script.util.Random;
import org.powerbot.script.wrappers.Area;
import org.powerbot.script.wrappers.Component;
import org.powerbot.script.wrappers.GameObject;
import org.powerbot.script.wrappers.Interactive;
import org.powerbot.script.wrappers.Item;
import org.powerbot.script.wrappers.Npc;
import org.powerbot.script.wrappers.Player;
import org.powerbot.script.wrappers.Tile;

@Manifest(name = "Standard RockTail", 
description = "Just your standard rocktail. Fishes rocktail for profit..", 
version = 1.0, authors = { "lolbus" }
)

public class main extends PollingScript implements PaintListener
{
    private int tries = 0;
    private boolean menuSelect(Interactive interactive, final String action, final String option, double distance) {
	    if(tries > 50){
		tries = 0;
		return false;
	    }
		if ((interactive!=null && interactive.isValid()) && interactive.isOnScreen()) {
			final Filter<Menu.Entry> filter = new Filter<Menu.Entry>() {
				@Override
				public boolean accept(Menu.Entry arg0) {
					return arg0.action.equalsIgnoreCase(action) && arg0.option.equalsIgnoreCase(option);
				}	
			};
			if (ctx.menu.click(filter)) {
				//boolean b = didInteract();
				tries = 0;
				//log("Interact status: "+b);
				return true;
			} else {  
			    if(distance > 5){
				return false;
			    }
			    tries ++;
				ctx.mouse.move(interactive);
				return menuSelect(interactive, action, option, distance);
			}
		}
		//log("Interact a null is invalid!");
		return false;
	}
    
    private final Tile CENTRAL_BANK_TILE = new Tile(3655, 5113, 0);
    private final int ROCKTAIL_SPAWN_ID = 8842;
    private final int ROCKTAIL_ID = 15270;
    private long startTime;
    private int rocktailPrice;
    private int fishingLevel;
    private int initialExp;
    private final Tile fishSpot[] = {new Tile(3645, 5084, 0), new Tile(3629, 5084, 0), new Tile(3618, 5089, 0), new Tile(3613, 5112, 0)};
    
    private final Area rockTailArea = new Area(new Tile(3800, 5200, 0),
	    new Tile(3499, 5000, 0));

    private final Area bankArea = new Area(new Tile(3660, 5116, 0), new Tile(
	    3650, 5111, 0));
    
    private boolean atBank() {
	return ctx.game.isLoggedIn() && bankArea.contains(Playar.getLocal(ctx).getLocation());
    } 
    private boolean atCave() {
	Area area = new Area(new Tile(3010, 9839, 0), new Tile(3063, 9760, 0));
	Player p = Playar.getLocal(ctx);
	return area.contains(p.getLocation());
    }
    private boolean atFalador() {
	Area area = new Area(new Tile(2936, 3396, 0), new Tile(3070, 3340, 0));
	Player p = Playar.getLocal(ctx);
	return area.contains(p.getLocation());
    } 
    private boolean atRockTail() {
	return (ctx.game.getClientState() == 11 && rockTailArea.contains(Playar
		.getLocal(ctx).getLocation()));
    }
    private int getMyFishingLevel() {
   	return ctx.skills.getLevel(Skills.FISHING);
    }
    private boolean fishAnimation() {
	return Playar.getLocal(ctx).getAnimation() == 623;
    }
    private double distanceTo(Tile b) {
	return ctx.players.local().getLocation().distanceTo(b);
    }
    private double distanceTo(Tile a, Tile b) {
	return a.distanceTo(b);
    }
    private boolean nearTile(Tile c){
	return distanceTo(c) < 3;
    }
    private boolean atFishSpot(){
	for(int i =0; i <fishSpot.length; i++){
	    if(nearTile(fishSpot[i])){
		return true;
	    } else continue;
	}
	return false;
	//return nearTile(SPOT_A_TILE) || nearTile(SPOT_B_TILE) || nearTile(SPOT_C_TILE) || nearTile(fishSpot[3]);
    }
    private boolean strikerHitAnimation() {
	int ani = Playar.getLocal(ctx).getAnimation();
	return (ani == 424 || ani == 1156 || ani == 4177);
    }
    private int startingRocktail;
    private int bankedRocktail;
    private int totalRocktail;
    private boolean safe = true;
    private boolean inCombat() {
	if (Playar.getLocal(ctx).getAnimation() == 423
		|| strikerHitAnimation()
		|| Playar.getLocal(ctx).isInCombat()
		|| (Playar.getLocal(ctx).getAnimation() > 0 && !fishAnimation() && !atBank())) {
	    safe = false;
	    return true;
	}
	return false;
    }
    private int fishingIndex = 0;
    private boolean validRSExistence(){
	Npc rocktail = ctx.npcs.select().select(new Filter<Npc>(){
	    @Override
	    public boolean accept(Npc a) {
		return (a.getId() == ROCKTAIL_SPAWN_ID) && (distanceTo(fishSpot[fishingIndex], a.getLocation())< 3 );
	    }
	}).first().poll();
	log("Rocktail "+rocktail.getLocation() + "  "+rocktail.isOnScreen()+ "  "+rocktail.getId() + "   "+(rocktail==null)+ "   "+rocktail.isValid());
	return rocktail.isValid();
    }
    
    private boolean rest() {
	boolean r = false;
	Component bootButton = ctx.widgets.get(1465, 4);
	bootButton.interact("Rest");
	sleep(2000);
	
	int i = 0;
	while (ctx.game.getClientState() == 11
		&& Playar.getLocal(ctx).getAnimation() > 0) {
	    sleep(1000);
	    if (ctx.movement.getEnergyLevel() > 25 || i > 20) {
		r = true;
		break;
	    }
	    i++;
	}
	return r;
    }
    private Tile deathTile;
    private boolean traverseTo(Tile t) {
	try {
	    if (atBank() && ctx.movement.getEnergyLevel() < 20) {
		rest();
		return false;
	    }
	    if (ctx.movement.getEnergyLevel() > 7) {
		ctx.movement.setRunning(true);
		sleep(500);
	    }
	    log("Trying to travese to: " + t);
	    if (inCombat() && deathTile == null) {
		t = CENTRAL_BANK_TILE;
	    }
	    if (atRockTail() || atCave() || atFalador()) {
		Tile destination = t;
		if (destination != null) {
		   return Walking.walk(ctx, destination);
		}
	    }
	} catch (Exception e) {}
	return false;
    }
    
    private boolean homeRun() {
	if (!atBank()) {
	    traverseTo(CENTRAL_BANK_TILE);
	}
	return false;
    }
    
    public void clickPulley() {
	try {
	    Component asker = ctx.widgets.get(1188, 6);
	    if (!asker.isValid()) {
		if(!ctx.hud.isVisible(Hud.Window.BACKPACK)){
		    ctx.hud.view(Hud.Window.BACKPACK); 
		    ctx.hud.sleep(1000,1800);
		}
		Item rocktail = Inventory.getItem(ctx, ROCKTAIL_ID);
		if (rocktail != null) {
		    log("gonna click");
		    rocktail.interact("Use");
		    ctx.backpack.sleep(1500, 1700);
		    GameObject pulley = ctx.objects.select().id(45079).poll();
		    if (pulley.isValid()) {
			ctx.camera.turnTo(pulley);
			menuSelect(pulley, "Use", "Raw rocktail -> Pulley lift", 0);
			sleep(1500);
		    }
		}
	    } else {
		bankedRocktail += Inventory.getCount(ctx, ROCKTAIL_ID);
		asker.click();
	    }
	} catch (Exception e) {
	}
    }
    
    private boolean atTargetFishspot(){
	return nearTile(fishSpot[fishingIndex]);
    }

    private enum status {LOGIN, STARTUP, RUN_TO_FISH_A, RUN_TO_FISH_B, RUN_TO_FISH_C, RUN_TO_FISH_D, FISH_A, FISH_B, FISH_C, FISH_D, FISHING, RUN_TO_BANK, BANK, ESCAPE_COMBAT, UNKNOWN}
    private status getStatus(){
	if(!ctx.game.isLoggedIn()){
	    return status.LOGIN;
	}
	if(firstLoop){
	    return status.STARTUP;
	}
	if(inCombat() || !safe){
	    return status.ESCAPE_COMBAT;
	}
	if(Inventory.getCount(ctx) == 28 && atBank()){
	    fishingIndex = 0;
	    return status.BANK;
	}
	if(Inventory.getCount(ctx) < 28 && (atBank() || !atTargetFishspot())){
	    if(fishingIndex == 0)
		return status.RUN_TO_FISH_A;
	    else if (fishingIndex == 1)
		return status.RUN_TO_FISH_B;
	    else if(fishingIndex == 2)
		return status.RUN_TO_FISH_C;
	    else if(fishingIndex == 3)
		return status.RUN_TO_FISH_D;
	}
	if(Inventory.getCount(ctx) < 28 && nearTile(fishSpot[0]) && !fishAnimation()){
	    return status.FISH_A;
	}
	if(Inventory.getCount(ctx) < 28 && nearTile(fishSpot[1]) && !fishAnimation()){
	    return status.FISH_B;
	}
	if(Inventory.getCount(ctx) < 28 && nearTile(fishSpot[2]) && !fishAnimation()){
	    return status.FISH_C;
	}
	if(Inventory.getCount(ctx) < 28 && nearTile(fishSpot[3]) && !fishAnimation()){
	    return status.FISH_D;
	}
	if(Inventory.getCount(ctx) < 28 && atFishSpot() && fishAnimation() && Playar.getLocal(ctx).getInteracting().isValid()){
	    return status.FISHING;
	}
	if(Inventory.getCount(ctx) == 28 && !atBank()){
	    return status.RUN_TO_BANK;
	}
	if(Inventory.getCount(ctx) == 28 && atBank()){
	    return status.BANK;
	}
	return status.UNKNOWN;
    }
    private void log(String s){
	log.info(s);
    }
    
    public void wiggle() {
	if (!inCombat()) {
	    int x, y;
	    x = Random.nextInt(460, 700);
	    y = Random.nextInt(300, 420);
	    ctx.mouse.move(x, y);
	    sleep(Random.nextInt(25, 214));
	    x = Random.nextInt(150, 660);
	    y = Random.nextInt(300, 420);
	    ctx.mouse.move(x, y);
	    sleep(Random.nextInt(1, 100));
	}
    }
    
    public static void notice() {
	try {
	    java.awt.Toolkit.getDefaultToolkit().beep();
	} catch (Exception e) {}
    }
    private int deathCount = 0;
    private final Color color3 = new Color(255, 50, 0);
    private final Color color4 = new Color(0, 0, 0);
    private final Color color5 = new Color(0, 255, 0);
    private final BasicStroke stroke1 = new BasicStroke(1);
    private int profit = 0;
    private long hours, minutes, seconds;

    @Override
    public void repaint(Graphics painting) {
	Graphics2D paint = (Graphics2D) painting;
	paint.setColor(color3);
	paint.fillOval((int)(ctx.mouse.getLocation().getX() - 2), (int)(ctx.mouse.getLocation().getY() - 2), 4, 4);
	paint.setStroke(stroke1);
	paint.drawOval((int)(ctx.mouse.getLocation().getX() - 3), (int)(ctx.mouse.getLocation().getY() - 3), 6, 6);
	paint.setColor(color5);
	paint.drawLine((int)ctx.mouse.getLocation().getX() + 16, (int)ctx.mouse.getLocation().getY() - 10, (int)ctx.mouse.getLocation().getX() - 16, (int)ctx.mouse.getLocation().getY() + 6);
	paint.drawLine((int)ctx.mouse.getLocation().getX() + 16, (int)ctx.mouse.getLocation().getY() - 6, (int)ctx.mouse.getLocation().getX() - 16, (int)ctx.mouse.getLocation().getY() + 10);
	paint.drawLine((int)ctx.mouse.getLocation().getX() - 16, (int)ctx.mouse.getLocation().getY() - 10, (int)ctx.mouse.getLocation().getX() + 16, (int)ctx.mouse.getLocation().getY() + 6);
	paint.drawLine((int)ctx.mouse.getLocation().getX() - 16, (int)ctx.mouse.getLocation().getY() - 6, (int)ctx.mouse.getLocation().getX() + 16, (int)ctx.mouse.getLocation().getY() + 10);
	paint.setColor(color4);
	profit = totalRocktail * rocktailPrice;
	//paint.drawString("World: " + atWorld + "  ", 440, 369);
	paint.drawString("Rocktails fished: " + totalRocktail, 600, 369);
	hours = (System.currentTimeMillis() - startTime) / 1000 / 3600;
	minutes = (System.currentTimeMillis() - startTime) / 1000 / 60- (hours * 60);
	seconds = (System.currentTimeMillis() - startTime) / 1000 - hours* 3600 - minutes * 60;
	paint.drawString("Time elapsed: " + (int) hours + ":" + (int) minutes+ ":" + (int) seconds, 600, 385);
	paint.drawString("Per hour: "+ (int) (totalRocktail * 3600 / (double) ((System.currentTimeMillis() - startTime) / 1000)),600, 401);
	int expGained = ctx.skills.getExperience(Skills.FISHING) - initialExp;
	double perHr = 3600000.0 / (System.currentTimeMillis() - startTime)* expGained;
	paint.drawString("Exp gained: " + (expGained) + " per/hr: " + Math.round(perHr), 600, 441);
	if (perHr > 10) {
	    int expTnl = ctx.skills.getExperienceAt(getMyFishingLevel() + 1) - ctx.skills.getExperience(Skills.FISHING);
	    if (getMyFishingLevel() < 99) {
		paint.drawString("Exp TNL: " + expTnl + " Estimate hour TNL: " + (float) (expTnl / perHr), 600, 461);
	    }
	}
	paint.drawString("Profit: " + profit + " per hour: " + (int) 3600000.0 / (System.currentTimeMillis() - startTime) * profit, 600, 481);
	paint.drawString("Death Count: " + deathCount, 600, 496);
    }
    
    private int random(int min, int max){
	return Random.nextInt(min, max);
    }

    private boolean interactRocktail(){

	Npc rocktailShoal = ctx.npcs.select().select(new Filter<Npc>(){
	    @Override
	    public boolean accept(Npc a) {
		return a.getId() == ROCKTAIL_SPAWN_ID && (distanceTo(a.getLocation(), fishSpot[fishingIndex]) < 4);
	    }
	    
	}).first().poll();
	if(rocktailShoal.isValid()){
	    if(!rocktailShoal.isOnScreen()){
		ctx.camera.turnTo(rocktailShoal);
		ctx.npcs.sleep(1000, 1400);
	    }
	   return rocktailShoal.click();
	}
	else return false;
    }
    private boolean firstLoop = true;

    @Override
    public int poll() { // this is my loop!
	if(getStatus() == status.LOGIN){
	    return 1000;
	}
	if(getStatus() == status.STARTUP){
	    log("Perform startup..");
	    initialExp = ctx.skills.getExperience(Skills.FISHING);
	    fishingLevel = ctx.skills.getLevel(Skills.FISHING);
	    startTime = System.currentTimeMillis();
	    rocktailPrice = GeItem.getProfile(ROCKTAIL_ID).getPrice(GeItem.PriceType.CURRENT).getPrice();
	    if(atRockTail() && !atBank()){
		double nearestDistFishSpot = 99999;
		int nearestDistFishSpotIndex = 0;
		for(int i =0; i <fishSpot.length; i++){
		    if(distanceTo(fishSpot[i]) < nearestDistFishSpot){
			nearestDistFishSpotIndex = i;
			nearestDistFishSpot = distanceTo(fishSpot[i]);
		    }
		    fishingIndex = nearestDistFishSpotIndex;
		}
	    }
	    startingRocktail = Inventory.getCount(ctx, ROCKTAIL_ID);
	    bankedRocktail = 0;
	    totalRocktail = 0;
	    ctx.game.setPreferredWorld(84);
	    log("Done.. StartingRocktail: "+startingRocktail+ " Fishing Level: "+fishingLevel+ " Rocktail price:"+rocktailPrice+ " Preferedworld"+ctx.game.getPreferredWorld());
	    sleep(2000);
	    firstLoop = false;
	}
	totalRocktail = Inventory.getCount(ctx, ROCKTAIL_ID) - startingRocktail + bankedRocktail;
	log("Fished rocktail: "+totalRocktail);
	if(nearTile(fishSpot[fishingIndex]) && atFishSpot() && !validRSExistence() ){
	    fishingIndex++;
	    if(fishingIndex > 2){ // you can activate the 4th fishing point by changing this to 3! but the golem are alot more aggressive on the 4th fishing spot as there are lesser crowd available
		fishingIndex = 0;
	    }
	} else {
	    log("Spawn is valid!");
	}
	log("Status: "+getStatus()+ " FishingIndex "+fishingIndex);
	if(getStatus() == status.RUN_TO_BANK){
	    homeRun();
	    return 1200;
	}
	if(getStatus() == status.BANK){
	    clickPulley();
	    return 1200;
	}
	if(getStatus() == status.FISHING){
	    if(ctx.hud.isVisible(Hud.Window.BACKPACK)){
		ctx.hud.close(Hud.Window.BACKPACK);
	    }
	    if(random(0,1088) > 608){
		wiggle();
		return random(388,888);
	    }
	    return 1000;
	}
	if(getStatus() == status.ESCAPE_COMBAT){
	    if(!atBank()){
		homeRun();
		safe = false;
	    } else {
		fishingIndex = 0;
		rest();
		ctx.environment.sleep(15000,20000);
		safe = true;
	    }
	    return 800;
	}
	if(getStatus() == status.RUN_TO_FISH_A || getStatus() == status.RUN_TO_FISH_B || getStatus() == status.RUN_TO_FISH_C || getStatus() == status.RUN_TO_FISH_D ){
	    if(!nearTile(fishSpot[fishingIndex])){
		traverseTo(fishSpot[fishingIndex]);
	    }
	    return 800;
	}
	if(getStatus() == status.FISH_A || getStatus() == status.FISH_B || getStatus() == status.FISH_C || getStatus() == status.FISH_D){
	    interactRocktail();
	    return 1000;
	}
	return 500;
    }
}