# Documentazione delle API REST del server "Darth Server"

L'API REST di questo progetto utilizza JSON come formato di rappresentazione sia dei dati ricevuti dai client sia dei dati inviati come risposta ad essi.
L'API REST espone le seguenti risorse per la gestione dei dati presenti nel sistema:


## `/users?email={value}`
Questa risorsa è dedicata all'ottenimento delle informazioni di un singolo utente.

### GET
**Descrizione**: Ottiene le informazioni sull'utente che ha email uguale a `value`.

**Parametri**: Un parametro `email` nella query della richiesta con valore `value`.

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: Risposta contenente la rappresentazione in formato JSON dell'utente con mail uguale a `value`

**Codici di stato restituiti**:
* `200: OK`: L'utente è stato trovato; una rappresentazione JSON dell'utente richiesto è contenuta nel body della risposta
* `400: BAD_REQUEST`: La mail passata come parametro è nulla
* `401: UNAUTHORIZED`: Il client che ha effettuato la richiesta non ha effettuato il login
* `404: NOT_FOUND`: L'utente richiesto non è stato trovato
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/users/register/`
Questa risorsa è dedicata alla registrazione di un nuovo utente nel sistema.

### POST
**Descrizione**: Registra un nuovo utente all'interno del sistema

**Parametri**: Nessun parametro richiesto

**Header**: Nessun header all'infuori di quelli impostati automaticamente dal client

**Body richiesta**: Un utente da registrare con i campi `name`, `surname` e `email` non nulli. Il body della richiesta deve essere un oggetto JSON con campi uguali a quelli sopra elencati.

**Risposta**: Riposta con body vuoto e header "Set-Cookie" uguale all'ID assegnato dal server all'utente

**Codici di stato restituiti**:
* `201: CREATED`: L'utente è stato registrato correttamente
* `400: BAD_REQUEST`: I dati di registrazione sono incompleti o invalidi
* `409: CONFLICT`: L'utente è già registrato
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/users/login?email={email}`
Questa risorsa è dedicata al login degli utenti

### GET
**Descrizione**: Effettua il login di un utente

**Parametri**: Un parametro `email` nella query della richiesta con valore `value`.

**Header**: Nessun header all'infuori di quelli impostati automaticamente dal client

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: Risposta con body vuoto e header "Set-Cookie" uguale all'ID assegnato dal server all'utente

**Codici di stato restituiti**:
* `200: OK`: Autenticazione avvenuta con successo
* `401: UNAUTHORIZED`: L'Utente con `email` uguale a `value` non è presente nel sistema
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/users/user/`
Questa risorsa è dedicata all'ottenimento delle informazioni dell'utente con cui il client ha effettuato il login

### GET
**Descrizione**: Restituisce le informazioni sull'utente corrente

**Parametri**: Nessun parametro richiesto

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: L'oggetto JSON che rappresenta l'utente

**Codici di stato restituiti**:
* `200: OK`: La richiesta è stata gestita con successo; la rappresentazione in formato JSON dell'utente è contenuta nel body della richiesta
* `401: UNAUTHORIZED`: L'Utente non ha effettuato l'autenticazione oppure ha un `cookie` invalido
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/domains/`
Questa risorsa è dedicata alla registrazione di un dominio e al recupero di tutti i domini presenti nel sistema:

### GET
**Descrizione**: Restituisce tutti i domini presenti nel sistema

**Parametri**: Nessun parametro richiesto

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: Un oggetto JSON contenente gli oggetti JSON che rappresentano i domini presenti sul sistema

**Codici di stato restituiti**:
* `200: OK`: La richiesta è stata gestita con successo; la rappresentazione in formato JSON dell'utente è contenuta nel body della richiesta
* `401: UNAUTHORIZED`: L'Utente non ha effettuato l'autenticazione oppure ha un `cookie` invalido
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

### POST
**Descrizione**: Registra un nuovo dominio sul sistema

**Parametri**: Nessun parametro richiesto

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve contenere un oggetto JSON con campi `registrationdate` (la data di registrazione del dominio), `expiredate` (la data di scadenza del dominio) e `name` (il nome del dominio) non nulli.

**Risposta**: Una risposta con body vuoto

**Codici di stato restituiti**:
* `201: CREATED`: Il dominio è stato registrato correttamente
* `400: BAD_REQUEST`: I dati del dominio da registrare sono incompleti o errati
* `401: UNAUTHORIZED`: Il client che ha fatto la richiesta non ha effettuato il login
* `409: CONFLICT`: Il dominio specificato è già stato registrato
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database


## `/domains/isAvailable?domain={domainName}`
Questa risorsa è dedicata al controllo della disponibilità di un certo dominio

### GET
**Descrizione**: Controlla se il dominio `domainName` è disponibile per la registrazione

**Parametri**: Un parametro `domain` nella query della richiesta con valore `domainName`.

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: Se il dominio è disponibile, una risposta con body vuoto. Se il dominio NON è disponibile, la risposta conterrà nel suo body l'oggetto JSON che rappresenta il dominio con nome `domainName`.

**Codici di stato restituiti**:
* `200: OK`: Il dominio NON è disponibile
* `204: NO_CONTENT`: Il dominio È disponibile
* `400: BAD_REQUEST`: La richiesta del client non ha specificato il valore di `domain` all'interno della query
* `401: UNAUTHORIZED`: Il client che ha fatto la richiesta non ha effettuato il login
* `409: CONFLICT:`: Il dominio è correntemente in registrazione da parte di un'altro utente
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/domains/{domain}`
Questa risorsa è dedicata al recupero del dominio `domain` dal database

### GET:

**Descrizione**: Restituisce, se esiste, le informazioni sul dominio `domain`

**Parametri**: Un parametro `domain` nel percorso della risorsa che indica di quale dominio si vuole ottenere le informazioni

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: Se il dominio esiste, la risposta conterrà nel suo body un oggetto JSON che rappresenta il dominio `domain`

**Codici di stato restituiti**:
* `200: OK`: Il dominio esiste; la risposta conterrà l'oggetto JSON corrispondente a `domain`
* `400: BAD_REQUEST:`: Il parametro `domain` è nullo
* `401: UNAUTHORIZED`: Il client che ha fatto la richiesta non ha effettuato il login
* `404: NOT_FOUND`: Il dominio specificato nella richiesta non è stato trovato
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/domains/renewDomain/{domain}`
Questa risorsa è dedicata al rinnovo del possesso del dominio `domain`

### GET
**Descrizione**: Effettua il rinnovo del possesso del dominio modificando la data di scadenza

**Parametri**: Un parametro `domain` nel percorso della risorsa che indica di quale dominio si vuole rinnovare il  possesso

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: La richiesta deve contenere nel suo body una stringa JSON che rappresenti la nuova data di scadenza del possesso del dominio. La data deve essere in formato `YYYY-MM-DD`

**Risposta**: Una risposta con body vuoto

**Codici di stato restituiti**:
* `200: OK`: La data di scadenza è stata modificata con successo
* `400: BAD_REQUEST`: La data di scadenza specificata non è valida o nulla
* `401: UNAUTHORIZED`: Il client che ha fatto la richiesta non ha effettuato il login
* `404: NOT_FOUND`: Il dominio `domain` non è presente nel database
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/domains/userDomains/`
Questa risorsa è dedicata al recupero di tutti i domini acquistati dall'utente correntemente autenticato

### GET
**Descrizione**: Restituisce tutti i domini acquistati dall'utente correntemente autenticato

**Parametri**: Nessun parametro

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: Un oggetto JSON contenente gli oggetti JSON che rappresentano i domini acquistati dall'utente

**Codici di stato restituiti**:
* `200: OK`: La richiesta è stata gestita con successo; i domini dell'utente sono presenti nel body della risposta
* `401: UNAUTHORIZED`: Il client che ha fatto la richiesta non ha effettuato il login
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database

## `/orders/`
Questa risorsa è dedicata al recupero degli ordini degli utenti e alla registrazione di nuovi ordini

### GET
**Descrizione**: Recupera tutti gli ordini dell'utente correntemente autenticato

**Parametri**:  Nessun parametro

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve essere vuoto

**Risposta**: Un oggetto JSON contenente gli oggetti JSON che rappresentano gli ordini dell'utente

**Codici di stato restituiti**:
* `200: OK`: La richiesta è stata gestita con successo; gli ordini dell'utente sono presenti nel body della risposta
* `401: UNAUTHORIZED`: Il client che ha fatto la richiesta non ha effettuato il login
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database`:

### POST
**Descrizione**: Registra un nuovo ordine nel sistema

**Parametri**:  Nessun parametro

**Header**: La richiesta deve contenere l'header `Cookie` Il cui valore deve essere un ID numerico valido associato ad un utente esistente

**Body richiesta**: Il body della richiesta deve contenere un oggetto JSON con campi `domain` (il dominio a cui è riferito l'ordine), `cost` (il costo dell'ordine) e `orderType` (il tipo di ordine) non nulli. In particolare, `orderType` può assumere solo i valori `RENEWAL` oppure `REGISTER`

**Risposta**: Una risposta con body vuoto

**Codici di stato restituiti**:
* `201: CREATED`: L'ordine è stato creato con successo
* `400: BAD_REQUEST`: L'ordine inviato dal client ha campi non validi o nulli
* `401: UNAUTHORIZED`: Il client che ha fatto la richiesta non ha effettuato il login
* `503: SERVICE_UNAVAILABLE`: Si è verificato un problema con il database`