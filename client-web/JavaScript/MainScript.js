const API_URI = "http://localhost:8080";

window.onload = init();

async function init() {
    // Get the form element for checking domain availability
    const form = document.getElementById('checkDomainForm');

    // Add an event listener to the form to prevent default submission and call checkDomain function
    form.addEventListener('submit', function (event) {
        event.preventDefault(); // Previene il comportamento di default del submit
        checkDomain(); // Chiama la funzione checkDomain
    });
    // Call functions to update the domain list, initialize the table, and update orders
    initializeTable();
    updateDomainTable();
    updateOrders();
}


// Function to redirect to the domain registration page
function redirectToRegisterPage() {
    window.location.href = "RegisterDomainPage.html";
}

// Function to redirect to the domain renewal page
function redirectToRenewPage() {
    window.location.href = "RenewPage.html";
}


// Function to check if a domain is available for registration
async function checkDomain() {
    // Get the domain name from the input field
    const domain = document.getElementById("domain").value;

    // Make a GET request to check if the domain is available
    const response = await fetch(API_URI + "/domains/isAvailable?domain=" + domain, {
        method: 'GET',
        credentials: 'include'
    });
    // If the domain is not available, display the current owner's details
    if (!isValidTld(domain)) {
        alert('Il TLD del dominio non è valido');
        return;
    }

    let resultText = "";
    if (response.status == 204) {
        // If the domain is available, inform the user
        resultText = "Il dominio " + domain + " è disponibile per la registrazione";
        alert(resultText);
    } else if (response.status == 200){
        const result = await response.json();
        const response2 = await fetch(API_URI + "/users?email=" + result.owner, {
            method: 'GET',
            credentials: 'include'
        });
        const result2 = await response2.json();
        // If the domain is not available, display the current owner's details
        resultText = "Il dominio " + domain + " è già registrato. \n" +
            "Registrato da: " + "\n" +
            "Nome: " + result2.name + "\n" +
            "Cognome: " + result2.surname + "\n" +
            "Email: " + result.owner + "\n" +
            "Data di scadenza: " + result.expireDate + "\n";
        document.getElementById("checkDomainResult").innerText = resultText;
    } else if(response.status == 400) {
        alert("La richiesta del client è malformata");
    } else if(response.status == 401) {
        alert("L'utente non è autenticato");
    } else if(response.status == 409) {
        alert("Il dominio è correntemente in registrazione da parte di un altro utente");
    } else if(response.status == 503) {
        alert("C'è stato un problema con il database");
    } else {
        alert("Errore sconosciuto");
    }
}
// Define a list of valid TLDs
var validTlds = ['.com', '.net', '.org', '.it'];

// Function to check if a domain has a valid TLD
function isValidTld(domain) {
    // Split the domain into parts and get the TLD
    const domainParts = domain.split('.');
    const tld = '.' + domainParts[domainParts.length - 1];
    // Check if the TLD is in the list of valid TLDs
    return validTlds.includes(tld);
}



// Function to update the table with domains registered by the user
async function updateDomainTable() {
    // Get the table elements
    const table = document.getElementById('domainsTable');
    // Fetch the list of domains registered by the user
    const responses = await fetch(API_URI + "/domains/userDomains", {
        method: 'GET',
        credentials: 'include'
    });
    // Parse the JSON response
    let domainJson = await responses.json();
    const domainMap = new Map(Object.entries(domainJson));
    // Iterate over each domain and add it to the table
    //let response of responses
    for (let key of Array.from(domainMap.keys())) {
        let domain = domainMap.get(key);
        // Insert a new row at the end of the table
        const row = table.insertRow(-1);
        const cell1 = row.insertCell(0); // Dominio
        const cell2 = row.insertCell(1); // Data_registrazione
        const cell3 = row.insertCell(2); // Dat_scadenza
        const cell4 = row.insertCell(3); // Azioni

        // Set the cell contents to domain details
        cell1.innerHTML = domain.name;
        cell2.innerHTML = domain.registrationdate;
        cell3.innerHTML = domain.expiredate;

        // Calculate the duration of the domain registration
        /*
        const registrationYear = new Date(domain.registrationdate).getFullYear();
        const currentYear = new Date().getFullYear();
        const duration = currentYear - registrationYear;
        */
        // Crea un oggetto Date per la data corrente
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const domainExpireDateObj = new Date(domain.expiredate);
        domainExpireDateObj.setHours(0, 0, 0, 0);

        const domainExpireDate = domainExpireDateObj > today;

        // If the domain has been registered for less than 10 years, add a renew button
        if(domainExpireDate){
            const renewButton = document.createElement('button');
            renewButton.innerHTML = 'Rinnova';
            renewButton.onclick = function () {
                sessionStorage.setItem('domainName', domain.name)
                redirectToRenewPage();
            };
            cell4.appendChild(renewButton);
        }
    }
}

// Function to initialize the table with a row for registering a new domain
function initializeTable() {
    const table = document.getElementById('domainsTable');
    // Add an empty row with a button to register a new domain
    const emptyRow = table.insertRow(-1);
    const emptyCell = emptyRow.insertCell(0);
    emptyCell.colSpan = 3; // Estendi la cella vuota su 3 colonne
    const registerCell = emptyRow.insertCell(1);
    const registerButton = document.createElement('button');
    registerButton.innerHTML = 'Registra nuovo dominio';
    registerButton.onclick = function () {
        // Redirect to the domain registration page when clicked
        redirectToRegisterPage();
    };
    registerCell.appendChild(registerButton);
}

// Function to update the orders table
async function updateOrders() {
    const table = document.getElementById('ordersTable');
    const responses = await fetch(API_URI + "/orders/", {
        method: 'GET',
        credentials: 'include'
    });

    // Parse the JSON responseù
    let orderJson = await responses.json();
    const orderMap = new Map(Object.entries(orderJson));

    // Iterate over each domain and add it to the table
    //let response of responses
    for (let key of Array.from(orderMap.keys())) {
        let order = orderMap.get(key);
        // Insert a new row at the end of the table
        const row = table.insertRow(-1);
        const cell1 = row.insertCell(0);
        const cell2 = row.insertCell(1);
        const cell3 = row.insertCell(2);
        const cell4 = row.insertCell(3);

        // Set the cell contents to order details
        cell1.innerHTML = order.domain;
        cell2.innerHTML = order.date;
        cell3.innerHTML = order.ordertype;
        cell4.innerHTML = order.cost + "€";
    }

}