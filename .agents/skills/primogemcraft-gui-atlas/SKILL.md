---
name: primogemcraft-gui-atlas
description: Use when building, changing, reviewing or debugging a container-style GUI in PrimogemCraftNeo — a storage window, a bag or pouch screen, a trash can screen, a slot grid, a scrollbar/slider, a panel, or any screen that needs its own textures. Covers the shared sprite sheet textures/gui/container_window.png, its cell grid and element table, the pixel alignment contract between slot sprites and slot coordinates, drawing through GuiAtlas, regenerating the placeholder art, and the mistakes that produced off-by-one, unaligned GUIs before.
---

# The shared container GUI sprite sheet

One sheet, one look, one place to fix. Every container-style screen in this mod draws from a single sprite sheet; no screen draws its own skin, and no two screens invent their own slot or scrollbar art.

| Fact | Value |
|---|---|
| Sheet | `src/main/resources/assets/primogemcraft/textures/gui/container_window.png` |
| Size | 64 × 64 (a 2 × 2 grid of 32 px cells, all four claimed) |
| Reader | `src/main/java/net/per/primogemcraft/client/gui/GuiAtlas.java` |
| Generator | `.agents/skills/primogemcraft-gui-atlas/scripts/GenerateGuiAtlas.java` |
| Current user | `ContainerWindowScreen` (pouches 18/27/66/143, trash cans 27/45/54) |
| Geometry owner | `system/menu/ContainerWindowLayout` |

## Hard rules

1. **Container screens take every pixel from this sheet.** Do not paint a slot, a panel or a scrollbar with `graphics.fill(...)`, and do not add a per-screen background PNG. A GUI that needs a new element adds a cell to this sheet. The one overlay that is not a sprite is the hover highlight: it is the vanilla translucent white square drawn over the hovered cell, exactly as `AbstractContainerScreen` draws it, so the sheet carries no highlight art.
2. **Sprites are drawn 1:1 or stretched, never scaled down, never rotated, never drawn at non-integer coordinates.** Blit positions are ints.
3. **Every sprite lives at its cell's top-left corner** (a 16 × 16 sprite occupies `(u, v)` … `(u + 15, v + 15)`), so `GuiAtlas` constants are readable straight off the PNG.
4. **The sheet is placeholder art.** When real art lands, keep the size, the cell origins and the opaque/transparent extent of each sprite; if any of those change, the `GuiAtlas` constants and the table below change *in the same edit*.
5. **Pixel art only**: no anti-aliasing, no alpha gradients inside a sprite, and no vertical detail inside a sprite that is stretched along that axis (tracks, thumbs, panel edges).
6. **Slots and scrollbars are bevelled the way vanilla bevels them.** A slot is dark `#373737` on its top and left edges and white on its bottom and right edges; a scrollbar thumb is a raised vanilla button (`#000000` outline, white on the light side, `#555555` on the shadow side). Art that is flat on all four edges reads as a spreadsheet, not as a Minecraft GUI.

## Element table

`u, v` are pixels in the sheet; `w, h` are the sprite's own size. Cells are 32 px, so a cell starts at `(32 × column, 32 × row)`.

| Constant | u, v | w × h | Cell | Use |
|---|---|---|---|---|
| `PANEL` | 0, 0 | 16 × 16 | (0, 0) | window background, nine-slice, inset `PANEL_INSET = 2` |
| `SLOT` | 32, 0 | 18 × 18 | (1, 0) | one container or inventory slot |
| `SCROLL_TRACK` | 0, 32 | 6 × 20 | (0, 1) | scrollbar groove, stretched vertically |
| `SCROLL_THUMB` | 32, 32 | 6 × 20 | (1, 1) | scrollbar thumb, stretched vertically |

Palette (keep it when extending): panel body `#C6C6C6`, panel border `#000000`, bevel light `#FFFFFF`, bevel shadow `#555555`, slot border `#373737`, slot body `#8B8B8B`, track border `#373737`, track body `#2E2E2E`, thumb border `#000000`, thumb body `#C6C6C6`. There is no hover colour: the hover highlight is the vanilla `0x80FFFFFF` overlay.

## Alignment contract

This is the part that was wrong before, so it is written out in full.

- **Slot coordinates are item coordinates.** `new Slot(container, index, x, y)` puts the 16 × 16 item at `(x, y)`, exactly like vanilla. A slot sprite is therefore 18 × 18 (1 px border + 16 px interior) and is drawn at **`(x - 1, y - 1)`**. Drawing it at `(x, y)` shifts every slot one pixel down-right; drawing a 16 × 16 sprite at `(x - 1, y - 1)` eats the border.
- **Slot pitch equals the sprite size.** `SLOT = 18` in `ContainerWindowLayout`; neighbouring cells tile with no gap and no overlap on any side.
- **The grid's frame rectangle is `[GRID_X - 1, GRID_X - 1 + COLUMNS × SLOT)`** horizontally and `[GRID_Y - 1, GRID_Y - 1 + visibleRows × SLOT)` vertically. Scissors, the scrollbar and hit tests all use that same rectangle — never `rows × SLOT + 2` and never a rounded value.
- **Panel padding is measured from the frame edge, not the slot edge.** `PANEL_PADDING = 7` around the content, so a window without a scrollbar is 176 px wide and one with a scrollbar is 188 px. A window that never scrolls must not reserve the scrollbar strip.
- **The scrollbar is exactly as tall as the grid** (`height = visibleRows × SLOT`), starts at the grid's top edge (`GRID_Y - 1`), and is `SCROLLBAR_WIDTH = 6` wide, `SCROLLBAR_GAP = 6` to the right of the grid. The thumb is at least one sprite tall (`GuiAtlas.SCROLL_THUMB.height()`), so the thumb never squashes into a sliver.
- **Text has reserved room.** The title sits at `y = 6`, above the grid; the vanilla inventory label sits 11 px above the inventory slots (`inventoryLabelY = inventoryY - 11`). `INVENTORY_GAP = 14` keeps the grid's bottom frame edge clear of that label — with a smaller gap the label overlaps the last container row.
- **Coordinates inside `renderBg`, `renderSlot` and `renderLabels` are panel-relative**, while `enableScissor` takes window coordinates and must be given `leftPos + …`, `topPos + …`. The framework calls `renderBg` as the background layer, before `AbstractContainerScreen.render` translates the pose and before any slot is drawn, so `renderBg` pushes and translates the pose for itself; drop that translation and the whole panel, its slots and their frames land in the screen's top-left corner while the real slots stay centred. `renderSlot` and `renderLabels` already run inside the framework's translation and must not translate again.
- **A scrolling grid keeps its slots off screen** (`HIDDEN_X/HIDDEN_Y`), draws only the visible rows inside a scissor, and routes clicks back through `slotClicked(menu.getSlot(index), index, …)`. The hit test uses the same fractional scroll offset the renderer used in that frame, so hovering, tooltips and clicks cannot disagree with what is on screen.

## Drawing

```java
GuiAtlas.panel(graphics, 0, 0, imageWidth, imageHeight);              // nine-slice background
GuiAtlas.sprite(graphics, GuiAtlas.SLOT, slot.x - 1, slot.y - 1);     // one slot
GuiAtlas.stretched(graphics, GuiAtlas.SCROLL_TRACK, x, top, ContainerWindowLayout.SCROLLBAR_WIDTH, height);
```

- `panel` takes a nine-slice inset from `GuiAtlas.PANEL_INSET`; the centre is stretched, never tiled, which is safe because the centre of a placeholder sprite is a flat colour.
- `sprite` blits a sprite at its own size. `stretched` blits it into an arbitrary rectangle — only for sprites that are uniform along the stretched axis.
- `GuiAtlas` enables blending before every blit, so alpha tinting (`graphics.setColor(1, 1, 1, alpha)` … `graphics.setColor(1, 1, 1, 1)`) remains available for fading one sprite into another. The hover highlight does not use it — it is a `fillGradient` over the 16 × 16 item area:

```java
var alpha = Mth.clamp((int) (HIGHLIGHT_ALPHA * hover), 0, HIGHLIGHT_ALPHA);
graphics.fillGradient(x, y, x + SLOT_ITEM, y + SLOT_ITEM, alpha << 24 | HIGHLIGHT_RGB, alpha << 24 | HIGHLIGHT_RGB);
```

## Adding an element

1. Claim a cell — the 2 × 2 sheet is full, so grow `COLUMNS`/`ROWS` in `GenerateGuiAtlas.java`, update `GuiAtlas.WIDTH`/`HEIGHT` and this table, and pick a sprite size that fits inside 32 × 32.
2. Add the drawing to `GenerateGuiAtlas.java` at that cell's origin and re-run it (below).
3. Add the constant to `GuiAtlas` and, if the element is part of the window geometry, the numbers to `ContainerWindowLayout`.
4. Add the row to the element table in this skill.
5. `compileJava`, then look at the GUI in game at GUI scale 1, 2 and 3 — alignment bugs of one pixel are invisible at a glance in code and obvious on screen.

## Regenerating the placeholder sheet

```powershell
java .agents/skills/primogemcraft-gui-atlas/scripts/GenerateGuiAtlas.java src/main/resources/assets/primogemcraft/textures/gui/container_window.png
```

The script is a single-file Java program (JDK 21, no build, no dependencies) that writes a 64 × 64 ARGB PNG from the same palette table. It overwrites the file, so keep the generator and the sheet in sync; never hand-edit the PNG without editing the generator.

## Checking alignment without launching the game

```powershell
java .agents/skills/primogemcraft-gui-atlas/scripts/MockContainerWindow.java src/main/resources/assets/primogemcraft/textures/gui/container_window.png mock.png
```

This draws a full container window offline from the real PNG: panel, a 6 × 9 grid, the scrollbar, the inventory block and the label strips. Look at the result before comparing anything in code — a slot that is one pixel off, a scrollbar that is taller than the grid, or a label that touches the grid is obvious in the image and invisible in a diff. It prints the panel size, the grid frame range, the scrollbar column, the inventory row and the hotbar row, which must match `ContainerWindowLayout`.

The mock mirrors the layout constants and the sprite table on purpose, so it runs without Minecraft. When `ContainerWindowLayout` or `GuiAtlas` changes, update the mock in the same edit; a mock with stale constants verifies the wrong geometry.

## Checklist before calling GUI work done

- The PNG's width and height are multiples of 32 and every sprite starts at a cell origin.
- `SLOT` is 18 × 18; any sprite that represents a slot cell is 18 × 18.
- Slots look sunken and the scrollbar thumb looks raised, not flat: dark top/left, white bottom/right.
- The hover highlight is the vanilla `0x80FFFFFF` overlay on the 16 × 16 item area, and the sheet has no highlight sprite.
- Every slot sprite is blitted at `slot.x - 1, slot.y - 1`, and every slot coordinate came from `ContainerWindowLayout`.
- The panel width grew or shrank with the scrollbar, and no empty strip is left behind.
- The scrollbar height, the scissor rectangle and the grid hit test all use the same frame rectangle.
- `MockContainerWindow` renders a window whose printed frame range and row positions match `ContainerWindowLayout`.
- `compileJava` passes.
- Legacy screens with their own background textures (`gorgeous_smithing_table.png`, `stellar_converter.png`, `choice_card*.png`) are out of scope until they are touched; when they are, move their reusable parts into this sheet rather than adding another background.

## Mistakes this sheet exists to prevent

- Slots painted with `graphics.fill`, so the frame and the 16 px item inside it drift apart by a pixel and the grid stops lining up with the panel and the scrollbar.
- A second sprite that only exists to tint the hovered slot, when vanilla's own translucent white highlight already does the job and keeps the cell reading as a slot.
- A slot or scrollbar that is flat on all four edges, so the window reads as a spreadsheet instead of a Minecraft GUI.
- A scrollbar that is two pixels taller than the grid because its height was `rows × SLOT + 2`.
- A window that always reserves the scrollbar column, leaving a dead strip in every non-scrolling container.
- The inventory label sitting on top of the container grid because the grid-to-inventory gap was smaller than the label's height.
- One background texture per screen, so three screens end up with three different borders, three slot colours and three scrollbar widths.
