/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package sockets;

/**
 *
 * @author Ha-Meem
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;


public class Cliente extends javax.swing.JFrame {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean conectado = false;
    private String nombreUsuario;
    private Thread hiloReceptor;
    private ChatPanel chatPanel;

    public Cliente() {
        initComponents();
        configurarInterfaz();
        configurarEventos();
        actualizarInterfazConexion();
    }

    private void configurarInterfaz() {
        this.setTitle("💬 Chat Client - Desconectado");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.getContentPane().setBackground(new Color(240, 242, 245));
        
        chatPanel = new ChatPanel();
        
        jScrollPane1.setViewportView(chatPanel);
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        jScrollPane1.getViewport().setBackground(new Color(230, 221, 212));
        
        jTextField1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jTextField1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        configurarBoton(jButton1, new Color(0, 123, 255), Color.WHITE);
        configurarBoton(botonConectar, new Color(40, 167, 69), Color.WHITE);
        configurarBoton(botonDesconectar, new Color(220, 53, 69), Color.WHITE);
        
        labelEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        this.setLocationRelativeTo(null);
        
        SwingUtilities.invokeLater(() -> {
            chatPanel.agregarMensaje("=== CLIENTE DE CHAT ===", "Sistema", true);
            chatPanel.agregarMensaje("Presiona 'Conectar' para unirte al chat", "Sistema", true);
        });
    }

    private void configurarBoton(JButton boton, Color fondo, Color texto) {
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = fondo;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (boton.isEnabled()) {
                    boton.setBackground(originalColor.darker());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (boton.isEnabled()) {
                    boton.setBackground(originalColor);
                }
            }
        });
    }

    private void configurarEventos() {
        botonConectar.addActionListener(e -> conectarAlServidor());
        
        botonDesconectar.addActionListener(e -> desconectarDelServidor());
        
        jButton1.addActionListener(e -> enviarMensaje());
        
        jTextField1.addActionListener(e -> enviarMensaje());
        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (conectado) {
                    desconectarDelServidor();
                }
                System.exit(0);
            }
        });
    }

    private void conectarAlServidor() {
        if (conectado) return;
        
        String nombre = JOptionPane.showInputDialog(
            this, 
            "Introduce tu nombre de usuario:", 
            "Conexión al Chat", 
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (nombre == null || nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this, 
                "Debes introducir un nombre para conectarte", 
                "Error", 
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        nombreUsuario = nombre.trim();
        
        try {
            chatPanel.agregarMensaje("🔄 Conectando al servidor...", "Sistema", true);
            
            socket = new Socket("localhost", 5050); 
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println("/nombre " + nombreUsuario);
            
            chatPanel.setUsuarioActual(nombreUsuario);
            
            conectado = true;
            actualizarInterfazConexion();
            
            chatPanel.agregarMensaje("✅ Conectado exitosamente como: " + nombreUsuario, "Sistema", true);
            
            iniciarHiloReceptor();
            
        } catch (IOException e) {
            chatPanel.agregarMensaje("❌ Error al conectar: " + e.getMessage(), "Sistema", true);
            JOptionPane.showMessageDialog(
                this, 
                "No se pudo conectar al servidor.\nVerifica que el servidor esté ejecutándose.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void desconectarDelServidor() {
        if (!conectado) return;
        
        try {
            if (out != null) {
                out.println("/salir");
            }
            
            cerrarConexiones();
            
            conectado = false;
            actualizarInterfazConexion();
            
            chatPanel.agregarMensaje("🔌 Te has desconectado del servidor", "Sistema", true);
            
        } catch (Exception e) {
            chatPanel.agregarMensaje("❌ Error al desconectar: " + e.getMessage(), "Sistema", true);
        }
    }

    private void iniciarHiloReceptor() {
        hiloReceptor = new Thread(() -> {
            try {
                String mensaje;
                while (conectado && (mensaje = in.readLine()) != null) {
                    final String mensajeFinal = mensaje;
                    
                    SwingUtilities.invokeLater(() -> {
                        if (mensajeFinal.startsWith("[") && mensajeFinal.contains("]:")) {
                            int finNombre = mensajeFinal.indexOf("]:");
                            if (finNombre > 0) {
                                String remitente = mensajeFinal.substring(1, finNombre);
                                String textoMensaje = mensajeFinal.substring(finNombre + 2).trim();
                                
                                chatPanel.agregarMensaje(textoMensaje, remitente, false);
                            } else {
                                chatPanel.agregarMensaje(mensajeFinal, "Sistema", true);
                            }
                        } else {
                            chatPanel.agregarMensaje(mensajeFinal, "Sistema", true);
                        }
                        
                        if (mensajeFinal.contains("se ha unido al chat") || 
                            mensajeFinal.contains("ha abandonado el chat")) {
                            actualizarContadorUsuarios(mensajeFinal);
                        }
                    });
                }
            } catch (IOException e) {
                if (conectado) {
                    SwingUtilities.invokeLater(() -> {
                        chatPanel.agregarMensaje("❌ Conexión perdida con el servidor", "Sistema", true);
                        conectado = false;
                        actualizarInterfazConexion();
                    });
                }
            }
        });
        
        hiloReceptor.setName("HiloReceptor-" + nombreUsuario);
        hiloReceptor.setDaemon(true);
        hiloReceptor.start();
    }
    
        private int contadorUsuarios = 0;

        private void actualizarContadorUsuarios(String mensaje) {
            if (mensaje.contains("se ha unido al chat")) {
                contadorUsuarios++;
            } else if (mensaje.contains("ha abandonado el chat")) {
                contadorUsuarios = Math.max(0, contadorUsuarios - 1);
            }
            SwingUtilities.invokeLater(() -> {
                labelUsuarios.setText("Usuarios: " + contadorUsuarios);
            });
        }

    private void enviarMensaje() {
        if (!conectado || out == null) {
            return;
        }
        
        String mensaje = jTextField1.getText().trim();
        if (mensaje.isEmpty()) {
            return;
        }
        
        if (!mensaje.startsWith("/")) {
            chatPanel.agregarMensaje(mensaje, nombreUsuario, false);
        }
        
        out.println(mensaje);
        
        jTextField1.setText("");
        jTextField1.requestFocus();
        
        if (mensaje.equals("/ayuda")) {
            mostrarAyudaLocal();
        }
    }

    private void mostrarAyudaLocal() {
        SwingUtilities.invokeLater(() -> {
            chatPanel.agregarMensaje("📋 === COMANDOS DISPONIBLES ===", "Sistema", true);
            chatPanel.agregarMensaje("Los comandos se envían al servidor:", "Sistema", true);
            chatPanel.agregarMensaje("/ayuda - Ver comandos disponibles", "Sistema", true);
            chatPanel.agregarMensaje("/usuarios - Ver usuarios conectados", "Sistema", true);
            chatPanel.agregarMensaje("/privado [usuario] [mensaje] - Mensaje privado", "Sistema", true);
            chatPanel.agregarMensaje("/salir - Desconectarse del chat", "Sistema", true);
        });
    }

    private void actualizarInterfazConexion() {
        SwingUtilities.invokeLater(() -> {
            if (conectado) {
                labelEstado.setText("🟢 Conectado");
                labelEstado.setForeground(new Color(40, 167, 69));
                this.setTitle("💬 Chat Client - Conectado como: " + nombreUsuario);
                
                botonConectar.setEnabled(false);
                botonDesconectar.setEnabled(true);
                jTextField1.setEnabled(true);
                jButton1.setEnabled(true);
                
                jTextField1.requestFocus();
                
            } else {
                labelEstado.setText("🔴 Desconectado");
                labelEstado.setForeground(new Color(220, 53, 69));
                labelUsuarios.setText("Usuario: 0");
                this.setTitle("💬 Chat Client - Desconectado");
                
                botonConectar.setEnabled(true);
                botonDesconectar.setEnabled(false);
                jTextField1.setEnabled(false);
                jButton1.setEnabled(false);
                
                botonConectar.requestFocus();
            }
        });
    }

    private void cerrarConexiones() {
        try {
            if (hiloReceptor != null && hiloReceptor.isAlive()) {
                hiloReceptor.interrupt();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando conexiones: " + e.getMessage());
        }
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        labelEstado = new javax.swing.JLabel();
        labelUsuarios = new javax.swing.JLabel();
        botonConectar = new javax.swing.JButton();
        botonDesconectar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton1.setText("Enviar");

        labelEstado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        labelEstado.setForeground(new java.awt.Color(255, 0, 51));
        labelEstado.setText("● Desconectado");

        labelUsuarios.setForeground(new java.awt.Color(51, 153, 255));
        labelUsuarios.setText("Usuario: 0");

        botonConectar.setText("Conectar");

        botonDesconectar.setText("Desconectar");
        botonDesconectar.setEnabled(false);

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                        .addComponent(botonConectar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botonDesconectar)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelUsuarios)
                    .addComponent(botonConectar)
                    .addComponent(botonDesconectar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
// Configuración inicial
    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        enviarMensaje();// TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    /**
     * @param args the command line arguments
     */
public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Cliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Cliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Cliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Cliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Cliente().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonConectar;
    private javax.swing.JButton botonDesconectar;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel labelEstado;
    private javax.swing.JLabel labelUsuarios;
    // End of variables declaration//GEN-END:variables
}
