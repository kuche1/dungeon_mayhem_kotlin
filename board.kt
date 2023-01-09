
package board

import classes.ALL_CLASSES
import player.Player
import cards.Card
import colors.*

class Board(){
    val all_classes = ALL_CLASSES
    private var players:MutableList<Player> = mutableListOf()
    var dmg_cards_target:Player? = null
    var dmg_cards_target_until_players_next_turn:Player? = null

    // player management

    fun add_player(player:Player){
        players += player
    }

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
            if(player.ready){
                player.write_sep()
            }
        }
    }

    fun write_board_state(){
        write_sep()

        if(dmg_cards_target != null){
            writeln()
            writeln("!!! all ${ICON_DMG} cards target: ${dmg_cards_target!!.toString(short=true)} !!!")
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
    }

    // getter

    fun get_vulnerable_players():Array<Player>{ // TODO what about effects that require only reading
        var targets:Array<Player> = arrayOf()
        for(player in players){
            if(player.is_dead()){
                continue
            }
            if(player.invulnerable_to_opponent_cards_until_next_turn){
                continue
            }
            targets += player
        }
        return targets
    }

    // on event

    private fun kick_disconnected_players(){
        var idx = 0
        while(idx < players.size){
            val player = players[idx]
            if(player.disconnected){
                players.remove(player)
            }else{
                idx += 1
            }
        }
    }

    fun on_game_start(){
        // kick the disconnected ppl
        kick_disconnected_players()

        // reset state
        dmg_cards_target = null
        dmg_cards_target_until_players_next_turn = null

        // call on_event for players
        for(player in players){
            player.on_game_start()
        }
    }

    fun on_game_end(){
        kick_disconnected_players()
        // call on_event for players
        for(player in players){
            player.on_game_end(this)
        }
    }

    fun on_player_turn(player:Player){
        if(player == dmg_cards_target_until_players_next_turn){
            dmg_cards_target = null
            dmg_cards_target_until_players_next_turn = null
        }
    }

    // choosing

    fun choose_player(caster:Player, except:Player?=null):Player?{
        val targets:Array<Player> = arrayOf()
        for(player in get_vulnerable_players()){
            if(player.is_dead()){
                continue
            }
            if(player == except){
                continue
            }
        }
        return caster.choice("select a player", targets)
    }

    fun choose_opponent(caster:Player, except:Player?=null):Player?{
        var targets:Array<Player> = arrayOf()
        for(player in get_vulnerable_players()){
            if(player == caster){
                continue
            }
            if(except != null){
                if(player == except){
                    continue
                }
            }
            targets += player
        }
        val choice:Player? = caster.choice("select opponent", targets)
        return choice
    }

    fun choose_opponent_on_left_or_right(caster:Player):Player?{
        val caster_idx = players.indexOf(caster)
        var left_idx = caster_idx - 1
        if(left_idx < 0){
            left_idx = players.size - 1
        }
        val right_idx = (caster_idx + 1) % players.size

        val player_left = players[left_idx]
        val player_right = players[right_idx]
        require(player_left != caster)
        require(player_right != caster)

        var options:Array<Player> = arrayOf(player_left)
        if(player_left != player_right){
            options += player_right
        }

        val player = caster.choice("select an opponent to your left or to your right", options)
        return player
    }

    fun choose_shield_card(caster:Player):Card?{
        var targets:Array<Card> = arrayOf()
        for(player in get_vulnerable_players()){
            if(player.is_dead()){
                continue
            }
            for(card in player.get_shield_cards_on_field()){
                targets += card
            }
        }
        val choice = caster.choice("select a shield card", targets)
        return choice
    }

    fun choose_opponents_shield_card(caster:Player):Card?{
        var targets:Array<Card> = arrayOf()
        for(player in get_vulnerable_players()){
            if(player.is_dead() || player == caster){
                continue
            }
            for(card in player.get_shield_cards_on_field()){
                targets += card
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

    fun damage_player(caster:Player, damage:Int){
        var targets:MutableList<Player> = mutableListOf()

        if(caster.attacks_hit_all_opponents_until_next_turn){
            for(player in players){
                if(player.is_dead()){
                    continue
                }
                if(player == caster && dmg_cards_target != caster){
                    // the guy's attacks are AOE to everyone but himself, and if he has been selected
                    // as the target of all DMG cards, then everyone AND him get damaged
                    continue
                }
                player.on_damaged(damage, caster)
            }
            return
        }

        if(dmg_cards_target == null){
            for(player in players){
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

        var idx = targets.size - 1
        while(idx > 0){
            val target = targets[idx]
            if(target.invulnerable_to_opponent_cards_until_next_turn){
                targets -= target
            }
            idx -= 1
        }

        val target:Player? = caster.choice("select attack target", targets.toTypedArray())
        if(target == null){
            return
        }
        target.on_damaged(damage, caster)
    }

    fun main_loop(){
        while(true){
            println("new game")
            // wait for everyone to ready up
            while(true){
                Thread.sleep(1_000)

                kick_disconnected_players()
                var ready = 0
                var unready = 0
                var bots = 0
                for(player in players){
                    if(player.is_bot){
                        bots += 1
                    }
                    if(player.ready){
                        ready += 1
                    }else{
                        unready += 1
                    }
                }
                if(unready > 0){
                    writeln("waiting for ${unready} player(s) to ready up...")
                    continue
                }
                if(ready <= 1){
                    writeln("only ${ready} player connectied, waiting for players...")
                    continue
                }
                if(bots == ready){
                    writeln("only bots connected...")
                    continue
                }
                break
            }

            // game start
            on_game_start()

            // game loop
            game_loop@ while(true){
                var idx_player = 0
                while(idx_player < players.size){
                // for(player in players){
                    val player = players[idx_player]

                    player.play_turn(this)

                    var players_alive = 0
                    for(p in players){
                        if(p.is_alive()){
                            players_alive += 1
                        }
                    }
                    if(players_alive == 0){
                        write_t1("draw")
                        break@game_loop
                    }
                    if(players_alive == 1){
                        for(p in players){
                            if(p.is_alive()){
                                write_t1("winner: ${p}")
                                break@game_loop
                            }
                        }
                    }

                    idx_player += 1
                }
            }

            // game end
            on_game_end()
        }
    }
}
