---
navigation:
  title: Build an Array
  position: 10
---

# Build your first Array

1. Place the **Processing Array controller** facing outward. Auto-build starts it at the front-left corner, one block above the floor. A manual build can place it elsewhere on the outward-facing wall. Open it and select **Build**.
2. Choose the outside width, depth and height. These dimensions include the shell.
3. Use the layer selector and guide projection. Build a complete shell of Array Casing or Observation Glass.
4. Add the marked **two-block-high rack pairs**. Leave other interior spaces clear, including the gantry space beneath the roof.
5. Replace shell positions with Input / Pattern Hatches and optional Output Ports. Ports can go on any shell face.
6. Press **Verify** to apply the dimensions and validate your manual structure. The controller discovers the complete front wall. Width runs across that face.

## Auto-build from supplied blocks

After choosing dimensions and a layout in **Build**, press **Check**, then open **Materials**. This is the same construction screen used by the reactor and fuel plants; no separate tool is required.

1. Put the listed blocks in your inventory and press **Supply**. Only required plain blocks move into the controller; extra blocks and renamed/component-bearing blocks stay with you.
2. Press **Build**. The controller fills the selected shell and rack layout, reusing existing matching blocks. For a custom interior or custom port positions, use manual building and **Verify** instead.
3. Use **Pause** to stop construction and further ME requests. Closing the screen leaves the job running while its chunks are loaded. **Return** pauses construction and retrieves stored blocks; rejected returns stay in the controller. Already placed blocks remain in the world.

Matching blocks are reused. Safely removable mismatches inside the selected plan can be replaced, including positions that should be air; their normal drops are retained before replacement. Protected blocks, liquids, unbreakable blocks and arbitrary block entities require manual attention. The builder never loads missing chunks or bypasses placement protection. A saved running job validates its area and resumes after a reload; a paused job stays paused. Changing a plan retains supplied materials.

Construction has a separate material bank. It does not use machine installations, crafting inputs or creative-mode free blocks. Enable **ME Supply** on Materials to pull network stock and request missing blocks through AE autocrafting. Supply and construction use bounded batches, so the whole Array does not need to fit in the bank at once. Rack pairs are built automatically; install your chosen machines afterward. See [Construction](multiblock_planner.md) for ME status, repair and return behavior.

## Install machines

Put in one exact machine type. Two Crystal Assemblers can share an Array; a Crystal Assembler and a Circuit Slicer cannot.

Each complete rack pair supports **eight machines**. The inventory refuses extra installations once its capacity is reached. More racks provide room for more simultaneous processing. Taller Arrays can have stacked rack pairs.

## Expand later

Take unfinished work into account before changing dimensions. The Build tab uses the server's size limits; the default maximum width/depth is 32 blocks and maximum height is 17. A formed Array can be wider, deeper or taller.
