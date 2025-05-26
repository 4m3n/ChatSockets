/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package sockets;

/**
 *
 * @author Ha-Meem
 */

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;


public class ChatPanel extends JPanel implements Scrollable {
    private List<Mensaje> mensajes;
    private String usuarioActual;
    private static final int PADDING = 10;
    private static final int BUBBLE_PADDING = 12;
    private static final int MAX_WIDTH = 300;
    
    private static final Color COLOR_MENSAJE_PROPIO = new Color(220, 248, 198); 
    private static final Color COLOR_MENSAJE_OTROS = Color.WHITE;
    private static final Color COLOR_TEXTO = new Color(33, 33, 33);
    private static final Color COLOR_HORA = new Color(115, 115, 115);
    private static final Color COLOR_SISTEMA = new Color(142, 142, 147);
    private static final Color COLOR_FONDO_SISTEMA = new Color(245, 245, 245);
    
    public ChatPanel() {
        this.mensajes = new ArrayList<>();
        this.setBackground(new Color(230, 221, 212)); 
    }
    
    public void setUsuarioActual(String usuario) {
        this.usuarioActual = usuario;
    }
    
    public void agregarMensaje(String texto, String remitente, boolean esSistema) {
        Mensaje mensaje = new Mensaje(texto, remitente, esSistema);
        mensajes.add(mensaje);
        repaint();
        
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
                scrollPane.revalidate();
                scrollPane.repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = PADDING;
        int anchoDisponible = getWidth(); 

        int alturaTotal = calcularAlturaTotal(g2d);
        setPreferredSize(new Dimension(anchoDisponible, Math.max(alturaTotal, getParent().getHeight())));

        for (Mensaje mensaje : mensajes) {
            if (mensaje.esSistema) {
                y += dibujarMensajeSistema(g2d, mensaje, y, anchoDisponible);
            } else {
                y += dibujarBurbuja(g2d, mensaje, y, anchoDisponible);
            }
            y += 8;
        }

        revalidate();
        g2d.dispose();
    }

        private int calcularAlturaTotal(Graphics2D g2d) {
            int altura = PADDING;
            int anchoDisponible = getWidth();

            for (Mensaje mensaje : mensajes) {
                if (mensaje.esSistema) {
                    altura += calcularAlturaMensajeSistema(g2d, mensaje, anchoDisponible); 
                } else {
                    altura += calcularAlturaBurbuja(g2d, mensaje, anchoDisponible);
                }
                altura += 8;
            }
            return altura + PADDING;
        }
    
        private int dibujarMensajeSistema(Graphics2D g2d, Mensaje mensaje, int y, int anchoDisponible) {
            Font font = new Font("Segoe UI", Font.ITALIC, 11);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();

            String[] lineas = dividirTexto(mensaje.texto, fm, anchoDisponible - 100);
            int alturaTotal = lineas.length * fm.getHeight() + 10;

            int anchoTexto = 0;
            for (String linea : lineas) {
                anchoTexto = Math.max(anchoTexto, fm.stringWidth(linea));
            }

            int x = (anchoDisponible - anchoTexto - 20) / 2;

            g2d.setColor(COLOR_FONDO_SISTEMA);
            RoundRectangle2D fondo = new RoundRectangle2D.Float(x, y, anchoTexto + 20, alturaTotal, 15, 15);
            g2d.fill(fondo);

            g2d.setColor(COLOR_SISTEMA);
            int yTexto = y + fm.getAscent() + 5;
            for (String linea : lineas) {
                g2d.drawString(linea, x + 10, yTexto);
                yTexto += fm.getHeight();
            }

            return alturaTotal + 5;
        }
    
    
    
        private int calcularAlturaMensajeSistema(Graphics2D g2d, Mensaje mensaje, int anchoDisponible) {
            Font font = new Font("Segoe UI", Font.ITALIC, 11);
            FontMetrics fm = g2d.getFontMetrics(font);
            String[] lineas = dividirTexto(mensaje.texto, fm, anchoDisponible - 100);
            return lineas.length * fm.getHeight() + 20;
        }
    
    private int dibujarBurbuja(Graphics2D g2d, Mensaje mensaje, int y, int anchoDisponible) {
        boolean esMensajePropio = mensaje.remitente.equals(usuarioActual);
        
        Font fontTexto = new Font("Segoe UI", Font.PLAIN, 13);
        Font fontHora = new Font("Segoe UI", Font.PLAIN, 10);
        Font fontNombre = new Font("Segoe UI", Font.BOLD, 11);
        
        FontMetrics fmTexto = g2d.getFontMetrics(fontTexto);
        FontMetrics fmHora = g2d.getFontMetrics(fontHora);
        FontMetrics fmNombre = g2d.getFontMetrics(fontNombre);
        
        String[] lineas = dividirTexto(mensaje.texto, fmTexto, MAX_WIDTH - BUBBLE_PADDING * 2);
        
        int anchoTexto = 0;
        for (String linea : lineas) {
            anchoTexto = Math.max(anchoTexto, fmTexto.stringWidth(linea));
        }
        
        String horaStr = mensaje.hora.format(DateTimeFormatter.ofPattern("HH:mm"));
        int anchoHora = fmHora.stringWidth(horaStr);
        int anchoNombre = esMensajePropio ? 0 : fmNombre.stringWidth(mensaje.remitente);
        
        int anchoBurbuja = Math.max(Math.max(anchoTexto, anchoHora), anchoNombre) + BUBBLE_PADDING * 2;
        anchoBurbuja = Math.min(anchoBurbuja, MAX_WIDTH);
        
        int alturaNombre = esMensajePropio ? 0 : fmNombre.getHeight() + 2;
        int alturaTexto = lineas.length * fmTexto.getHeight();
        int alturaHora = fmHora.getHeight();
        int alturaBurbuja = alturaNombre + alturaTexto + alturaHora + BUBBLE_PADDING * 2 + 5;
        
        int xBurbuja = esMensajePropio ? 
            anchoDisponible - anchoBurbuja - PADDING : 
            PADDING;
        
        Color colorBurbuja = esMensajePropio ? COLOR_MENSAJE_PROPIO : COLOR_MENSAJE_OTROS;
        g2d.setColor(colorBurbuja);
        RoundRectangle2D burbuja = new RoundRectangle2D.Float(
            xBurbuja, y, anchoBurbuja, alturaBurbuja, 18, 18
        );
        g2d.fill(burbuja);
        
        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.draw(new RoundRectangle2D.Float(
            xBurbuja + 1, y + 1, anchoBurbuja, alturaBurbuja, 18, 18
        ));
        
        int yContenido = y + BUBBLE_PADDING;
        
        if (!esMensajePropio) {
            g2d.setFont(fontNombre);
            g2d.setColor(new Color(0, 150, 255));
            g2d.drawString(mensaje.remitente, xBurbuja + BUBBLE_PADDING, yContenido + fmNombre.getAscent());
            yContenido += alturaNombre;
        }
        
        g2d.setFont(fontTexto);
        g2d.setColor(COLOR_TEXTO);
        for (String linea : lineas) {
            g2d.drawString(linea, xBurbuja + BUBBLE_PADDING, yContenido + fmTexto.getAscent());
            yContenido += fmTexto.getHeight();
        }
        
        g2d.setFont(fontHora);
        g2d.setColor(COLOR_HORA);
        int xHora = esMensajePropio ? 
            xBurbuja + anchoBurbuja - BUBBLE_PADDING - anchoHora :
            xBurbuja + BUBBLE_PADDING;
        g2d.drawString(horaStr, xHora, yContenido + fmHora.getAscent() + 3);
        
        return alturaBurbuja;
    }
    
    private int calcularAlturaBurbuja(Graphics2D g2d, Mensaje mensaje, int anchoDisponible) {
        boolean esMensajePropio = mensaje.remitente.equals(usuarioActual);
        Font fontTexto = new Font("Segoe UI", Font.PLAIN, 13);
        Font fontHora = new Font("Segoe UI", Font.PLAIN, 10);
        Font fontNombre = new Font("Segoe UI", Font.BOLD, 11);
        
        FontMetrics fmTexto = g2d.getFontMetrics(fontTexto);
        FontMetrics fmHora = g2d.getFontMetrics(fontHora);
        FontMetrics fmNombre = g2d.getFontMetrics(fontNombre);
        
        String[] lineas = dividirTexto(mensaje.texto, fmTexto, MAX_WIDTH - BUBBLE_PADDING * 2);
        
        int alturaNombre = esMensajePropio ? 0 : fmNombre.getHeight() + 2;
        int alturaTexto = lineas.length * fmTexto.getHeight();
        int alturaHora = fmHora.getHeight();
        
        return alturaNombre + alturaTexto + alturaHora + BUBBLE_PADDING * 2 + 5;
    }
    
    private String[] dividirTexto(String texto, FontMetrics fm, int maxWidth) {
        List<String> lineas = new ArrayList<>();
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();
        
        for (String palabra : palabras) {
            String pruebaLinea = lineaActual.length() == 0 ? palabra : lineaActual + " " + palabra;
            
            if (fm.stringWidth(pruebaLinea) <= maxWidth) {
                if (lineaActual.length() > 0) {
                    lineaActual.append(" ");
                }
                lineaActual.append(palabra);
            } else {
                if (lineaActual.length() > 0) {
                    lineas.add(lineaActual.toString());
                    lineaActual = new StringBuilder(palabra);
                } else {
                    lineas.add(palabra);
                }
            }
        }
        
        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }
        
        return lineas.toArray(new String[0]);
    }
    
    public void limpiar() {
        mensajes.clear();
        repaint();
    }
    
    private static class Mensaje {
        String texto;
        String remitente;
        LocalTime hora;
        boolean esSistema;
        
        public Mensaje(String texto, String remitente, boolean esSistema) {
            this.texto = texto;
            this.remitente = remitente;
            this.hora = LocalTime.now();
            this.esSistema = esSistema;
        }
    }
    
@Override
public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
}

@Override
public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 16;
}

@Override
public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 50; 
}

@Override
public boolean getScrollableTracksViewportWidth() {
    return true;
}

@Override
public boolean getScrollableTracksViewportHeight() {
    return false;
}
}