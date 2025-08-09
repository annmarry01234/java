import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.*;

public class PathfindingVisualizer extends Application {
    final int rows = 20, cols = 30, cellSize = 30;
    Node[][] grid = new Node[rows][cols];
    Node start, end;
    boolean settingStart = true, settingEnd = false;

    class Node {
        int row, col;
        boolean wall;
        double g, h, f;
        Node parent;
        Node(int r, int c) { row = r; col = c; }
    }

    @Override
    public void start(Stage stage) {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = new Node(r, c);

        Canvas canvas = new Canvas(cols * cellSize, rows * cellSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        draw(gc);

        canvas.setOnMouseClicked(e -> {
            int c = (int)(e.getX() / cellSize);
            int r = (int)(e.getY() / cellSize);
            if (settingStart) { start = grid[r][c]; settingStart = false; settingEnd = true; }
            else if (settingEnd) { end = grid[r][c]; settingEnd = false; }
            else if (e.getButton() == MouseButton.PRIMARY) grid[r][c].wall = !grid[r][c].wall;
            draw(gc);
        });

        canvas.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE -> { if (start != null && end != null) runAStar(gc); }
                case R -> { for (Node[] row : grid) for (Node n : row) { n.wall = false; n.parent = null; } draw(gc); }
            }
        });

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("A* Pathfinding Visualizer");
        stage.show();
        canvas.requestFocus();
    }

    void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, cols * cellSize, rows * cellSize);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                Node n = grid[r][c];
                if (n == start) gc.setFill(Color.GREEN);
                else if (n == end) gc.setFill(Color.RED);
                else if (n.wall) gc.setFill(Color.BLACK);
                else gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                gc.setStroke(Color.GRAY);
                gc.strokeRect(c * cellSize, r * cellSize, cellSize, cellSize);
            }
    }

    void runAStar(GraphicsContext gc) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<Node> closed = new HashSet<>();
        start.g = 0; start.h = dist(start, end); start.f = start.h;
        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current == end) { reconstruct(gc, current); return; }
            closed.add(current);
            for (Node neighbor : neighbors(current)) {
                if (neighbor.wall || closed.contains(neighbor)) continue;
                double tentativeG = current.g + 1;
                if (!open.contains(neighbor) || tentativeG < neighbor.g) {
                    neighbor.g = tentativeG;
                    neighbor.h = dist(neighbor, end);
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                    if (!open.contains(neighbor)) open.add(neighbor);
                }
            }
        }
    }

    List<Node> neighbors(Node n) {
        List<Node> list = new ArrayList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nr = n.row + d[0], nc = n.col + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols)
                list.add(grid[nr][nc]);
        }
        return list;
    }

    double dist(Node a, Node b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    void reconstruct(GraphicsContext gc, Node current) {
        while (current != null) {
            if (current != start && current != end)
                gc.setFill(Color.YELLOW);
            else if (current == start)
                gc.setFill(Color.GREEN);
            else if (current == end)
                gc.setFill(Color.RED);
            gc.fillRect(current.col * cellSize, current.row * cellSize, cellSize, cellSize);
            gc.setStroke(Color.GRAY);
            gc.strokeRect(current.col * cellSize, current.row * cellSize, cellSize, cellSize);
            current = current.parent;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
