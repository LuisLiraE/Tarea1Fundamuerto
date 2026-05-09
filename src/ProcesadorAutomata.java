import Controlador.Automata;
import Modelo.Estado;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.*;

public class ProcesadorAutomata {

    public static Automata convertirAFNDaAFD(Automata afnd) {
        //Si ya es un afd no hacemos nada
        if(!afnd.isEsAFND()) {
            System.out.println("El automata ya es un AFD");
            return afnd;
        }

        //Creamos un nuevo afd y le traspasamos las caracteristicas
        Automata afd =new Automata();
        Set<String>sigma = afnd.getAlfabeto();

        //Copiamos el alfabeto
        for(String s : sigma) {
            afd.agregarAlfabeto(s);
        }

        Set<Estado> conjuntoInicial = new HashSet<Estado>();
        conjuntoInicial.add(afnd.getEstado(obtenerNombreInicial(afnd)));
        conjuntoInicial= afnd.clausuraLambda(conjuntoInicial);

        String nombreInicial= conjuntoANombre(conjuntoInicial);
        afd.setInicial(nombreInicial);

        //Si algun estado inicial es final
        if(tieneEstadoFinal(conjuntoInicial)){
            afd.MarcarEstadoFinal(nombreInicial);
        }

        //conjuntos pendientes por procesar
        Queue<Set<Estado>> pendientes = new LinkedList<>();

        //map para no procesar el mismo conjunto 2 veces
        Map<String, Set<Estado>> procesados = new HashMap<>();

        pendientes.add(conjuntoInicial);
        procesados.put(nombreInicial, conjuntoInicial);

        while(!pendientes.isEmpty()) {
            Set<Estado> conjuntoActual = pendientes.poll();
            String nombreActual = conjuntoANombre(conjuntoActual);

            //para cada simbolo del alfabeto calculamos donde ira este conjunto
            for(String simbolo: sigma){
                // Saltamos epsilon, no es un simbolo real del AFD
                if (simbolo.equals("!") || simbolo.equals("lambda") || simbolo.equals("eps")) continue;

                Set<Estado> mover = new HashSet<>();
                for(Estado e : conjuntoActual){
                    mover.addAll(e.getDestino(simbolo));
                }

                Set<Estado> siguiente = afnd.clausuraLambda(mover);

                if (siguiente.isEmpty()){
                    String trampa = "sumidero";
                    afd.AgregarEstado(trampa);
                    afd.conectar(nombreActual,simbolo,trampa);

                    if(!procesados.containsKey(trampa)){
                        procesados.put(trampa, new HashSet<>());
                        for(String s2 : sigma){
                            if (s2.equals("!") || s2.equals("lambda") || s2.equals("eps")) continue;
                            afd.conectar(trampa, s2 , trampa);
                        }

                    }
                    continue;
                }
                String nombreSiguiente = conjuntoANombre(siguiente);

                afd.conectar(nombreActual,simbolo,nombreSiguiente);

                if(tieneEstadoFinal(siguiente)){
                    afd.MarcarEstadoFinal(nombreSiguiente);
                }

                if(!procesados.containsKey(nombreSiguiente)){
                    procesados.put(nombreSiguiente, siguiente);
                    pendientes.add(siguiente);
                }
            }
        }

        // Renombrar estados del AFD a q0, q1, q2... (el inicial siempre queda como q0)
        return renombrarEstados(afd);
    }

    public static Automata minimizarAFD(Automata afd) {
        // Si es AFND, convertir primero
        if (afd.isEsAFND()) {
            System.out.println("El autómata es AFND, convirtiendo a AFD primero...");
            afd = convertirAFNDaAFD(afd);
        }

        Set<String> sigma = afd.getAlfabeto();
        HashMap<String, Estado> todosEstados = afd.getEstados();

        //Particion inicial: Grupo 0 "Estados finales"  Grupo 1 "Estados no finales"
        // Se excluye el estado trampa de las particiones (no aparece en el resultado)
        Set<String> finales = new HashSet<>();
        Set<String> noFinales = new HashSet<>();

        for(Map.Entry<String, Estado> entry : todosEstados.entrySet()){
            if(entry.getKey().equals("sumidero")) continue; // el sumidero no entra en particiones
            if(entry.getValue().isesFinal()){
                finales.add(entry.getKey());
            }else{
                noFinales.add(entry.getKey());
            }
        }

        //Lista de particiones actuales
        List<Set<String>> particiones = new ArrayList<>();
        if(!finales.isEmpty()){
            particiones.add(finales);
        }
        if(!noFinales.isEmpty()){
            particiones.add(noFinales);
        }

        //refinar particiones
        boolean cambio = true;
        while(cambio){
            cambio = false;
            List<Set<String>> nuevasParticiones = new ArrayList<>();

            for(Set<String> grupo : particiones){
                //dividir ese grupo
                List<Set<String>> subGrupos = dividirGrupo(grupo, particiones, sigma, afd);

                if(subGrupos.size()>1){
                    cambio = true;
                }
                nuevasParticiones.addAll(subGrupos);
            }
            particiones= nuevasParticiones;
        }

        // --- Renombrar particiones a q0, q1, q2... con el inicial siempre como q0 ---
        String nombreInicial = obtenerNombreInicial(afd);

        // Aseguramos que el grupo que contiene el estado inicial quede primero (será q0)
        List<Set<String>> particionesOrdenadas = new ArrayList<>();
        Set<String> grupoInicial = null;
        for (Set<String> grupo : particiones) {
            if (grupo.contains(nombreInicial)) {
                grupoInicial = grupo;
            }
        }
        if (grupoInicial != null) particionesOrdenadas.add(grupoInicial);
        for (Set<String> grupo : particiones) {
            if (grupo != grupoInicial) particionesOrdenadas.add(grupo);
        }

        // Asignar nombre simple a cada partición
        Map<String, String> estadoAgrupos = new HashMap<>();
        int contador = 0;
        Map<Set<String>, String> nombreParticion = new HashMap<>();
        for (Set<String> grupo : particionesOrdenadas) {
            String nombreSimple = "q" + contador++;
            nombreParticion.put(grupo, nombreSimple);
            for (String e : grupo) {
                estadoAgrupos.put(e, nombreSimple);
            }
        }

        Automata minimo = new Automata();

        //copiar el alfabeto
        for(String s : sigma){
            minimo.agregarAlfabeto(s);
        }

        for (Set<String> grupo : particionesOrdenadas) {
            minimo.AgregarEstado(nombreParticion.get(grupo));
        }

        String grupoInicialNombre = estadoAgrupos.get(nombreInicial);
        minimo.setInicial(grupoInicialNombre);

        for (Set<String> grupo : particionesOrdenadas) {
            // Un grupo es final si CUALQUIERA de sus estados era final
            for (String e : grupo) {
                if (afd.getEstado(e) != null && afd.getEstado(e).isesFinal()) {
                    minimo.MarcarEstadoFinal(estadoAgrupos.get(e));
                    break;
                }
            }
        }
        for (Set<String> grupo : particionesOrdenadas) {
            // Tomamos un representante del grupo
            String representante = grupo.iterator().next();
            String nombreOrigen  = estadoAgrupos.get(representante);

            for (String simbolo : sigma) {
                // A dónde va el representante con este símbolo
                Set<Estado> destinos = afd.getEstado(representante).getDestino(simbolo);
                if (!destinos.isEmpty()) {
                    Estado destino = destinos.iterator().next(); // AFD → solo hay uno
                    String nombreDestino = estadoAgrupos.get(destino.getnombre());
                    if (nombreDestino != null) {
                        minimo.conectar(nombreOrigen, simbolo, nombreDestino);
                    }
                }
            }
        }

        return minimo;
    }

    public static boolean sonEquivalentes(Automata a1, Automata a2) {
        // Asegurarse de que ambos sean AFDs
        if (a1.isEsAFND()) {
            System.out.println("AFD 1 es AFND, convirtiendo...");
            a1 = convertirAFNDaAFD(a1);
        }
        if (a2.isEsAFND()) {
            System.out.println("AFD 2 es AFND, convirtiendo...");
            a2 = convertirAFNDaAFD(a2);
        }

        // Verificar que los alfabetos sean iguales
        if (!a1.getAlfabeto().equals(a2.getAlfabeto())) {
            System.out.println("Los autómatas tienen alfabetos distintos → NO son equivalentes.");
            return false;
        }

        Set<String> sigma = a1.getAlfabeto();

        // BFS sobre pares de estados (p, q)
        Queue<String[]> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();

        // Estado inicial del producto
        String inicialA1 = obtenerNombreInicial(a1);
        String inicialA2 = obtenerNombreInicial(a2);

        cola.add(new String[]{inicialA1, inicialA2});
        visitados.add(inicialA1 + "|" + inicialA2);

        while (!cola.isEmpty()) {
            String[] par = cola.poll();
            String p = par[0]; // estado actual en A1
            String q = par[1]; // estado actual en A2

            // Obtener los estados reales (pueden ser null si son TRAMPA)
            Estado ep = a1.getEstado(p);
            Estado eq = a2.getEstado(q);

            boolean pFinal = (ep != null) && ep.isesFinal();
            boolean qFinal = (eq != null) && eq.isesFinal();

            // Si uno acepta y el otro no → distinguibles → NO equivalentes
            if (pFinal != qFinal) {
                System.out.println("Par distinguible encontrado: (" + p + ", " + q + ")");
                System.out.println("  " + p + (pFinal ? " ES" : " NO ES") + " final en AFD1");
                System.out.println("  " + q + (qFinal ? " ES" : " NO ES") + " final en AFD2");
                return false;
            }

            // Explorar los destinos para cada símbolo
            for (String simbolo : sigma) {
                String destP = "Estado trampa";
                String destQ = "Estado trampa";

                if (ep != null) {
                    Set<Estado> ds = ep.getDestino(simbolo);
                    if (!ds.isEmpty()) destP = ds.iterator().next().getnombre();
                }
                if (eq != null) {
                    Set<Estado> ds = eq.getDestino(simbolo);
                    if (!ds.isEmpty()) destQ = ds.iterator().next().getnombre();
                }
                String clave = destP + "|" + destQ;
                if (!visitados.contains(clave)) {
                    visitados.add(clave);
                    cola.add(new String[]{destP, destQ});
                }
            }
        }
        // Ningún par distinguible → equivalentes
        System.out.println("Ningún par distinguible encontrado → los autómatas son EQUIVALENTES.");
        return true;
    }


    //Metodos privados


    private static String obtenerNombreInicial(Automata a) {
        Estado inicial = a.getEstadoInicial();
        if (inicial == null) {
            throw new IllegalStateException("El autómata no tiene estado inicial definido.");
        }
        return inicial.getnombre();
    }

    //convierte Estados en un nombre de estados ordenados.
    private static String conjuntoANombre(Set<Estado> conjunto){
        List<String> nombres = new ArrayList<>();
        for(Estado e : conjunto){
            nombres.add(e.getnombre());
        }
        Collections.sort(nombres);
        return "{" + String.join(",", nombres) + "}";
    }

    //Verifica si algun estado del conjunto es final
    private static boolean tieneEstadoFinal(Set<Estado> conjunto) {
        for(Estado e : conjunto){
            if(e.isesFinal()){
                return true;
            }
        }
        return false;
    }

    private static List<Set<String>> dividirGrupo(Set<String>grupo, List<Set<String>> particiones, Set<String> sigma, Automata afd) {
        List<Set<String>> resultado = new ArrayList<>();

        for(String estado : grupo){
            boolean encajado= false;

            for(Set<String> subgrupo : resultado){
                String representante = subgrupo.iterator().next();

                if(sonIndistinguibles(estado, representante, particiones, sigma, afd)){
                    subgrupo.add(estado);
                    encajado = true;
                    break;
                }
            }
            if(!encajado){
                Set<String> nuevo = new HashSet<>();
                nuevo.add(estado);
                resultado.add(nuevo);
            }
        }
        return resultado;
    }

    private static boolean sonIndistinguibles(String e1, String e2, List<Set<String>> particiones, Set<String> sigma, Automata afd) {

        for (String simbolo : sigma) {
            String dest1 = obtenerDestino(afd, e1, simbolo);
            String dest2 = obtenerDestino(afd, e2, simbolo);

            int grupo1 = obtenerIndiceGrupo(dest1, particiones);
            int grupo2 = obtenerIndiceGrupo(dest2, particiones);

            // Si van a grupos distintos → son distinguibles
            if (grupo1 != grupo2) return false;
        }

        return true;
    }

    private static String obtenerDestino(Automata afd, String estado, String simbolo) {
        Estado e = afd.getEstado(estado);
        if (e == null) return "Estado trampa";
        Set<Estado> destinos = e.getDestino(simbolo);
        if (destinos.isEmpty()) return "Estado trampa";
        return destinos.iterator().next().getnombre();
    }

    private static int obtenerIndiceGrupo(String estado, List<Set<String>> particiones) {
        for (int i = 0; i < particiones.size(); i++) {
            if (particiones.get(i).contains(estado)) return i;
        }
        return -1; // estado trampa o no encontrado
    }

    private static String conjuntoStringANombre(Set<String> conjunto) {
        List<String> nombres = new ArrayList<>(conjunto);
        Collections.sort(nombres);
        return "{" + String.join(",", nombres) + "}";
    }

    // Renombra todos los estados de un automata a q0, q1, q2...
    // El estado inicial siempre queda como q0.
    // Los estados trampa se eliminan (no se incluyen en el resultado).
    private static Automata renombrarEstados(Automata original) {
        Automata nuevo = new Automata();

        // Copiar alfabeto
        for (String s : original.getAlfabeto()) {
            nuevo.agregarAlfabeto(s);
        }

        // Construir mapeo viejo -> nuevo, con el inicial primero (q0)
        // El sumidero mantiene su nombre; los demás se renombran q0, q1, q2...
        Map<String, String> mapeo = new LinkedHashMap<>();
        String nombreInicial = obtenerNombreInicial(original);
        mapeo.put(nombreInicial, "q0");
        int contador = 1;
        for (String nombre : original.getEstados().keySet()) {
            if (!nombre.equals(nombreInicial) && !nombre.equals("sumidero")) {
                mapeo.put(nombre, "q" + contador++);
            }
        }
        // El sumidero se preserva con su nombre solo si viene del AFD (no del mínimo)
        // En el mínimo no existirá porque fue excluido de las particiones
        if (original.getEstados().containsKey("sumidero")) {
            mapeo.put("sumidero", "sumidero");
        }

        // Agregar estados con nuevos nombres
        for (String nuevoNombre : mapeo.values()) {
            nuevo.AgregarEstado(nuevoNombre);
        }

        // Estado inicial
        nuevo.setInicial("q0");

        // Estados finales
        for (Map.Entry<String, Estado> entry : original.getEstados().entrySet()) {
            if (entry.getValue().isesFinal() && mapeo.containsKey(entry.getKey())) {
                nuevo.MarcarEstadoFinal(mapeo.get(entry.getKey()));
            }
        }

        // Transiciones (incluyendo las que van al sumidero)
        for (Map.Entry<String, Estado> entry : original.getEstados().entrySet()) {
            String origenViejo = entry.getKey();
            String origenNuevo = mapeo.get(origenViejo);
            if (origenNuevo == null) continue;
            for (Map.Entry<String, Set<Estado>> trans : entry.getValue().getDireccion().entrySet()) {
                String simbolo = trans.getKey();
                for (Estado destino : trans.getValue()) {
                    String destinoNuevo = mapeo.get(destino.getnombre());
                    if (destinoNuevo != null) {
                        nuevo.conectar(origenNuevo, simbolo, destinoNuevo);
                    }
                }
            }
        }

        return nuevo;
    }
}
