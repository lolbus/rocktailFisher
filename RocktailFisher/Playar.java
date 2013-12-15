package RocktailFisher;
import org.powerbot.script.methods.MethodContext;
import org.powerbot.script.wrappers.Player;


public class Playar {

    public static Player getLocal(MethodContext ctx){
	return ctx.players.local();
    }
}
