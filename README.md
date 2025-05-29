# Chat por Sockets en Java

Este proyecto implementa una aplicación de chat basada en sockets, desarrollada como parte de la asignatura de **Servicios y Procesos**.

## 💬 Descripción

El sistema consta de un **servidor** y múltiples **clientes** que pueden conectarse para intercambiar mensajes en tiempo real. Utiliza Java puro y TCP/IP a través de la clase `Socket`, con una interfaz gráfica construida usando **Swing**.

## 🛠️ Tecnologías

- **Lenguaje**: Java
- **Red**: Sockets TCP/IP
- **GUI**: Swing (Interfaz gráfica en `ChatPanel`, `Cliente`)
- **IDE sugerido**: [NetBeans](https://netbeans.apache.org/)
- **Sistema de construcción**: Ant (`build.xml`)

## 📁 Estructura del Proyecto

Sockets/
├── src/sockets/ # Código fuente
│ ├── ChatPanel.java
│ ├── Cliente.java
│ ├── ManejadorCliente.java
│ └── Servidor.java
├── build/ # Archivos compilados
├── nbproject/ # Configuración de NetBeans
├── build.xml # Script de compilación con Ant
└── manifest.mf # Manifest del JAR

markdown
Copiar
Editar

## 🚀 Cómo ejecutar

1. Abre el proyecto en NetBeans o compílalo manualmente con Ant.
2. Ejecuta primero el archivo `Servidor.java` para levantar el servidor.
3. Ejecuta uno o más clientes (`Cliente.java`) para conectarse al servidor.
4. Escribe y recibe mensajes a través de la interfaz gráfica.


## 👤 Autor

- **Hameem Afnan** – Proyecto académico de la asignatura Servicios y Procesos.

## 📝 Licencia

Este proyecto se distribuye con fines educativos. Puedes modificarlo y reutilizarlo bajo los términos que indique el autor.
¿Quieres que este README incluya imágenes, instrucciones para empaqueta
