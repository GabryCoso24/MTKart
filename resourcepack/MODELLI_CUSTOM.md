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

## Integrazione ModelEngine (Opzionale)

Se vuoi usare modelli entity avanzati senza dipendere da OptiFine, MTKart supporta una
integrazione opzionale con ModelEngine.

### Requisiti

1. Plugin `ModelEngine` installato sul server.
2. Comando `meg` funzionante (o equivalente della tua versione).
3. Modelli già importati in ModelEngine con ID coerenti.

### Configurazione MTKart

In `config.yml` usa la sezione:

```yml
kart:
  modelengine:
    enabled: true
    models:
      default: "mtkart_default"
      speed_kart: "mtkart_speed"
      balance_kart: "mtkart_balance"
      acceleration_kart: "mtkart_acceleration"
    commands:
      add: "meg model add {entity} {model}"
      remove: "meg model remove {entity} {model}"
```

Placeholder supportati nei comandi:

1. `{entity}` UUID dell'entità sedile del kart.
2. `{model}` ID modello ModelEngine del kart.
3. `{player}` nome del giocatore.

### Come Funziona il Fallback

1. Se ModelEngine non è presente o fallisce il comando `add`, MTKart usa automaticamente il rendering vanilla con `ItemDisplay + CustomModelData`.
2. Quindi puoi tenere entrambe le pipeline attive durante la migrazione.