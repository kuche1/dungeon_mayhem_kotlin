
// run with
// clear && kotlinc dungeon_meyhem.kt classes.kt cards.kt colors.kt player.kt board.kt -include-runtime -d dungeon_meyhem.jar && java -jar dungeon_meyhem.jar 6969

package dungeon_mayhem

import java.net.ServerSocket

import classes.Class
import player.Player
import board.Board

fun accept_new_connections(server:ServerSocket, board:Board){
    while(true){
        val sock = server.accept()
        val player = Player(sock=sock)
        board.players += player
        Thread{
            player.select_class(board)
        }.start()
    }
}

fun start_server(port:Int){
    println("starting server on port ${port}")

    var board = Board()
    val server = ServerSocket(port)
    Thread{
        accept_new_connections(server, board)
    }.start()
    board.main_loop()
}

fun main(args:Array<String>){
    require(args.size == 1){"you need to specify the port you want to run the server on"}
    val port_str = args[0]
    val port_int = port_str.toInt()
    start_server(port_int)
}
