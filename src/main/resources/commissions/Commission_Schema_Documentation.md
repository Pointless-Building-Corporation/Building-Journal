# Commission Schema

This Schema describes a commission in the Building Journal.

**Schema:** `commission.schema.json`  
**JSON Schema Draft:** 2020-12

Current latest schema version: `1`. \
All supported schema versions as of writing: `1`

---

## Top-Level Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `$schema` | string | No | This current schema |
| `schemaVersion` | enum | **Yes** | Schema version — must be one of the allowed schema versions. |
| `id` | string | No | Unique identifier of the Commission |
| `title` | string | **Yes** | Title of the Commission |
| `thumbnailPath` | string | No | Path to the thumbnail of the Commission |
| `conditions` | array | **Yes** | Conditions required to satisfy the Commission |
| `unlocks` | array | No | What the Commission unlocks once completed |
| `prerequisites` | array of strings | No | Commission IDs that must be completed to unlock this Commission |

---

## Conditions

Each entry in `conditions` describes a requirement the player must meet. All conditions share these base fields:

| Field | Type | Required | Description |
|---|---|---|---|
| `condition` | enum | **Yes** | The condition type (see types below) |
| `title` | string | No | What the condition is called in-game |
| `failureDescription` | string | No | What to tell the player if the condition fails |

### Condition Types

#### `BlockAdded`

Checks how many of certain blocks have been added. This includes fluids but not air.

| Field | Type | Required | Description |
|---|---|---|---|
| `blocks` | array of strings | No | Block IDs to check |
| `operator` | `<` \| `>` \| `==` | **Yes** | Comparison operator |
| `threshold` | integer | **Yes** | Value to compare against |

#### `BlockRemoved`

Checks how many of certain blocks have been removed. This includes fluids but not air.

| Field | Type | Required | Description |
|---|---|---|---|
| `blocks` | array of strings | No | Block IDs to check |
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

Checks that the structure is present at one of the given dimensions.

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

Checks the length of the structure.

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
  "id": "tall-tower",
  "title": "Tall Tower",
  "thumbnailPath": "thumbnails/tall-tower.png",
  "prerequisites": ["first-structure"],
  "conditions": [
    {
      "title": "Build tall enough",
      "condition": "Tallness",
      "operator": ">",
      "threshold": 20,
      "failureDescription": "The tower must be taller than 20 blocks!"
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
