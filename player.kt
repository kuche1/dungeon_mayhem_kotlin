
package player

import java.net.Socket
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.InputStreamReader

import cards.Card
import classes.Class
import board.Board
import colors.*

class Player(
    sock:Socket,
    var name:String = "unnamed",
    var ready:Boolean = false,
){
    val input:BufferedReader
    val output:PrintWriter
    var class_:Class = Class()
    var deck:MutableList<Card> = mutableListOf()
    var hand:MutableList<Card> = mutableListOf()
    var board:MutableList<Card> = mutableListOf()
    var discard:MutableList<Card> = mutableListOf()
    var hp:Int = 10
    var thunder:Int = 1
    var shield_penetration_until_end_of_turn:Boolean = false

    init{
        input = BufferedReader(InputStreamReader(sock.inputStream))
        // output = PrintWriter(sock.getOutputStream(), true) // flush on every new line
        output = PrintWriter(sock.getOutputStream()) // don't flush
    }

    // read and write

    fun readln():String{
        val red = input.readLine() // TODO this is null if the bitch has disconnected
        return red
    }
    fun write(text:String){
        output.write(text)
        // output.flush()
    }
    fun writeln(line:String=""){
        // output.println(line)
        write("${line}\n")
        write_flush()
    }
    fun write_flush(){
        output.flush()
    }

    fun write_sep(char:String="~"){
        require(char.length == 1){"not implemented"}
        val repeat = 100
        val sep = char.repeat(repeat)
        writeln(sep)
    }

    fun write_t1(text:String){
        write_sep("~")
        val lines = text.split("\n")
        for(line in lines){
            writeln("~~~ ${line}")
        }
        write_sep("~")
    }

    // print

    override fun toString():String{
        return toString(show_private=false)
    }
    fun toString(show_private:Boolean=false, short:Boolean=false):String{
        var ret = "player `${name}` (${class_})"
        if(show_private){
            ret += " (you)"
        }
        ret += "\n"
        if(!short){
            if(is_dead()){
                ret += "    ${COL_HEAL}dead${COL_RESET}\n"
            }else{
                ret += "    ${COL_HEAL}hp:${hp}${COL_RESET}, ${COL_THUNDER}thunder:${thunder}${COL_RESET}, ${COL_DRAW}cards:${hand.size}${COL_RESET}, deck:${deck.size}, discard:${discard.size}\n"
                if(board.size > 0){
                    ret += "    board:\n"
                    for(card in board){
                    ret += "        ${card}\n"
                    }
                }
                if(show_private){
                    ret += "    hand:\n"
                    for(card in hand){
                        ret += "        ${card}\n"
                    }
                }
            }
        }
        return ret.dropLast(1)
    }

    // choice selection

    fun <T> choice(info:String, choices:Array<T>):T?{
        writeln("make a choice: ${info}")

        // require(choices.size < 100){"not implemented"}
        for(idx in 0..choices.size-1){
            writeln("%2d/%s".format(idx+1, choices[idx]))
        }

        // require(choices.size > 0){"not implemented"}
        if(choices.size == 0){
            writeln("no valid choices available, not selecting anything")
            return null
        }

        if(choices.size == 1){
            writeln("only 1 choice available, automatically selecting")
            return choices[0]
        }

        var choice_int:Int
        while(true){
            write("> ")
            write_flush()
            val choice_str = readln()
            
            try{
                choice_int = choice_str.toInt()
            }catch(nfe: NumberFormatException){
                writeln("choice must be an integer")
                continue
            }

            if(choice_int <= 0){
                writeln("choice must be greter than or equal to 1")
                continue
            }

            if(choice_int > choices.size){
                writeln("choice must be less than or equal to ${choices.size}")
                continue
            }

            choice_int -= 1
            break
        }

        return choices[choice_int]
    }

    // lobby stuff

    fun select_class(board:Board){
        write_t1("welcome to the cum zone")

        while(true){
            val selected_or_null:Class? = choice("select class", board.all_classes)
            val selected = selected_or_null!!

            writeln("you selected ${selected}")
            writeln()
            writeln("${selected}'s cards:")
            selected.print_cards(this)
            val confirm = choice("confirm pick ${selected}?", arrayOf("yes", "no"))
            if(confirm == "yes"){
                class_ = selected
                break
            }
        }

        write_t1("waiting for next round to start")
        ready = true
    }

    // game stuff

    fun on_damaged(damage:Int, damager:Player){ // TODO rename to `damage` or `get_damage` or smt
        if(!damager.shield_penetration_until_end_of_turn){
            for(card in board){
                if(card.shield > 0){
                    card.shield -= damage
                    if(card.shield <= 0){
                        val dmg_left = -card.shield
                        card.destroy(this, damager)
                        if(dmg_left > 0){
                            return on_damaged(dmg_left, damager)
                        }
                    }
                    return
                }
            }
        }
        // getting here means that there are no valid cards on the board to attack
        hp -= damage
    }

    fun on_game_start(){
        // hp
        hp = 10
        // hand
        hand = mutableListOf()
        // board
        board = mutableListOf()
        // generate deck
        deck = class_.generate_deck(this).toMutableList().shuffled().toMutableList()
        // discard
        discard = mutableListOf()
        // draw
        draw()
        draw()
        draw()
    }

    fun on_game_end(board:Board){
        ready = false
        Thread{
            select_class(board)
        }.start()
    }

    private fun shuffle_discard_into_deck(){
        // shuffle discard and put into deck
        deck = discard
        discard = mutableListOf()
        deck = deck.shuffled().toMutableList()
    }

    fun draw(){
        if(deck.size == 0){
            shuffle_discard_into_deck()
        }

        // TODO not sure if this is fast or slow
        val card = deck[0]
        deck.removeAt(0)
        hand += card
    }

    fun heal(amount:Int){
        hp += amount
        if(hp > 10){
            hp = 10
        }
    }

    fun is_alive():Boolean{
        return hp > 0
    }
    fun is_dead():Boolean{
        return !is_alive()
    }

    fun play_turn(board:Board){
        board.on_player_turn(this)

        if(is_dead()){
            // TODO deal 1 dmg to someone
            return
        }

        draw()
        thunder = 1

        // board.write_board_state() // TODO this is fucky

        while(thunder > 0){
            board.write_board_state()
            play_a_card_from_hand(board)
            thunder -= 1
            // board.write_board_state() // TODO or maybe this is fucky instead?
        }

        shield_penetration_until_end_of_turn = false
    }

    private fun play_a_card_from_hand(board:Board){
        val card_or_null = choice("select a card to play", hand.toTypedArray())
        val card = card_or_null!!

        card.summon(this, board)

        if(hand.size == 0){
            draw()
            draw()
        }
    }
}
