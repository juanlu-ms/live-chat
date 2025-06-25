# Java Live Chat - WebSocket Server

Proyecto de chat en tiempo real implementado desde cero en Java 21, utilizando la API estándar de WebSocket (`jakarta.websocket`) y la implementación Tyrus.

Este proyecto no utiliza frameworks como Spring para favorecer el aprendizaje de bajo nivel en la gestión de conexiones WebSocket, sesiones y mensajes entre múltiples clientes.

---

## Características

- Conexión en tiempo real mediante WebSockets
- Servidor WebSocket propio con `Tyrus`
- Arquitectura cliente-servidor sin frameworks
- Broadcast de mensajes a todos los clientes conectados
- Validación y control de tamaño del mensaje
- Gestión básica de errores y sesiones
- Comunicación en formato JSON
- Implementado con Java 21 y Maven

---

## Cómo ejecutar

### Requisitos

- Java 21
- Maven 3.9+

### Compilar

```
mvn clean package
```

### Ejecutar el servidor

```bash
java -jar target/live-chat-1.0-SNAPSHOT.jar
```
Esto inicia un servidor WebSocket en: ws://localhost:8025/chat


### Probar el cliente

Se puede utilizar el index.html proporcionado en la carpeta `src/main/resources` para probar el cliente WebSocket.
Simplemente, abre el archivo en un navegador compatible con WebSockets.


### Próximamente

- Servidor HTTP para servir el cliente
- Soporte para múltiples salas de chat
- Persistencia de mensajes
- Autenticación de usuarios