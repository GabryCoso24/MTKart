# MTKart - Mario Kart per Minecraft

Plugin Spigot/Paper per gare di kart in stile Mario Kart su Minecraft.

## Funzionalità

### 🏁 Sistema di Gare
- Circuiti personalizzabili con punti di start/finish
- Sistema di giri con rilevamento automatico
- Classifica in tempo reale
- Tempi sul giro e migliori tempi
- Scoreboard di gara aggiornata

### 🛒 Kart con Modelli Custom
- 4 tipi di kart con statistiche diverse:
  - **Default** - Bilanciato
  - **Speed Kart** - Più veloce, meno accelerazione
  - **Balance Kart** - Tutte le statistiche equilibrate
  - **Acceleration Kart** - Accelerazione rapida
- Modelli 3D tramite ItemDisplay (nessuna texture ridimensionata)
- Kart che segue il giocatore

### 🍄 Power-Up (8 tipi)
| Power-Up | Effetto |
|----------|---------|
| 🟢 Guscio Verde | Proiettile rettilineo |
| 🔴 Guscio Rosso | Proiettile a ricerca |
| 🍌 Banana | Trappola a terra |
| 🍄 Fungo | Boost di velocità |
| ⭐ Stella | Invincibilità + velocità |
| ⚡ Fulmine | Colpisce tutti gli avversari |
| 🦑 Calamaro | Acceca gli avversari |
| 💣 Bob-omba | Esplosivo ritardato |

### 🎮 GUI Personalizzate
- **Mappa del Circuito** - Visualizza posizione giocatori
- **Classifica** - Top 3 in tempo reale
- **Inventario Power-Up** - I tuoi oggetti

### 🎵 Sistema Audio
- Countdown sonoro (3, 2, 1, GO!)
- OST personalizzate per circuito
- Effetti sonori per power-up e eventi

## Comandi

### Comandi Base
```
/race create <nome>           - Crea un nuovo circuito
/race set inizio              - Imposta linea di partenza
/race set fine                - Imposta linea di arrivo
/race set laps <numero>       - Imposta numero giri
/race set ost <nome>          - Impatta musica di sottofondo
/race start <nome>            - Avvia la gara
/race stop <nome>             - Ferma la gara
```

### Kart
```
/kart equip <tipo>            - Equipaggia un kart
/kart remove                  - Rimuovi il kart
/kart list                    - Lista kart disponibili
```

### GUI
```
/mtkgui mappa                 - Apri mappa circuito
/mtkgui classifica            - Apri classifica
/mtkgui powerups              - Apri inventario oggetti
```

### Power-Up
```
/powerup                      - Ottieni power-up casuale
```

## Permessi

| Permesso | Descrizione | Default |
|----------|-------------|---------|
| `mtkart.race` | Comandi di gara | op |
| `mtkart.kart` | Comandi kart | true |
| `mtkart.gui` | Comandi GUI | true |
| `mtkart.powerup` | Usa power-up | true |
| `mtkart.admin` | Admin commands | op |

## PlaceholderAPI

Il plugin supporta PlaceholderAPI:

| Placeholder | Descrizione |
|-------------|-------------|
| `%mtkart_race_name%` | Nome gara attuale |
| `%mtkart_lap_current%` | Giro attuale |
| `%mtkart_lap_total%` | Giri totali |
| `%mtkart_lap_remaining%` | Giri rimanenti |
| `%mtkart_in_race%` | In gara (true/false) |
| `%mtkart_finished%` | Ha finito (true/false) |
| `%mtkart_status%` | Stato (idle/racing/finished) |

## Installazione

1. Scarica Spigot/Paper 1.21+
2. Installa PlaceholderAPI (opzionale)
3. Copia `MTKart.jar` in `plugins/`
4. Riavvia il server
5. Configura `plugins/MTKart/config.yml`

## Resource Pack

Il plugin supporta un resource pack opzionale per texture e modelli custom.

### Configurazione Resource Pack
```yaml
resource-pack:
  enabled: true
  url: "URL_al_tuo_pack.zip"
  hash: "SHA1_hash"
  prompt: true
```

### Creare il Resource Pack
1. Usa la cartella `resourcepack/` come template
2. Aggiungi le tue texture in `assets/mtkart/textures/item/`
3. I modelli sono in `assets/mtkart/models/item/`
4. Zippa e carica su un server

Guida ai modelli custom: [resourcepack/MODELLI_CUSTOM.md](resourcepack/MODELLI_CUSTOM.md)

## Configurazione

Vedi `config.yml` per:
- Statistiche dei kart
- Effetti dei power-up
- Distribuzione power-up basata sulla posizione
- Impostazioni GUI
- Suoni e messaggi

## Sviluppo

### Build
```bash
mvn clean package
```

### Struttura Codice
```
src/main/java/com/ideovision/
├── MTKart.java              # Main class
├── commands/                # Comandi
├── config/                  # Gestione config
├── gui/                     # GUI personalizzate
├── karts/                   # Sistema kart
├── listeners/               # Event listeners
├── managers/                # Manager (Circuit, Laps, etc.)
├── placeholders/            # PlaceholderAPI
├── powerups/                # Sistema power-up
└── race/                    # Race manager & scoreboard
```

## API per Sviluppatori

```java
MTKart plugin = MTKart.getInstance();

// Power-Up
PowerUp powerUp = new PowerUp(PowerUpType.GREEN_SHELL, plugin);
powerUp.use(player);

// Kart
plugin.getKartManager().equipKart(player, KartType.SPEED_KART);

// Gara
plugin.getRaceManager().getPosition(player);
plugin.getScoreboardManager().showRaceScoreboard(player);

// GUI
plugin.getRaceGUI().openMapGUI(player);
```

## Dipendenze

- **Required**: Spigot/Paper 1.21+
- **Optional**: PlaceholderAPI 2.11+

## Crediti

- Sviluppato da gabrycoso
- Ispirato a Mario Kart di Nintendo

## License

Questo plugin è fornito "as-is" per uso personale.
