# Open LecternBlock.java

### At lines 43-51
These define in box() calls, the actual collision and render shape of the lectern.

### At line 67
The Occlusion shape has no top surface, so shadows pass through.

### At line 90
The collision shape does have the top surface lowered down, but is not the same as the actual shape of the block.

### At line 94
This is the real shape of the block - defined via directions. This is probably only for interaction purposes.
