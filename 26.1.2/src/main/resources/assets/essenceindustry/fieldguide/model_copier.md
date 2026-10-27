---
navigation:
  title: Copy trained models
  position: 85
item_ids:
- essenceindustry:model_copier
---

# Copy trained HNN models

The **Model Copier** duplicates a Hostile Neural Networks Data Model, including its complete training, simulation count and custom data. The original model stays in the input inventory and can be used again.

## One copy

Place these items in any of the input slots:

| Item | What happens |
| --- | --- |
| 1 valid Data Model | Retained unchanged as the source. |
| 1 Blank Data Model | Consumed to create the new model. |
| 1 Quantum Circuit | Consumed for each copy. |

The output is **one exact copy** of the source. A diamond is not a copying ingredient. A full output inventory pauses copying without consuming a framework or circuit.

Find the Quantum Circuit's crafting recipe in JEI. Its materials depend on which AE2 addons are installed; the copier always consumes one circuit per completed copy.

## Automate copies

Keep one source model in an input slot, then supply Blank Data Models and Quantum Circuits through item pipes or AE2. Extract finished copies from the output slots. Use only one source model at a time, and do not split one ingredient across several input slots.

The source can be untrained or already Self Aware. Copies inherit its current training. JEI shows the exact training of a model you look up and labels the original as retained.

If an older machine still contains diamonds, remove them manually before inserting circuits. Existing stored items are preserved when the mod updates.

This machine and its recipes require Hostile Neural Networks.

## Build the copier

The machine uses four Iron Blocks, two Diamond Blocks, an Industrial Control Circuit, one Blank Data Model and a Comparator. The Quantum Circuit is paid per copy, separately from building the machine.

<Recipe id="essenceindustry:model_copier" />
