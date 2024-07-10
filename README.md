# Progetto di sistemi distribuiti A.A. 2023-2024: Gruppo HTCP-Legends
## Componenti del gruppo e componente sviluppata
* spitfire233 - Server Web
* Andex125 - Database
* Scoal2053 - Client Web

## Descrizione del Lavoro Svolto

Il progetto "HTCP-Legends" è un'applicazione distribuita progettata e sviluppata per gestire l'acquisto e la gestione di domini internet. L'architettura del sistema si compone di tre principali componenti: client-web, server-web e database documentale.

### CLIENT WEB

Il client web è sviluppato utilizzando HTML e JavaScript e implementa le seguenti funzionalità:

- **Sistema di Registrazione e Login:** Gli utenti possono registrarsi al sito inserendo nome, cognome e indirizzo email. Possono effettuare il login inserendo l'indirizzo email.
  
- **Verifica Disponibilità di un Dominio:** Gli utenti possono verificare se un dominio è disponibile per la registrazione. Se non disponibile, vengono mostrati i dettagli dell'attuale proprietario (nome, cognome, email) e la data di scadenza.

- **Registrazione di un Dominio:** Gli utenti possono acquistare e registrare nuovi domini. La registrazione richiede dati personali e informazioni della carta di credito per completare l'acquisto. I domini posso avere i seguenti TLD:
    - `.it`
    - `.net`
    - `.com`
    - `.org`
- **Visualizzazione dei Domini Registrati:** Gli utenti possono visualizzare l'elenco dei domini posseduti, inclusi quelli scaduti e non rinnovati, con relative date di registrazione e scadenza.

- **Visualizzazione degli Ordini:** Gli utenti possono vedere l'elenco degli ordini effettuati, con dettagli come dominio, data, tipo di operazione (REGISTRAZIONE o RINNOVO) e costo.

- **Rinnovo di un Dominio:** Gli utenti possono rinnovare un dominio, estendendo la sua scadenza fino a un massimo di 10 anni dalla data odierna.

Per avviare il client web è possibile utilizzare l'estensione di VSCode `Live Preview` come visto a laboratorio.
Per assicurarsi che i `cookie` vengano settati correttamente, è necessario abilitare i cookies di terze parti nel browser che si sta utilizzando. Il progetto è stato testato sui seguenti browser:
- `Google Chrome` (presente sulla macchina virtuale)
- `Brave Browser` (presente sulla macchina di un componente del gruppo)

### SERVER WEB
Il server web è sviluppato utilizzando Jetty, un server web e container servlet Java, integrato con Maven per automatizzare il deploy delle servlet la gestione delle dipendenze. Le servlet sono implementate utilizzando le API Jakarta EE 10. Le principali funzionalità del server includono:
- **Gestione della comunicazione con il client**: Il server-web espone un API REST, descritta nel file `REST.md`, che permette al client di effettuare richieste e ottenere risposte riguardanti:
    - la registrazione di un nuovo utente
    - il login di un utente esistente
    - Il controllo di disponibilità di un dominio
    - L'acquisto di un dominio
    - Il rinnovo di un dominio precedentemente registrato

Il server ritorna al client l'esito della richiesta tramite delle risposte HTTP.
- **Esecuzione di query sul database per soddisfare le richieste dei client**: Il server-web comunica con il database tramite il protocollo descritto in `TCP.md`.

#### Componenti del server web:
Il server web è costituito da più file .java divisi in diversi package:
- **Entities**: Contiene le classi Java che rappresentano le entità del dominio applicativo
- **Resources**: Contiene le classi Java che rappresentano le risorse disponibili e gli endpoint esposti dalla REST API
- **Utils**: Contiene le classi Java che offrono servizi utilizzati dalle altre classi.

#### Esecuzione del server web:
Il server web deve essere avviato tramite linea di comando con:
```bash
mvn jetty:run
```

### DATABASE

Il database è implementato in Java e supporta operazioni su documenti JSON.

#### Caratteristiche del Database

- **Tipo:** Database documentale che memorizza le informazioni in file .json.
  
- **Struttura di Riferimento:** Utilizza una mappa (Map) in cui la chiave è il nome del file (che funge da "tabella") e il valore è il riferimento al file stesso (ad esempio, Foo.json).

#### Comunicazione Server-Database

La comunicazione tra il server e il database è dettagliata nel file `TCP.md`.

#### Implementazione

Il database è implementato come un singleton chiamato `R2DB.java`, che contiene la logica principale:

- **Struttura Dati:** Utilizza una `ConcurrentHashMap<String, File>` per gestire le operazioni sul database. Ogni file corrisponde ad una tabella
del database
  
- **Serializzazione:** Utilizza la libreria JsonB per la serializzazione e deserializzazione delle stringhe in formato JSON.
  
- **Accesso Concorrente:** Utilizza un oggetto `ReentrantReadWriteLock` per gestire l'accesso concorrente ai file.
  
- **Gestione Query:** Include classi specifiche per l'esecuzione di query concorrenti.

#### Funzionalità Disponibili

Le operazioni nel database, descritte in `TCP.md`, includono:
- Creazione di un file .json nel database.
- Cancellazione di un file .json dal database.
- Aggiunta di una coppia chiave-valore a un array in un file.
- Rimozione di una coppia chiave-valore da un array in un file.
- Modifica del valore in una coppia chiave-valore in un array in un file.
- Ricerca e restituzione di una o più coppie chiave-valore in un file.

#### STRUTTURA TABELLE
Ogni file JSON creato ha la seguente struttura:
```Javascript
{
    "key1" : {
        "Key" : "value"
        //...
    },
    "key2": {
        "Key": "value"
        //...
    }
    //...
}
```
#### Persistenza

Esiste una cartella denominata dbJsons utilizzata per la persistenza dei dati. All'avvio del database, verranno letti tutti i file .json presenti in questa cartella e i loro contenuti verranno inseriti nel database. Questa operazione viene eseguita prima di accettare qualsiasi messaggio dal server. Poiché tutte le operazioni di modifica dei dati del database sono eseguite direttamente sui file, si garantisce la persistenza dei dati anche in caso di errore del database.

#### Configurazione

È possibile utilizzare la cartella dbJsons per caricare file .json nel database. Questi file verranno letti e inseriti nel database secondo la logica descritta sopra. Per avviare il database è possibile eseguire il comando:
```bash
mvn exec:java
```
È inoltre possibile creare file .json da riga di comando specificando il nome del file nel comando per eseguire il database 
Ad esempio, per eseguire il database con i file User.json, Domain.json e Order.json, utilizzare il seguente comando:
```bash
mvn exec:java "-Dexec.args=User.json Domain.json Order.json"
```
## JSON di Test
Come da specifica, all'interno della cartella `dbJsons` sono presenti dei file .json che contengono dati relativi a domini, utenti e ordini preesistenti. Essi sono:
- `User.json`: Tabella che contiene gli utenti; gli utenti registrati sono:
    - `Emanuele Petriglia` con email `e.petriglia@unimib.it`
    - `Ahsoka Tano` con email `ahsokatano@nolongerajedi.com`
    - `Sheev Palaptine` con email `sheevpalpatine@empire.com`
- `Domain.json`: Tabella che contiene i domini registrati
- `Order.json`: Tabella che contiene gli ordini effettuati
