---
navigation:
  title: Accelerate a machine
  position: 65
---

# Accelerate a machine

The **ME Block Accelerator** mounts on an AE cable. Its outward plate targets the machine directly beside that cable face, leaving the cable space available for other AE parts.

1. Place an ME cable beside the machine you want to accelerate.
2. Attach the accelerator to the cable face pointing at that machine.
3. Connect the cable to a powered ME network.
4. Open the accelerator to inspect its target and set the speed.

The accelerator uses network power. A filled internal energy buffer is not required. Raising the speed increases power consumption; an unpowered network cannot accelerate the target.

## What can be accelerated?

The target must support extra server block-entity ticks. Not every machine does: a machine with its own timing system may ignore extra ticks. Resource generators continue operating normally and do not become on-demand crafting machines.

The existing full-block AE Block Accelerator remains available. Use the cable part when you want the same job in a smaller connection space.

## Power cables

The full-block accelerator and tiered Growth Accelerators also accept FE from power cables such as Powah. Connect to an existing power/ME connector face: the Growth Accelerators use their two end faces, while the full-block accelerator accepts power on every face except its machine-facing target side. No Energy Acceptor is needed for direct FE input.

A full-block accelerator prefers a sufficiently powered ME network. Otherwise it uses its own FE buffer, including when connected to an unpowered ME network. Each operation is paid by one source; it does not charge both. The cable-mounted ME Block Accelerator continues to use ME power through its host cable.

<Recipe id="essenceindustry:block_accelerator_part" />
