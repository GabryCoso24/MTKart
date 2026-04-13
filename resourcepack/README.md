# MTKart Resource Pack

Questo resource pack contiene tutte le texture e i modelli per MTKart.

## Struttura

```
resourcepack/
├── assets/
│   ├── mtkart/
│   │   ├── models/
│   │   │   └── item/
│   │   │       ├── green_shell.json
│   │   │       ├── red_shell.json
│   │   │       ├── banana.json
│   │   │       ├── mushroom.json
│   │   │       ├── star.json
│   │   │       ├── lightning.json
│   │   │       ├── blooper.json
│   │   │       ├── bob_omb.json
│   │   │       ├── kart_default.json
│   │   │       ├── kart_speed.json
│   │   │       ├── kart_balance.json
│   │   │       └── kart_acceleration.json
│   │   └── textures/
│   │       └── item/
│   │           (texture dei power-up e kart)
│   └── minecraft/
│       └── optifine/
│           └── cit/ (opzionale, per CustomItemTextures)
└── pack.mcmeta
```

## CustomModelData

### Power-Up
| Power-Up | ModelData ID | Base Item |
|----------|--------------|-----------|
| Green Shell | 2001 | SLIME_BALL |
| Red Shell | 2002 | SLIME_BALL |
| Banana | 2003 | GOLDEN_HOE |
| Mushroom | 2004 | RED_MUSHROOM |
| Star | 2005 | NETHER_STAR |
| Lightning | 2006 | FEATHER |
| Blooper | 2007 | INK_SAC |
| Bob-omb | 2008 | TNT |

### Kart
| Kart Type | ModelData ID | Base Item |
|-----------|--------------|-----------|
| Default | 1000 | MINECART |
| Speed Kart | 1001 | MINECART |
| Balance Kart | 1002 | MINECART |
| Acceleration Kart | 1003 | MINECART |

## Texture Template

Le texture devono essere 64x64 o 128x128 pixel.

### Power-Up Templates
- `green_shell.png` - Guscio verde
- `red_shell.png` - Guscio rosso
- `banana.png` - Buccia di banana
- `mushroom.png` - Fungo rosso
- `star.png` - Stella (con effetto brillante)
- `lightning.png` - Fulmine
- `blooper.png` - Calamaro
- `bob_omb.png` - Bomba nera/rossa

### Kart Templates
- `kart_default.png` - Kart standard
- `kart_speed.png` - Kart rosso veloce
- `kart_balance.png` - Kart bilanciato
- `kart_acceleration.png` - Kart accelerazione

## Come Installare

1. Copia la cartella `resourcepack` in `.minecraft/resourcepacks`
2. Rinomina in `MTKart.zip` se preferisci
3. Attiva nel menu Resource Pack di Minecraft

## Ottieni Texture

Puoi creare le tue texture o scaricare template da:
- https://www.minecraft.net/en-us/addons
- https://www.planetminecraft.com/texture-packs/
