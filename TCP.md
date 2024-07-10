# Documentazione del protocollo di comunicazione con il database

## Introduzione
Il protocollo per il nostro database (R2DB) si basa sul protocollo TCP e segue una struttura di richiesta e risposta. Il server invia una richiesta al database, e il database risponde con una risposta in base alle specifiche seguenti. 

Le richieste e le risposte sono in formato testuale naturale, come separatore per le varie parti del messaggio si utilizza " ".

Il database utilizzerà come porta d'ascolto la porta 3030.

## Comandi Supportati

### CREATE
Il comando `CREATE` viene utilizzato per creare una nuova tabella nel database.

- **Sintassi:** `CREATE <NOME_TAB>`
- **Risposte:**
  - `CREATE: OK!`
  - `CREATE: ALREADY_EXISTS!`
  - `CREATE: ERROR`

### CREATE_KEY
Il comando `CREATE_KEY` viene utilizzato per creare una nuova chiave in una tabella esistente.

- **Sintassi:** `CREATE_KEY <NOME_TAB> <KEY> <VALUE>`
- **Risposte:**
  - `CREATE_KEY: OK!`
  - `CREATE_KEY: ALREADY_EXISTS!`
  - `CREATE_KEY: ERROR`

### SELECT
Il comando `SELECT` viene utilizzato per ottenere il valore associato a una chiave specifica o per ottenere tutti i valori di una tabella con o senza condizioni.
Per non segnalare un key specifica si utilizza il carattere "*" in <KEY>.

- **Sintassi:**
  - `SELECT <TAB> <KEY>`: Selezione di una chiave senza condizioni.
  - `SELECT <TAB> <KEY> WHERE <PARAM> <E/B/S/LE/BE> <VALUE>`: Selezione di una chiave con condizioni.
- **Risposte:**
  - `SELECT: {JSON}`
  - `SELECT: TABLE_NOT_FOUND`
  - `SELECT: KEY_NOT_FOUND`
  - `SELECT: PARAM_NOT_FOUND`

### MODIFY
Il comando `MODIFY` viene utilizzato per modificare il valore di una chiave esistente in una tabella.

- **Sintassi:** `MODIFY <TAB> <KEY> <NEW_JSON>`
- **Risposte:**
  - `MODIFY: OK!`
  - `MODIFY: TABLE_NOT_FOUND`
  - `MODIFY: KEY_NOT_FOUND`
  - `MODIFY: PARAM_NOT_FOUND`

### DELETE
Il comando `DELETE` viene utilizzato per eliminare una tabella dal database.

- **Sintassi:** `DELETE <TABLE>`
- **Risposte:**
  - `DELETE: OK!`
  - `DELETE: TABLE_NOT_FOUND`
  - `DELETE: ERROR`

### DELETE_KEY
Il comando `DELETE_KEY` viene utilizzato per eliminare una chiave specifica da una tabella.

- **Sintassi:** `DELETE_KEY <TABLE> <KEY>`
- **Risposte:**
  - `DELETE_KEY: OK!`
  - `DELETE_KEY: TABLE_NOT_FOUND`
  - `DELETE_KEY: KEY_NOT_FOUND`

### GET_LAST_INDEX
Il comando `GET_LAST_INDEX` viene utilizzato per ottenere la key più grande di una tabella.

- **Sintassi:** `GET_LAST_INDEX <TAB>`
- **Risposte:**
  - `GET_LAST_INDEX: {INDEX}`
  - `GET_LAST_INDEX: TABLE_NOT_FOUND`
  - `GET_LAST_INDEX: EMPTY`

## Sintassi dei Messaggi
Ogni messaggio inviato al server deve terminare con la stringa `END`.

## Esempi di Messaggi
- `CREATE User END`
- `CREATE_KEY User 1 {"name": "John Doe"} END`
- `SELECT User * END`
- `SELECT User * WHERE age E 30 END`
- `MODIFY User 1 {"name": "Jane Doe"} END`
- `DELETE User END`
- `DELETE_KEY User 1 END`
- `GET_LAST_INDEX User END`

