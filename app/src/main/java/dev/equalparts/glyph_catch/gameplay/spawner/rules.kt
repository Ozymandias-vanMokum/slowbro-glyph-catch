package dev.equalparts.glyph_catch.gameplay.spawner

import dev.equalparts.glyph_catch.data.Item
import dev.equalparts.glyph_catch.data.Pokemon
import dev.equalparts.glyph_catch.data.Type
import dev.equalparts.glyph_catch.gameplay.spawner.models.SpawnRules
import dev.equalparts.glyph_catch.gameplay.spawner.models.SpawnScaling
import dev.equalparts.glyph_catch.gameplay.spawner.models.increaseBy
import dev.equalparts.glyph_catch.gameplay.spawner.models.percent
import kotlin.time.Duration.Companion.minutes

/**
 * This defines the spawn rules for the game using a custom DSL.
 */
fun createSpawnRules(context: GameplayContext): SpawnRules {
    val dsl = PokemonSpawnDsl(context)

    val time = context.time
    val weather = context.weather
    val season = context.season
    val events = context.events
    val phone = context.phone
    val trainer = context.trainer
    val scaling = SpawnScaling(context)

    return dsl.pools {
        // Global modifiers
        // ================
        //
        // These affect all spawn pools, boosting certain types and suppressing
        // others based on weather conditions and time of day.

        modifiers {
            during(weather::rain) {
                boost(Type.WATER) by 5.0f
                boost(Type.GRASS) by 3.0f
            }

            during(weather::thunderstorm) {
                boost(Type.ELECTRIC) by 5.0f
                boost(Type.STEEL) by 3.0f
            }

            during(weather::snow) {
                boost(Type.ICE) by 5.0f
                suppress(Type.GRASS) by 0.5f
                suppress(Type.FIRE) by 0.5f
            }

            during(time::night) {
                suppress(Type.NORMAL) by 0.5f
            }
        }

        // Starter events
        // ==============
        //
        // In the Slowbro version, Slowpoke appears as the starter Pokémon.

        special(Pokemon.SLOWPOKE) {
            activate(50.percent) given { trainer.hasNotFound(Pokemon.SLOWPOKE) }
        }

        // Common Pokémon
        // ==============
        //
        // In the Slowbro version, Slowpoke spawns commonly.

        pool("Common", 87.percent) {
            Pokemon.SLOWPOKE at 10.0f
        }

        // Fishing pool
        // ============
        //
        // In the Slowbro version, fishing spawns more Slowpoke.

        pool("Fishing") {
            activate(70.percent) given { trainer.isUsingItem(Item.SUPER_ROD) }

            Pokemon.SLOWPOKE at 5.0f
        }

        // Uncommon & Rare
        // ===============
        //
        // In the Slowbro version, Slowbro appears as the rarer evolution.

        pool(
            "Uncommon",
            12.percent increaseBy {
                scaling.timeBoost(
                    gain = 30.percent,
                    over = 120.minutes
                ) + scaling.sleepBonus(5.percent)
            }
        ) {
            Pokemon.SLOWBRO at 1.0f
        }

        // In the Slowbro version, no rare pool is needed

        // Special events removed for Slowbro version
        // ==========================================
        //
        // The Slowbro version focuses only on Slowpoke and Slowbro.
    }
}
