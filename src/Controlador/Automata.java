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
    public void setInicial(Estado e) {
        Estado xd = this.estados.get(e.getnombre()); //se busca si existe
        if (xd == null) { //si no lo creamos y lo metemos
            xd = new Estado(e.getnombre());
            this.estados.put(e.getnombre(), e);
        }
        // y se hace inciial
        this.estadoInicial = e;

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
