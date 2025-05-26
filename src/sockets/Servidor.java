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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Servidor {
    
    private static final int PUERTO = 5050;
    private static ServerSocket servidor;
    private static boolean servidorActivo = true;
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("🚀 SERVIDOR DE CHAT INICIANDO...");
        System.out.println("=================================");
        
        try {
            servidor = new ServerSocket(PUERTO);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            
            System.out.println("✅ Servidor iniciado exitosamente");
            System.out.println("📅 Fecha y hora: " + timestamp);
            System.out.println("🌐 Puerto: " + PUERTO);
            System.out.println("💻 Dirección: localhost:" + PUERTO);
            System.out.println("=================================");
            System.out.println("Comandos disponibles:");
            System.out.println("  'estado'  - Ver estadísticas del servidor");
            System.out.println("  'usuarios' - Ver usuarios conectados");
            System.out.println("  'salir'   - Cerrar servidor");
            System.out.println("=================================");
            
            Thread comandosThread = new Thread(Servidor::procesarComandosServidor);
            comandosThread.setDaemon(true);
            comandosThread.start();
            
            Thread estadisticasThread = new Thread(Servidor::mostrarEstadisticasPeriodicas);
            estadisticasThread.setDaemon(true);
            estadisticasThread.start();
            
            while (servidorActivo) {
                try {
                    Socket cliente = servidor.accept();
                    String clienteIP = cliente.getInetAddress().getHostAddress();
                    String timestampConexion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    
                    System.out.println("🔗 [" + timestampConexion + "] Nueva conexión desde: " + clienteIP);
                    
                    Thread hiloCliente = new Thread(new ManejadorCliente(cliente));
                    hiloCliente.setName("Cliente-" + clienteIP);
                    hiloCliente.start();
                    
                } catch (IOException e) {
                    if (servidorActivo) {
                        System.err.println("❌ Error aceptando conexión: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error iniciando servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarServidor();
        }
    }
    
    private static void procesarComandosServidor() {
        Scanner scanner = new Scanner(System.in);
        
        while (servidorActivo) {
            try {
                String comando = scanner.nextLine().toLowerCase().trim();
                
                switch (comando) {
                    case "estado":
                        mostrarEstadoServidor();
                        break;
                        
                    case "usuarios":
                        mostrarUsuariosConectados();
                        break;
                        
                    case "salir":
                        System.out.println("🛑 Cerrando servidor...");
                        servidorActivo = false;
                        if (servidor != null && !servidor.isClosed()) {
                            servidor.close();
                        }
                        System.exit(0);
                        break;
                        
                    case "ayuda":
                        mostrarAyudaServidor();
                        break;
                        
                    default:
                        if (!comando.isEmpty()) {
                            System.out.println("❌ Comando no reconocido: '" + comando + "'");
                            System.out.println("💡 Escribe 'ayuda' para ver comandos disponibles");
                        }
                }
            } catch (Exception e) {
            }
        }
    }
    
    private static void mostrarEstadoServidor() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        int usuariosConectados = ManejadorCliente .getUsuariosConectados();
        
        System.out.println("\n📊 === ESTADO DEL SERVIDOR ===");
        System.out.println("⏰ Fecha y hora: " + timestamp);
        System.out.println("🌐 Puerto: " + PUERTO);
        System.out.println("👥 Usuarios conectados: " + usuariosConectados);
        System.out.println("🔄 Estado: " + (servidorActivo ? "Activo" : "Inactivo"));
        System.out.println("💾 Memoria libre: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB");
        System.out.println("===============================\n");
    }
    
    private static void mostrarUsuariosConectados() {
        var usuarios = ManejadorCliente.getNombresUsuarios();
        
        System.out.println("\n👥 === USUARIOS CONECTADOS ===");
        if (usuarios.isEmpty()) {
            System.out.println("   No hay usuarios conectados");
        } else {
            System.out.println("   Total: " + usuarios.size() + " usuarios");
            for (String usuario : usuarios) {
                System.out.println("   🟢 " + usuario);
            }
        }
        System.out.println("==============================\n");
    }
    
    private static void mostrarAyudaServidor() {
        System.out.println("\n📋 === COMANDOS DEL SERVIDOR ===");
        System.out.println("   estado   - Mostrar estadísticas del servidor");
        System.out.println("   usuarios - Ver lista de usuarios conectados");
        System.out.println("   ayuda    - Mostrar esta ayuda");
        System.out.println("   salir    - Cerrar el servidor");
        System.out.println("=================================\n");
    }
    
    private static void mostrarEstadisticasPeriodicas() {
        while (servidorActivo) {
            try {
                Thread.sleep(300000); 
                if (servidorActivo) {
                    int usuarios = ManejadorCliente.getUsuariosConectados();
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    System.out.println("📊 [" + timestamp + "] Usuarios conectados: " + usuarios);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private static void cerrarServidor() {
        try {
            if (servidor != null && !servidor.isClosed()) {
                servidor.close();
                System.out.println("✅ Servidor cerrado correctamente");
            }
        } catch (IOException e) {
            System.err.println("❌ Error cerrando servidor: " + e.getMessage());
        }
    }
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Cerrando servidor...");
            servidorActivo = false;
            cerrarServidor();
        }));
    }
}