---
navigation:
  title: Steam Turbine
  position: 74
item_ids:
- essenceindustry:turbine_controller
- essenceindustry:turbine_blade
- essenceindustry:turbine_coil_copper
---
# Steam Turbine

A turbine converts reactor steam into rotor motion, then electricity. Its blades visibly speed up as rotor RPM increases. Small tangential nozzles show steam entering and pushing the blades while steam is consumed. The effects stop when flow stops and respect Minecraft particle settings. Closing the steam valve lets the rotor coast down; engaged coils keep generating until its stored mechanical energy runs out.

## Build and connect

Craft a controller, open **Construction**, and choose horizontal or vertical orientation and diameter, blade stages, coil rings and coil material. The shared planner calculates the exact parts, accepts inventory supplies or ME supply, and builds the selected layout. Start with the 5 x 5 x 9 layout, two blade stages and two copper coil rings.

For a compact turbine bank, choose **Vertical 7 x 7**, **8 blade stages** and **4 coil rings**. This makes a **7 x 7 footprint, 16 blocks tall**. Four turbines fit inside one chunk: start their footprints at local X/Z coordinates (0, 0), (8, 0), (0, 8) and (8, 8). This leaves one-block service gaps; cables can run above or below the bank. Each turbine remains independent and needs its own ports and controller. Shorter vertical layouts use the same cross-section with fewer stages or rings.

Use reactor casing and observation glass for the shell. The shaft, bearings, blade stages and coils occupy the displayed interior positions. Complete each coil ring with eight coil blocks around the shaft. Mixed coil materials are allowed: each contributes its own rating. More blade stages increase the maximum steam throughput; more or stronger coils increase electrical loading.

Place at least one Fluid Port set to **Input** and an Energy Port on the shell. Add an optional Fluid Port set to **Output** to recover condensate. Ports may be moved around the shell. Face the controller outward; manual placement on its front wall is supported after setting the matching dimensions. The controller position shown by Construction is the default, not a required coordinate.

- **Fluid Input:** accepts Essence & Industry steam from an actively cooled reactor.
- **Fluid Output (optional):** returns water. One mB of steam becomes one mB of water; excess water is voided when storage fills.
- **Energy Port:** supplies FE to adjacent machinery or cables.

## Run the turbine

Open the steam valve. The **target RPM** governor limits steam intake as the rotor approaches its target. The flow limit caps how much steam can be admitted. The normal design speed is **1,800 RPM**; the target is adjustable from 300 to 2,700 RPM.

Engaged coils draw energy from the rotor. Disengage them to build speed without electrical generation. Re-engaging them slows the rotor unless the steam supply can maintain the load. A redstone signal at the controller closes steam intake while allowing the rotor to coast.

The output rating is measured at 1,800 RPM. At 900 RPM, a coil offers approximately half that power. Actual output can be lower when steam, stored rotor energy or electrical storage space is insufficient. For readable, stable motion, the rotor animation uses one-twentieth of the physical RPM. Like Extreme Reactors, it keeps a continuous client-local angle; our slower presentation keeps the four-paddle rotor readable down to 20 FPS. The client integrates motion every frame and smoothly follows speed updates without snapping its angle. This affects appearance only; power uses the real RPM shown here.

| Coil | FE/t per block at 1,800 RPM | Four rings, 32 blocks |
| --- | ---: | ---: |
| Copper | 1,000 | 32,000 |
| Uranium | 4,000 | 128,000 |
| Enriched uranium | 16,000 | 512,000 |
| MOX | 64,000 | 2,048,000 |
| Plutonium | 256,000 | 8,192,000 |
| Advanced fuel | 2,000,000 | 64,000,000 |

Steam carries 100 FE of mechanical work per mB, with a small rotational loss. A 64M FE/t coil set therefore needs **more than 640,000 mB/t** to sustain its rating. Five large blade stages cannot quite sustain this after losses; use additional stages and a reactor that can supply the required steam. A low-output reactor does not become stronger by connecting higher-tier coils.

## Stalled output

If condensate storage is full, drain the water. If the energy buffer is full, extract energy; the governor reduces steam intake as the rotor reaches its target. A broken shell stops steam consumption and electricity generation. Fluid and energy transfers use the shared configurable ports.

The server saves steam, water, FE, controls and rotor energy. Unloaded turbines do not run or accumulate offline power. Controller drops retain the turbine ledger; construction supplies are returned separately.

<Recipe id="essenceindustry:turbine_controller" />

[Reactor steam mode](reactor.md) | [Construction and ME supplies](multiblock_planner.md)

The steam inlet and energy port are required. A condensate outlet is optional: when the water tank fills, excess water is voided without stopping the rotor. The rotor now gains speed gradually; its stored mechanical energy is preserved while coasting.
