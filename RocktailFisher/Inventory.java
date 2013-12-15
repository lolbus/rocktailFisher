package RocktailFisher;


import org.powerbot.event.PaintListener;
import org.powerbot.script.AbstractScript;
import org.powerbot.script.Manifest;
import org.powerbot.script.PollingScript;
import org.powerbot.script.lang.BasicNamedQuery;
import org.powerbot.script.lang.Filter;
import org.powerbot.script.lang.GroundItemQuery;
import org.powerbot.script.methods.Game;
import org.powerbot.script.methods.GroundItems;
import org.powerbot.script.methods.MethodContext;
import org.powerbot.script.methods.Objects;
import org.powerbot.script.methods.Players;
import org.powerbot.script.methods.Widgets;
import org.powerbot.script.wrappers.Area;
import org.powerbot.script.wrappers.GameObject;
import org.powerbot.script.wrappers.GroundItem;
import org.powerbot.script.wrappers.Item;
import org.powerbot.script.wrappers.Player;
import org.powerbot.script.wrappers.Tile;


public class Inventory {
    
    public static Item getItem(MethodContext ctx, int... id){
	return ctx.backpack.select().id(id).first().poll();
    }
    public static Item[] getItems(MethodContext ctx){
	return ctx.backpack.getAllItems();
    }
    public static int getCount(MethodContext ctx, int id){
	return ctx.backpack.select().id(id).count();
    }
    public static int getCount(MethodContext ctx, int... id){
	return ctx.backpack.select().id(id).count();
    }
    public static int getCount(MethodContext ctx){
	return ctx.backpack.select().count();
    }

}
