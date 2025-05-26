/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package sockets;

/**
 *
 * @author Ha-Meem
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManejadorCliente implements Runnable {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nombre;
    private static int contadorUsuarios = 0;
    
    private static Map<String, PrintWriter> clientes = Collections.synchronizedMap(new HashMap<>());
    
    private static final String[] COMANDOS_AYUDA = {
        "/ayuda - Mostrar comandos disponibles",
        "/usuarios - Ver usuarios conectados", 
        "/privado [usuario] [mensaje] - Enviar mensaje privado",
        "/salir - Desconectarse del chat"
    };
    
    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String lineaInicial = in.readLine();
            if (lineaInicial != null && lineaInicial.startsWith("/nombre ")) {
                nombre = lineaInicial.substring(8).trim();
                
                if (clientes.containsKey(nombre)) {
                    nombre = nombre + "_" + System.currentTimeMillis() % 1000;
                }
            } else {
                nombre = "Usuario" + (contadorUsuarios++);
            }

            clientes.put(nombre, out);
            
            out.println("¡Bienvenido al chat, " + nombre + "! 🎉");
            out.println("Escribe /ayuda para ver los comandos disponibles");
            
            String mensajeUnion = "✅ " + nombre + " se ha unido al chat";
            enviarATodos(mensajeUnion, nombre);
            
            mostrarUsuariosConectados();
            
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                procesarMensaje(mensaje.trim());
            }

        } catch (IOException e) {
            System.out.println("Error en cliente " + nombre + ": " + e.getMessage());
        } finally {
            desconectarCliente();
        }
    }
    
    private void procesarMensaje(String mensaje) {
        if (mensaje.isEmpty()) return;
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + timestamp + "] [" + nombre + "]: " + mensaje);
        
        if (mensaje.startsWith("/")) {
            procesarComando(mensaje);
        } else {
            String mensajeFormateado = "[" + nombre + "]: " + mensaje;
            enviarATodos(mensajeFormateado, nombre);
        }
    }
    
    private void procesarComando(String comando) {
        String[] partes = comando.split(" ", 3);
        String cmd = partes[0].toLowerCase();
        
        switch (cmd) {
            case "/ayuda":
                mostrarAyuda();
                break;
                
            case "/usuarios":
                mostrarUsuariosConectados();
                break;
                
            case "/privado":
                if (partes.length >= 3) {
                    enviarMensajePrivado(partes[1], partes[2]);
                } else {
                    out.println("❌ Uso: /privado [usuario] [mensaje]");
                }
                break;
                
            case "/salir":
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
                
            default:
                out.println("❌ Comando no reconocido. Escribe /ayuda para ver comandos disponibles.");
        }
    }
    
    private void mostrarAyuda() {
        out.println("📋 === COMANDOS DISPONIBLES ===");
        for (String ayuda : COMANDOS_AYUDA) {
            out.println("   " + ayuda);
        }
        out.println("===============================");
    }
    
    private void mostrarUsuariosConectados() {
        synchronized (clientes) {
            out.println("👥 === USUARIOS CONECTADOS (" + clientes.size() + ") ===");
            for (String usuario : clientes.keySet()) {
                if (usuario.equals(nombre)) {
                    out.println("   🟢 " + usuario + " (tú)");
                } else {
                    out.println("   🟢 " + usuario);
                }
            }
            out.println("=============================");
        }
    }
    
    private void enviarMensajePrivado(String destinatario, String mensaje) {
        PrintWriter destinatarioOut = clientes.get(destinatario);
        
        if (destinatarioOut != null) {
            destinatarioOut.println("💬 [PRIVADO de " + nombre + "]: " + mensaje);
            out.println("💬 [PRIVADO para " + destinatario + "]: " + mensaje);
        } else {
            out.println("❌ Usuario '" + destinatario + "' no encontrado");
        }
    }
    
    private void enviarATodos(String mensaje, String remitente) {
        synchronized (clientes) {
            for (Map.Entry<String, PrintWriter> entry : clientes.entrySet()) {
                String usuario = entry.getKey();
                PrintWriter cliente = entry.getValue();
                
                if (!usuario.equals(remitente) || remitente == null) {
                    cliente.println(mensaje);
                }
            }
        }
    }
    
    private void desconectarCliente() {
        if (nombre != null && clientes.containsKey(nombre)) {
            clientes.remove(nombre);
            
            String mensajeSalida = "❌ " + nombre + " ha abandonado el chat";
            enviarATodos(mensajeSalida, null);
            
            System.out.println("Cliente desconectado: " + nombre);
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static int getUsuariosConectados() {
        return clientes.size();
    }
    
    public static Set<String> getNombresUsuarios() {
        synchronized (clientes) {
            return new HashSet<>(clientes.keySet());
        }
    }
}