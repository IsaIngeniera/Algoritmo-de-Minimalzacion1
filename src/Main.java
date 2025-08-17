import java.util.*;

/**
 * Main.java
 * Versión robusta para leer una tabla de transiciones que puede incluir
 * el índice del estado al principio de cada fila (ej: "0 1 2") o no ("1 2").
 */
class DFA {
    int numEstados;
    char[] alfabeto;
    int[][] transiciones;
    Set<Integer> finales;

    public DFA(int numEstados, char[] alfabeto, int[][] transiciones, Set<Integer> finales) {
        this.numEstados = numEstados;
        this.alfabeto = alfabeto;
        this.transiciones = transiciones;
        this.finales = finales;
    }
}

class MinimizarDFA {

    public static List<int[]> minimizar(DFA dfa) {
        int n = dfa.numEstados;
        boolean[][] marcado = new boolean[n][n];

        // 1) Marcar pares donde uno es final y otro no
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (dfa.finales.contains(i) != dfa.finales.contains(j)) {
                    marcado[i][j] = true;
                }
            }
        }

        // 2) Propagar hasta estabilizar
        boolean cambio;
        do {
            cambio = false;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (!marcado[i][j]) {
                        for (int k = 0; k < dfa.alfabeto.length; k++) {
                            int p1 = dfa.transiciones[i][k];
                            int q1 = dfa.transiciones[j][k];
                            int a = Math.min(p1, q1);
                            int b = Math.max(p1, q1);
                            if (marcado[a][b]) {
                                marcado[i][j] = true;
                                cambio = true;
                                break;
                            }
                        }
                    }
                }
            }
        } while (cambio);

        // 3) Recoger pares no marcados -> equivalentes
        List<int[]> equivalentes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!marcado[i][j]) equivalentes.add(new int[]{i, j});
            }
        }
        return equivalentes;
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // --- Lectura robusta de entradas ---
        System.out.print("Número de estados: ");
        int numEstados;
        // leer entero (admite que el usuario lo escriba y presione Enter)
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            numEstados = Integer.parseInt(line);
            break;
        }

        System.out.print("Alfabeto (ej: a b): ");
        String[] alfTokens;
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            alfTokens = line.split("\\s+");
            break;
        }
        char[] alfabeto = new char[alfTokens.length];
        for (int i = 0; i < alfTokens.length; i++) alfabeto[i] = alfTokens[i].charAt(0);

        System.out.print("Estados finales (ej: 1 2 5) [dejar vacío = ninguno]: ");
        Set<Integer> finales = new HashSet<>();
        String finalsLine = sc.nextLine().trim();
        if (!finalsLine.isEmpty()) {
            String[] finParts = finalsLine.split("\\s+");
            for (String f : finParts) {
                if (!f.isEmpty()) finales.add(Integer.parseInt(f));
            }
        }

        System.out.println("Ingrese la tabla de transiciones (una fila por estado).");
        System.out.println("Cada fila puede ser: \"t0 t1 ...\" o \"i t0 t1 ...\" (i = índice de estado).");
        int[][] transiciones = new int[numEstados][alfabeto.length];

        for (int i = 0; i < numEstados; i++) {
            // leer la(s) línea(s) que correspondan a la fila i
            String line;
            do {
                line = sc.nextLine();
                if (line == null) line = "";
                line = line.trim();
            } while (line.isEmpty());

            String[] parts = line.split("\\s+");

            // Si no alcanzan tokens suficientes, seguir leyendo y concatenando
            if (parts.length < alfabeto.length) {
                List<String> tokens = new ArrayList<>(Arrays.asList(parts));
                while (tokens.size() < alfabeto.length) {
                    String extra = sc.nextLine().trim();
                    if (extra.isEmpty()) continue;
                    tokens.addAll(Arrays.asList(extra.split("\\s+")));
                }
                parts = tokens.toArray(new String[0]);
            }

            // Para ser robustos: tomar los últimos |alfabeto| tokens como las transiciones.
            // Esto cubre tanto "t0 t1" como "i t0 t1".
            int start = Math.max(0, parts.length - alfabeto.length);
            for (int k = 0; k < alfabeto.length; k++) {
                transiciones[i][k] = Integer.parseInt(parts[start + k]);
            }
        }

        // --- Ejecutar minimización ---
        DFA dfa = new DFA(numEstados, alfabeto, transiciones, finales);
        List<int[]> eq = MinimizarDFA.minimizar(dfa);

        // --- Salida ---
        if (eq.isEmpty()) {
            System.out.println("No hay estados equivalentes.");
        } else {
            // Imprimir en una sola línea, pares en orden lexicográfico (i, j)
            StringBuilder sb = new StringBuilder();
            for (int[] par : eq) {
                sb.append("(").append(par[0]).append(",").append(par[1]).append(") ");
            }
            System.out.println(sb.toString().trim());
        }
    }
}
