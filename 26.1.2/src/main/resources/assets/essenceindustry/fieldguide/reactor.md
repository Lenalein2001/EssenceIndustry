---
navigation:
  title: Reactor controls
  position: 110
item_ids:
- essenceindustry:reactor_controller
- essenceindustry:reactor_fuel_input
- essenceindustry:reactor_waste_output
- essenceindustry:reactor_energy_out
- essenceindustry:reactor_coolant_in
- essenceindustry:reactor_coolant_out
---

# Starting and controlling a reactor

A reactor converts fuel into either FE or transportable steam, plus matching solid spent fuel. Start with the [5 by 5 by 5 example](reactor_building.md); ordinary uranium works in Direct FE mode at the default settings. Fuel plants are only needed when you want to enrich uranium or reuse spent fuel.

## First startup

1. Build the complete shell and full-height fuel assembly columns. Place one control actuator in the roof directly above each fuel column.
2. Open the controller and set the **outside** width, depth and height to match the build. Keep 5 / 5 / 5 for the starter. Check the structure.
3. Right-click one Item Port and set it to **Output**, then connect a chest or extraction pipe. Connect an FE cable or storage device to the separate Energy Port.
4. Set another Item Port to **Input** and supply tagged uranium ingots or uranium storage blocks, or use the controller's fuel slot. Both Item Port modes use the same crafted block.
5. Set control-rod insertion, then start. Higher insertion reduces fuel use and output; 100% insertion stops the burn. Watch heat and the status line while increasing output.

No AE2 network is required. Ordinary item and energy transport works with the ports. Ports can face outward from any valid shell face, including the roof and floor; place shell ports away from edges and corners.

## Reading the controller

**Fuel** shows the remaining liquid-equivalent amount of the selected fuel. **Spent fuel** shows burned material waiting to become solid output. **Energy** shows stored FE in Direct FE mode; **Steam** shows stored vapor in Steam mode. The center readout shows current FE/t or mB/t. **Heat** rises during Direct FE operation and falls through passive cooling. In Steam mode, fuel heat is transferred into vapor instead. The status line explains what currently permits or prevents a burn.

Start and stop control the burn. Stopping keeps fuel, spent material and stored energy. Rod insertion lets you adjust output without rebuilding. The Build controls describe the structure size and validation result; the guide gives a complete example rather than requiring you to guess a layout.

A redstone signal at the controller pauses an enabled reactor. Removing the signal lets it resume when its other operating conditions are met.

## Fuel accounting

One tagged uranium ingot supplies **1,000 mB**; a tagged uranium storage block supplies **9,000 mB**. Each prepared fuel item supplies **1,000 mB**. Uranium nuggets and dusts are not accepted as fuel.

Each 1,000 mB burned produces one matching solid spent-fuel item. Smaller remainders stay in the reactor, including across saves and unloads. A uranium block therefore yields nine spent-fuel items after it has been fully consumed. The tank holds one fuel type at a time; retained fuel or spent material must be dealt with before a different type can be loaded.

[Enrichment, fabrication and spent-fuel reprocessing](nuclear_fuels.md)

## Cooling and interior choices

The reactor removes heat passively. **Direct FE** mode generates power and does not consume water. Internal coolant pipes are not required in either mode.

For **Steam** mode, place two Fluid Ports and right-click them to set one to **Input** and the other to **Output**. Supply ordinary water to the input and extract steam from the output. The ports communicate with the reactor directly; no internal pipe connection or perimeter ring is needed. Stop the reactor, select Steam at the top of the controller, then start again.

Every 1 mB of water becomes 1 mB of steam. Steam mode produces **no new FE** in the reactor; the energy leaves as vapor. Existing FE remains available for extraction. Steam production follows the reactor's fuel output and available water; excess steam is vented when storage is full. Water and steam tanks grow with fuel segments, from 64,000 mB up to 1,000,000,000 mB.

The starter produces about **500 mB/t** at 0% rod insertion. A dry inlet or missing fluid port pauses the burn. If the steam tank is full, excess steam is vented and the reactor keeps running. Mode changes require the reactor to be stopped and the vapor outlet drained. Fractional output is preserved, including through mode changes and saves.

The fluid is `essenceindustry:steam`, tagged `c:steam`. Other machines must actually accept this fluid or tag to use it; a matching name alone does not guarantee compatibility. Connect this output to a [Steam Turbine](turbine.md), then return its condensate to the reactor water inlet.

Moderators next to fuel reduce heat production. Reflectors next to fuel improve energy yield. A checkerboard is an option, not a required pattern. Each fuel assembly contains an integrated control absorber, driven by the actuator above its column. Insertion adjusts burn independently of the water and steam ports. Watch the heat gauge and adjust insertion as you move to stronger fuels. Server configuration can change capacities and fuel rates.

**Fuel and control are one block.** Every full-height fuel column needs exactly one actuator directly above it in the roof. The central absorber moves inside the assembly as insertion changes; fuel rods remain around it. Columns may touch each other or the inside of the shell. The controller adjusts all absorbers together.

## When the reactor stops

| Status or symptom | What to check |
| --- | --- |
| Structure incomplete | Check outside dimensions, the sealed shell, required ports and continuous columns. Each fuel column needs its roof actuator. |
| Add an actuator above fuel | Replace the roof block directly above the reported fuel column with a Control Actuator. Existing blocks and stored fuel are preserved. |
| Waiting for loaded chunks | Keep the entire reactor loaded. The controller does not force-load the rest of the structure. |
| No fuel | Supply an accepted fuel and check that retained material is not locking a different fuel type. |
| Rods inserted | Reduce insertion below 100% when you want power. |
| Redstone pause | Remove the signal from the controller when you want the enabled reactor to resume. |
| Energy full | Connect a consuming machine or a larger energy buffer. Fuel consumption pauses while FE cannot be accepted. |
| Water needed / fluid ports missing | Supply water and provide Fluid Ports in Input and Output modes. Internal pipes are not required. |
| Steam full | Excess steam is vented. Extract it to use the energy instead. |
| Drain old coolant | Extract retained water from an older reactor save before starting Steam mode. |
| Spent fuel full | Extract solid spent fuel and provide storage for it. The reactor keeps its retained material. |
| Thermal trip | The burn has stopped safely. Let it cool passively, add moderators beside fuel or increase rod insertion to reduce heat production, then reset and start again. |
| Configuration prevents operation | A server limit may be below retained contents. Extract what you can or ask the server owner to review its reactor settings. |

Heat protection stops the reactor instead of causing an explosion. Breaking or unloading part of the structure stops the burn; it does not keep running through an invalid shell.

## Build larger

Width, depth and height are independent outside dimensions. Keep a sealed shell with an outward-facing controller on a horizontal shell face away from edges, at least one Item Port in **Input** mode, one in **Output** mode and an Energy Port. Fuel assembly sections span the entire interior height, with an actuator directly above every column. The starter guide illustrates these rules with exact layers and counts; its controller position is an example, not a required coordinate.

The auto-builder puts an actuator above every fuel column, without occupying additional core cells. A 13 by 13 checkerboard core has 61 fuel/control columns and 61 actuators; a 32 by 32 checkerboard core has 450 of each. Height extends each column and changes its assembly count; the actuator count stays the same.

Old separate Control Rod blocks and items convert one-for-one to Reactor Moderators when loaded. Their obsolete registration and assets are removed; the conversion does not create additional fuel. Existing fuel columns require their own roof actuators before the reactor can run. Saving or loading alone never replaces world blocks. Rebuilding through the planner returns removed moderators through the normal construction storage path.

[Build the starter reactor](reactor_building.md) - [Craft the parts](reactor_parts.md)

## Output and scale

At default balance, the 5 by 5 by 5 starter has one fuel column containing three segments. With ordinary uranium and **0% rod insertion**, it generates approximately **50,000 FE/t**. The default 50% insertion gives about 25,000 FE/t. One uranium ingot lasts about 5 hours 20 minutes at maximum starter output; one block lasts about 48 hours.

Output scales with complete fuel segments, not empty shell volume. A column contains outside height minus two segments. These examples assume 0% insertion, no reflectors, default configuration and enough output capacity:

| Outer size | Fuel columns | Fuel segments | Uranium FE/t | Steam mB/t |
| --- | --- | --- | --- | --- |
| 5 x 5 x 5 | 1 | 3 | 50,000 | 500 |
| 7 x 7 x 7 | 4 | 20 | 333,336 | 3,333 |
| 9 x 9 x 9 | 12 | 84 | 1,400,011 | 14,000 |
| 11 x 11 x 11 | 24 | 216 | 3,600,029 | 36,000 |
| 16 x 16 x 24 | 49 | 1,078 | 17,966,810 | 179,668 |

These are example populations, not maximum capacities. Steam estimates require enough water supply and steam extraction. Stronger fuels may need moderators or increased rod insertion to run continuously. [Compare fuel outputs](nuclear_fuels.md).

Control rod buttons move by **1%** per click or **10%** with Shift held. Input ports can actively pull from the block they face; output ports can actively push into it. Open a port to toggle **Active transfer**. External pipes work even when active transfer is off.

[Calculate insertion, neighbour bonuses, fuel lifetime and output](reactor_calculations.md).
