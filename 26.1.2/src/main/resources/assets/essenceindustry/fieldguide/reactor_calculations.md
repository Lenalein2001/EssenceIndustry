---
navigation:
  title: Reactor calculations
  position: 111
---

# Plan your reactor output

Start with [building a reactor](reactor_building.md). This page explains the current gameplay rules, using default balance. Server settings can change the fuel rates.

## What interacts with what?

Each Fuel Assembly is one **fuel segment** with its own integrated control absorber. Stack segments into a full-height column and put one actuator directly above it in the roof. All columns use the controller's same insertion percentage.

Neighbouring fuel columns do not multiply each other's output. There is no simulated neutron exchange, distance falloff, or independent rod setting. Adding another complete column adds its segments to the total.

For each fuel segment, check the four horizontal neighbours at the **same height**:

- At least one Moderator: that segment counts as moderated.
- At least one Reflector: that segment counts as reflected.
- Both types present: it counts toward both bonuses.
- Diagonal neighbours and blocks above or below do not count.
- Two or more neighbours of the same type do not increase that segment's bonus.

A single moderator or reflector can benefit several fuel segments around it. Ordinary casing and glass do not provide either bonus. A fuel column touching the wall remains valid, but that wall does not act as a reflector.

## Count the useful blocks

Let **F** be the total number of fuel segments. For full-height columns:

`F = fuel columns x (outside height - 2)`

Let **M** be the number of segments with a moderator neighbour, and **R** the number with a reflector neighbour. Count covered fuel segments, not the number of moderator or reflector blocks. Both counts are between zero and F.

`Reflector multiplier = 1 + R / (4 x F)`

`Heat multiplier = 1 - M / (4 x F)`

Full reflector coverage gives 25% more output at the same fuel consumption. Full moderator coverage gives 25% less heat at the same output. Half coverage gives 12.5%. Coverage is averaged over the entire reactor.

## A compact layout with both bonuses

Repeat this interior layer for the full interior height. F = fuel, M = moderator, R = reflector, dot = air. The surrounding casing is omitted.

```
. R .
M F M
. R .
```

In a 5 x 5 x 5 shell this gives three fuel segments, three moderated segments and three reflected segments. One roof actuator belongs directly over F. The second moderator and reflector on each layer are optional: one of each already gives that segment both bonuses.

At 0% insertion, uranium gives about **62,500 FE/t**, or **625 mB/t steam**, with about **5 hours 20 minutes per ingot**. At 50% insertion these become about **31,250 FE/t**, **312.5 mB/t average steam**, and **10 hours 40 minutes per ingot**. Integer steam output can alternate between ticks.

## Calculate fuel and power

Let **I** be insertion from 0 to 100. Set `A = (100 - I) / 100`. At 100%, the burn stops; lifetime is not a useful division by zero.

Let **B** be the configured burn in mB per segment per tick, and **E** the configured FE per mB, after the world multipliers. Config burn values use fixed-point units: divide `burnUnitsPerSegmentTick` by 1,000,000 to get B.

`Fuel mB/t = B x F x A`

`Direct FE/t = fuel mB/t x E x reflector multiplier`

`Steam mB/t = direct FE/t / 100`

`Seconds per ingot = 1000 / (fuel mB/t x 20)`

A uranium block lasts nine times as long as an ingot. These times assume 20 server ticks per second. Low TPS makes the same number of ticks take longer in real time.

Default uranium uses B = **0.000868 mB per segment per tick**. For quick output estimates, each unreflected uranium segment produces about **16,667 FE/t** at 0% insertion. Use the [fuel comparison](nuclear_fuels.md) for other fuels.

Example: four columns in a seven-block-high shell give F = 20. At 40% insertion, A = 0.6. If half the segments touch reflectors, the multiplier is 1.125. Uranium gives about **225,000 FE/t**, **2,250 mB/t steam**, and consumes **0.010416 mB/t**. One ingot lasts about **80 minutes**.

## Check heat before starting

In Direct FE mode, let H be the fuel's configured heat per mB:

`Heat added per tick = fuel mB/t x H x heat multiplier`

`Passive heat removed per tick = F x passive cooling per segment`

Default passive cooling is **16.6668 displayed heat units per segment per tick**. Config stores that as 16,666,800 fixed-point units. If added heat exceeds removal, heat accumulates until the reactor trips. More moderators or higher insertion can reduce the excess. A cold reactor may still gain a small steady amount because cooling happens before the tick's burn.

In Steam mode, water carries away the generated heat instead; the moderator heat reduction does not increase steam output. Reflectors still improve steam yield. Provide one mB of water for every mB of steam produced. Excess steam is vented if storage fills, so venting wastes fuel's useful output.

## Limits of an estimate

The game rounds rates and preserves fractional work, so individual ticks can differ slightly from these averages. Fuel or water shortages, full waste storage, full FE storage, redstone pause and thermal trips can reduce actual production. Increasing fuel efficiency in the server config reduces burn while preserving output; increasing the energy multiplier raises FE and steam without increasing burn.

[Choose and obtain fuel](nuclear_fuels.md) - [Controller and troubleshooting](reactor.md)
