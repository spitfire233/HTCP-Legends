const API_URI = "http://localhost:8080";

window.onload = init();

async function init() {
    // Fetch user information from the server
    const response = await fetch(API_URI + "/users/user", {
        method: 'GET',
        credentials: 'include'
    });

    // Parse the JSON response to get user data
    const result = await response.json();

    // Display the fetched user information in the respective fields
    document.getElementById("registerName").value = result.name;
    document.getElementById("registerSurname").value = result.surname;
    document.getElementById("registerEmail").value = result.email;
    const form = document.getElementById('registerDomainForm');

    // Add an event listener to the domain registration form to handle its submission
    form.addEventListener('submit', function (event) {
        event.preventDefault(); // Previene il comportamento di default del submit
        registerDomain(); // Chiama la funzione checkDomain
    });
}

// Function to calculate the cost of domain registration based on its duration
function calculateCost(durationRegistration) {
    // The cost is 10€ per year of registration
    return 10 * durationRegistration;
}

// Function to update the displayed cost when the registration duration changes
function changeCost(durationRegistration) {
    document.getElementById("cost").innerText = "Costo: " + (calculateCost(durationRegistration)) + "€";
}

// Function to handle the domain registration process
async function registerDomain() {
    // Retrieve the domain registration details from the form
    const newDomain = document.getElementById("newDomain").value;
    if(!isValidTld(newDomain)) {
        alert("Il dominio non ha un TLD valido!");
        return;
    }
    const durationRegistration = document.getElementById("durationRegistration").value;
    const cost = calculateCost(durationRegistration);
    const email = document.getElementById("registerEmail").value;
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


    // Check if the domain is available for registration
    const response = await fetch(API_URI + "/domains/isAvailable?domain=" + newDomain, {
        method: 'GET',
        credentials: 'include'
    });

    // Prepare the current date and calculate the future expiration date based on the registration duration
    const today = new Date();
    const dateString = today.toISOString().split('T')[0]; //ottemgo il formato YYYY-MM-DD

    if (response.status == 200) {
        // If the domain is already registered, inform the user
        alert("Il dominio è già registrato");
    } else if (response.status == 204){
        // Calculate the expiration date of the domain registration
        const currentYear = today.getFullYear();
        const futureYear = currentYear + parseInt(durationRegistration);
        const futureDate = new Date(today);
        futureDate.setFullYear(futureYear);
        const futureDateString = futureDate.toISOString().split('T')[0];

        // Attempt to register the domain with the provided details
        const response3 = await fetch(API_URI + "/domains/", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: 'include',
            body: JSON.stringify({
                name: newDomain,
                registrationdate: dateString,
                expiredate: futureDateString,
                owner: email
            })
        });

        let resultText = "";

        if(response3.status == 409){
            // If there's a conflict (e.g., another user is registering the same domain), alert the user
            resultText = "C'è qualcun'altro che sta comprando questo dominio.";
            alert(resultText);
        }else if (response3.status == 201) {
            // On successful domain registration, create an order for the registration and redirect to the main page
            await fetch(API_URI + "/orders/", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: 'include',
                body: JSON.stringify({
                    domain: newDomain,
                    date: dateString,
                    orderType: "REGISTER",
                    cost: cost
                })
            });
            window.location.href = "MainPage.html";
        } else {
            // If the registration fails, inform the user
            alert("Qualcosa è andato storto. Riprova più tardi!");
        }
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

// Function to check if the credit card's expiry date is valid (not before today's date)
function isCardExpiryValid(ccExpiry) {
    const expiryDate = new Date(ccExpiry); // Converts the expiry date from a string to a Date object
    const today = new Date(); // Gets today's date as a Date object
    // Sets today's time to midnight to ensure the comparison is based solely on the date
    today.setHours(0, 0, 0, 0);
    // The expiry date must be greater than or equal to today's date
    return expiryDate >= today;
}