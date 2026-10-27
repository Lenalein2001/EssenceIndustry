---
navigation:
  title: Configure quantum tools
  position: 66
item_ids:
- essenceindustry:quantum_sword
- essenceindustry:quantum_staff
---

# Configure quantum tools

Sneak and use a Quantum Edge Sword or Quantum Staff to open its 48 module sockets. Modules can be removed and reused. Each module has one effect, not multiple tiers; installing more copies adds that effect up to the limit in its tooltip.

## Reach and mining

**Reach** works on both tools. Each copy adds one block to both block interaction and entity attack range, up to eight extra blocks. Hold the tool in your main hand.

The Staff's **Area Mining** module increases the mining plane from 1×1 to 3×3, 5×5 and 7×7. **Vein Mining** instead follows face-connected blocks of the same block type, breaking up to 16 additional blocks per module, up to 64. If both are installed, vein mining takes precedence. Hold Shift for a single-block precision break.

Automatic mining respects protection checks, skips block inventories, and only works in loaded chunks within reach. It processes a bounded number of blocks per tick. Moving away, switching tools or holding Shift stops the job.

**Underwater Mining** adds 20 percentage points of underwater mining efficiency per copy. Four copies remove the normal underwater penalty. It does not supply breathing or remove other mining penalties.

<Recipe id="essenceindustry:quantum_vein_mining_module" />

<Recipe id="essenceindustry:quantum_submerged_mining_module" />

## Area damage

The **Area Damage** module replaces the former Sweeping module. Existing installed modules keep working under the new behavior. Successful melee hits damage nearby eligible targets without requiring a sweep, jumping critical or fully charged attack. Ordinary attack cooldown still affects the original strike.

One module provides a 1.5-block radius around the struck target. Each additional copy adds 0.5 blocks, up to 5 blocks with eight copies. Walls, allies, owned pets and disabled player-versus-player combat prevent collateral hits. Every eligible target receives a full native sword attack, including applicable enchantments and combat effects. Damage is not divided between targets. The same swing charge is used for every target, and area hits cannot recursively trigger another area attack.

Left-click a block with an Area Damage module installed to trigger a critical area strike around that block. The block is not broken. This uses the sword's attack cooldown; wait for the attack to recharge before another ground strike. Allies, pets, walls and protection rules still apply.

## Critical hits

The tool screen shows the bonuses supplied by its modules. Each Crit Chance module adds 10 percentage points; each Crit Damage module adds 0.25 to the critical multiplier.

With Apothic Attributes installed, these bonuses contribute directly to its attributes and appear in its attribute display while the sword is held. Apothic handles the critical roll, avoiding two independent module rolls. Without it, the tool applies its own chance to charged attacks and its damage bonus to critical hits.

Other equipment, effects and server configuration can change your final critical values. Module bonuses alone are not the player's total attributes.
