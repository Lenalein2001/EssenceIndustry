---
navigation:
  title: Fuel Reprocessor
  position: 130
item_ids:
- essenceindustry:fuel_reprocessor
---

# Reprocessing plant

Recover useful material from solid spent reactor fuel.

Build **7 wide, 5 deep and 5 high**. These are outside dimensions. The example puts the controller at the front center, one block above the floor. For manual construction it can replace a casing on a vertical wall away from edges; fill its original position with a casing. Ports may replace shell blocks anywhere; keep at least one Item Port in Input mode and one in Output mode. The shown port positions are defaults. Process units can be rearranged inside the shell; keep the listed quantities and place actuators in the roof.

<GameScene interactive={true} fullWidth={true} zoom="1.15">
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="0" p:shell="21" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="0" p:shell="22" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="1" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="1" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="2" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="2" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="3" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="3" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="4" p:shell="37" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="4" p:shell="38" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="2" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:fuel_reprocessor" x="3" y="1" z="0" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="5" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="6" y="1" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_energy_out" x="6" y="1" z="1" p:facing="east" p:powered="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="0" y="1" z="2" p:facing="west" p:output="false" p:powered="false" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="2" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="4" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="6" y="1" z="2" p:facing="east" p:output="true" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_cooling_channel" x="1" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="false" />
  <Block id="essenceindustry:reactor_cooling_channel" x="2" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_cooling_channel" x="3" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_cooling_channel" x="4" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_cooling_channel" x="5" y="1" z="3" p:down="false" p:east="false" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_casing" x="6" y="1" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="5" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="6" y="1" z="4" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="0" p:bottom="false" p:facing="north" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="5" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="1" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="2" p:shell="1" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="2" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="4" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="2" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="5" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="4" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="0" p:bottom="true" p:facing="north" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="5" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="1" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="2" p:shell="1" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="2" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="4" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="2" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="5" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="4" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="0" p:shell="25" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="0" p:shell="26" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="1" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="1" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="2" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="2" y="4" z="2" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="4" y="4" z="2" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="2" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="3" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="3" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="4" p:shell="41" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="4" p:shell="42" />
</GameScene>

## Gather the parts

| Part | Place |
| --- | ---: |
| Fuel Reprocessor | 1 |
| Reactor Control Actuator | 2 |
| Reactor Casing | 114 |
| Process Conduit | 5 |
| Energy Port | 1 |
| Item Port | 2 |
| Reactor Observation Glass | 10 |
| Fuel Plant Treatment Vessel | 6 |

**141 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

Set Item Ports: **1 Input / 1 Output**.

<Recipe id="essenceindustry:fuel_reprocessor" />

[Craft the structural and interior parts](reactor_parts.md).

## Run the plant

Each batch needs 8 items of one spent-fuel type. Different spent-fuel types can occupy the input buffer at the same time; they are processed as separate batches and are never blended. Uranium and enriched uranium produce 1 Recovered Plutonium plus 7 Nuclear Waste. Other supported spent fuels produce 1 Recovered Fissile Material plus 7 Nuclear Waste. A batch takes 20 seconds and consumes 128,000 FE.

Connect FE to the Energy Port or controller. Craft the same Item Port for both directions. Right-click each port and select **Input** for ingredients or **Output** for products, matching the arrows in the layer scenes. The controller also accepts ingredients. You can use ordinary pipes or an AE2 Pattern Provider with a processing pattern; include both products and byproducts.

Inputs are a shared buffer, not a crafting grid. You may supply ingredients for several recipes together. The plant processes one complete batch of each ready recipe in turn; incomplete recipes wait without blocking other ready batches. This is a repeating recipe order, not the order in which individual items arrived.

**Current batch** shows the reserved work and its progress. **Next batch** shows the next complete recipe in the input buffer; the ready count includes waiting batches only. It counts available ingredients, so power or output space may still be needed. Keep space for every product and byproduct. Missing power, a blocked output, or an incomplete or unloaded structure pauses the reserved batch without losing its ingredients or progress.


[All fuel recipes and quantities](nuclear_fuels.md)

## Assemble from bottom to top

Each scene shows exactly one layer. Keep the same front direction for every layer and leave all unshown spaces empty. The controller is the only plant-specific outer block; the remaining parts are shared reactor parts.


## Layer 1 of 5: Floor

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="0" p:shell="21" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="0" p:shell="20" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="0" p:shell="22" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="1" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="1" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="1" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="2" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="2" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="2" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="3" p:shell="5" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="3" p:shell="4" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="3" p:shell="6" />
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="4" p:shell="37" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="5" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="6" y="0" z="4" p:shell="38" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 35 |

**35 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 2 of 5: Interior 1

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="2" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:fuel_reprocessor" x="3" y="1" z="0" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="5" y="1" z="0" p:shell="16" />
  <Block id="essenceindustry:reactor_casing" x="6" y="1" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_energy_out" x="6" y="1" z="1" p:facing="east" p:powered="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="0" y="1" z="2" p:facing="west" p:output="false" p:powered="false" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="2" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="4" y="1" z="2" p:bottom="true" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="6" y="1" z="2" p:facing="east" p:output="true" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_cooling_channel" x="1" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="false" />
  <Block id="essenceindustry:reactor_cooling_channel" x="2" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_cooling_channel" x="3" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_cooling_channel" x="4" y="1" z="3" p:down="false" p:east="true" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_cooling_channel" x="5" y="1" z="3" p:down="false" p:east="false" p:north="false" p:south="false" p:up="false" p:west="true" />
  <Block id="essenceindustry:reactor_casing" x="6" y="1" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="5" y="1" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="6" y="1" z="4" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Fuel Reprocessor | 1 |
| Reactor Casing | 16 |
| Process Conduit | 5 |
| Energy Port | 1 |
| Item Port | 2 |
| Fuel Plant Treatment Vessel | 2 |

**27 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

Set Item Ports: **1 Input / 1 Output**.

## Layer 3 of 5: Interior 2

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="0" p:bottom="false" p:facing="north" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="5" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="1" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="2" p:shell="1" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="2" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="4" y="2" z="2" p:bottom="false" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="2" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="5" y="2" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="6" y="2" z="4" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 15 |
| Reactor Observation Glass | 5 |
| Fuel Plant Treatment Vessel | 2 |

**22 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 4 of 5: Interior 3

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="0" p:bottom="true" p:facing="north" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="5" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="1" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="1" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="2" p:shell="1" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="2" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_treatment_vessel" x="4" y="3" z="2" p:bottom="false" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="2" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="3" p:shell="1" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="3" p:shell="2" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_casing" x="1" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="2" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="3" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="5" y="3" z="4" p:shell="32" />
  <Block id="essenceindustry:reactor_casing" x="6" y="3" z="4" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 15 |
| Reactor Observation Glass | 5 |
| Fuel Plant Treatment Vessel | 2 |

**22 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 5 of 5: Roof

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="0" p:shell="25" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="0" p:shell="26" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="1" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="1" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="1" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="2" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="2" y="4" z="2" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_actuator" x="4" y="4" z="2" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="2" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="2" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="3" p:shell="9" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="3" p:shell="8" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="3" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="4" p:shell="41" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="5" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="6" y="4" z="4" p:shell="42" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Control Actuator | 2 |
| Reactor Casing | 33 |

**35 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.
