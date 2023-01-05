
// run with
// clear && kotlinc dungeon_meyhem.kt classes.kt cards.kt colors.kt player.kt board.kt bot.kt -include-runtime -d dungeon_meyhem.jar && java -jar dungeon_meyhem.jar 6969 3

// TODO
// attack only players to the left/right
// ghosts
// spectators
// not being able to join in the middle of the game
// show owner in card selection

package dungeon_mayhem

import java.net.ServerSocket

import classes.Class
import player.Player
import board.Board
import bot.Bot

private fun add_bot(board:Board){
    val bot = Bot()
    board.players += bot
    bot.select_name_and_class(board)
}

private fun accept_new_connections(server:ServerSocket, board:Board){
    while(true){
        val sock = server.accept()
        val player = Player(sock=sock)
        board.players += player
        Thread{
            player.select_name_and_class(board)
        }.start()
    }
}

private fun start_server(port:Int, bots:Int){
    println("starting server on port ${port}")

    var board = Board()
    val server = ServerSocket(port)
    
    // accept new connections
    Thread{
        accept_new_connections(server, board)
    }.start()

    // add some bots
    for(_tmp in 0..bots){
        Thread{
            add_bot(board)
        }.start()
    }

    board.main_loop()
}

fun main(args:Array<String>){
    require(args.size >= 1){"you need to specify the port you want to run the server on"}
    val port_str = args[0]
    val port_int = port_str.toInt()

    require(args.size >= 2){"you need to specify the number of bots"}
    val bots_str = args[1]
    val bots_int = bots_str.toInt()

    start_server(port_int, bots_int)
}
