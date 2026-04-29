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

        // 1. Cargar el autómata
        lector.cargarAutomata(ruta, automata);

        // 2. Información básica
        System.out.println("\nResumen del Autómata:");
        System.out.println("- Tipo: " + (automata.isEsAFND() ? "AFND" : "AFD"));
        System.out.println("- Alfabeto: " + automata.getAlfabeto());
        System.out.println("- Cantidad de estados: " + automata.getEstados().size());

        // 3. Validar cadenas
        System.out.println("\n--- PRUEBA DE CADENAS ---");
        System.out.println("Escriba cadenas para validar (o 'salir' para terminar):");
        while (true) {
            System.out.print("Cadena: ");
            String cadena = sc.nextLine();
            if (cadena.equalsIgnoreCase("salir")) break;

            boolean esValida = automata.validarCadena(cadena);
            System.out.println("Resultado: " + (esValida ? "ACEPTADA" : "RECHAZADA"));
        }

        // 4. Generar archivo para Graphviz
        System.out.println("\n--- EXPORTACIÓN GRAPHVIZ ---");
        String dotSource = automata.generarDOT();
        try (PrintWriter out = new PrintWriter("automata.dot")) {
            out.println(dotSource);
            System.out.println("Archivo 'automata.dot' generado con éxito.");
            System.out.println("Para generar la imagen usa: dot -Tpng automata.dot -o automata.png");
        } catch (Exception e) {
            System.err.println("Error al generar el archivo DOT: " + e.getMessage());
        }

        System.out.println("\n¡Gracias por usar el Gestor de Autómatas!");
    }
}
