---
navigation:
  title: Troubleshooting
  position: 60
---

# Find a stalled job

## Pattern will not go in

Check that a Pattern Hatch exists, that there is an enabled free pattern slot, and that the ingredients and results match a native recipe for the installed machine. Check the machine type first: a pattern that *crafts* a Circuit Slicer belongs in a Crystal Assembler Array, not inside a Circuit Slicer Array.

## ME says Crafting, but nothing arrives

Check both item and fluid storage space. Completed results remain buffered when the network cannot accept them. The Array retries delivery when it can; cancelling the ME request does not delete already accepted work.

## Work pauses

Check formation, power, redstone control and loaded chunks. No offline progress is invented when chunks unload.

## Move or rebuild

Use **Return inputs** to refund unfinished operations. Finished results stay in the output buffer. Remove stored contents before moving the controller. Removing a Pattern Hatch preserves its extra patterns for retrieval, but disables those pages for new recipes.

## Keep it efficient

The controller performs the crafting; racks and casings do not each simulate another machine. Use counted outputs and direct ME delivery. Many connected pipes, ME networks, open menus and world rendering can still add their own cost.
