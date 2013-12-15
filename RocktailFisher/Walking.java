package RocktailFisher;

import org.powerbot.script.lang.Filter;
import org.powerbot.script.methods.Menu;
import org.powerbot.script.methods.MethodContext;
import org.powerbot.script.util.Random;
import org.powerbot.script.wrappers.Locatable;
import org.powerbot.script.wrappers.Tile;



public class Walking {

    public static boolean walk(MethodContext ctx, Locatable l){
	return ctx.movement.stepTowards(l);
    }
    
    public static boolean menuSelect(MethodContext ctx, final String s){
	boolean b = ctx.menu.hover(new Filter<Menu.Entry>(){
		@Override
		public boolean accept(Menu.Entry a) {
		    if(a.action.contains(s)){ 
			return true;
		    }
		    return false;
		}
    });
	int timeOut = 0;
	o:while(!b){
		ctx.environment.sleep(200);
		b = ctx.menu.hover(new Filter<Menu.Entry>(){
			@Override
			public boolean accept(Menu.Entry a) {
			    if(a.action.contains(s)){ 
				return true;
			    }
			    return false;
			}
	    });
		timeOut++;
		if(timeOut>10){
		    break o;
		    }
		}
	boolean c = ctx.menu.click(new Filter<Menu.Entry>(){
		@Override
		public boolean accept(Menu.Entry a) {
		    if(a.action.contains(s)){ 
			return true;
		    }
		    return false;
		}
    });

	return (b && c);
    }
    

    
    public static boolean walkTileOnScreen(MethodContext ctx, Tile l){
	if(Playar.getLocal(ctx).isInMotion()){
	    int timeOut = 0;
	    o : while(Playar.getLocal(ctx).isInMotion()){
		ctx.movement.sleep(400);
		timeOut++;
		if(timeOut > 5){
		    break o;
		    }
	    }
	}
	double distanceToDest = ctx.players.local().getLocation().distanceTo(l);
	if(!l.getMatrix(ctx).isOnScreen()){
	    ctx.camera.turnTo(l);
	    ctx.environment.sleep(200,500);
	}
	if(distanceToDest > 0){
	    if(ctx.menu.isOpen()){
		int x, y;
		x = Random.nextInt(460, 700);
		y = Random.nextInt(300, 420);
		ctx.mouse.move(x, y);
		ctx.environment.sleep(Random.nextInt(25, 214));
		x = Random.nextInt(150, 660);
		y = Random.nextInt(300, 420);
		ctx.mouse.move(x, y);
		ctx.environment.sleep(Random.nextInt(400, 670));
		ctx.mouse.click(Random.nextInt(291, 375), Random.nextInt(481, 513), true);
	    }

	    if (ctx.mouse.move(l.getMatrix(ctx)) && l.getMatrix(ctx).click(false)) {
		ctx.environment.sleep(320, 380);
			if (ctx.menu.isOpen()) {
			    return menuSelect(ctx, "Walk here");
			} else {
			    int x, y;
			    x = Random.nextInt(460, 700);
			    y = Random.nextInt(300, 420);
			    ctx.mouse.move(x, y);
			    x = Random.nextInt(150, 660);
			    y = Random.nextInt(300, 420);
			    ctx.mouse.move(x, y);
			    ctx.environment.sleep(Random.nextInt(400, 670));
			    ctx.environment.sleep(400, 800);
			    return l.getMatrix(ctx).click(true);
			}
		    }

	}
	return false;
	}
    
}
