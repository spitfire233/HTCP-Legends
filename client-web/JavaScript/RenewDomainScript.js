const API_URI = "http://localhost:8080";

window.onload = init();


async function init() {
    const domainName = sessionStorage.getItem("domainName");

    // Fetches user data from the API and updates the DOM with the user's information
    const response = await fetch(API_URI + "/users/user", {
        method: 'GET',
        credentials: 'include'
    });

    // Parses the JSON response and updates the DOM elements with user data
    const result = await response.json();

    document.getElementById("domain").value = domainName;
    document.getElementById("registerName").value = result.name;
    document.getElementById("registerSurname").value = result.surname;
    document.getElementById("registerEmail").value = result.email;

    const form = document.getElementById('renewDomainForm');

    // Adds an event listener to the form to prevent its default submission behavior and calls renewDomain() instead

    form.addEventListener('submit', function (event) {
        event.preventDefault(); 
        renewDomain();
    });
    
}


// Calculates the cost of domain registration based on the duration
function calculateCost(durationRegistration) {
    return 10 * durationRegistration;
}

// Updates the cost displayed on the webpage based on the selected registration duration
function changeCost(durationRegistration) {
    document.getElementById("cost").innerText = "Costo: " + (calculateCost(durationRegistration)) + "€";
}

// This function renews a domain

async function renewDomain() {
    // Collects form data, calculates the cost, and sends a request to renew the domain
    const domain2 = document.getElementById("domain").value;
    if(!isValidTld(domain2)) {
        alert("Il dominio non ha un TLD valido!");
        return;
    }
    // Updates the maximum number of years for renewal based on the domain
    const years = document.getElementById("years").value;
    const ccExpiry = document.getElementById("ccExpiry").value;
    const ccNumber = document.getElementById("ccNumber").value;
    const ccCVV = document.getElementById("ccCVV").value;

    // Check if the credit card expiry date is valid
    if (!isCardExpiryValid(ccExpiry)) {
        alert("La data di scadenza della carta di credito non è valida o è precedente alla data odierna.");
        return; // Stops the function execution if the expiry date is not valid. This ensures that the user cannot proceed with an expired card.
    }

    if(ccCVV.length != 3){
        alert("Il CVV deve essere di 3 cifre.");
        return; // Stops the function execution if the CVV is not exactly 3 digits. This is a standard length for CVV codes on credit cards.
    }

    if(ccNumber.length != 16){
        alert("Il numero di carta di credito deve essere di 16 cifre.");
        return; // Stops the function execution if the credit card number is not exactly 16 digits. This is a common length for credit card numbers.
    }

    const cost = calculateCost(years);

    const today = new Date();
    // Formats the current date as YYYY-MM-DD
    const dateString = today.toISOString().split('T')[0]; 

    const response = await fetch(API_URI + "/domains/" + domain2, {
        method: 'GET',
        credentials: 'include'
    });

    // If the domain exists, prepares and sends the renewal request
    if (response.status == 200) {
        const domain = await response.json();
        const domainExpireDate = new Date(domain.expireDate);
        const futureYear = domainExpireDate.getFullYear() + parseInt(years);
        console.log(futureYear);
        if(futureYear - new Date(today).getFullYear() > 10) {
            alert("La data di rinnovo non può essere di 10 anni maggiore rispetto alla data odierna!");
        } else {
            const futureDate = new Date(today);
            futureDate.setFullYear(futureYear);
            const futureDateString = futureDate.toISOString().split('T')[0];
            const response3 = await fetch(API_URI + "/domains/renewDomain/" + domain2 +"?date=" + futureDateString, {
                method: 'GET',
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: 'include',
            });


            // If the renewal is successful, redirects to the main page
            if (response3.ok) {
                await fetch(API_URI + "/orders/", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    credentials: 'include',
                    body: JSON.stringify({
                        domain: domain2,
                        date: dateString,
                        orderType: "RENEWAL",
                        cost: cost
                    })
                });
                window.location.href = "MainPage.html";
            }
        }
        } else if(response.status == 404) {
            alert("Dominio non trovato!");
        } else {
            alert("Errore nel rinnovo!");
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


// Function to check if the credit card's expiry date is valid (not before today's date)
function isCardExpiryValid(ccExpiry) {
    const expiryDate = new Date(ccExpiry); // Converts the expiry date from a string to a Date object
    const today = new Date(); // Gets today's date as a Date object
    // Sets today's time to midnight to ensure the comparison is based solely on the date
    today.setHours(0, 0, 0, 0);
    // The expiry date must be greater than or equal to today's date
    return expiryDate >= today;
}
