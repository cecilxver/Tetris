import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    static final int W = 10, H = 10;
    static int[][] board = new int[H][W];
    static int[][] piece;
    static int score = 0;
    static String ruta = "Logs/" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
            "partida.txt";
    static File puntuacion =new File(ruta);
    static PrintWriter pw;


    static Random rnd = new Random();

    static int[][][] SHAPES = {
            {{0,0},{0,1},{1,0},{1,1}}, // O
            {{0,0},{1,0},{2,0},{3,0}}, // I
            {{0,0},{1,0},{2,0},{2,1}}, // L
            {{0,0},{0,1},{1,0},{2,0}}, // J
            {{0,1},{1,0},{1,1},{2,0}}, // S
            {{0,0},{1,0},{1,1},{2,1}}, // Z
            {{0,0},{0,1},{0,2},{1,1}}, // T
    };

    public Main() throws IOException {
    }

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        puntuacion.createNewFile();
        pw = new PrintWriter(new FileWriter(puntuacion, true));
        logear(pw,"Partida empezada ");
        piece = spawn();


        while (true) {
            draw();
            System.out.print("(a=← d=→ s=↓ r=rotar Enter=caída rápida): ");
            String cmd = sc.nextLine().trim().toLowerCase();

            switch (cmd) {
                case "a" -> {
                    if (move(0, -1)) logear(pw, "Acción: LEFT -> Ejecutado");
                    else logear(pw, "Acción: LEFT -> Bloqueado");
                }
                case "d" -> {
                    if (move(0, 1)) logear(pw, "Acción: RIGHT -> Ejecutado");
                    else logear(pw, "Acción: RIGHT -> Bloqueado");
                }
                case "s" -> {
                    if (move(1, 0)) logear(pw, "Acción: DOWN -> Ejecutado");
                    else logear(pw, "Acción: DOWN -> Bloqueado");
                }
                case "r" -> { rotate(); logear(pw, "Acción: ROTAR"); }
                case ""  -> { while (move(1, 0)); logear(pw, "Acción: HARD DROP"); }
            }

            // Gravedad: baja una fila por turno pase lo que pase
            if (!move(1, 0)) {
                place();
                score += clearLines() * 100;

                piece = spawn();
                if (collides(piece)) {
                    draw();
                    System.out.println("GAME OVER - Puntos: " + score);
                    logear(pw,"GAME OVER. Puntuación final: "+score);

                    break;
                }
            }
        }
        pw.flush();
        pw.close();


    }

    static int[][] spawn() throws FileNotFoundException {
        int indice= rnd.nextInt(SHAPES.length);
        int[][] shape = SHAPES[indice];
        int[][] p = new int[4][2];
        for (int i = 0; i < 4; i++) {
            p[i][0] = shape[i][0];
            p[i][1] = shape[i][1] + W / 2 - 1;
        }

    switch (indice){
        case 0:
            logear(pw,"La pieza es: O");
            break;
        case 1:
            logear(pw,"La pieza es: I");
            break;
        case 2:
            logear(pw,"La pieza es: L");
            break;
        case 3:
            logear(pw,"La pieza es: J");
            break;
        case 4:
            logear(pw,"La pieza es: S");
            break;
        case 5:
            logear(pw,"La pieza es: Z");
            break;
        case 6:
            logear(pw,"La pieza es: T");
            break;
    }

        return p;
    }

    static boolean move(int dr, int dc) throws FileNotFoundException {
        int[][] next = new int[4][2];
        for (int i = 0; i < 4; i++) {
            next[i][0] = piece[i][0] + dr;
            next[i][1] = piece[i][1] + dc;
        }
        if (collides(next)){
            logear(pw,"Movimiento bloqueado");
            return false;
        }
        piece = next;
        return true;
    }

    static void rotate() {
        // Calcular esquina superior izquierda del bounding box
        int minR = Integer.MAX_VALUE, minC = Integer.MAX_VALUE;
        for (int[] c : piece) { minR = Math.min(minR, c[0]); minC = Math.min(minC, c[1]); }

        // Normalizar al origen, obtener maxFila
        int[][] norm = new int[4][2];
        int maxNormR = 0;
        for (int i = 0; i < 4; i++) {
            norm[i][0] = piece[i][0] - minR;
            norm[i][1] = piece[i][1] - minC;
            maxNormR = Math.max(maxNormR, norm[i][0]);
        }

        // Rotar 90° (r,c) → (c, maxR-r) y volver a posicionar
        int[][] rotated = new int[4][2];
        for (int i = 0; i < 4; i++) {
            rotated[i][0] = norm[i][1] + minR;
            rotated[i][1] = maxNormR - norm[i][0] + minC;

        }

        if (!collides(rotated)) piece = rotated; // solo rota si cabe
    }

    static boolean collides(int[][] p) {
        for (int[] c : p) {
            if (c[0] < 0 || c[0] >= H || c[1] < 0 || c[1] >= W) return true;
            if (board[c[0]][c[1]] != 0) return true;
        }
        return false;
    }

    static void place() {
        for (int[] c : piece) board[c[0]][c[1]] = 1;
    }

    static int clearLines() throws FileNotFoundException {
        int lines = 0;
        for (int r = H - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < W; c++) if (board[r][c] == 0) { full = false; break; }
            if (full) {
                for (int rr = r; rr > 0; rr--) board[rr] = board[rr - 1].clone();
                board[0] = new int[W];
                lines++;
                r++; // revisitar la misma fila (ahora tiene el contenido de la anterior)
                logear(pw,"Línea eliminada. Puntuación: "+score);
            }

        }
        return lines;
    }

    static void draw() {
        System.out.print("\033[H\033[2J"); // limpiar pantalla ANSI
        System.out.flush();

        int[][] display = new int[H][W];
        for (int r = 0; r < H; r++) display[r] = board[r].clone();
        for (int[] c : piece)
            if (c[0] >= 0 && c[0] < H) display[c[0]][c[1]] = 2;

        System.out.println("┌" + "──".repeat(W) + "┐  Puntos: " + score);
        for (int r = 0; r < H; r++) {
            System.out.print("│");
            for (int c = 0; c < W; c++) {
                System.out.print(switch (display[r][c]) {
                    case 1 -> "██";
                    case 2 -> "[]";
                    default -> "  ";
                });
            }
            System.out.println("│");
        }
        System.out.println("└" + "──".repeat(W) + "┘");
    }
    static void logear(PrintWriter pw, String msj) throws FileNotFoundException {
        //TODO
        String hora = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "]";
        pw.println(hora + " " + msj);
        pw.flush();

    }
}