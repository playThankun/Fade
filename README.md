# Fade

Fade is a powerful client-side and server-side utility mod that hides or fades entities within a configurable block range to optimize performance and clear your view.

## Features

* **Entity Culling:** Hide or fade nearby entities to boost your FPS and clear clutter.
* **Flexible Filtering:** Easily filter targets (Players only, Entities only, or All).

## When do you use it?

This mod is extremely useful in situations where crowded entities block your view or cause massive frame drops:
* Highly populated multiplayer servers or spawn areas.
* Massive mob farms causing rendering lags.
* Challenging jump maps (Parkour) or tight spaces where other players obstruct your sight.

Using Fade helps create a cleaner, smoother, and more comfortable gameplay experience.

## Configuration & Usage

### For Clients (In-game GUI)
You don't need to memorize any commands! Clients can easily configure all settings via the standard Minecraft options screen:
* **Path:** `Options...` -> `Video Settings...` -> **Fade Settings Menu**
* Change the distance, target filters, and rendering modes in real-time with a clean graphical user interface.

### For Server Admins (Commands)
Server operators (OPs) and the server console can manage settings remotely and save them to the configuration file using the `/fade config` command tree:

| Command | Arguments / Range | Description |
| :--- | :--- | :--- |
| `/fade config distance` | `<1 ~ 128>` | Sets the block range where culling triggers. |
| `/fade config target` | `PLAYERS_ONLY`<br>`ENTITIES_ONLY`<br>`ALL` | Selects which types of entities to cull. |
| `/fade config mode` | `VANISH` | Sets the render mode when hidden. *(Server environment only supports `VANISH`)* |
| `/fade config operation` | `OPTIMIZE`<br>`FADE_OUT` | `OPTIMIZE`: Hides entities far away.<br>`FADE_OUT`: Hides entities close to you. |
| `/fade config lang` | `ko_kr`<br>`en_us` | Syncs the system language for feedback messages. |
| `/fade config fallingblocks` | `<true \| false>` | Toggles whether falling blocks (sand, gravel) are culled. |
| `/fade config fadeitemvanish` | `<true \| false>` | Toggles the item vanish mode during the faded state. |
| `/fade config filter` | `<string (spaces allowed)>` | Updates the custom target filtering string. |
