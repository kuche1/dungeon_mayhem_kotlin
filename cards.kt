
package cards

import java.net.Socket

import colors.*
import player.Player
import board.Board

const val ICON_SHIELD:String = "${COL_SHIELD}shield${COL_RESET}"
const val ICON_HEAL:String = "${COL_HEAL}heal${COL_RESET}"
const val ICON_DMG:String = "${COL_DMG}dmg${COL_RESET}"

open class Card(
    val original_owner:Player,
    val name:String = "unnamed",
    val occur:Int = 1,
    val dmg:Int = 0,
    val shield_max:Int = 0,
    val heal:Int = 0,
    val draw:Int = 0,
    val thunder:Int = 0,
    val desc:String = "",
){
    var shield:Int

    init{
        shield = shield_max
    }

    override fun toString():String{
        return toString(show_occur=false)
    }
    fun toString(show_occur:Boolean=false, align_left:Boolean=true):String{
        var ret = "%"
        if(align_left){
            ret += "-"
        }
        ret += "23s" // TODO do something about this fucking retarded shit, hardcoding this is cancer
        ret = ret.format(name)

        ret += " <"
        if(show_occur){
            ret += " | occurances:${occur}"
        }
        if(dmg != 0){
            ret += " | ${COL_DMG}damage:${dmg}${COL_RESET}"
        }
        if(shield != 0 || shield_max != 0){
            ret += " | ${COL_SHIELD}shield:${shield}/${shield_max}${COL_RESET}"
        }
        if(heal != 0){
            ret += " | ${COL_HEAL}heal:${heal}${COL_RESET}"
        }
        if(draw != 0){
            ret += " | ${COL_DRAW}draw:${draw}${COL_RESET}"
        }
        if(thunder != 0){
            ret += " | ${COL_THUNDER}thunder:${thunder}${COL_RESET}"
        }
        if(desc != ""){
            ret += " | ${desc}"
        }
        ret += " | >"
        return ret
    }

    fun summon(player:Player, board:Board){
        // announce // TODO
        // board.writeln_except(player, "${}")

        // remove from hand
        player.hand.remove(this)

        // activate effects
        special_effect(player, board)
        player.heal(heal)
        player.thunder += thunder
        for(_tmp in 1..draw){
            player.draw()
        }
        if(dmg != 0){
            board.damage_player(player, dmg)
        }

        // add to board
        player.board += this

        // and remove from board if no shield
        if(shield <= 0){
            destroy(player, player) // we're getting a bit philosophical here...
        }
    }

    fun destroy(current_owner:Player, destroyer:Player){
        val belongs_to_board = current_owner.board
        belongs_to_board.remove(this)
        on_destroy_special_effect(destroyer, current_owner)
        on_discard()
        original_owner.discard += this
    }

    open fun on_destroy_special_effect(destroyer:Player, current_card_owner:Player){
        // do nothing by default
    }

    private fun on_discard(){
        shield = shield_max
    }

    open fun special_effect(caster:Player, board:Board){
        // no special effect by default
    }
}

/////////////////////////////////////////////////////////////////////////////// azzan

class Fireball(
    original_owner:Player,
):Card(
    original_owner,
    name="Fireball",
    occur=2,
    desc="Each player (including you) takes 3 damage!",
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){
            if(player.is_dead()){
                continue
            }
            player.on_damaged(3, caster)
        }
    }
}

class Charm(
    original_owner:Player,
):Card(
    original_owner,
    name="Charm",
    occur=2,
    desc="Take a ${ICON_SHIELD} card that an opponent has in play - it protects you now!"
){
    override fun special_effect(caster:Player, board:Board){
        val choosen_card = board.choose_opponents_shield_card(caster)
        if(choosen_card == null){ // compiler is smart enough and knows that `choosen_card` can no longer be null after this check
            return
        }
        val card_owner = board.find_card_owner(choosen_card)
        val belongs_to_board = card_owner.board
        belongs_to_board.remove(choosen_card)
        caster.board += choosen_card
    }
}

class Vampiric_touch(
    original_owner:Player,
):Card(
    original_owner,
    name="Vampiric_touch",
    occur=2,
    desc="Swap your hit points with an opponent's.",
){
    override fun special_effect(caster:Player, board:Board){
        val opponent = board.choose_opponent(caster)
        if(opponent == null){
            return
        }
        val op_hp = opponent.hp
        opponent.hp = caster.hp
        caster.hp = op_hp
    }
}

class Lightning_bolt(
    original_owner:Player,
):Card(
    original_owner,
    name = "Lightning Bolt",
    occur = 4,
    dmg = 3,
)

class Speed_of_thought(
    original_owner:Player,
):Card(
    original_owner,
    name = "Speed of thought",
    occur = 3,
    thunder = 2,
)

class Knowledge_is_power(
    original_owner:Player,
):Card(
    original_owner,
    name="Knowledge is power",
    occur=3,
    draw=3,
)

class Evil_sneer(
    original_owner:Player,
):Card(
    original_owner,
    name="Evil Sneer",
    occur=2,
    heal=1,
    thunder=1,
)

class Shield(
    original_owner:Player,
):Card(
    original_owner,
    name="Shield",
    occur=2,
    shield_max=1,
    draw=1,
)

class Mirror_image(
    original_owner:Player,
):Card(
    original_owner,
    name="Mirror Image",
    occur=1,
    shield_max=3,
)

class Magic_missile(
    original_owner:Player,
):Card(
    original_owner,
    name="Magic Missile",
    occur=3,
    dmg=1,
    thunder=1,
)

class Burning_hands(
    original_owner:Player,
):Card(
    original_owner,
    name="Burning Hands",
    occur=3,
    dmg=2,
)

class Stoneskin(
    original_owner:Player,
):Card(
    original_owner,
    name="Stoneskin",
    occur=1,
    shield_max=2,
)

/////////////////////////////////////////////////////////////////////////////// blorp

class Hugs(
    original_owner:Player,
):Card(
    original_owner,
    name="HUGS!",
    occur=2,
    desc="Destroy a ${ICON_SHIELD} card and then ${ICON_HEAL} for each starting ${ICON_SHIELD} on that card."
){
    override fun special_effect(caster:Player, board:Board){
        val card = board.choose_shield_card(caster)
        if(card == null){
            return
        }
        caster.heal(card.shield_max)
        card.destroy(board.find_card_owner(card), caster)
    }
}

class Here_i_come(
    original_owner:Player,
):Card(
    original_owner,
    name="Here I Come!",
    occur=2,
    dmg=3,
    desc="This turn, your ${ICON_DMG} cards ignore ${ICON_SHIELD} cards."
){
    override fun special_effect(caster:Player, board:Board){
        caster.shield_penetration_until_end_of_turn = true
    }
}

class Burped_up_bones(
    original_owner:Player,
):Card(
    original_owner,
    name="Burped-Up Bones",
    occur=2,
    shield_max=3,
    desc="When this card is destroyed ${ICON_DMG} ${ICON_DMG}"
){
    override fun on_destroy_special_effect(destroyer:Player, current_card_owner:Player){
        destroyer.on_damaged(2, current_card_owner)
    }
}

class Arcane_appetizer(
    original_owner:Player,
):Card(
    original_owner,
    name="Arcane Appetizer",
    occur=2,
    dmg=2,
    draw=1,
)

class Cleric_a_la_slime(
    original_owner:Player,
):Card(
    original_owner,
    name="Cleric a la Slime",
    occur=2,
    dmg=2,
    heal=1,
)

class Acid_burp(
    original_owner:Player,
):Card(
    original_owner,
    name="Acid Burp",
    occur=2,
    dmg=1,
    thunder=1,
)

class Slime_time(
    original_owner:Player,
):Card(
    original_owner,
    name="Slime Time",
    occur=2,
    thunder=2,
)

class Open_wide(
    original_owner:Player,
):Card(
    original_owner,
    name="Open Wide",
    occur=1,
    draw=2,
    thunder=1,
)

class Fastest_cube_alive(
    original_owner:Player,
):Card(
    original_owner,
    name="Fastest Cube Alive",
    occur=3,
    dmg=1,
    draw=1,
    heal=1,
)

class Cubes_have_feelings_too(
    original_owner:Player,
):Card(
    original_owner,
    name="Cubes Have Feelings Too",
    occur=2,
    heal=1,
    thunder=1,
)

class Former_friends(
    original_owner:Player,
):Card(
    original_owner,
    name="Former Friends",
    occur=1,
    shield_max=2,
)

class Combat_cubed(
    original_owner:Player,
):Card(
    original_owner,
    name="Combat Cubed",
    occur=3,
    dmg=3,
)

class Sugar_rush(
    original_owner:Player,
):Card(
    original_owner,
    name="Sugar Rush",
    occur=2,
    draw=2,
)

class D6_of_doom(
    original_owner:Player,
):Card(
    original_owner,
    name="D6 of Doom",
    occur=2,
    dmg=2,
)
