
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
    // IO
    val input:BufferedReader
    val output:PrintWriter
    var disconnected:Boolean = false
    // game stuff
    var class_:Class = Class()
    private var deck:MutableList<Card> = mutableListOf()
    private var hand:MutableList<Card> = mutableListOf()
    private var field:MutableList<Card> = mutableListOf()
    private var discard:MutableList<Card> = mutableListOf()
    var hp:Int = 0
    var thunder:Int = 0
    // card effects
    var shield_penetration_until_end_of_turn:Boolean = false
    var attacks_hit_all_opponents_until_next_turn:Boolean = false

    init{
        input = BufferedReader(InputStreamReader(sock.inputStream))
        // output = PrintWriter(sock.getOutputStream(), true) // flush on every new line
        output = PrintWriter(sock.getOutputStream()) // don't flush
    }

    // toString

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
            ret += "    ${COL_HEAL}"
            if(disconnected){
                ret += "disconnected"
            }else if(!ready){
                ret += "not ready"
            }else if(is_dead()){
                ret += "dead"
            }else{
                // is alive and well
                ret += "hp:${hp}"
            }
            ret += COL_RESET

            if(is_alive()){
                ret += ", ${COL_THUNDER}thunder:${thunder}${COL_RESET}, ${COL_DRAW}cards:${hand.size}${COL_RESET}, deck:${deck.size}, discard:${discard.size}\n"
                if(shield_penetration_until_end_of_turn){
                    ret += "    attacks penetrate shield until end of turn\n"
                }
                if(attacks_hit_all_opponents_until_next_turn){
                    ret += "    attacks hit all opponents until next turn\n"
                }

                
                ret += "    field:\n"
                if(field.size == 0){
                    ret += "        no cards on field"
                }else{
                    for(card in field){
                        ret += "        ${card}\n"
                    }
                }

                if(show_private){
                    ret += "    hand:\n"
                    for(card in hand){
                        ret += "        ${card}\n"
                    }
                }
            }else{
                ret += "\n"
            }
        }
        return ret.dropLast(1)
    }

    // read and write

    fun readln():String?{ // result is `null` if player has disconnected
        write("> ")
        write_flush()

        val red = input.readLine()
        if(red == null){
            hp = 0
            ready = false
            disconnected = true
        }
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
        val repeat = 120
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

    // choice selection

    fun <T> choice(info:String, choices:Array<T>):T?{
        writeln()
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
            // write("> ")
            // write_flush()
            val choice_str = readln()
            if(choice_str == null){
                return null
            }
            
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

    // drawing, removing card, switching hands...

    fun get_hand_size():Int{
        return hand.size
    }

    fun get_shield_cards_on_field():Array<Card>{
        var shields:Array<Card> = arrayOf()
        for(card in field){
            if(card.shield_max > 0){
                shields += card
            }
        }
        return shields
    }

    fun add_card_to_hand(card:Card){
        hand += card
        card.owner = this
    }

    fun remove_card_from_hand(card:Card){
        hand -= card
    }

    fun add_card_to_field(card:Card){
        field += card
        card.owner = this
    }

    fun remove_card_from_field(card:Card){
        field -= card
    }

    fun add_card_to_discard(card:Card){
        discard += card
        card.owner = this
    }

    fun is_this_card_on_your_field(card:Card):Boolean{
        for(card_f in field){
            if(card_f == card){
                return true
            }
        }
        return false
    }

    fun draw(){
        if(deck.size == 0){
            shuffle_discard_into_deck()
        }

        // TODO not sure if this is fast or slow
        // we could switch it up for the last card instead
        val card = deck[0]
        deck.remove(card)
        add_card_to_hand(card)
    }

    fun discard_hand(){
        while(hand.size > 0){
            remove_card_from_hand(hand[0])
        }
        draw()
        draw()
    }

    fun switch_hands(with:Player){
        val other_hand = with.hand
        with.hand = hand
        hand = other_hand

        for(card in hand){
            card.owner = this
        }
        for(card in with.hand){
            card.owner = with
        }
    }

    fun pop_last_card_from_discard():Card?{
        if(discard.size == 0){
            return null
        }
        val last_card = discard.last()
        discard.remove(last_card)
        return last_card
    }

    fun choose_and_pop_card_from_discard():Card?{
        val card = choice("select a card from your discard pile", discard.toTypedArray())
        if(card == null){
            return null
        }
        discard -= card
        return card
    }

    // lobby stuff

    fun select_name_and_class(board:Board){
        write_t1("welcome to the cum zone")
        writeln("what's your name?")
        val selected_name = readln()
        if(selected_name == null){
            return
        }
        name = selected_name

        select_class(board)
    }

    fun select_class(board:Board){
        while(true){
            val selected:Class? = choice("select class", board.all_classes)
            if(selected == null){ // the little bitch disconnected in the class selection screen
                return
            }

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

    fun say(text:String, board:Board){
        board.writeln()
        board.writeln("${toString(short=true)}: ${text}")
    }

    // game stuff

    fun on_damaged(damage:Int, damager:Player){ // TODO rename to `damage` or `get_damage` or smt
        if(!damager.shield_penetration_until_end_of_turn){
            for(card in field){
                if(card.shield > 0){
                    card.shield -= damage
                    if(card.shield <= 0){
                        val dmg_left = -card.shield
                        card.destroy(damager)
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
        // field
        field = mutableListOf()
        // generate deck // TODO instead of doing that, generate the deck on player creation and just call the shuffle_discard function
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

        attacks_hit_all_opponents_until_next_turn = false

        if(!ready){ // TODO we also need a way to disconect newly connected players (or do we?)
            return
        }
        if(is_dead()){
            // TODO deal 1 dmg to someone
            return
        }
        draw()
        thunder = 1

        while(thunder > 0){
            board.write_board_state()
            play_a_card_from_hand(board)
            thunder -= 1
        }

        shield_penetration_until_end_of_turn = false
    }

    private fun play_a_card_from_hand(board:Board){
        val card = choice("select a card to play", hand.toTypedArray())
        if(card == null) { // disconnected
            return
        }

        card.summon(this, board)

        if(hand.size == 0){
            draw()
            draw()
        }
    }
}
