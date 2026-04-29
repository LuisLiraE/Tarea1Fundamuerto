import Controlador.Automata;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LectorArchivo {

    public void cargarAutomata(String ruta, Automata auto) {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            boolean leyendoDelta = false;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                // Si la línea es "delta:", activamos el modo de lectura de transiciones
                if (linea.equalsIgnoreCase("delta:")) {
                    leyendoDelta = true;
                    continue;
                }

                // Manejo de transiciones (origen, simbolo, destino)
                if (leyendoDelta && linea.startsWith("(") && linea.endsWith(")")) {
                    String contenido = linea.substring(1, linea.length() - 1);
                    String[] partes = contenido.split(",");
                    if (partes.length == 3) {
                        auto.conectar(partes[0].trim(), partes[1].trim(), partes[2].trim());
                    }
                    continue;
                }

                // Manejo de etiquetas k=, sigma=, s=, f=
                if (linea.contains("=")) {
                    String[] partes = linea.split("=");
                    if (partes.length < 2) continue;

                    String etiqueta = partes[0].trim().toLowerCase();
                    String valor = partes[1].trim();

                    // Limpiar llaves { } si existen
                    if (valor.startsWith("{") && valor.endsWith("}")) {
                        valor = valor.substring(1, valor.length() - 1);
                    }

                    switch (etiqueta) {
                        case "k": // ESTADOS
                            for (String n : valor.split(",")) {
                                auto.AgregarEstado(n.trim());
                            }
                            break;

                        case "sigma": // ALFABETO
                            for (String s : valor.split(",")) {
                                auto.agregarAlfabeto(s.trim());
                            }
                            break;

                        case "s": // INICIAL
                            auto.setInicial(valor.trim());
                            break;

                        case "f": // FINALES
                            for (String f : valor.split(",")) {
                                auto.MarcarEstadoFinal(f.trim());
                            }
                            break;
                    }
                }
            }
            System.out.println("¡Archivo cargado con éxito según el formato del PDF!");

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
