
package classes

import colors.*
import cards.*
import player.Player

// base class

open class Class(
    val name:String = "class not picked",
    val color:String = COL_WHITE_DARK,
){
    override fun toString():String{
        return "${color}${name}${COL_RESET}"
    }

    // deck generation
    open fun generate_deck_uncked(p:Player):Array<Card>{
        require(false){"unreachable"}
        return arrayOf()
    }
    private fun check_deck(deck:Array<Card>){
        // TODO super fucking inefficient
        for(card in deck){
            var occurances = 0
            for(card2 in deck){
                if(card.name == card2.name){ // this might be retarded
                    occurances += 1
                }
            }
            require(occurances == card.occur){"occurances should have been ${card.occur}, but are in reality ${occurances}"}
        }
    }
    fun generate_deck(original_owner:Player):Array<Card>{
        val deck = generate_deck_uncked(original_owner)
        check_deck(deck)
        return deck
    }
    // print cards
    fun print_cards(player:Player){
        // TODO this is really fucking inefficient
        val deck = generate_deck(player)
        var printed:Array<String> = arrayOf()
        for(card in deck){
            if(card.name !in printed){ // this might be retarded
                player.writeln(card.toString(show_occur=true, align_left=false))
                printed += card.name
            }
        }
    }
}

// instances

class Class_azzan:Class(
    name = "Azzan",
    color = COL_YELLOW_BRIGHT,
){
    override fun generate_deck_uncked(p:Player):Array<Card>{
        return arrayOf(
            Fireball(p), Fireball(p),
            Charm(p), Charm(p),
            Vampiric_touch(p), Vampiric_touch(p),
            Lightning_bolt(p), Lightning_bolt(p), Lightning_bolt(p), Lightning_bolt(p),
            Speed_of_thought(p), Speed_of_thought(p), Speed_of_thought(p),
            Knowledge_is_power(p), Knowledge_is_power(p), Knowledge_is_power(p),
            Evil_sneer(p), Evil_sneer(p),
            Shield(p), Shield(p),
            Mirror_image(p),
            Magic_missile(p), Magic_missile(p), Magic_missile(p),
            Burning_hands(p), Burning_hands(p), Burning_hands(p),
            Stoneskin(p),
        )
    }
}

class Class_blorp:Class(
    name = "Blorp",
    color = COL_CYAN_BRIGHT,
){
    override fun generate_deck_uncked(p:Player):Array<Card>{
        return arrayOf(
            Hugs(p), Hugs(p),
            Here_i_come(p), Here_i_come(p),
            Burped_up_bones(p), Burped_up_bones(p),
            Arcane_appetizer(p), Arcane_appetizer(p),
            Cleric_a_la_slime(p), Cleric_a_la_slime(p),
            Acid_burp(p), Acid_burp(p),
            Slime_time(p), Slime_time(p),
            Open_wide(p),
            Fastest_cube_alive(p), Fastest_cube_alive(p), Fastest_cube_alive(p),
            Cubes_have_feelings_too(p), Cubes_have_feelings_too(p),
            Former_friends(p),
            Combat_cubed(p), Combat_cubed(p), Combat_cubed(p),
            Sugar_rush(p), Sugar_rush(p),
            D6_of_doom(p), D6_of_doom(p),
        )
    }
}

class Class_deliah:Class(
    name = "Deliah Deathray",
    color = COL_BLUE_DARK,
){
    override fun generate_deck_uncked(p:Player):Array<Card>{
        return arrayOf(
            Praise_me(p), Praise_me(p),
            Charm_ray(p), Charm_ray(p),
            Death_ray(p), Death_ray(p), Death_ray(p),
            Fashion_police(p), Fashion_police(p),
            Tyrany_of_beauty(p),
            Multitask(p), Multitask(p),
            Beauty_barrage(p), Beauty_barrage(p), Beauty_barrage(p),
            Mirror_mirror(p), Mirror_mirror(p),
            Double_trouble(p), Double_trouble(p),
            Make_it_work(p), Make_it_work(p),
            Rays_for_days(p), Rays_for_days(p),
            Laser_show(p), Laser_show(p), Laser_show(p),
            Me_myself_and_eye(p),
            Cuter_than_you(p),
        )
    }
}

class Class_dr_tentaculous:Class(
    name = "Dr. Tentaculous",
    color = COL_BLACK_BRIGHT, // should have been brown
){
    override fun generate_deck_uncked(p:Player):Array<Card>{
        return arrayOf(
            Mind_games(p), Mind_games(p), Mind_games(p),
            Tell_me_about_your_mother(p), Tell_me_about_your_mother(p),
            Mind_blast(p), Mind_blast(p),
            Diagnosis_evil(p), Diagnosis_evil(p), Diagnosis_evil(p),
            Sip_tea(p), Sip_tea(p),
            Receptionist(p),
            Phd_in_psychology(p), Phd_in_psychology(p),
            Id_insinuation(p), Id_insinuation(p), Id_insinuation(p),
            Just_a_nibble(p),
            Superego_whip(p), Superego_whip(p), Superego_whip(p),
            Puppet_therapy(p), Puppet_therapy(p), Puppet_therapy(p),
            Eego_whip(p),
            Enthralled_thrall(p), Enthralled_thrall(p),
            Relax_after_work(p),
        )
    }
}

class Class_hoots_mcgoots:Class(
    name = "Hoots McGoots",
    color = COL_YELLOW_DARK,
){
    override fun generate_deck_uncked(p:Player):Array<Card>{
        return arrayOf(
            To_the_face(p), To_the_face(p),
            Owlbear_boogie(p), Owlbear_boogie(p),
            For_my_next_trick(p), For_my_next_trick(p),
            Send_in_the_clowns(p), Send_in_the_clowns(p), Send_in_the_clowns(p),
            The_hoots_fan_club(p), The_hoots_fan_club(p),
            Grand_finale(p), Grand_finale(p),
            Look_out_below(p), Look_out_below(p),
            Very_very_fast(p), Very_very_fast(p), Very_very_fast(p), Very_very_fast(p),
            Wise_as_an_owl(p),
            Talk_to_my_agent(p), Talk_to_my_agent(p),
            Made_you_look(p),
            Strong_as_a_bear(p), Strong_as_a_bear(p),
            Crushing_hug(p), Crushing_hug(p),
            Intermission(p),
        )
    }
}

class Class_lia:Class(
    name = "Lia (unplayable)",
    color = COL_RED_DARK,
)

class Class_lord_cinderpuff:Class(
    name = "Lord Cinderpuff (unplayable)",
    color = COL_CYAN_DARK,
)

class Class_mimi_lechaise:Class(
    name = "Mimi LeChaise (unplayable)",
    color = COL_GREEN_DARK,
)

class Class_oriax:Class(
    name = "Oriax (unplayable)",
    color = COL_MAGENTA_DARK,
)

class Class_sutha:Class(
    name = "Sutha (unplayable)",
    color = COL_GREEN_BRIGHT,
)

val ALL_CLASSES = arrayOf(
    Class_azzan(),
    Class_blorp(),
    Class_deliah(),
    Class_dr_tentaculous(),
    Class_hoots_mcgoots(),
    Class_lia(),
    Class_lord_cinderpuff(),
    Class_mimi_lechaise(),
    Class_oriax(),
    Class_sutha(),
)
