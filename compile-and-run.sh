#! /usr/bin/env bash

clear &&
kotlinc dungeon_meyhem.kt classes.kt colors.kt player.kt board.kt bot.kt cards.kt -include-runtime -d dungeon_meyhem.jar &&
java -jar dungeon_meyhem.jar 6969 1
