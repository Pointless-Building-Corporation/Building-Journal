# Open Button.java

PageButton extends Button.

### On line 22
Button can use a Builder to initialize itself.

### Line 26, 32
Constructors. The first one is general, the second uses the Builder

### Line 52
Use this to build a button. Call the constructor first, then bounds preferably, and finally build().

### RENDERING NOTE
To override the way buttons are rendered, override renderWidget(). Inside, account for `this.isHoveredOrFocused()`. Check PageButton implementation.