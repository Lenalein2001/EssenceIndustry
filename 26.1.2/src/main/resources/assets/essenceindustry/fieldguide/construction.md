---
navigation:
  title: Quantum Builder
  position: 70
item_ids:
- essenceindustry:quantum_builder
- essenceindustry:quantum_wireless_booster
---

# Quantum Builder

Sneak-click a building block to select its material and orientation. Right-click to build, or right-click air to open settings when unanchored. A remappable **Quantum Builder settings** key is available in Controls; it starts unbound to avoid modpack conflicts.

Choose Surface, Line, Column, Floor, Wall, Stairs, Cuboid, Hollow cuboid or Frame. The sampled block orientation is preserved wherever you place it. Rotation changes only the construction shape. Adjust dimensions, rotation, alignment and the offset from the clicked face. The cyan world preview marks empty positions; red positions are occupied. Server protection rules are checked when placing.

Blocks appear directly. Large operations run in bounded batches, with no construction animation. Maximum dimensions are 32 blocks, with at most 4,096 planned positions per click. Shapes exceeding this position limit are refused; reduce the dimensions.

<Recipe id="essenceindustry:quantum_builder" />

## Range and anchor

The **All axes** slider sets width, height and depth together, from 1 to 32. The individual axis controls remain independent. Increase/decrease-all hotkeys adjust each current dimension by one, retaining differences until a dimension reaches its limit.

Assign **Toggle construction anchor** in Controls, then press it while looking at a block. The server locks that block position, clicked face and layout heading. Looking elsewhere or turning around leaves the preview in place. You can adjust dimensions, offset and rotation around the same anchor. Right-click, including into air, builds the anchored preview.

Press the Anchor key again, or **Unanchor** in settings, to clear it. While anchored, sneak-right-click air or use the Settings key to open settings. Anchors persist with the tool but cannot build across dimensions or beyond 64 blocks; they never load chunks.

All Builder hotkeys start unbound to avoid modpack conflicts: settings, increase/decrease all dimensions, individual axes, anchor, rotate, next mode and Undo. They operate while holding the tool and do not run through an open GUI.

## Undo and materials

**Undo** reverses one build at a time, newest first, in the same dimension. Click it again to undo the previous build. The screen shows how many builds remain. It skips blocks whose state has changed and retains denied or unloaded positions for a later attempt. It follows break-protection events. Fresh, unchanged containers and machines can be undone; blocks whose saved contents or configuration changed stay in place and remain retryable. Emptying a container back to its initial state allows Undo again. Machines with changing saved runtime data may also be retained.

ME-sourced blocks go back into their original ME network automatically when undone. A full or unavailable network leaves those refunds in the tool, tagged with their original network. **Return materials** retries those ME refunds; it moves inventory-sourced materials into available player inventory space. Neither full storage nor a full inventory discards the remainder. Creative construction does not generate material refunds.

**Cancel** stops remaining work; completed placements stay in Undo history. History and buffered materials survive a normal save/reload. New builds keep earlier operations. Up to **32 builds or 8,192 placed-block records** are retained; the oldest Undo records expire when either limit is reached. Buffered materials never expire with history. A partially blocked Undo stays on top until its remaining positions are resolved, so it cannot silently skip into an older build.

The tool also selects containers and modded machines. Like the Building Gadget, it copies the block type and orientation, resets transient properties such as lit/open/waterlogged/crop age, and places a fresh block using plain matching items. Source contents, energy, machine settings and seed genetics are never cloned. Neighbor connections are recalculated at the destination. Doors/beds and other multi-part blocks, combined stacks such as double slabs, and blocks without a matching placeable item still need manual placement. Install the same version on client and server.

## Optional ME sourcing

Sneak-click a Wireless Access Point to link, or bind the tool through the access point's normal item slots. Select **Inventory**, **ME only**, or **Inventory then ME** in settings. Existing buffered materials already belong to the tool.

The **Quantum Wireless Booster Card** goes in the access point's ordinary booster slot. It gives that access point unlimited range and cross-dimension access, including native wireless terminal hosts. It uses **128 AE/t**, still requires a channel and powered network, and never force-loads chunks. Other access points keep their normal range.

<Recipe id="essenceindustry:quantum_wireless_booster" />

An ME extraction with an unknown result locks the tool and retains the exact request for reconciliation; it is never automatically repeated. Known buffered items and Undo history remain saved.
