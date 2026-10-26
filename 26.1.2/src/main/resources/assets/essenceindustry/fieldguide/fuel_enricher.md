---
navigation:
  title: Fuel Enricher
  position: 120
item_ids:
- essenceindustry:fuel_enricher
---

# Enrichment plant

Prepare enriched uranium and depleted uranium from ordinary tagged uranium ingots.

Build **5 wide, 7 deep and 5 high**. These are outside dimensions. The example puts the controller at the front center, one block above the floor. For manual construction it can replace a casing on a vertical wall away from edges; fill its original position with a casing. Ports may replace shell blocks anywhere; keep at least one Item Port in Input mode and one in Output mode. The shown port positions are defaults. Process units can be rearranged inside the shell; keep the listed quantities and place actuators in the roof.

<GameScene interactive={true} fullWidth={true} zoom="1.15">
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="0" p:shell="21" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="0" p:shell="22" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="1" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="1" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="2" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="2" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="3" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="3" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="4" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="4" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="4" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="4" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="4" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="5" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="5" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="5" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="5" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="5" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="6" p:shell="37" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="6" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="6" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="6" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="6" p:shell="38" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:fuel_enricher" x="2" y="1" z="0" />
  <Block id="essenceindustry:reactor_casing" x="3" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="1" p:shell="2" />
  <Block id="essenceindustry:reactor_fuel_input" x="0" y="1" z="2" p:facing="west" p:output="false" p:powered="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_energy_out" x="4" y="1" z="2" p:facing="east" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="4" p:shell="1" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="1" z="4" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="1" z="4" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="4" y="1" z="4" p:facing="east" p:output="true" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="5" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="5" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="6" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="6" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="1" z="6" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="1" z="6" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="6" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="0" p:bottom="false" p:facing="north" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="1" p:bottom="false" p:facing="west" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="1" p:bottom="false" p:facing="east" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="2" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="2" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="3" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="3" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="4" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="2" z="4" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="2" z="4" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="4" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="5" p:bottom="false" p:facing="west" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="5" p:bottom="false" p:facing="east" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="6" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="6" p:bottom="false" p:facing="south" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="6" p:bottom="false" p:facing="south" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="6" p:bottom="false" p:facing="south" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="6" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="0" p:bottom="true" p:facing="north" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="1" p:bottom="true" p:facing="west" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="1" p:bottom="true" p:facing="east" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="2" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="2" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="3" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="3" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="4" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="3" z="4" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="3" z="4" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="4" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="5" p:bottom="true" p:facing="west" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="5" p:bottom="true" p:facing="east" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="6" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="6" p:bottom="true" p:facing="south" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="6" p:bottom="true" p:facing="south" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="6" p:bottom="true" p:facing="south" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="6" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="0" p:shell="25" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="0" p:shell="26" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="1" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="1" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="2" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="2" y="4" z="2" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="2" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="3" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="3" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="4" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="4" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="2" y="4" z="4" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="4" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="4" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="5" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="5" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="5" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="5" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="5" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="6" p:shell="41" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="6" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="6" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="6" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="6" p:shell="42" />
</GameScene>

## Gather the parts

| Part | Place |
| --- | ---: |
| Fuel Enricher | 1 |
| Reactor Control Actuator | 2 |
| Reactor Casing | 92 |
| Fuel Plant Centrifuge | 12 |
| Energy Port | 1 |
| Item Port | 2 |
| Reactor Observation Glass | 32 |

**142 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

Set Item Ports: **1 Input / 1 Output**.

<Recipe id="essenceindustry:fuel_enricher" />

[Craft the structural and interior parts](reactor_parts.md).

## Run the plant

9 uranium ingots become 1 Enriched Uranium Fuel and 8 Depleted Uranium. A batch takes 10 seconds at full power and consumes 64,000 FE.

Connect FE to the Energy Port or controller. Craft the same Item Port for both directions. Right-click each port and select **Input** for ingredients or **Output** for products, matching the arrows in the layer scenes. The controller also accepts ingredients. You can use ordinary pipes or an AE2 Pattern Provider with a processing pattern; include both products and byproducts.

The progress display shows the current batch. Missing power or an incomplete or unloaded structure pauses a reserved batch without losing its inputs or progress. Keep space for every output.


[All fuel recipes and quantities](nuclear_fuels.md)

## Assemble from bottom to top

Each scene shows exactly one layer. Keep the same front direction for every layer and leave all unshown spaces empty. The controller is the only plant-specific outer block; the remaining parts are shared reactor parts.


## Layer 1 of 5: Floor

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="0" p:shell="21" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="0" p:shell="22" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="1" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="1" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="2" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="2" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="3" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="3" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="4" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="4" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="4" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="4" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="4" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="5" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="5" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="5" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="5" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="5" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="6" p:shell="37" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="6" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="6" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="6" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="6" p:shell="38" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 35 |

**35 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 2 of 5: Interior 1

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:fuel_enricher" x="2" y="1" z="0" />
  <Block id="essenceindustry:reactor_casing" x="3" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="1" p:shell="2" />
  <Block id="essenceindustry:reactor_fuel_input" x="0" y="1" z="2" p:facing="west" p:output="false" p:powered="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_energy_out" x="4" y="1" z="2" p:facing="east" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="4" p:shell="1" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="1" z="4" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="1" z="4" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="4" y="1" z="4" p:facing="east" p:output="true" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="5" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="5" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="6" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="6" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="1" z="6" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="1" z="6" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="6" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Fuel Enricher | 1 |
| Reactor Casing | 16 |
| Fuel Plant Centrifuge | 4 |
| Energy Port | 1 |
| Item Port | 2 |

**24 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

Set Item Ports: **1 Input / 1 Output**.

## Layer 3 of 5: Interior 2

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="0" p:bottom="false" p:facing="north" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="1" p:bottom="false" p:facing="west" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="1" p:bottom="false" p:facing="east" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="2" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="2" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="3" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="3" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="4" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="2" z="4" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="2" z="4" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="4" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="5" p:bottom="false" p:facing="west" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="5" p:bottom="false" p:facing="east" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="6" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="6" p:bottom="false" p:facing="south" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="6" p:bottom="false" p:facing="south" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="6" p:bottom="false" p:facing="south" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="6" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 4 |
| Fuel Plant Centrifuge | 4 |
| Reactor Observation Glass | 16 |

**24 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 4 of 5: Interior 3

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="0" p:bottom="true" p:facing="north" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="1" p:bottom="true" p:facing="west" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="1" p:bottom="true" p:facing="east" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="2" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="2" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="3" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="3" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="4" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_centrifuge" x="1" y="3" z="4" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_centrifuge" x="3" y="3" z="4" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="4" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="5" p:bottom="true" p:facing="west" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="5" p:bottom="true" p:facing="east" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="6" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="6" p:bottom="true" p:facing="south" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="6" p:bottom="true" p:facing="south" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="6" p:bottom="true" p:facing="south" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="6" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 4 |
| Fuel Plant Centrifuge | 4 |
| Reactor Observation Glass | 16 |

**24 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 5 of 5: Roof

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="0" p:shell="25" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="0" p:shell="26" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="1" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="1" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="2" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="2" y="4" z="2" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="2" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="3" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="3" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="4" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="4" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="2" y="4" z="4" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="4" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="4" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="5" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="5" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="5" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="5" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="5" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="6" p:shell="41" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="6" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="6" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="6" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="6" p:shell="42" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Control Actuator | 2 |
| Reactor Casing | 33 |

**35 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

[Reactor controls and troubleshooting](reactor.md)
