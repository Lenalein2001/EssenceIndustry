---
navigation:
  title: Recipe examples
  position: 40
---

# Working examples

These examples require the named mods and their recipes. A modpack can change or remove a recipe.

## Circuit Slicer - ExtendedAE

**1 Gold Block -> 9 Printed Logic Processors**

Install Circuit Slicers and encode this as a processing pattern. This is slicing; crafting the Circuit Slicer itself is a separate recipe.

## Roller - FTB Industrial Contraptions

**1 Iron Ingot -> 1 Iron Plate**

Normal and advanced native profiles are discovered from their processing family. Keep one exact variant per Array.

## Compressor - FTB Industrial Contraptions

**4 Blaze Powder -> 1 Blaze Rod**

Use all four inputs for one native operation.

## Assembler - Oritech

**1 Flint + 2 Sticks + 1 Feather -> 16 Arrows**

Repeated ingredients count individually. The Array does not require sixteen separate machine calls for sixteen arrows.

## Reaction Chamber - AdvancedAE

**1 Quantum Infused Dust + 4,000 mB Water -> 1,000 mB Quantum Infusion Source**

A processing pattern can include fluids. The output is a fluid, so the ME network needs fluid storage space as well as item storage.

## Furnace - Minecraft

**1 Raw Iron -> 1 Iron Ingot**

This also works through ordinary hopper/pipe input. A Blast Furnace handles blasting recipes; a Smoker handles smoking recipes.

## Alloy Smelter and SAG Mill - Ender IO

Install one machine type and encode its complete native recipe. Deterministic SAG examples include **1 Bone Block -> 9 Bone Meal** and **1 Blaze Rod -> 4 Blaze Powder**. A recipe with any random byproduct is refused as a whole; the Array does not silently omit it.

## Fluid machines - FTB Industrial Contraptions

A pack can supply deterministic recipes with one fluid input and one fluid output for native fluid-processing machines. Encode every item and fluid in the processing pattern, and provide both item and fluid output storage. Probabilistic and soil-dependent recipes remain excluded. The Array does not add new production recipes for these machines.
