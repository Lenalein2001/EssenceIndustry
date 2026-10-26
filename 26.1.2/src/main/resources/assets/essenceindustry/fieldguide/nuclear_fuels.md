---
navigation:
  title: Nuclear fuels
  position: 90
item_ids:
- essenceindustry:enriched_uranium_fuel
- essenceindustry:depleted_uranium
- essenceindustry:recovered_plutonium
- essenceindustry:plutonium_fuel
- essenceindustry:mox_fuel
- essenceindustry:thorium_fuel
- essenceindustry:advanced_nuclear_fuel
- essenceindustry:spent_uranium_fuel
- essenceindustry:spent_enriched_uranium_fuel
- essenceindustry:spent_plutonium_fuel
- essenceindustry:spent_mox_fuel
- essenceindustry:spent_thorium_fuel
- essenceindustry:spent_advanced_nuclear_fuel
- essenceindustry:recovered_fissile_material
- essenceindustry:nuclear_waste
---
# Nuclear fuel preparation

Uranium ingots in `c:ingots/uranium` supply 1,000 mB each. Blocks in `c:storage_blocks/uranium` supply 9,000 mB each. Different mods work through these tags. Nuggets and dusts are not reactor fuel. Each prepared fuel item represents 1,000 mB. Burned volume becomes matching spent fuel, solidified at 1 item per 1,000 mB; smaller remainders stay inside. Blocks therefore produce nine spent-fuel items, not one.

Start with ordinary uranium in a [reactor](reactor.md). Its solid spent fuel is the starting material for reprocessing. Enrichment and fabrication open stronger fuel options through the chain below.

## Which fuel should I make?

| Goal | Start here |
| --- | --- |
| First power, no processing plants | Put a tagged uranium ingot directly in the reactor. |
| Stronger fuel without waiting for spent fuel | Enrich nine uranium ingots into one Enriched Uranium Fuel. Store the eight Depleted Uranium byproducts. |
| Reuse the first spent fuel | Collect eight matching Spent Uranium or Spent Enriched Uranium items, then reprocess them into Recovered Plutonium. |
| Turn that plutonium into more fuel items | Select MOX in the Fabricator; combine one Recovered Plutonium with three Depleted Uranium for four MOX Fuel. |
| Reach Advanced Nuclear Fuel | Burn a prepared plutonium, MOX, thorium or advanced fuel, then reprocess eight matching spent items to get Recovered Fissile Material. Combine it with enriched fuel and thorium fuel. |

Recovered Plutonium is a fabrication ingredient, not ready-to-burn Plutonium Fuel. Recovered Fissile Material is a different ingredient. Do not mix different spent types in one reprocessing batch.

## First batch of Advanced Nuclear Fuel

1. Make two Enriched Uranium Fuel in the Enricher using 18 uranium ingots. Keep these for fabrication.
2. Select Thorium in the Fabricator and turn one tagged thorium ingot into one Thorium Fuel. Keep it too.
3. Separately obtain and burn enough MOX, Plutonium Fuel or Thorium Fuel to collect eight matching spent items. Burning ordinary or enriched uranium produces the plutonium recovery branch instead.
4. Reprocess those eight spent items into one Recovered Fissile Material and seven Nuclear Waste.
5. Select Advanced in the Fabricator. Supply one Recovered Fissile Material, two Enriched Uranium Fuel and one Thorium Fuel. The result is four Advanced Nuclear Fuel.

The two enriched items and one thorium item are ingredients, not the fuel burned in step 3. There is no self-starting advanced-fuel loop: the first recovered material must come from an earlier fuel. With longer fuel lifetimes, spent material arrives slowly; count your fuel's burned volume rather than expecting immediate reprocessing output.

## The fuel chain

**Uranium -> Enricher -> Enriched fuel + Depleted uranium**

**Reactor -> Solid spent fuel -> Reprocessor -> Recovered material + Nuclear waste**

**Recovered material -> Fabricator -> Prepared fuel -> Reactor**

Each processor is a multiblock with a distinct interior and required component counts. The illustrated layouts are examples; valid shell ports and internal components can be rearranged. Build the [Enricher](fuel_enricher.md), [Reprocessor](fuel_reprocessor.md) or [Fabricator](fuel_fabricator.md) from its layer guide. Craft Item Ports for both ingredients and products; right-click each port to choose **Input** or **Output**. Every controller accepts ingredients and FE. All three auto-build layouts include an Energy Port. Controllers also accept FE. Energy Ports automatically pull power when Active; Passive leaves transfer to external cables.

Pipes and AE2 Pattern Providers both work. Use processing patterns with all products and byproducts. Each plant stores 1,000,000 FE. A reserved batch keeps its ingredients and progress through a power interruption or incomplete structure; blocked outputs wait rather than discard products.

| Machine | Inputs | Outputs | FE per batch |
| --- | --- | --- | --- |
| Fuel Enricher | 9 tagged uranium ingots | 1 Enriched Uranium Fuel + 8 Depleted Uranium | 64,000 |
| Fuel Reprocessor | 8 Spent Uranium Fuel or 8 Spent Enriched Uranium Fuel | 1 Recovered Plutonium + 7 Nuclear Waste | 128,000 |
| Fuel Reprocessor | 8 matching spent Plutonium, MOX, Thorium or Advanced fuel | 1 Recovered Fissile Material + 7 Nuclear Waste | 128,000 |
| Fuel Fabricator | 1 Recovered Plutonium | 1 Plutonium Fuel | 32,000 |
| Fuel Fabricator | 1 Recovered Plutonium + 3 Depleted Uranium | 4 MOX Fuel | 32,000 |
| Fuel Fabricator | 1 tagged thorium ingot | 1 Thorium Fuel | 16,000 |
| Fuel Fabricator | 1 Recovered Fissile Material + 2 Enriched Uranium Fuel + 1 Thorium Fuel | 4 Advanced Nuclear Fuel | 256,000 |

Thorium ingots use `c:ingots/thorium`. If the pack supplies no matching item, that branch is unavailable until a pack adds one. No unrelated resource substitutes for thorium. Different spent fuel types are kept separate.

The Enricher takes 10 seconds per batch. Reprocessing takes 20 seconds. Fabrication takes 5 seconds, or 20 seconds for Advanced fuel. These are operating times at full power; a paused plant takes longer.

Select Plutonium, MOX, Thorium or Advanced mode in the Fabricator before supplying ingredients. The mode is saved and applies to automated input too. Selecting MOX prevents a partial delivery of plutonium from being consumed by the Plutonium Fuel recipe. A reserved batch locks its mode until it finishes.

## Craft the controllers

<Recipe id="essenceindustry:fuel_enricher" />
<Recipe id="essenceindustry:fuel_reprocessor" />
<Recipe id="essenceindustry:fuel_fabricator" />

Keep nuclear waste in storage. These machines do not automatically delete it.

[Enricher build](fuel_enricher.md) - [Reprocessor build](fuel_reprocessor.md) - [Fabricator build](fuel_fabricator.md)

## Fuel output comparison

Default potential output for the starter's three fuel segments at 0% rod insertion, without reflectors. Stronger fuels may require moderators or increased rod insertion to control heat; these are not promises of sustained operation at full output. Steam values assume adequate water input and steam extraction through Fluid Ports. No internal coolant pipes are needed. One prepared fuel item contains 1,000 mB.

| Fuel | Direct FE/t | Steam mB/t | Time per 1,000 mB |
| --- | --- | --- | --- |
| Uranium | 50,000 | 500 | 5 hours 20 minutes |
| Enriched uranium | 200,002 | 2,000 | 5 hours 20 minutes |
| MOX | 270,000 | 2,700 | 5 hours 56 minutes |
| Plutonium | 499,997 | 5,000 | 4 hours 16 minutes |
| Thorium | 45,001 | 450 | 8 hours 53 minutes |
| Advanced nuclear | 1,200,010 | 12,000 | 3 hours 33 minutes |

50% insertion halves both output and fuel consumption. A larger core consumes fuel faster in proportion to its installed fuel segments. Adjacent reflectors improve energy yield by up to 25%; adjacent moderators reduce heat generation by up to 25%. Server owners may customize every fuel's burn, energy and heat values.

## Grow lower-tier nuclear materials

Ordinary uranium already uses Mystical Agriculture's Uranium Seeds. Three additional resource crops support the lower part of the nuclear chain:

| Crop | Tier | Essence conversion |
| --- | --- | --- |
| Depleted Uranium | 4 | 8 essence around an empty center -> 4 Depleted Uranium |
| Thorium Fuel | 4 | 8 essence around an empty center -> 1 Thorium Fuel |
| Enriched Uranium | 5 | 9 essence in a full grid -> 1 Enriched Uranium Fuel |

Create the first seeds with the normal MA infusion recipe and the matching material, or use the Seed Crafter with that recipe. The seeds require material already obtained through the fuel chain; they do not unlock the first batch for free. They work with the Seed Library, Crop Simulator and essence conversion patterns. Plutonium, MOX and Advanced Nuclear Fuel have no new crop in this update.

Existing modpack crops with the same name are preserved. Our conversion recipes only load when the crop belongs to Essence & Industry and uses its intended tier.

## World balance settings

Fuel lasts approximately 32 times longer than in Beta 1, at the same power and steam output. Reprocessing recipes are unchanged, so spent fuel also accumulates more slowly.

Server owners and single-player users can edit `config/essenceindustry-reactor-server.toml` in the server or game instance folder while the world is stopped. Under `[reactor]`, `energyProductionMultiplier = 1.0` controls FE and steam output without changing fuel consumption. `fuelEfficiencyMultiplier = 1.0` controls fuel lifetime without changing output: 2.0 lasts twice as long. Both accept 0.25 through 8.0. These multiply the per-fuel settings and use fixed-point rounding and safety limits. Turbine coil ratings are unchanged. Restart the world after editing.

Untouched earlier fuel presets migrate automatically. Customized per-fuel presets are preserved.

For layout bonuses and your own estimates, see [Reactor calculations](reactor_calculations.md).
