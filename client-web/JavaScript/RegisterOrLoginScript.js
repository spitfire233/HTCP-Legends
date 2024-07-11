const API_URI = "http://localhost:8080";

window.onload = init();

async function init() {
    // Add click event listener to the "register" button
    document.getElementById("register").addEventListener("click", function (event) {
        event.preventDefault(); 
        toggleForms('register');
    });

    // Add click event listener to the "login" button
    document.getElementById("login").addEventListener("click", function (event) {
        event.preventDefault();
        toggleForms('login'); 
    });

    // Add submit event listener to the registration form
    const form = document.getElementById("registerUserForm");
    form.addEventListener('submit', function (event) {
        event.preventDefault(); 
        register();
    });
}

// Function to toggle between registration and login forms
function toggleForms(accessType) {
    var registerSection = document.getElementById('registerSection');
    var loginSection = document.getElementById('loginSection');

    // Display the appropriate form based on the accessType argument
    if (accessType === 'register') {
        registerSection.style.display = 'block';
        loginSection.style.display = 'none';
    } else {
        registerSection.style.display = 'none';
        loginSection.style.display = 'block';
    }
}

// Function to handle user registration
async function register() {
     // Retrieve user input from form fields
    const name = document.getElementById("registerName").value;
    const surname = document.getElementById("registerSurname").value;
    const email = document.getElementById("registerMail").value;

    // Send a POST request to the server to register the user
    const response = await fetch(API_URI + "/users/register/", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            name: name,
            surname: surname,
            email: email
        })
    });


    let resultText = "";
    // Check the response status to determine the outcome
    if (response.status == 201) {
        // Registration already done
        resultText = "Registrazione effettuata! Ora puoi fare il login";
    } else if (response.status == 400) {
        resultText = "I dati di registrazione sono incompleti o invalidi";
    } else if (response.status == 409) {
        // Redirect to main page on successful registration
        resultText = "Errore durante la registrazione!";
    } else if (response.satus == 500) {
        resultText = "Un errore lato server è avvenuto";
    } else if (response.satus == 503){
        resultText = "C'è stato un problema con il database";
    }else {
        resultText = "Errore sconosciuto";
    }
    alert(resultText);
}



// Function to handle user login
async function login() {
    // Retrieve email from login form
    const email = document.getElementById("loginMail").value;

    // Send a GET request to the server to log in the user
    const response = await fetch(API_URI + "/users/login?email=" + email, {
        method: 'GET',
        // Include credentials for session management
        credentials: 'include'
    });


    // Check the response status to determine the outcome
    if (response.status == 200) {
        window.location.href = "MainPage.html";
    } else if(response.status == 401) {
        alert("Email non corretta");
    } else if (response.status == 503) {
        alert("Si è verificato un errore con il database");
    }
}
