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

                // Si dice "delta", activamos el modo de lectura de transiciones
                if (linea.equalsIgnoreCase("delta:")) {
                    leyendoDelta = true;
                    continue;
                }

                // aqui vemos como son las transiciones (origen, simbolo, destino)
                if (leyendoDelta && linea.startsWith("(") && linea.endsWith(")")) {
                    String contenido = linea.substring(1, linea.length() - 1); // para quitar los parentesis
                    String[] partes = contenido.split(","); // esto para separar por las ,
                    if (partes.length == 3) {
                        auto.conectar(partes[0].trim(), partes[1].trim(), partes[2].trim()); //aqui se verifica que sean 3 partes (origen, simbolo, destino)
                    }
                    continue;
                }

                // Aqui se maneja lo de k, sigma, s, f
                if (linea.contains("=")) {
                    String[] partes = linea.split("=");
                    if (partes.length < 2) continue;

                    String etiqueta = partes[0].trim().toLowerCase();
                    String valor = partes[1].trim();

                    // aqui se Limpiar llavessi esque existen
                    if (valor.startsWith("{") && valor.endsWith("}")) {
                        valor = valor.substring(1, valor.length() - 1);
                    }

                    switch (etiqueta) {
                        case "k": // estados
                            for (String n : valor.split(",")) {
                                auto.AgregarEstado(n.trim());
                            }
                            break;

                        case "sigma": // alfabeto
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
            System.out.println("Archivo cargado" );

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
