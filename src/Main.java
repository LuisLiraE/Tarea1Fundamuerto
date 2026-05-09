import Controlador.Automata;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LectorArchivo lector = new LectorArchivo();

        // pa cargar el archivo
        System.out.println("=== MÓDULO DE PRUEBAS - PROYECTO FUNDAMUERTOS ===");
        Automata auto1 = new Automata();
        System.out.print("Ruta del Autómata 1: ");
        lector.cargarAutomata(sc.nextLine(), auto1);

        // Las opciones
        boolean salir = false;
        while (!salir) {
            System.out.println("\n--- MENÚ DE PRUEBAS ---");
            System.out.println("1. Validar cadenas en Autómata 1");
            System.out.println("2. Ver información técnica y DOT");
            System.out.println("3. Convertir a AFD");
            System.out.println("4. Minimizar");
            System.out.println("5. Cargar un 2do Autómata y comparar");
            System.out.println("0. Salir");
            System.out.print("Opción: ");

            String opcion = sc.nextLine();
            switch (opcion) {
                case "1":
                    probarCadenas(auto1, sc);
                    break;
                case "2":
                    System.out.println("--- INFORMACIÓN TÉCNICA ---");
                    System.out.println("Tipo: " + (auto1.isEsAFND() ? "AFND" : "AFD"));
                    System.out.println("Alfabeto: " + auto1.getAlfabeto());
                    System.out.println("Estados: " + auto1.getEstados().keySet());
                    System.out.println("Código DOT generado:\n" + auto1.generarDOT());
                    System.out.println("Generando imagen...");
                    auto1.dibujar("automata_actual");
                    break;
                case "3":
                    System.out.println("Convirtiendo AFND a AFD...");
                    auto1 = ProcesadorAutomata.convertirAFNDaAFD(auto1);
                    System.out.println("¡Conversión realizada!");
                    auto1.dibujar("automata_afd");
                    break;
                case "4":
                    System.out.println("Minimizando AFD...");
                    auto1 = ProcesadorAutomata.minimizarAFD(auto1);
                    System.out.println("¡Minimización realizada!");
                    auto1.dibujar("automata_minimo");
                    break;
                case "5":
                    Automata auto2 = new Automata();
                    System.out.print("Ruta del Autómata 2: ");
                    lector.cargarAutomata(sc.nextLine(), auto2);
                    boolean eq = ProcesadorAutomata.sonEquivalentes(auto1, auto2);
                    System.out.println("¿Son equivalentes?: " + (eq ? "SÍ" : "NO"));
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    // metodo aparte pa que no sea tan enredado
    private static void probarCadenas(Automata a, Scanner sc) {
        System.out.println("\n--- MODO VALIDACIÓN (escribe 'volver' para el menú) ---");
        while (true) {
            System.out.print("Ingrese cadena: ");
            String cadena = sc.nextLine();
            if (cadena.equalsIgnoreCase("volver")) break;

            boolean resultado = a.validarCadena(cadena);

            System.out.println("Resultado: " + (resultado ? "ACEPTADA" : "RECHAZADA"));
        }
    }
}