---
navigation:
  title: Build a reactor
  position: 100
item_ids:
- essenceindustry:reactor_casing
- essenceindustry:reactor_casing
- essenceindustry:reactor_glass
- essenceindustry:reactor_glass
- essenceindustry:reactor_fuel_assembly
- essenceindustry:reactor_actuator
---

# Your first nuclear reactor

This starter is **5 wide, 5 deep and 5 high**, including the outer shell. It runs ordinary uranium in Direct FE mode at the default settings.

The front has the controller between two Item Ports: set the left port to **Input** and the right port to **Output** by right-clicking each port. Both use the same crafted block. The Energy Port is at the back. The integrated fuel/control column fills all three interior layers. A control actuator replaces the roof block directly above it.

Each fuel assembly already contains its control mechanism. Larger reactors use the same rule: one full-height fuel assembly column, one roof actuator directly above it. No separate control columns or spacing checks are needed.

For a custom layout, the [core designer](multiblock_planner.md#design-a-reactor-footprint) edits one top-down footprint. Fuel, moderator and reflector cells extend through the full interior height, with roof actuators added automatically above fuel.

For Steam mode, add a Fluid Port set to **Input** for water and another set to **Output** for steam. No internal coolant pipes are needed; the integrated fuel columns stay the same.

Drag a scene to inspect it. Every layer below uses the same orientation; the front is the side with the controller. Build from the floor upward and leave every unshown space empty. Glass is a window, not an air gap.

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
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="4" p:shell="37" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="4" p:shell="38" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_fuel_input" x="1" y="1" z="0" p:facing="north" p:output="false" p:powered="false" />
  <Block id="essenceindustry:reactor_controller" x="2" y="1" z="0" p:facing="north" p:powered="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="3" y="1" z="0" p:facing="north" p:output="true" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="1" z="1" p:bottom="false" p:facing="west" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="1" z="1" p:bottom="false" p:facing="east" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="1" z="2" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_fuel_assembly" x="1" y="1" z="2" p:bottom="true" p:fuel="0" p:level="4" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="1" z="2" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="1" z="3" p:bottom="false" p:facing="west" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="1" z="3" p:bottom="false" p:facing="east" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="1" z="4" p:bottom="false" p:facing="south" p:left="false" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_energy_out" x="2" y="1" z="4" p:facing="south" p:powered="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="1" z="4" p:bottom="false" p:facing="south" p:left="false" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="4" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="0" p:bottom="false" p:facing="north" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="1" p:bottom="true" p:facing="west" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="1" p:bottom="true" p:facing="east" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="2" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_fuel_assembly" x="1" y="2" z="2" p:bottom="false" p:fuel="0" p:level="4" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="2" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="3" p:bottom="true" p:facing="west" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="3" p:bottom="true" p:facing="east" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="4" p:bottom="true" p:facing="south" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="4" p:bottom="false" p:facing="south" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="4" p:bottom="true" p:facing="south" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="4" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="0" p:bottom="true" p:facing="north" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="0" p:bottom="true" p:facing="north" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="1" p:bottom="true" p:facing="west" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="1" p:bottom="true" p:facing="east" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="2" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_fuel_assembly" x="1" y="3" z="2" p:bottom="false" p:fuel="0" p:level="4" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="2" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="3" p:bottom="true" p:facing="west" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="3" p:bottom="true" p:facing="east" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="4" p:bottom="true" p:facing="south" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="4" p:bottom="true" p:facing="south" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="4" p:bottom="true" p:facing="south" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="4" p:shell="34" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="0" p:shell="25" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="0" p:shell="26" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="1" p:shell="9" />
  <Block id="essenceindustry:reactor_glass" x="1" y="4" z="1" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="2" y="4" z="1" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="3" y="4" z="1" p:facing="up" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="1" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="2" p:shell="9" />
  <Block id="essenceindustry:reactor_actuator" x="1" y="4" z="2" p:facing="north" p:powered="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="4" z="2" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="3" y="4" z="2" p:facing="up" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="2" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="3" p:shell="9" />
  <Block id="essenceindustry:reactor_glass" x="1" y="4" z="3" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="2" y="4" z="3" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="3" y="4" z="3" p:facing="up" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="3" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="4" p:shell="41" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="4" p:shell="42" />
</GameScene>

## Gather the parts

| Part | Place |
| --- | ---: |
| Reactor Control Actuator | 1 |
| Reactor Casing | 53 |
| Nuclear Reactor Controller | 1 |
| Energy Port | 1 |
| Reactor Fuel Assembly | 3 |
| Item Port | 2 |
| Reactor Observation Glass | 40 |

**101 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

Set Item Ports: **1 Input / 1 Output**.

[Craft the reactor parts](reactor_parts.md). Use the same Reactor Casing for walls, corners and edges.

Once built, open **Build** from the controller, keep the dimensions at **5 / 5 / 5**, and run **Verify** to check the manual build. The illustrated controller position is a default: manual reactors accept an outward-facing controller on any horizontal shell face away from edges. Then follow [Starting and controlling a reactor](reactor.md).


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
  <Block id="essenceindustry:reactor_casing" x="0" y="0" z="4" p:shell="37" />
  <Block id="essenceindustry:reactor_casing" x="1" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="2" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="3" y="0" z="4" p:shell="36" />
  <Block id="essenceindustry:reactor_casing" x="4" y="0" z="4" p:shell="38" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 25 |

**25 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 2 of 5: Interior 1

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_fuel_input" x="1" y="1" z="0" p:facing="north" p:output="false" p:powered="false" />
  <Block id="essenceindustry:reactor_controller" x="2" y="1" z="0" p:facing="north" p:powered="false" />
  <Block id="essenceindustry:reactor_fuel_input" x="3" y="1" z="0" p:facing="north" p:output="true" p:powered="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="1" z="1" p:bottom="false" p:facing="west" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="1" z="1" p:bottom="false" p:facing="east" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="1" z="2" p:bottom="false" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_fuel_assembly" x="1" y="1" z="2" p:bottom="true" p:fuel="0" p:level="4" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="1" z="2" p:bottom="false" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="1" z="3" p:bottom="false" p:facing="west" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="1" z="3" p:bottom="false" p:facing="east" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="0" y="1" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="1" z="4" p:bottom="false" p:facing="south" p:left="false" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_energy_out" x="2" y="1" z="4" p:facing="south" p:powered="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="1" z="4" p:bottom="false" p:facing="south" p:left="false" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="1" z="4" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 4 |
| Nuclear Reactor Controller | 1 |
| Energy Port | 1 |
| Reactor Fuel Assembly | 1 |
| Item Port | 2 |
| Reactor Observation Glass | 8 |

**17 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

Set Item Ports: **1 Input / 1 Output**.

## Layer 3 of 5: Interior 2

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="0" p:shell="17" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="0" p:bottom="false" p:facing="north" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="0" p:bottom="false" p:facing="north" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="0" p:shell="18" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="1" p:bottom="true" p:facing="west" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="1" p:bottom="true" p:facing="east" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="2" p:bottom="true" p:facing="west" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_fuel_assembly" x="1" y="2" z="2" p:bottom="false" p:fuel="0" p:level="4" p:powered="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="2" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="0" y="2" z="3" p:bottom="true" p:facing="west" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="2" z="3" p:bottom="true" p:facing="east" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="0" y="2" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="2" z="4" p:bottom="true" p:facing="south" p:left="true" p:right="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="2" y="2" z="4" p:bottom="false" p:facing="south" p:left="true" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="3" y="2" z="4" p:bottom="true" p:facing="south" p:left="false" p:right="true" p:top="true" />
  <Block id="essenceindustry:reactor_casing" x="4" y="2" z="4" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 4 |
| Reactor Fuel Assembly | 1 |
| Reactor Observation Glass | 12 |

**17 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

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
  <Block id="essenceindustry:reactor_fuel_assembly" x="1" y="3" z="2" p:bottom="false" p:fuel="0" p:level="4" p:powered="false" p:top="true" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="2" p:bottom="true" p:facing="east" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="0" y="3" z="3" p:bottom="true" p:facing="west" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="4" y="3" z="3" p:bottom="true" p:facing="east" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="0" y="3" z="4" p:shell="33" />
  <Block id="essenceindustry:reactor_glass" x="1" y="3" z="4" p:bottom="true" p:facing="south" p:left="true" p:right="false" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="3" z="4" p:bottom="true" p:facing="south" p:left="true" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_glass" x="3" y="3" z="4" p:bottom="true" p:facing="south" p:left="false" p:right="true" p:top="false" />
  <Block id="essenceindustry:reactor_casing" x="4" y="3" z="4" p:shell="34" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Casing | 4 |
| Reactor Fuel Assembly | 1 |
| Reactor Observation Glass | 12 |

**17 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

## Layer 5 of 5: Roof

<GameScene interactive={true} fullWidth={true} zoom="1.4">
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="0" p:shell="25" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="0" p:shell="24" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="0" p:shell="26" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="1" p:shell="9" />
  <Block id="essenceindustry:reactor_glass" x="1" y="4" z="1" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="2" y="4" z="1" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="3" y="4" z="1" p:facing="up" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="1" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="2" p:shell="9" />
  <Block id="essenceindustry:reactor_actuator" x="1" y="4" z="2" p:facing="north" p:powered="false" />
  <Block id="essenceindustry:reactor_glass" x="2" y="4" z="2" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="3" y="4" z="2" p:facing="up" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="2" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="3" p:shell="9" />
  <Block id="essenceindustry:reactor_glass" x="1" y="4" z="3" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="2" y="4" z="3" p:facing="up" />
  <Block id="essenceindustry:reactor_glass" x="3" y="4" z="3" p:facing="up" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="3" p:shell="10" />
  <Block id="essenceindustry:reactor_casing" x="0" y="4" z="4" p:shell="41" />
  <Block id="essenceindustry:reactor_casing" x="1" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="2" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="3" y="4" z="4" p:shell="40" />
  <Block id="essenceindustry:reactor_casing" x="4" y="4" z="4" p:shell="42" />
</GameScene>

| Part | Place |
| --- | ---: |
| Reactor Control Actuator | 1 |
| Reactor Casing | 16 |
| Reactor Observation Glass | 8 |

**25 blocks total.** Counts are placed blocks; some crafting recipes produce several at once.

[Reactor controls and troubleshooting](reactor.md)
