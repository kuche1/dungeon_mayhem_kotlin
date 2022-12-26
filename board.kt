
package board

import classes.CLASS_ALL
import player.Player
import cards.Card

class Board(){
    val all_classes = CLASS_ALL
    var players:Array<Player> = arrayOf()

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
        val times = 80
        writeln("=".repeat(times))
        writeln("-".repeat(times))
        writeln("~".repeat(times))
    }

    fun write_board_state(){
        write_sep()

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
        for(player in players){
            if(player != caster){
                targets += player
            }
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
                if(ready != ready){
                    writeln("waiting for ${unready} players to ready up")
                    continue
                }
                if(ready <= 1){
                    writeln("only ${ready} player connectied, waiting for players...")
                    continue
                }
                break
            }

            // game start
            for(player in players){
                player.on_game_start()
            }

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
