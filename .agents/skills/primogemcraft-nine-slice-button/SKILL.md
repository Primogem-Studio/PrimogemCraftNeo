---
name: primogemcraft-nine-slice-button
description: Draw or restyle PrimogemCraftNeo GUI buttons using the shared nine-slice renderer. Use when adding buttons, changing button textures or label colors, or fixing button stretching, alignment, hover, selected, pressed, or disabled states.
---

# Nine-Slice GUI Button

Apply [the project standard](../primogemcraft-standard/SKILL.md) for Ponytail compatibility, verification, and concise replies. Reusing this renderer is the minimal implementation; preserve the state, input, focus, and narration contracts when changing its appearance.

Follow the Elixir nine-slice geometry and state contract, using the textures and label colors already used by GorgeousSmithingTableScreen in this project.

## Spec

Textures live in `src/main/resources/assets/primogemcraft/textures/gui/widget/`, 20x25 pixels each:

| File | State |
| --- | --- |
| `u_button.png` | Normal |
| `u_button_press.png` | Hover, selected, pressed |
| `u_button_no.png` | Disabled |

Insets in source pixels (left/right/top/bottom):

| Skin | Left | Right | Top | Bottom |
| --- | --- | --- | --- | --- |
| `u_button` | 2 | 2 | 2 | 6 |
| `u_button_press` | 2 | 2 | 2 | 4 |
| `u_button_no` | 2 | 2 | 2 | 6 |

- Normal and disabled label color: `0xFFE5C99C`.
- Hover, selected, and pressed label color: `0xFF2D313F`.
- Draw labels without a text shadow, matching the Gorgeous Smithing Table. Do not copy Elixir's green label color.
- Center the label inside the stretched face, then move it down 1 pixel. While an enabled, hovered button is held, sink the label 1 additional pixel.
- Prefer a minimum height of `top + bottom + font.lineHeight`: 17 pixels with the standard font and normal skin. For shorter buttons, trim the bottom band from its outer edge until the label fits, preserving its inner highlight line.

## Constraints

- Never scale the complete button texture. Stretch the center in both axes and edge strips only along their length; preserve corner pixels and border thickness.
- Never trim the top or side borders. Only the bottom band may be trimmed for a short button.
- Reuse the existing textures and renderer instead of vanilla button rendering or a separately painted skin. Button skins are the exception to the container atlas rule; panels, slots, and scrollbars still use `GuiAtlas`.
- Keep labels localized and use the existing font. Java comments are limited to required public API documentation.
- Selected is distinct from disabled. A toggle that disables a machine channel remains an enabled, clickable button; render its active toggle state as selected so it can be clicked again.
- Rendering and hit testing must use the same rectangle. Preserve widget input, focus, and narration when overriding a vanilla widget's appearance.

## API

`net.per.primogemcraft.client.gui.NineSliceButton` is the shared button renderer:

```java
NineSliceButton.draw(graphics, font, x, y, width, height, text, enabled, hover);
NineSliceButton.draw(graphics, font, x, y, width, height, text, enabled, hover, selected);
```

The first overload uses `selected = false`. Disabled overrides hover and selected. Selected uses the highlight skin without simulating a held mouse button.

Existing callers: `GorgeousSmithingTableScreen.renderButtons` and the channel widgets in `StellarConverterScreen.init`.
