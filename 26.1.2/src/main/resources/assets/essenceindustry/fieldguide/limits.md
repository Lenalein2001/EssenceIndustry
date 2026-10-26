---
navigation:
  title: What will not work
  position: 50
---

# Processing limits

## No per-machine item list

The Array reads native processing families. A new machine ID using a recognized framework can work without adding its ID to a list. That does not make unrelated machine frameworks interchangeable.

## Examples that do not work

- **Mekanism machines:** this processing bridge is not implemented in this build.
- **Ender IO chance-based SAG recipes or grinding-ball bonuses:** these are not simulated. Deterministic SAG recipes and native Alloy Smelter recipes are supported.
- **Oritech Atomic Forge:** its specialized energy and completion rules are not simulated by the standard item-processing bridge.
- **Oritech Pulverizer, fluid processors and custom output behavior:** these need additional handling. The Array refuses a specialized implementation rather than skipping its extra steps.
- **ExtendedAE Crystal Fixer:** this is not a normal shared-executor recipe machine in the current bridge.
- **FTBIC fluid recipes requiring multiple input/output tanks or more than native tank capacity:** these are refused. Deterministic recipes for native fluid machines, including Ore Washers, can use their single input/output tank pair.
- **FTBIC recipes with random byproducts or soil rules:** these are excluded from the current FTBIC recipe bridge.
- **Miners, generators or machines that interact with the world:** an input/output pattern cannot describe mining locations, entities or world conditions.
- **A made-up pattern:** changing the output or dropping required ingredients does not authorize a recipe.

## Native upgrades

Inserted machines are capacity tokens; their individual tickers and upgrade logic do not run. Use the Array's own Speed, Efficiency and Capacity modules. Ender IO recipes use the base capacitor rate, rounded up to whole processing ticks. Grinding balls and saved per-machine capacitor settings are not imported. Modified machine items are refused rather than stripping their saved contents or upgrades.

## Missing mods

A missing optional mod contributes no machines or recipes. The book remains readable and other integrations remain available. AE2 itself is required for the Array feature.
