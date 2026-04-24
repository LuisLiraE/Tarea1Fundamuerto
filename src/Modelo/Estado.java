package Modelo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Estado {
    private String nombre;
    private boolean esFinal;
    private HashMap<String, Set<Estado>> Direccion;

    public Estado(String nombre) {
        this.nombre = nombre;
        this.Direccion = new HashMap<>();
        this.esFinal = false;
    }
    public void AgregarTransicion(String letra, Estado destino) {
        // ver si existe la posicion
        if(!Direccion.containsKey(letra)) {
            //sino existe se crea
            Direccion.put(letra,new HashSet<Estado>());
        }
        //se agrega
        Direccion.get(letra).add(destino);
    }

    public String getnombre() {
        return nombre;
    }

    public boolean isesFinal() {
        return esFinal;
    }

    public Set<Estado> getDestino(String letra) {
        return Direccion.getOrDefault(letra,new HashSet<>());
    }

    public HashMap<String, Set<Estado>> getDireccion() {
        return Direccion;
    }

    public void setEsFinal(boolean esFinal) {
        this.esFinal = esFinal;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
