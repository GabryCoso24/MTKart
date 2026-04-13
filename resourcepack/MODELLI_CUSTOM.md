# Modelli Custom MTKart

Questa guida spiega come creare e integrare modelli custom nel resource pack di MTKart.

## Struttura Base

Ogni oggetto custom usa:

1. una texture PNG in `resourcepack/assets/mtkart/textures/item/`
2. un modello JSON in `resourcepack/assets/mtkart/models/item/`
3. un `CustomModelData` nel codice Java

## Flusso Consigliato

1. Scegli il materiale base dell'item.
2. Crea la texture PNG.
3. Aggiungi il modello JSON che punta alla texture.
4. Collega il modello al `CustomModelData` nel plugin.
5. Rigenera il pack e distribuiscilo ai giocatori.

## Esempio Di Modello

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "mtkart:item/green_shell"
  }
}
```

## ID Già Usati

### Power-Up

| Oggetto | CustomModelData | Materiale base |
|---|---:|---|
| Green Shell | 2001 | SLIME_BALL |
| Red Shell | 2002 | SLIME_BALL |
| Banana | 2003 | GOLDEN_HOE |
| Mushroom | 2004 | RED_MUSHROOM |
| Star | 2005 | NETHER_STAR |
| Lightning | 2006 | FEATHER |
| Blooper | 2007 | INK_SAC |
| Bob-omb | 2008 | TNT |

### Kart

| Kart | CustomModelData | Materiale base |
|---|---:|---|
| Default | 1000 | MINECART |
| Speed Kart | 1001 | MINECART |
| Balance Kart | 1002 | MINECART |
| Acceleration Kart | 1003 | MINECART |

## Come Aggiungere Un Nuovo Modello

1. Scegli un `CustomModelData` libero.
2. Crea la texture PNG con il nome desiderato.
3. Aggiungi il JSON in `assets/mtkart/models/item/`.
4. Aggiorna il codice dove viene creato l'`ItemStack`.
5. Aggiorna questa guida con il nuovo ID.

## Nota Su OptiFine/CIT

La cartella `minecraft/optifine/cit/` è opzionale. MTKart funziona già con i modelli vanilla basati su `CustomModelData`.