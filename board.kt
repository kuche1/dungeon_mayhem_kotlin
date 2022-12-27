
package board

import classes.ALL_CLASSES
import player.Player
import cards.Card
import colors.*

class Board(){
    val all_classes = ALL_CLASSES
    var players:Array<Player> = arrayOf()
    var dmg_cards_target:Player? = null
    var dmg_cards_target_until_players_next_turn:Player? = null

    // writing

    fun writeln(msg:String=""){
        for(player in players){
            if(player.ready){
                player.writeln(msg)
            }
        }
    }

    fun write_t1(msg:String){
        for(player in players){
            if(player.ready){
                player.write_t1(msg)
            }
        }
    }

    fun write_sep(){
        for(player in players){
            player.write_sep()
        }
    }

    fun write_board_state(){
        write_sep()

        if(dmg_cards_target != null){
            writeln("${ICON_DMG} cards target: ${dmg_cards_target!!.toString(short=true)}")
            writeln()
        }

        for(player in players){
            for(player_draw in players){
                if(player != player_draw){
                    player.writeln()
                    player.writeln(player_draw.toString())
                }
            }
            player.writeln()
            player.writeln(player.toString(show_private=true))
        }
        writeln()
    }

    // on event

    fun on_game_start(){
        dmg_cards_target = null
        dmg_cards_target_until_players_next_turn = null

        for(player in players){
            player.on_game_start()
        }
    }

    fun on_player_turn(player:Player){
        if(player == dmg_cards_target_until_players_next_turn){
            dmg_cards_target = null
            dmg_cards_target_until_players_next_turn = null
        }
    }

    // choosing

    fun choose_opponent(caster:Player):Player?{
        var targets:Array<Player> = arrayOf()
        for(player in players){
            if(player.is_dead() || player == caster){
                continue
            }
            targets += player
        }
        val choice:Player? = caster.choice("select opponent", targets)
        return choice
    }

    fun choose_shield_card(caster:Player):Card?{
        var targets:Array<Card> = arrayOf()
        for(player in players){
            if(player.is_dead()){
                continue
            }
            for(card in player.board){
                if(card.shield_max > 0){
                    targets += card
                }
            }
        }
        val choice = caster.choice("select a shield card", targets)
        return choice
    }

    fun choose_opponents_shield_card(caster:Player):Card?{
        var targets:Array<Card> = arrayOf()
        for(player in players){
            if(player.is_dead() || player == caster){
                continue
            }
            for(card in player.board){
                if(card.shield > 0){
                    targets += card
                }
            }
        }
        val choice:Card? = caster.choice("select an opponent's shield card", targets)
        return choice
    }

    // else

    fun set_dmg_cards_target_until_players_next_turn(dmg_target:Player, until_players_next_turn:Player){
        dmg_cards_target = dmg_target
        dmg_cards_target_until_players_next_turn = until_players_next_turn
    }

    fun find_card_owner(card_to_find:Card):Player{
        var found = 0
        var owner:Player? = null
        for(player in players){
            for(card in player.board){
                if(card == card_to_find){
                    found += 1
                    owner = player
                }
            }
        }
        require(found == 1){"unreachable"}
        return owner!!
    }

    fun damage_player(caster:Player, damage:Int){
        var targets:Array<Player> = arrayOf()
        
        if(dmg_cards_target == null){
            for(player in players){ // TODO we're kinda duplicating code here...
                if(player.is_dead()){
                    continue
                }
                if(player != caster){
                    targets += player
                }
            }
        }else{
            // this is fucking cancer shit, `kotlin` thinks that there might be a thread that changes the type of `dmg_cards_target`
            // and so it doesn't let me assign it normally, it's making me use the retarded `!!` shit fuck
            targets += dmg_cards_target!! // 2 bitches talked me into bending the rules so that the attacker can also be the target
        }

        val target:Player? = caster.choice("select attack target", targets)
        if(target == null){
            return
        }
        target.on_damaged(damage, caster)
    }

    fun main_loop(){
        while(true){
            // wait for everyone to ready up
            while(true){
                Thread.sleep(1_000)
                var ready = 0
                var unready = 0
                for(player in players){
                    if(player.ready){
                        ready += 1
                    }else{
                        unready += 1
                    }
                }
                if(unready > 0){
                    writeln("waiting for ${unready} player(s) to ready up")
                    continue
                }
                if(ready <= 1){
                    writeln("only ${ready} player connectied, waiting for players...")
                    continue
                }
                break
            }

            // game start
            on_game_start()

            // game loop
            game_loop@ while(true){
                for(player in players){
                    player.play_turn(this)

                    var players_alive = 0
                    for(player in players){
                        if(player.is_alive()){
                            players_alive += 1
                        }
                    }
                    if(players_alive == 0){
                        write_t1("draw")
                        break@game_loop
                    }
                    if(players_alive == 1){
                        for(player in players){
                            if(player.is_alive()){
                                write_t1("winner: ${player}")
                                break@game_loop
                            }
                        }
                    }
                }
            }

            // game end
            for(player in players){
                player.on_game_end(this)
            }
        }
    }
}
