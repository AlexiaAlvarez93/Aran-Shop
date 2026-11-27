Arán Shop – Sistema Web de Comercio en Línea

Descripción del proyecto

Arán Shop es un sistema web de comercio en línea desarrollado como proyecto final del curso de Programación Avanzada de la Universidad Internacional San Isidro Labrador (UISIL).

El sistema permite administrar productos, categorías y pedidos, además de ofrecer a los clientes la posibilidad de consultar el catálogo, realizar compras y revisar sus pedidos.

Tecnologías utilizadas

Java.

Spring Boot.

Spring MVC.

Spring Data JPA.

Thymeleaf.

HTML, CSS y JavaScript.

Microsoft SQL Server.

Maven.

Funcionalidades principales

Administrador

Iniciar sesión.

Registrar, modificar, consultar y eliminar productos.

Administrar categorías.

Consultar pedidos.

Consultar los detalles de cada pedido.

Actualizar el estado de los pedidos.

Cliente

Iniciar sesión.

Consultar el catálogo de productos.

Agregar productos al carrito.

Realizar compras.

Consultar el historial de sus pedidos.

Base de datos

El sistema utiliza una base de datos llamada BDProductos en Microsoft SQL Server.

Está compuesta por las siguientes tablas:

Usuarios.

Categorias.

Productos.

Pedidos.

DetallesPedido.

La relación entre usuarios y pedidos permite asociar las compras con la cuenta del cliente mediante el campo usuario_id.

El proyecto incluye el archivo AranShop_BaseDatos_Completa.sql, que contiene la estructura y los datos de prueba de la base de datos.

Requisitos para ejecutar el proyecto

Tener instalado Java JDK.

Tener instalado IntelliJ IDEA o un entorno compatible.

Tener Microsoft SQL Server.

Tener Maven.

Configurar la conexión a la base de datos BDProductos.

Ejecución del sistema

Abrir el proyecto en IntelliJ IDEA.

Verificar la configuración de conexión a SQL Server.

Esperar a que Maven cargue las dependencias.

Ejecutar la clase principal de Spring Boot.

Abrir el navegador e ingresar a:

http://localhost:8080

Repositorio GitHub

https://github.com/AlexiaAlvarez93/Aran-Shop

Autora

Proyecto desarrollado por Alexia Stefanny Álvarez Hernández.

Universidad Internacional San Isidro Labrador (UISIL).

Curso: Programación Avanzada.

Año: 2026.