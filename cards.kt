
package cards

import java.net.Socket

import colors.*
import player.Player
import board.Board

open class Card(
    val original_owner:Player,
    val occur:Int,
    val name:String = "unnamed",
    val dmg:Int = 0,
    val shield_max:Int = 0,
    val heal:Int = 0,
    val draw:Int = 0,
    val thunder:Int = 0,
    val desc:String = "",
){
    var shield:Int
    var owner:Player

    init{
        owner = original_owner
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
        ret += "26s" // TODO do something about this fucking retarded shit, hardcoding this is cancer
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
        require(player == owner)

        // announce
        board.writeln()
        board.writeln("${player.toString(short=true)}")
        board.writeln("has played")
        board.writeln("${this}")

        // remove from hand
        player.remove_card_from_hand(this)

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

        // add to field
        player.add_card_to_field(this)

        // and remove from board if no shield
        if(shield <= 0){
            destroy(player) // we're getting a bit philosophical here...
        }
    }

    fun destroy(destroyer:Player){
        // remove from field
        owner.remove_card_from_field(this)
        // activate on_destroy effect
        on_destroy_special_effect(destroyer)
        // restore the internal state in case anyone needs to take the card out of the discard pile
        shield = shield_max
        // add to discard pile
        owner.add_card_to_discard(this)
    }

    fun discard(){ // TODO use this!!!

    }

    open fun on_destroy_special_effect(destroyer:Player){
        // do nothing by default
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
        val card_owner = choosen_card.owner
        card_owner.remove_card_from_field(choosen_card)
        caster.add_card_to_field(choosen_card)
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
        card.destroy(caster)
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
    override fun on_destroy_special_effect(destroyer:Player){
        destroyer.on_damaged(2, this.owner)
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

/////////////////////////////////////////////////////////////////////////////// deliah

class Praise_me(
    original_owner:Player,
):Card(
    original_owner,
    name="Praise Me",
    occur=2,
    draw=3,
    desc="Each opponent can choose to praise your greatness. ${ICON_DMG} ${ICON_DMG} those who choose not to."
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){
            if(player.is_dead() || player == caster){
                continue
            }
            // TODO this is retarded and needs to be multithreaded
            // TODO add a little flavour
            val choice = player.choice("praise ${caster.toString(short=true)}? (take ${ICON_DMG} ${ICON_DMG} if you refuse)", arrayOf("yes", "no"))
            if(choice == "yes"){
                // board.writeln("${player.toString(short=true)} to ${caster.toString(short=true)}: I love the taste of your semen")
                caster.say("I love the taste of ${caster.toString(short=true)}'s semen", board)
            }else{
                player.on_damaged(2, caster)
            }
        }
    }
}

class Charm_ray(
    original_owner:Player,
):Card(
    original_owner,
    name="Charm Ray",
    occur=2,
    desc="Until your next turn, choose the target of all ${ICON_DMG} cards."
){
    override fun special_effect(caster:Player, board:Board){
        val target = board.choose_opponent(caster)
        if(target == null){
            return
        }
        board.set_dmg_cards_target_until_players_next_turn(target, caster)
        // TODO make it so that the card stays on the board
    }
}

class Death_ray(
    original_owner:Player,
):Card(
    original_owner,
    name="Death Ray",
    occur=3,
    desc="${ICON_DMG} ${ICON_DMG} each opponent with no ${ICON_SHIELD} cards in play. Then destroy all ${ICON_SHIELD} cards - including yours!"
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){
            if(player.is_dead()){
                continue
            }

            var to_destroy = player.get_shield_cards_on_field()
            if(to_destroy.size == 0){
                if(player != caster){
                    player.on_damaged(2, caster)
                }
            }else{
                for(card in to_destroy){
                    card.destroy(caster)
                }
            }
        }
    }
}

class Fashion_police(
    original_owner:Player,
):Card(
    original_owner,
    name="Fashion Police",
    occur=2,
    shield_max=3,
)

class Tyrany_of_beauty(
    original_owner:Player,
):Card(
    original_owner,
    name="Tyrany of Beauty",
    occur=1,
    dmg=1,
    heal=1,
    thunder=1,
)

class Multitask(
    original_owner:Player,
):Card(
    original_owner,
    name="Multitask",
    occur=2,
    dmg=2,
    heal=1,
)

class Beauty_barrage(
    original_owner:Player,
):Card(
    original_owner,
    name="Beauty Barrage",
    occur=3,
    dmg=3,
)

class Mirror_mirror(
    original_owner:Player,
):Card(
    original_owner,
    name="Mirror, Mirror",
    occur=2,
    shield_max=2,
)

class Double_trouble(
    original_owner:Player,
):Card(
    original_owner,
    name="Double Trouble",
    occur=2,
    dmg=2,
)

class Make_it_work(
    original_owner:Player,
):Card(
    original_owner,
    name="Make it Work!",
    occur=2,
    heal=2,
    draw=1,
)

class Rays_for_days(
    original_owner:Player,
):Card(
    original_owner,
    name="Rays for Days",
    occur=2,
    thunder=2,
)

class Laser_show(
    original_owner:Player,
):Card(
    original_owner,
    name="Laser Show!",
    occur=3,
    dmg=1,
    thunder=1,
)

class Me_myself_and_eye(
    original_owner:Player,
):Card(
    original_owner,
    name="Me, Myself and Eye",
    occur=1,
    heal=1,
    draw=1,
)

class Cuter_than_you(
    original_owner:Player,
):Card(
    original_owner,
    name="Cuter Than You",
    occur=1,
    draw=2,
)

/////////////////////////////////////////////////////////////////////////////// dr tentaculous

class Mind_games(
    original_owner:Player,
):Card(
    original_owner,
    name="Mind Games",
    occur=3,
    desc="Swap your hand with an opponent's hand."
){
    override fun special_effect(caster:Player, board:Board){
        val opponent = board.choose_opponent(caster)
        if(opponent == null){
            return
        }
        // val op_hand = opponent.hand
        // opponent.hand = caster.hand
        // caster.hand = op_hand
        caster.switch_hands(opponent)
    }
}

class Tell_me_about_your_mother(
    original_owner:Player,
):Card(
    original_owner,
    name="Tell Me About Your Mother",
    occur=2,
    desc="Add the top card of each opponent's discard pile to your hand."
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){
            if(player.is_dead()){
                continue
            }
            val last_card = player.pop_last_card_from_discard()
            if(last_card == null){
                continue
            }
            caster.add_card_to_hand(last_card)
        }
    }
}

class Mind_blast(
    original_owner:Player,
):Card(
    original_owner,
    name="Mind Blast",
    occur=2,
    desc="${ICON_DMG} an opponent once for each card they have in their hand (max of 5 damage)."
){
    override fun special_effect(caster:Player, board:Board){
        val opponent = board.choose_opponent(caster)
        if(opponent == null){
            return
        }
        var dmg = opponent.get_hand_size()
        if(dmg > 5){
            dmg = 5
        }
        opponent.on_damaged(dmg, caster)
    }
}

class Diagnosis_evil(
    original_owner:Player,
):Card(
    original_owner,
    name="Diagnosis: Evil!",
    occur=3,
    draw=1,
    dmg=2,
)

class Sip_tea(
    original_owner:Player,
):Card(
    original_owner,
    name="Sip Tea",
    occur=2,
    heal=2,
    draw=1,
)

class Receptionist(
    original_owner:Player,
):Card(
    original_owner,
    name="Receptionist",
    occur=1,
    shield_max=1,
    dmg=1,
)

class Phd_in_psychology(
    original_owner:Player,
):Card(
    original_owner,
    name="PhD in Psychology",
    occur=2,
    thunder=2,
)

class Id_insinuation(
    original_owner:Player,
):Card(
    original_owner,
    name="Id Insinuation",
    occur=3,
    dmg=1,
    thunder=1,
)

class Just_a_nibble(
    original_owner:Player,
):Card(
    original_owner,
    name="Just a Nibble",
    occur=1,
    draw=1,
    heal=1,
    thunder=1,
)

class Superego_whip(
    original_owner:Player,
):Card(
    original_owner,
    name="Superego Whip",
    occur=3,
    dmg=3,
)

class Puppet_therapy(
    original_owner:Player,
):Card(
    original_owner,
    name="Puppet Therapy",
    occur=3,
    shield_max=1,
    dmg=2,
)

class Eego_whip(
    original_owner:Player,
):Card(
    original_owner,
    name="Eego Whip",
    occur=1,
    dmg=2,
)

class Enthralled_thrall(
    original_owner:Player,
):Card(
    original_owner,
    name="Enthralled_thrall",
    occur=2,
    shield_max=2,
)

class Relax_after_work(
    original_owner:Player,
):Card(
    original_owner,
    name="Relax After Work",
    occur=1,
    draw=3,
)

/////////////////////////////////////////////////////////////////////////////// hoots mcgoots

class To_the_face(original_owner:Player,):Card(original_owner,
    name="To the Face!",
    occur=2,
    desc="Destroy a ${ICON_SHIELD} card and then ${ICON_DMG} for each starting ${ICON_SHIELD} on that card.",
){
    override fun special_effect(caster:Player, board:Board){
        val card = board.choose_shield_card(caster)
        if(card == null){
            return
        }
        card.destroy(caster) // TODO what if u die here, do u still do dmg? the solution could be adding a check in the `damage_player` fnc
        board.damage_player(caster, card.shield_max)
    }
}

class Owlbear_boogie(original_owner:Player,):Card(original_owner,
    name="Owlbear Boogie",
    occur=2,
    desc="Each player may do a little dance and then ${ICON_DRAW}. You ${ICON_DRAW} for each player who danced.",
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){ // TODO this is retarded and needs to be multithreaded
            if(player.is_dead()){ // if can also decide to draw for yourself
                continue
            }
            val choice = player.choice("do a little dance and ${ICON_DRAW}?", arrayOf("yes", "no"))
            if(choice == "yes"){
                // TODO add multiple dances?
                player.say("brrr shtibidi top top top top top yes yes yes yes shtip shtibididip shtibidi w w w w yes yes yes yes", board)
                player.draw()
                caster.draw()
            }
        }
    }
}

class For_my_next_trick(original_owner:Player,):Card(original_owner,
    name="For My Next Trick...",
    occur=2,
    dmg=1,
    thunder=1,
    desc="Until your next turn, your attacks hit all opponents.",
){
    override fun special_effect(caster:Player, board:Board){
        caster.attacks_hit_all_opponents_until_next_turn = true
    }
}

class Send_in_the_clowns(original_owner:Player):Card(original_owner,
    name="Send in the Clowns",
    occur=3,
    shield_max=1,
    thunder=1,
)

class The_hoots_fan_club(original_owner:Player):Card(original_owner,
    name="The Hoots Fan Club",
    occur=2,
    shield_max=2,
    heal=1,
)

class Grand_finale(original_owner:Player):Card(original_owner,
    name="Grand Finale",
    occur=2,
    dmg=3,
)

class Look_out_below(original_owner:Player):Card(original_owner,
    name="Look Out Below!",
    occur=2,
    dmg=2,
)

class Very_very_fast(original_owner:Player):Card(original_owner,
    name="Very Very Fast",
    occur=4,
    dmg=1,
    thunder=1,
)

class Wise_as_an_owl(original_owner:Player):Card(original_owner,
    name="Wise as an Owl",
    occur=1,
    draw=3,
)

class Talk_to_my_agent(original_owner:Player):Card(original_owner,
    name="Talk to My Agent",
    occur=2,
    shield_max=3,
)

class Made_you_look(original_owner:Player):Card(original_owner,
    name="Made you look",
    occur=1,
    draw=2,
    thunder=1,
)

class Strong_as_a_bear(original_owner:Player):Card(original_owner,
    name="Strong as a Bear",
    occur=2,
    dmg=2,
    heal=1,
)

class Crushing_hug(original_owner:Player):Card(original_owner,
    name="Crushing Hug",
    occur=2,
    dmg=2,
    heal=2,
)

class Intermission(original_owner:Player):Card(original_owner,
    name="Intermission",
    occur=1,
    draw=2,
)

/////////////////////////////////////////////////////////////////////////////// lia

class Divine_inspiration(original_owner:Player,):Card(original_owner,
    name="Divine Inspiration",
    occur=2,
    heal=2,
    desc="Choose any card in your discard pile and put it into your hand.",
){
    override fun special_effect(caster:Player, board:Board){
        val card = caster.choose_and_pop_card_from_discard()
        if(card == null){
            return
        }
        caster.add_card_to_hand(card)
    }
}

class Banishing_smite(original_owner:Player,):Card(original_owner,
    name="Banishing Smite",
    occur=1,
    thunder=1,
    desc="Destroy all ${ICON_SHIELD} cards in play (including yours).",
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){
            if(player.is_dead()){
                continue
            }
            val shield_cards = player.get_shield_cards_on_field()
            for(card in shield_cards){
                card.destroy(caster)
            }
        }
    }
}

class For_even_more_justice(original_owner:Player):Card(original_owner,
    name="For Even More Justice!",
    occur=4,
    dmg=2,
)

class Spinning_parry(original_owner:Player):Card(original_owner,
    name="Spinning Parry",
    occur=2,
    draw=1,
    shield_max=1,
)

class Divine_smite(original_owner:Player):Card(original_owner,
    name="Divine Smite",
    occur=3,
    dmg=3,
    heal=1,
)

class Cure_wounds(original_owner:Player):Card(original_owner,
    name="Cure Wounds",
    occur=1,
    draw=2,
    heal=1,
)

class For_justice(original_owner:Player):Card(original_owner,
    name="For Justice!",
    occur=3,
    dmg=1,
    thunder=1,
)

class Finger_wag_of_judgement(original_owner:Player):Card(original_owner,
    name="Finger-wag of Judgement",
    occur=2,
    thunder=2,
)

class Fighting_words(original_owner:Player):Card(original_owner,
    name="Fighting Words",
    occur=3,
    dmg=2,
    heal=1,
)

class Divine_shield(original_owner:Player):Card(original_owner,
    name="Divine Shield",
    occur=2,
    shield_max=3,
)

class For_the_most_justice(original_owner:Player):Card(original_owner,
    name="For the Most Justice!",
    occur=2,
    dmg=3,
)

class Fluffy(original_owner:Player):Card(original_owner,
    name="Fluffy",
    occur=1,
    shield_max=3,
)

class High_charisma(original_owner:Player):Card(original_owner,
    name="High Charisma",
    occur=2,
    draw=2,
)

/////////////////////////////////////////////////////////////////////////////// lord cinderpuff

class Hostile_takeover(original_owner:Player,):Card(original_owner,
    name="Hostile Takeover",
    occur=3,
    desc="${ICON_DMG} all opponents. ${ICON_DMG} ${ICON_DMG} one opponent. Then ${ICON_DMG} a different opponent.",
){
    override fun special_effect(caster:Player, board:Board){
        // 1 dmg to all
        for(player in board.players){
            if(player.is_dead() || player == caster){
                continue
            }
            player.on_damaged(1, caster)
        }
        // 2 dmg to one
        val target1 = board.choose_opponent(caster)
        if(target1 == null){
            return
        }
        target1.on_damaged(2, caster)
        // 1 dmg to another
        val target2 = board.choose_opponent(caster, except=target1)
        if(target2 == null){
            return
        }
        target2.on_damaged(1, caster)
    }
}

class Liquidate_assets(original_owner:Player,):Card(original_owner,
    name="Liquidate Assets",
    occur=2,
    desc="Discard your hand and ${ICON_DMG} equal to the number of cards discarded (max of 5 damage).",
){
    override fun special_effect(caster:Player, board:Board){
        var dmg = caster.get_hand_size()
        if(dmg > 5){
            dmg = 5
        }
        caster.discard_hand()
        board.damage_player(caster, dmg)
    }
}

class Murderous_and_acquisitions(original_owner:Player,):Card(original_owner,
    name="Murderous and Acquisitions",
    occur=2,
    desc="Each player must ${ICON_DMG}, ${ICON_HEAL}, or ${ICON_DRAW}. Start with you and go right. You repeat all choices.",
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){ // TODO multithread this
            if(player.is_dead()){
                continue
            }
            val choice = player.choice("select action", arrayOf(ICON_DMG, ICON_HEAL, ICON_DRAW))
            when(choice){
                ICON_DMG->{
                    board.damage_player(player, 1)
                    board.damage_player(caster, 1)
                }
                ICON_HEAL->{
                    player.heal(1)
                    caster.heal(1)
                }
                ICON_DRAW->{
                    player.draw()
                    caster.draw()
                }
                else->require(false)
            }
        }
    }
}

class Wing_buffet(original_owner:Player):Card(original_owner,
    name="Wing Buffet",
    occur=2,
    dmg=2,
)

class Eviler_sneer(original_owner:Player):Card(original_owner,
    name="Eviler Sneer",
    occur=2,
    heal=1,
    thunder=1,
)

class Peaceful_nap(original_owner:Player):Card(original_owner,
    name="Peaceful Nap",
    occur=1,
    draw=3,
)

class Tooth_and_claw(original_owner:Player):Card(original_owner,
    name="Tooth and Claw",
    occur=2,
    dmg=3,
)

class Kobold_maid(original_owner:Player):Card(original_owner,
    name="Kobold Maid",
    occur=2,
    shield_max=1,
    dmg=1,
)

class Ancient_anger(original_owner:Player):Card(original_owner,
    name="Ancient Anger",
    occur=3,
    dmg=1,
    thunder=1,
)

class Wall_of_money(original_owner:Player):Card(original_owner,
    name="Wall of Money",
    occur=2,
    shield_max=2,
)

class Wisdom_of_ages(original_owner:Player):Card(original_owner,
    name="Wisdom of Ages",
    occur=2,
    draw=2,
)

class Bull_market(original_owner:Player):Card(original_owner,
    name="Bull Market",
    occur=2,
    thunder=2,
)

class Mob_of_lawyers(original_owner:Player):Card(original_owner,
    name="Mob of Lawyers",
    occur=1,
    shield_max=3,
)

class Investment_opportunity(original_owner:Player):Card(original_owner,
    name="Investment Opportunity",
    occur=2,
    heal=1,
    draw=1,
)

/////////////////////////////////////////////////////////////////////////////// mimi lechaise

class Definetely_just_a_mirror(original_owner:Player,):Card(original_owner,
    name="Definetely Just a Mirror",
    occur=2,
    desc="Play this card as a copy of any other ${ICON_SHIELD} card in play.",
){
    override fun special_effect(caster:Player, board:Board){
        require(false){"not implemented"}
    }
}

class A_book_cannot_bite(original_owner:Player,):Card(original_owner,
    name="A Book (Cannot Bite)",
    occur=3,
    desc="Use the top-listed Mighty Power of the player to your self or right.",
){
    override fun special_effect(caster:Player, board:Board){
        require(false){"not implemented"}
    }
}

class Its_not_a_trap(original_owner:Player,):Card(original_owner,
    name="It's Not a Trap",
    occur=2,
    desc="Make one player's hit points equal to another player's hit points.",
){
    override fun special_effect(caster:Player, board:Board){
        require(false){"not implemented"}
    }
}

class Definetely_not_a_trap(original_owner:Player):Card(original_owner,
    name="Definetely Not a Trap",
    occur=3,
    dmg=2,
)

class Not_a_mimic_really(original_owner:Player):Card(original_owner,
    name="Not a Mimic (Really)",
    occur=1,
    shield_max=2,
)

class Completely_safe_door(original_owner:Player):Card(original_owner,
    name="Completely Safe Door",
    occur=2,
    thunder=2,
)

class A_well_fitted_hat(original_owner:Player):Card(original_owner,
    name="A Well-Fitted Hat",
    occur=2,
    dmg=1,
    thunder=1,
)

class A_potted_plant_honest(original_owner:Player):Card(original_owner,
    name="A Potted Plant (Honest!)",
    occur=2,
    shield_max=1,
    dmg=1,
)

class Just_another_coat_rack(original_owner:Player):Card(original_owner,
    name="Just Another Coat Rack",
    occur=2,
    heal=2,
)

class Non_carnivorous_couch(original_owner:Player):Card(original_owner,
    name="Non-Carnivorous Couch",
    occur=2,
    dmg=2,
    heal=1,
)

class Probably_just_dirty_socks(original_owner:Player):Card(original_owner,
    name="Probably Just Dirty Socks",
    occur=2,
    draw=3,
)

class A_delicious_pie(original_owner:Player):Card(original_owner,
    name="A Delicious Pie!",
    occur=2,
    dmg=2,
    draw=1,
)

class Actually_an_empty_chest(original_owner:Player):Card(original_owner,
    name="Actually an Empty Chest",
    occur=2,
    shield_max=1,
    draw=1,
)

class Harmless_pile_of_rocks(original_owner:Player):Card(original_owner,
    name="Harmless Pile of Rocks",
    occur=1,
    dmg=2,
    thunder=1,
)

/////////////////////////////////////////////////////////////////////////////// oriax

class Clever_disguise(original_owner:Player,):Card(original_owner,
    name="Clever Disguise",
    occur=2,
    desc="None of your opponents' cards affect you or your ${ICON_SHIELD} cards until your next turn.",
){
    override fun special_effect(caster:Player, board:Board){
        require(false){"not implemented"}
    }
}

class Sneak_attack(original_owner:Player,):Card(original_owner,
    name="Sneak Attack!",
    occur=2,
    thunder=1,
    desc="Destroy one ${ICON_SHIELD} card in play.",
){
    override fun special_effect(caster:Player, board:Board){
        require(false){"not implemented"}
    }
}

class Pick_pocket(original_owner:Player,):Card(original_owner,
    name="Pick Pocket",
    occur=2,
    desc="Steal the top card of any player's deck and play it.",
){
    override fun special_effect(caster:Player, board:Board){
        require(false){"not implemented"}
    }
}

class Even_more_daggers(original_owner:Player):Card(original_owner,
    name="Even More Daggers",
    occur=1,
    draw=2,
    heal=1,
)

class All_the_thrown_daggers(original_owner:Player):Card(original_owner,
    name="All the Thrown Daggers",
    occur=3,
    dmg=3,
)

class One_thrown_dagger(original_owner:Player):Card(original_owner,
    name="One Thrown Dagger",
    occur=5,
    dmg=1,
    thunder=1,
)

class My_little_friend(original_owner:Player):Card(original_owner,
    name="My Little Friend",
    occur=1,
    shield_max=3,
)

class Winged_serpent(original_owner:Player):Card(original_owner,
    name="Winged Serpent",
    occur=2,
    shield_max=1,
    draw=1,
)

class Stolen_potion(original_owner:Player):Card(original_owner,
    name="Stolen Potion",
    occur=2,
    heal=1,
    thunder=1,
)

class The_goon_squad(original_owner:Player):Card(original_owner,
    name="The Goon Squad",
    occur=2,
    shield_max=2,
)

class Two_thrown_daggers(original_owner:Player):Card(original_owner,
    name="Two Thrown Daggers",
    occur=4,
    dmg=2,
)

class Cunning_action(original_owner:Player):Card(original_owner,
    name="Cunning Action",
    occur=1,
    thunder=2,
)

/////////////////////////////////////////////////////////////////////////////// sutha

class Whirling_axes(original_owner:Player,):Card(original_owner,
    name="Whirling Axes",
    occur=2,
    desc="You ${ICON_HEAL} once per opponent, then ${ICON_DMG} each opponent.",
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){
            if(player.is_dead()){
                continue
            }
            caster.heal(1)
            player.on_damaged(1, caster)
        }
    }
}

class Battle_roar(original_owner:Player,):Card(original_owner,
    name="Battle Roar",
    occur=2,
    thunder=1,
    desc="Each player (including you) discards their hand, then draws three cards.",
){
    override fun special_effect(caster:Player, board:Board){
        for(player in board.players){
            if(player.is_dead()){
                continue
            }
            player.discard_hand()
            player.draw()
            require(player.get_hand_size() == 3)
        }
    }
}

class Mighty_toss(original_owner:Player,):Card(original_owner,
    name="Mighty Toss",
    occur=2,
    draw=1,
    desc="Destroy one ${ICON_SHIELD} card in play.",
){
    override fun special_effect(caster:Player, board:Board){
        val card = board.choose_shield_card(caster)
        if(card == null){
            return
        }
        card.destroy(caster)
    }
}

class Rage(original_owner:Player):Card(original_owner,
    name="RAGE!",
    occur=2,
    dmg=4,
)

class Riff(original_owner:Player):Card(original_owner,
    name="Riff",
    occur=1,
    shield_max=3,
)

class Big_axe_is_best_axe(original_owner:Player):Card(original_owner,
    name="Big Axe is Best Axe",
    occur=5,
    dmg=3,
)

class Brutal_punch(original_owner:Player):Card(original_owner,
    name="Brutal Punch",
    occur=2,
    dmg=2,
)

class Head_butt(original_owner:Player):Card(original_owner,
    name="Head Butt",
    occur=2,
    dmg=1,
    thunder=1,
)

class Two_axes_are_better_than_one(original_owner:Player):Card(original_owner,
    name="Two Axes Are Better Than One",
    occur=2,
    thunder=2,
)

class Raff(original_owner:Player):Card(original_owner,
    name="Raff",
    occur=1,
    shield_max=3,
)

class Spiked_shield(original_owner:Player):Card(original_owner,
    name="Spiked Shield",
    occur=1,
    shield_max=2,
)

class Open_the_armory(original_owner:Player):Card(original_owner,
    name="Open the Armory",
    occur=2,
    draw=2,
)

class Bag_of_rats(original_owner:Player):Card(original_owner,
    name="Bag of Rats",
    occur=1,
    shield_max=1,
    draw=1,
)

class Flex(original_owner:Player):Card(original_owner,
    name="Flex!",
    occur=2,
    heal=1,
    draw=1,
)

class Snack_time(original_owner:Player):Card(original_owner,
    name="Snack Time",
    occur=1,
    draw=2,
    heal=1,
)

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

// class (original_owner:Player):Card(original_owner,
//     name="",
//     occur=1,

// )

