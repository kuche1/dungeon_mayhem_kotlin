
package bot

import java.net.Socket

import player.Player
import board.Board

class Bot(
):Player(
    sock=null,
    is_bot=true,
){
    // no need for output since this is a bot

    override fun write(text:String){
    }

    override fun write_flush(){
    }

    // input needs to be pre-determined since this is a bot

    override fun <T> choice(info:String, choices:Array<T>):T?{
        if(choices.size == 0){
            return null
        }
        return choices.random()
    }

    override fun select_name():String?{
        return "Bot Guy ${(0..999).random()}"
    }

    override fun select_class(board:Board){
        class_ = board.all_classes.random()
        ready = true
    }

}
