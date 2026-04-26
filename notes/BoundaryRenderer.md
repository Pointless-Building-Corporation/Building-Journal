# Open BoundaryRenderer.java

### `getDirection()`
Given two points, determines the direction the line between them is moving across.

### `renderThickLine()`
The idea behind this is that by rendering a cuboid instead of a line the boundary looks more full.

### `renderCuboid()`
This function calls multiple renderThickLines. The origin/destination is offset based on their relative positions,since we need a box even if the origin and destination are the same place.