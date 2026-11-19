document.addEventListener("DOMContentLoaded", function() {
    const formulario = document.getElementById("formLogin");

    formulario.addEventListener("submit", function(event) {
        // Detiene el envío por defecto para controlarlo con JS
        event.preventDefault();

        const usuario = document.getElementById("usuario").value.trim();
        const contrasena = document.getElementById("contrasena").value.trim();
        const mensaje = document.getElementById("mensajeLogin");

        // Estilos base de la caja de alerta
        mensaje.style.display = "block";
        mensaje.style.padding = "12px 15px";
        mensaje.style.borderRadius = "6px";
        mensaje.style.marginTop = "15px";
        mensaje.style.fontSize = "14px";
        mensaje.style.fontWeight = "500";
        mensaje.style.boxShadow = "0 2px 5px rgba(0,0,0,0.05)";

        // 1. Validación de campos vacíos (Advertencia)
        if(usuario === "" || contrasena === ""){
            mensaje.innerHTML = "<strong>[ADVERTENCIA]</strong> Debe completar todos los campos obligatorios.";
            mensaje.style.backgroundColor = "#fff3cd";
            mensaje.style.borderLeft = "5px solid #ffc107";
            mensaje.style.color = "#856404";
            return;
        }

        // 2. Validación de credenciales correctas (Éxito y Salto de página)
        if(usuario === "admin" && contrasena === "1234"){
            mensaje.innerHTML = "<strong>[ÉXITO]</strong> Inicio de sesión correcto. ¡Bienvenido a Arán Shop!";
            mensaje.style.backgroundColor = "#d4edda";
            mensaje.style.borderLeft = "5px solid #28a745";
            mensaje.style.color = "#155724";

            // Salto automático hacia el controlador de Spring Boot (/productos)
            setTimeout(function() {
                window.location.href = "/productos";
            }, 800); // Espera 0.8 segundos para que se alcance a visualizar el mensaje de éxito

        } else {
            // 3. Credenciales incorrectas (Error)
            mensaje.innerHTML = "<strong>[ERROR]</strong> Usuario o contraseña incorrectos.";
            mensaje.style.backgroundColor = "#f8d7da";
            mensaje.style.borderLeft = "5px solid #dc3545";
            mensaje.style.color = "#721c24";
        }
    });
});