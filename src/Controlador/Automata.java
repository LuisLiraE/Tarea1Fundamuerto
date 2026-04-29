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
        // se busca el objetito
        Estado e = this.estados.get(xd);
        if (e != null) {
            // lo cambiamos a final por dentro
            e.setEsFinal(true);

            // se agrega a la lista de finales
            this.estadosFinales.put(xd, e);
        }
        // nose que hacer si null jsjsj, por ahora...
    }

    public void conectar(String origenNombre, String simbolo, String destinoNombre) {
        // Aseguramos que existan
        AgregarEstado(origenNombre);
        AgregarEstado(destinoNombre);

        Estado origen = estados.get(origenNombre);
        Estado destino = estados.get(destinoNombre);

        // ver si es AFND:
        // Si el símbolo es lambda (usaremos "!" como convención)
        if (simbolo.equals("!") || simbolo.equals("lambda")) {
            this.esAFND = true;
        }
        // Si el origen ya tiene destinos para ese símbolo (ya existía la llave)
        if (!origen.getDestino(simbolo).isEmpty()) {
            this.esAFND = true;
        }

        // Realizamos la conexión
        origen.AgregarTransicion(simbolo, destino);

        // Guardamos el símbolo en el alfabeto (si no es lambda)
        if (!simbolo.equals("!") && !simbolo.equals("lambda")) {
            this.alfabeto.add(simbolo);
        }
    }
    public void agregarAlfabeto(String simbolo) {
        // agregado simple
        this.alfabeto.add(simbolo);
    }

    public Set<Estado> clausuraLambda(Set<Estado> estados) {
        Set<Estado> clausura = new HashSet<>(estados);
        boolean cambios = true;
        while (cambios) {
            int tamanoInicial = clausura.size();
            Set<Estado> nuevos = new HashSet<>();
            for (Estado e : clausura) {
                nuevos.addAll(e.getDestino("!"));
                nuevos.addAll(e.getDestino("lambda"));
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

            // Después de cada símbolo, calculamos la clausura lambda
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
        dot.append("    rankdir=LR;\n"); // Para que el dibujo sea de izquierda a derecha
        dot.append("    node [shape = point]; start;\n"); // La flechita de inicio

        // 1. Dibujar el estado inicial
        if (estadoInicial != null) {
            dot.append("    start -> ").append(estadoInicial.getnombre()).append(";\n");
        }

        // 2. Marcar los estados finales con doble círculo
        dot.append("    node [shape = doublecircle];");
        for (Estado f : estadosFinales.values()) {
            dot.append(" ").append(f.getnombre());
        }
        dot.append(";\n");

        // 3. Los demás estados son círculos normales
        dot.append("    node [shape = circle];\n");

        // 4. Recorrer todas las transiciones
        for (Estado origen : estados.values()) {
            // Obtenemos el mapa de transiciones del estado
            for (String simbolo : origen.getDireccion().keySet()) {
                for (Estado destino : origen.getDestino(simbolo)) {
                    dot.append("    ")
                            .append(origen.getnombre())
                            .append(" -> ")
                            .append(destino.getnombre())
                            .append(" [label = \"")
                            .append(simbolo)
                            .append("\"];\n");
                }
            }
        }

        dot.append("}");
        return dot.toString();
    }
    // los metodo de Consulta
    public boolean isEsAFND() {
        return this.esAFND;
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






}
