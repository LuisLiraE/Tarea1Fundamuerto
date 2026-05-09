package Controlador;

import Modelo.Estado;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Automata {
    private HashMap<String, Estado> estados;
    private Set<String> alfabeto;
    private Estado estadoInicial;
    private HashMap<String, Estado> estadosFinales;
    private boolean esAFND;

    public Automata() {
        this.estados = new HashMap<>();
        this.alfabeto = new HashSet<>();
        this.estadosFinales = new HashMap<>();
        this.esAFND = false;
    }
    public void AgregarEstado(String e) {
        if (!this.estados.containsKey(e)) {
            estados.put(e, new Estado(e));
        }
    }
    public void setInicial(String e) {
        Estado xd = this.estados.get(e); //se busca si existe
        if (xd == null) { //si no lo creamos y lo metemos donde estan todos
            xd = new Estado(e);
            this.estados.put(e, xd);
        }
        // y se hace inciial
        this.estadoInicial = xd;

    }
    public void MarcarEstadoFinal(String xd) {
        // Si no existe el estado, lo creamos primero
        if (!this.estados.containsKey(xd)) {
            this.estados.put(xd, new Estado(xd));
        }
        Estado e = this.estados.get(xd);
        // lo cambiamos a final por dentro
        e.setEsFinal(true);
        // se agrega a la lista de finales
        this.estadosFinales.put(xd, e);
    }

    public void conectar(String origenNombre, String simbolo, String destinoNombre) {
        // Aseguramos que existan
        AgregarEstado(origenNombre);
        AgregarEstado(destinoNombre);

        Estado origen = estados.get(origenNombre);
        Estado destino = estados.get(destinoNombre);

        // ver si es AFND:
        // Si el símbolo es lambda (usaremos "!", "lambda" o "eps" como convención)
        if (simbolo.equals("!") || simbolo.equals("lambda") || simbolo.equals("eps")) {
            this.esAFND = true;
        }
        
        // Si el origen ya tiene destinos para ese símbolo, al agregar uno nuevo será AFND
        if (origen.getDestino(simbolo).size() >= 1) {
            // Pero solo si el destino que vamos a agregar no es el mismo (para evitar duplicados simples)
            // (La mayoría de los AFND permiten múltiples transiciones al mismo estado, pero aquí 
            // nos interesa si hay NO determinismo)
            this.esAFND = true;
        }

        // Realizamos la conexión
        origen.AgregarTransicion(simbolo, destino);

        // Guardamos el símbolo en el alfabeto (si no es lambda)
        if (!simbolo.equals("!") && !simbolo.equals("lambda") && !simbolo.equals("eps")) {
            this.alfabeto.add(simbolo);
        }
    }
    public void agregarAlfabeto(String simbolo) {
        // No agregar epsilon al alfabeto real (es una convención de transición vacía, no un símbolo)
        if (!simbolo.equals("!") && !simbolo.equals("lambda") && !simbolo.equals("eps")) {
            this.alfabeto.add(simbolo);
        }
    }

    public Set<Estado> clausuraLambda(Set<Estado> estados) {
        // este metodo es para ver por donde se puede llegar a pasar desde cierto estado mediante los lambda
        Set<Estado> clausura = new HashSet<>(estados);
        boolean cambios = true;
        while (cambios) {
            int tamanoInicial = clausura.size();
            Set<Estado> nuevos = new HashSet<>();
            for (Estado e : clausura) {
                nuevos.addAll(e.getDestino("!"));
                nuevos.addAll(e.getDestino("lambda"));
                nuevos.addAll(e.getDestino("eps"));
            }
            clausura.addAll(nuevos);
            if (clausura.size() == tamanoInicial) cambios = false;
        }
        return clausura;
    }

    public boolean validarCadena(String cadena) {
        if (this.estadoInicial == null) return false;

        Set<Estado> estadosActuales = new HashSet<>();
        estadosActuales.add(this.estadoInicial);
        
        // Aplicar clausura lambda inicial
        estadosActuales = clausuraLambda(estadosActuales);

        for (int i = 0; i < cadena.length(); i++) {
            String simbolo = String.valueOf(cadena.charAt(i));
            Set<Estado> proximosEstados = new HashSet<>();

            for (Estado e : estadosActuales) {
                proximosEstados.addAll(e.getDestino(simbolo));
            }

            if (proximosEstados.isEmpty()) return false;

            // Después de cada símbolo calculamos la clausura lambda para ver lo estados alcanzables
            estadosActuales = clausuraLambda(proximosEstados);
        }

        for (Estado e : estadosActuales) {
            if (e.isesFinal()) return true;
        }

        return false;
    }

    public String generarDOT() {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("    rankdir=LR;\n");

        // 1. Definir la flecha de inicio (invisible para que parezca que viene de la nada)
        dot.append("    node [shape = none, label=\"\"]; start;\n");

        // 2. Definir estados finales (con doble círculo y su nombre)
        dot.append("    node [shape = doublecircle, label=\"\\N\"];\n");
        for (Estado f : estadosFinales.values()) {
            dot.append("    \"").append(f.getnombre()).append("\";\n");
        }

        // 3. Definir los demás estados (círculos normales con su nombre)
        dot.append("    node [shape = circle];\n");
        for (Estado e : estados.values()) {
            if (!e.isesFinal()) {
                dot.append("    \"").append(e.getnombre()).append("\";\n");
            }
        }

        // 4. Flecha de entrada al inicial
        if (estadoInicial != null) {
            dot.append("    start -> \"").append(estadoInicial.getnombre()).append("\";\n");
        }

        // 5. Transiciones con etiquetas limpias
        for (Estado origen : estados.values()) {
            for (String simbolo : origen.getDireccion().keySet()) {
                // Usamos "eps" para representar transiciones vacías (cubriendo todas las convenciones)
                String label = (simbolo.equals("!") || simbolo.equals("lambda") || simbolo.equals("eps")) ? "eps" : simbolo;

                for (Estado destino : origen.getDestino(simbolo)) {
                    dot.append("    \"").append(origen.getnombre()).append("\" -> \"")
                            .append(destino.getnombre()).append("\" [label = \"")
                            .append(label).append("\"];\n");
                }
            }
        }

        dot.append("}");
        return dot.toString();
    }

    public void dibujar(String nombre) {
        try {
            // Usamos un nombre de archivo que incluya el nombre sugerido
            String dotPath = nombre + ".dot";
            String pngPath = nombre + ".png";
            
            // Guardar el contenido DOT en un archivo
            java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(dotPath));
            pw.print(generarDOT());
            pw.close();

            // Ejecutar el comando de Graphviz
            // -Gdpi=300 es para mejor calidad si lo deseas
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotPath, "-o", pngPath);
            Process process = pb.start();
            
            // Esperar un poco a que termine el proceso
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Imagen generada con éxito: " + new java.io.File(pngPath).getAbsolutePath());
                // Intentar abrir la imagen
                try {
                    ProcessBuilder openPb = new ProcessBuilder("cmd", "/c", "start", pngPath);
                    openPb.start();
                } catch (Exception e) {
                    System.err.println("No se pudo abrir la imagen automáticamente: " + e.getMessage());
                }
            } else {
                System.err.println("Graphviz devolvió un error (Código: " + exitCode + "). Verifique que el archivo no esté abierto en otro programa.");
            }
            
            // Eliminar el temporal .dot si todo salió bien
            if (exitCode == 0) {
                new java.io.File(dotPath).delete();
            }

        } catch (Exception e) {
            System.err.println("Error en el proceso de dibujo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // los metodo de Consulta
    public boolean isEsAFND() {
        return this.esAFND;
    }

    public void setEsAFND(boolean esAFND) {
        this.esAFND = esAFND;
    }
    public Estado getEstado(String xd) {
        return estados.get(xd);
    }
    public Set<String> getAlfabeto() {
        return alfabeto;
    }
    public HashMap<String, Estado> getEstados() {
        return estados;
    }
    public Estado getEstadoInicial() {return this.estadoInicial;}





}
