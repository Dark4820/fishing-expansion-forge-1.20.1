# Fishing Expansion - port a Forge 1.20.1 (no oficial)

Port del datapack **Fishing Expansion 1.3.2** de lullaby (AGPL-3.0-or-later) a un mod de **Forge 1.20.1**.

## Qué incluye
- **Pesca de mobs:** ~25 % de los "peces" que sacás son mobs vivos (salmón, bacalao, pez globo, ahogado,
  ahogado con tridente, calamar, calamar luminoso, tortuga, pez tropical, axolote, delfín, guardián).
- **7 encantamientos** que se consiguen **pescando** (libros, cañas y arcos del tesoro), no salen en la mesa
  de encantamientos ni en aldeanos:
  - **Sea Lure** (caña, I-V): -5 s de espera por nivel. Incompatible con Lure.
  - **Treasure Hunter** (caña, I-V): +suerte de pesca (1,5 por nivel, redondeado). Incompatible con Luck of the Sea.
  - **Hooked Strike** (caña, I-III): el anzuelo lanzado daña (2/4/6) lo que golpea.
  - **Ender Hook** (caña): si el anzuelo cae en tierra te teletransporta a él, con efectos negativos.
  - **Water Trail** (arco): las flechas dejan una estela de agua temporal. Incompatible con Flame.
  - **Thundering** (tridente): rayo al herir bajo el cielo abierto y al golpear un pararrayos, sin tormenta.
  - **Dolphin's Grace** (pantalón): gracia del delfín al nadar.
- **Tesoro de pesca:** 6 mapas (barco, tesoro enterrado, monumento, ruina oceánica, portal en ruinas, naufragio),
  3 barriles de naufragio con botín, y libros/cañas/arcos encantados.
- **Estructura del bote** flotante en océanos no congelados (cada ~50 chunks).

## No incluye (se descartó a propósito)
El jefe Elder Drowned (y su Corazón), la armadura de tortuga y su avance. Los "peces especiales" que menciona la
tabla original no existen en el datapack que recibimos, así que tampoco están.

## Cómo compilar (Windows)
Igual que Satchels:
1. En una consola: `set JAVA_HOME=...\jdk-17...` y `set PATH=%JAVA_HOME%\bin;%PATH%`.
2. En esta carpeta: `gradlew.bat build`
3. El jar queda en `build\libs\fishing_expansion-0.1.0.jar`.

## Ajustes
- Probabilidad de mobs: se cambia en `data/minecraft/loot_tables/gameplay/fishing/fish.json` (pesos de las entradas con kelp).
- Todo lo de pesca está en `data/`, así que un datapack del mundo puede sobrescribirlo.
