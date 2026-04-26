# Open ClientRenderEvents.java

### `renderCompassBoundaries(event)`

This renders the bounding box around the compass boundaries defined in the builder's compass tags. This is rendered via a MultiBufferSource.

The boundary is rendered by checking if the compass is being held, and fetching the second look position if the first tag is present. The rendering is handled at the AFTER_TRIPWIRE_BLOCKS stage, and handled by `BoundaryRenderer`.