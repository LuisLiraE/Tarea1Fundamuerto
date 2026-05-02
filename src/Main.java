import Controlador.Automata;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LectorArchivo lector = new LectorArchivo();
        Automata automata = new Automata();

        System.out.println("--- PROYECTO FUNDAMUERTOS: GESTOR DE AUTÓMATAS ---");
        System.out.print("Ingrese la ruta del archivo del autómata (ej: automata.txt): ");
        String ruta = sc.nextLine();

        // Primero buscamos la ruta del txt
        lector.cargarAutomata(ruta, automata);

        // colocamos la informacion basica de este
        System.out.println("\nResumen del Autómata:");
        System.out.println("- Tipo: " + (automata.isEsAFND() ? "AFND" : "AFD"));
        System.out.println("- Alfabeto: " + automata.getAlfabeto());
        System.out.println("- Cantidad de estados: " + automata.getEstados().size());

        // Aqui validamos la cadena
        System.out.println("\n--- PRUEBA DE CADENAS ---");
        System.out.println("Escriba cadenas para validar (o 'salir' para terminar):");
        while (true) {
            System.out.print("Cadena: ");
            String cadena = sc.nextLine();
            if (cadena.equalsIgnoreCase("salir")) break;

            boolean esValida = automata.validarCadena(cadena);
            System.out.println("Resultado: " + (esValida ? "ACEPTADA" : "RECHAZADA"));
        }

        // aca hacemos el archivo para El gravitz
        System.out.println("\n--- EXPORTACIÓN GRAPHVIZ ---");
        String xd = automata.generarDOT();
        try (PrintWriter out = new PrintWriter("automata.dot")) {
            out.println(xd);
            System.out.println("Archivo 'automata.dot' generado con éxito.");
            System.out.println("Para generar la imagen usa: dot -Tpng automata.dot -o automata.png");
        } catch (Exception e) {
            System.err.println("Error al generar el archivo DOT: " + e.getMessage());
        }

        System.out.println("\nFin");
    }
}
