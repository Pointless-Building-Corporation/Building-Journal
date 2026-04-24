# Open gui/JournalUI.md

### `List<ResourceLocation> JOURNAL_PAGES`
Locations for Journal GUI backgrounds.
TODO: This definiton breaks down a bit when multiple languages need to be supported.

### `ui_width and ui_height`
Size of the texture in the window.

### `public JournalUI()`
Constructor. This just needs to set a title if required. Right now just sets it to no title, as in BookViewScreen.

### `public void render()`
Has to be overridden. This renders all the necessary elements of the screen.

### `protected void flex()`
Should adjust ui height and width to match current screen height and width, based on if the minecraft window is resized or not. It's called both initially and in repositionElements.

### `protected void renderMenuButtons()`
Renders the menu buttons in the first Journal screen. Currently uses PageButtons.