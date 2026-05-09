import Controlador.Automata;
import java.util.Scanner;

/**
 * Clase principal del programa.
 *
 * Implementa el flujo completo solicitado por la tarea:
 *   1. Carga dos autómatas desde archivos de texto.
 *   2. Detecta si son AFD o AFND y convierte los AFND a AFD.
 *   3. Comprueba si ambos autómatas son equivalentes mediante minimización.
 *   4. Dibuja ambos autómatas resultantes en pantalla (via Graphviz).
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LectorArchivo lector = new LectorArchivo();

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Tarea 1 - Fundamentos de CC  Otoño 2026 ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // ── Paso 1: cargar los dos autómatas ──────────────────────────────
        Automata auto1 = cargarAutomata(lector, sc, 1);
        Automata auto2 = cargarAutomata(lector, sc, 2);

        // ── Paso 2: detectar tipo y convertir si es necesario ─────────────
        auto1 = detectarYConvertir(auto1, 1);
        auto2 = detectarYConvertir(auto2, 2);

        // ── Paso 3: comparar equivalencia (minimiza ambos internamente) ───
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("Paso 3 · Comparando equivalencia...");
        boolean equivalentes = ProcesadorAutomata.sonEquivalentes(auto1, auto2);
        System.out.println("\n  Resultado: los autómatas " +
            (equivalentes ? "SÍ son equivalentes ✓" : "NO son equivalentes ✗") +
            " (aceptan el " + (equivalentes ? "mismo" : "distinto") + " lenguaje)");

        // ── Paso 4: minimizar y dibujar ambos autómatas resultantes ───────
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("Paso 4 · Minimizando y dibujando autómatas...");

        Automata min1 = ProcesadorAutomata.minimizarAFD(auto1);
        Automata min2 = ProcesadorAutomata.minimizarAFD(auto2);

        System.out.println("\n  Autómata 1 mínimo → " + min1.getEstados().size() + " estados");
        System.out.println("  Generando imagen: automata1_minimo.png");
        min1.dibujar("automata1_minimo");

        System.out.println("\n  Autómata 2 mínimo → " + min2.getEstados().size() + " estados");
        System.out.println("  Generando imagen: automata2_minimo.png");
        min2.dibujar("automata2_minimo");

        System.out.println("\n  ¡Listo! Revisa las imágenes generadas.");

        sc.close();
    }

    // ── Métodos auxiliares ────────────────────────────────────────────────

    /**
     * Solicita la ruta de un archivo al usuario y carga el autómata.
     * Reintenta si la ruta es inválida o el archivo no existe.
     *
     * @param lector  instancia del lector de archivos
     * @param sc      scanner de entrada
     * @param numero  número del autómata (1 o 2), solo para mensajes
     * @return        el autómata cargado
     */
    private static Automata cargarAutomata(LectorArchivo lector, Scanner sc, int numero) {
        Automata automata = null;
        while (automata == null) {
            System.out.print("\nRuta del Autómata " + numero + ": ");
            String ruta = sc.nextLine().trim();
            automata = new Automata();
            try {
                lector.cargarAutomata(ruta, automata);
                System.out.println("  Autómata " + numero + " cargado correctamente.");
            } catch (Exception e) {
                System.out.println("  ✗ No se pudo cargar el archivo: " + e.getMessage());
                System.out.println("  Intenta de nuevo.");
                automata = null;
            }
        }
        return automata;
    }

    /**
     * Comprueba si el autómata es AFD o AFND.
     * Si es AFND, lo convierte a AFD y devuelve el resultado.
     *
     * @param automata  el autómata a evaluar
     * @param numero    número del autómata (para mensajes)
     * @return          el mismo autómata si ya era AFD, o el AFD convertido
     */
    private static Automata detectarYConvertir(Automata automata, int numero) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("Paso 2 · Autómata " + numero + ": tipo detectado = " +
            (automata.isEsAFND() ? "AFND" : "AFD"));

        if (automata.isEsAFND()) {
            System.out.println("  Convirtiendo AFND → AFD...");
            automata = ProcesadorAutomata.convertirAFNDaAFD(automata);
            System.out.println("  Conversión completada. Estados resultantes: " +
                automata.getEstados().size());
        } else {
            System.out.println("  Ya es AFD, no requiere conversión.");
        }

        return automata;
    }
}
