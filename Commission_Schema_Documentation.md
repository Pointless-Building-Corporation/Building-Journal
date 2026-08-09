# Commission Schema

This Schema describes a commission in the Building Journal.

**Schema:** `commission.schema.json`  
**JSON Schema Draft:** 2020-12

Current latest schema version: `1`. \
All supported schema versions as of writing: `1`

### Where to place Custom Commissions

For _Singleplayer_ worlds: Add them to the commissions/ folder in your world's directory. You can find it by opening the Resource Pack folder from the game and going up one level.

For _Multiplayer_ servers: Add them to the server's commissions/ folder in that world's directory. These commissions are shared amongst every player in the server.

***NOTE***\
As of this version, note that blueprints used to evaluate commission conditions may not be perfect; Due to the nature of how identical seeds generate in Minecraft, identical chunks in worlds with identical seeds may still have minor changes between them. This will *very rarely* affect commission evaluation; but regardless ensure your conditions cannot be adversely affected by such anomalies, and test them as much as you can. 

---

## Top-Level Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `$schema` | string | No | This current schema |
| `schemaVersion` | enum | **Yes** | Schema version — must be one of the allowed schema versions. |
| `id` | string | No | Unique identifier of the Commission. Note that the 'daily_' prefix is reserved for daily commissions and cannot be used for regularly. |
| `title` | string | **Yes** | Title of the Commission |
| `thumbnailPath` | string | No | Path to the thumbnail of the Commission. For correct aspect ratio, attach images of aspect ratio equivalent to 122x75, max 500 kb in size.|
| `conditions` | array | **Yes** | Conditions required to satisfy the Commission |
| `unlocks` | array | No | What the Commission unlocks once completed |
| `prerequisites` | array of strings | No | Commission IDs that must be completed to unlock this Commission |

---

## Conditions

Each entry in `conditions` describes a requirement the player must meet. All conditions share these base fields:

| Field | Type | Required | Description |
|---|---|---|---|
| `condition` | enum | **Yes** | The condition type (see types below) |
| `title` | string | No | What the condition is called in-game. It must ideally describe the condition well, so players can understand it. |
| `failureDescription` | string | No | What to tell the player if the condition fails. You can use templating to reference the relevant in-game value the condition is testing using curly braces around `value`. For example: `{value} is too low! Must be 50 or higher.` |
---

### Condition Types

#### `BlockAdded`

Checks how many of certain blocks have been added. This includes fluids but not air.

| Field | Type | Required | Description |
|---|---|---|---|
| `blocks` | array of strings | No | Block IDs to check. If empty, checks for all added blocks in general. Can be searched by block name (`minecraft:red_wool`), block tag (`#minecraft:wool`) or regex (`regex:^.*woo.*`). |
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `BlockRemoved`

Checks how many of certain blocks have been removed. This includes fluids but not air.

| Field | Type | Required | Description |
|---|---|---|---|
| `blocks` | array of strings | No | Block IDs to check. If empty, checks for all removed blocks in general. Can be searched by block name (`minecraft:red_wool`), block tag (`#minecraft:wool`) or regex (`regex:^.*woo.*`). |
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `BlockModified`

Checks how many blocks have been modified (either added or removed). This does not count a block which has replaced another as two modifications.

| Field | Type | Required | Description |
|---|---|---|---|
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `Density`

Checks the density of the structure. 1 is a fully dense structure i.e every block inside the outermost changed blocks has also been changed.

| Field | Type | Required | Description |
|---|---|---|---|
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | number | **Yes** | Value to compare against |

#### `Dimension`

Checks that the structure is present at one of the given dimensions. `{value}` is the dimension the build is in.

| Field | Type | Required | Description |
|---|---|---|---|
| `dimensions` | array of strings | **Yes** | Dimensions allowed (at least one required) |

#### `Elevation`

Checks the elevation of the structure. This is maximal comparison i.e the entire structure must be lower or higher than the threshold.

| Field | Type | Required | Description |
|---|---|---|---|
| `operator` | `<` \| `>` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `Length`

Checks the length of the structure. This is a conservative check; for example if the length cannot exceed 50 it is enough if one of the two lengths (Either X or Z coordinate length) is less than 50. Similarly if it has to be minimum of 50 or equal to 50 just one of the two needs to satisfy it.

| Field | Type | Required | Description |
|---|---|---|---|
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `Tallness`

Checks the height of the structure.

| Field | Type | Required | Description |
|---|---|---|---|
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `TotalVolume`

Checks the total volume of the structure.

| Field | Type | Required | Description |
|---|---|---|---|
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `Whitelist`

Checks the list of blocks given against the build and verifies if they are present. If isBlacklist is true, instead verifies that the blocks are NOT present.
`{value}` for this condition is the first block requirement found that clashes with the build.

| Field | Type | Required | Description |
|---|---|---|---|
| `blocks` | array of strings | **Yes** | Block ids to check. Unlike the other conditions this cannot be empty. Can be searched by block name (`minecraft:red_wool`), block tag (`#minecraft:wool`) or regex (`regex:^.*woo.*`). |
| `isBlacklist` | integer | No | Is this a blacklist? By default this is false. |

#### `Biome`

Checks that the structure is within one of the given biomes. Part of the build can be inside the biome to satisfy this. `{value}` is not relevant here and will not work.

| Field | Type | Required | Description |
|---|---|---|---|
| `biomes` | array of strings | **Yes** | List of biomes that are valid. Can be searched by biome name (`minecraft:forest`), biome tag (`#minecraft:is_mountain`) or regex (`regex:^.*for.*`) |

---

## Unlocks

Each entry in `unlocks` describes a reward granted when the Commission is completed. All unlocks share these base fields:

| Field | Type | Required | Description |
|---|---|---|---|
| `unlock` | enum | **Yes** | The unlock type (see types below) |
| `title` | string | No | What the unlock is called in-game |

### Unlock Types

#### `BlockReward`

Rewards the player with a number of blocks.

| Field | Type | Required | Description |
|---|---|---|---|
| `block` | string | **Yes** | The block ID |
| `count` | integer | **Yes** | Number of blocks rewarded |

#### `ExpReward`

Rewards the player with experience.

| Field | Type | Required | Description |
|---|---|---|---|
| `expAmount` | integer | **Yes** | The amount of exp rewarded |
| `expType` | `Points` \| `Levels` | **Yes** | Whether the amount is in levels or points |

#### `CommissionReward`

Unlocks another Commission.

| Field | Type | Required | Description |
|---|---|---|---|
| `commission` | string | **Yes** | The commission ID to unlock |

---

## Example

```json
{
  "$schema": "commission.schema.json",
  "schemaVersion": 1,
  "id": "tall_tower",
  "title": "Tall Tower",
  "thumbnailPath": "thumbnails/tall_tower.png",
  "prerequisites": ["first-structure"],
  "conditions": [
    {
      "title": "Tower taller than 20 blocks",
      "condition": "Tallness",
      "operator": ">",
      "threshold": 20,
      "failureDescription": "The tower must be taller than 20 blocks, currently {value}."
    }
  ],
  "unlocks": [
    {
      "title": "Exp Bonus",
      "unlock": "ExpReward",
      "expAmount": 500,
      "expType": "Points"
    }
  ]
}
```
