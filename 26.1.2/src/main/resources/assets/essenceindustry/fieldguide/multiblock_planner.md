---
navigation:
  title: Construction
  position: 75
item_ids:
- essenceindustry:multiblock_planner
---
# Controller construction

Open a controller and choose **Build** (or **Construction** on the turbine). The Array, reactor, turbine and fuel-processing plants share this construction screen; no extra tool is needed. **Controller** returns to the machine controls.

1. Set the outer dimensions and choose layout options from the dropdowns. Use the 3D view, Cutaway and Turn controls to inspect the plan, or switch to Layers for exact placement. Large plans use the bounded layer view. Hover a cell in the layer map to identify its block.
2. Press **Check**. Existing matching blocks are reused. Replaceable mismatches are included in the repair plan; the scan itself changes no blocks. Protected blocks, fluids, block entities with private data and unloaded chunks stop the scan.
3. Open **Materials**. Each row shows supplies stored in this controller, blocks still needed, and items in your inventory.
4. Press **Supply**, then **Build**. Only required plain blocks are consumed, including in creative mode. Build also replaces safely removable wrong blocks inside this exact layout, including blocks where the plan expects air. Their normal drops enter construction storage before replacement. Existing matching blocks and anything outside the plan are left alone.
5. Construction runs automatically after Build. Close the screen, use other machines or leave the area; the controller keeps working while its chunks are loaded. Reopening shows the same job and scan. Changes inside the plan are detected automatically; nearby unrelated updates do not restart the scan.
6. Press **Pause** to stop both construction and ME supply, including while waiting for materials. **Build** resumes a paused plan. Press **Return** to pause and retrieve unused supplies. With ME enabled, returns go to the connected network; otherwise they go to your inventory. Rejected returns stay in the controller. Breaking a controller drops ordinary remaining supplies; an unresolved transfer prevents normal removal until its saved record is reconciled.

## ME supplies

On Materials, enable **ME Supply**. Connect a powered ME cable to the controller. Reactor, turbine and fuel-plant controllers are real ME endpoints and use one channel. The Array uses its existing network connection. The network must have storage and, for missing blocks, encoded patterns and a crafting CPU.

Existing stock is pulled in bounded batches, prioritizing the next placements. The controller keeps a working reserve instead of trying to store the entire structure: large builds refill as they go. Materials outside that reserve, unused materials and repair drops are returned to ME automatically. A full or read-only network leaves them safely buffered. Crafting calculations do not block other stored materials. Missing materials are requested in adaptive batches, shrinking down to one when ingredients or CPU capacity cannot support a larger request. Existing jobs for the same item are allowed to finish before another is submitted. Supply and construction continue with the screen closed. After a server restart or controller chunk reload, the saved job validates its area and resumes automatically.

Pause stops construction and further ME requests; already submitted AE jobs still finish into network storage. Return also pauses before giving back unused materials.

The ME button shows connection, pulling, crafting or waiting state. When construction needs an item, Materials also names the next required block. Supplied materials belong to the controller. No chunks are force-loaded.

## Layout choices

Reactors offer single, spaced or checkerboard fuel columns, moderators or reflectors, windows, and Direct FE or Steam mode. Steam plans add Fluid Ports for water input and steam output; they use the same interior fuel/filler pattern as Direct FE plans and do not add internal coolant pipes. Fuel assemblies combine fuel and control in one column. The preview and material list automatically include one roof actuator above every fuel column.

For manual layouts, put one actuator directly above each full-height fuel assembly column. There is no separate control-rod spacing rule. Insertion adjusts the absorbers inside all fuel columns together.

## Design a reactor footprint

In a reactor controller, open **Build**, then **Design**. Opening Design copies the current preset into an editable **top-down interior footprint**. Choose **Fuel + control**, **Moderator**, **Reflector** or **Empty space**, then click or drag across the horizontal cells. Right-click a cell to erase it. The shell, controller and ports still follow the construction settings. Each painted cell extends through the full interior height; every fuel column gets a roof actuator automatically. There is no need to draw individual layers.

Use a preset as a starting point or edit the footprint directly. **Layout** shows the resulting structure; **Use preset core** switches back to the selected automatic pattern and discards the controller's custom footprint. The preview and material requirements use the same saved footprint as construction. Height can change without redrawing it. Increasing width or depth adds empty cells while preserving existing positions. Shrinking is refused if it would discard a non-empty cell; clear or move those cells first.

Incomplete drafts can be saved and previewed, but construction requires at least one fuel column. Actuators are added automatically; they are not a separate paint choice. Building the design uses the normal Materials bank or ME Supply and the same bounded construction worker.

### Save a reusable design

Craft a **Multiblock Planner** with four iron nuggets, four paper and one redstone. Carry it in your inventory while using the controller's Design tab. The planner stores a reusable layout; the controller still performs construction.

<Recipe id="essenceindustry:multiblock_planner" />

1. In **Design**, click **Planner: slot ...** to cycle between the planner items in your inventory. **No planner item** means you need to carry one first.
2. Press **Save** to copy the current dimensions, layout settings and footprint into the selected planner. Saving again replaces that planner's previous design; carry another planner to keep a separate design.
3. At another reactor controller, open **Build**, then **Design**, select the saved planner and press **Load**. The footprint and dimensions are copied into that controller. The planner keeps its saved copy.
4. Use **Check**, then **Materials** and **Build** to construct the loaded design. Layouts exceeding the server's size limits are refused.

Save and Load copy only layout settings and the core design. Supplied materials, ME configuration and the controller's position stay with their existing controller. An incomplete draft can be carried and edited later, but it cannot start construction until it contains at least one fuel column.

Arrays offer dimensions, full or starter racks, windows and additional pattern hatches. Machine items and upgrades are installed separately. Materials saved in the Array's previous builder are retained and transferred into the shared supplies.

Enrichers, reprocessors and fabricators use their fixed process layouts. Turbines use this same screen. Select horizontal or vertical orientation with a 5- or 7-block diameter, 1 to 8 blade stages, 1 to 4 coil rings, and the coil material. Length follows the rotor configuration automatically; vertical turbines grow upward instead of backward.

## Manual controller placement

Use **Verify** to apply the selected dimensions and check a manual build without placing blocks or requiring its interior to match an auto-build pattern. **Project** toggles the Array's in-world guide.

The illustrated controller position is a building default. For manual construction, an Array controller can occupy its outward-facing wall; a reactor controller can occupy any horizontal shell face away from edges. Place the controller facing out, complete the wall, and set the matching dimensions. Width is measured across that face. Interior rack and column rules still apply.

Fuel-processing plants accept their controller in place of a casing on a vertical wall, away from edges. Fill the original controller position with a casing. Keep all process units and required ports in their shown positions.

## Preserving supplies

Settings and supplies belong to each controller and survive saving. Only one player can edit or supply a controller at a time; closing its construction screen releases it for other players. Repair respects block-breaking permissions and never removes the controller, unbreakable blocks, liquids or arbitrary block entities such as chests and machines. Blocks without recoverable item drops require manual removal. Our stateless nuclear ports can be replaced or reoriented. Old standalone planners can still return their saved materials, but new construction uses the controller.


## Configurable ports

Craft Item Ports for both incoming materials and outgoing products, and Fluid Ports for coolant input and fluid output. Right-click a port to open its settings, then choose Input or Output. Its face changes with the mode. The builder uses the same port item for both directions and sets the planned mode during placement.

Energy Ports remain separate. Their direction follows the machine: reactor and turbine energy leaves through them; fuel-processing plants receive energy through them. Existing separate output blocks remain compatible.

## Older core designs

Saved separate-control cells become empty cells; existing fuel, moderator and reflector cells keep their positions. An actuator is generated above each retained fuel column. This does not add fuel or turn old controls into extra generating capacity. Existing building supplies stay stored, and the worker rescans old construction checkpoints before resuming.

The **Windows** dropdown offers solid, strip, full, panoramic, centered, diamond, vertical-bay and horizontal-band layouts for reactors, arrays and turbines. Preview and material counts use the selected layout. Old saved strip/full designs keep their original shape. Use the same Reactor Casing for nuclear shells and fuel plants, and the same Observation Glass for walls and roofs.
