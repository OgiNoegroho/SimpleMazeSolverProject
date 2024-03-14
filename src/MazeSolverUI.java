import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

class Graph {
    private List<Point> nodes;        // List to store nodes (points) in the graph
    private List<List<Point>> adjList; // Adjacency list to represent edges between nodes

    // Constructor to initialize the graph
    public Graph() {
        nodes = new ArrayList<>();
        adjList = new ArrayList<>();
    }

    // Method to add a node to the graph
    public void addNode(Point p) {
        nodes.add(p);
        adjList.add(new ArrayList<>());
    }

    // Method to add an edge between two nodes in the graph
    public void addEdge(Point p1, Point p2) {
        int index1 = nodes.indexOf(p1);
        int index2 = nodes.indexOf(p2);

        if (index1 != -1 && index2 != -1) {
            adjList.get(index1).add(p2);
            adjList.get(index2).add(p1);
        }
    }

    // Method to get neighbors of a node in the graph
    public Iterable<Point> getNeighbors(Point p) {
        int index = nodes.indexOf(p);
        return (index != -1) ? adjList.get(index) : null;
    }
}

public class MazeSolverUI extends JFrame {
    private int[][] maze;
    private int rows, cols;
    private Point start, end;
    private Graph graph;
    private MazePanel mazePanel;
    private static boolean darkMode = false;

    public MazeSolverUI(int[][] maze, Point start, Point end) {
        this.maze = maze;
        this.rows = maze.length;
        this.cols = maze[0].length;
        this.start = start;
        this.end = end;



        graph = new Graph();
        buildGraph();

        setTitle("Maze Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(cols * 13, rows * 10);

        getContentPane().setBackground(Color.WHITE);

        int cellSize = 50;
        mazePanel = new MazePanel(cellSize);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mazePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());


        JButton darkModeButton = new JButton("Toggle Dark Mode");
        darkModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDarkMode();
            }
        });

        darkModeButton.setBackground(Color.WHITE);
        buttonPanel.add(darkModeButton);
        buttonPanel.add(Box.createVerticalStrut(380));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        JButton solveButton = new JButton("Solve Maze");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveMaze();
            }
        });

        JButton randomizeButton = new JButton("Randomize Maze");
        randomizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randomizeMaze();
            }
        });

        buttonPanel.add(solveButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(randomizeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 130));
        buttonPanel.setBackground(Color.WHITE);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private void applyDarkMode(Container container) {
        Color backgroundColor = darkMode ? Color.BLACK : Color.WHITE;
        Color foregroundColor = darkMode ? Color.WHITE : Color.BLACK;

        container.setBackground(backgroundColor);
        container.setForeground(foregroundColor);

        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                applyDarkMode((Container) component);
            } else {
                component.setBackground(backgroundColor);
                component.setForeground(foregroundColor);
            }
        }
    }

    private void buildGraph() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Point current = new Point(i, j);
                graph.addNode(current);

                for (int[] move : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
                    int newRow = i + move[0];
                    int newCol = j + move[1];

                    if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                        Point neighbor = new Point(newRow, newCol);
                        graph.addEdge(current, neighbor);
                    }
                }
            }
        }
    }

    private void solveMaze() {
        int[][] visited = new int[rows][cols];
        if (bfs(start, visited)) {
            highlightPath(visited);
        } else {
            JOptionPane.showMessageDialog(this, "No path found!");
        }
    }

    private boolean bfs(Point start, int[][] visited) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited[start.x][start.y] = 1; // Mark the start point as visited

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int row = current.x;
            int col = current.y;

            // Check if the current cell is the end point
            if (row == end.x && col == end.y) {
                return true;
            }

            for (Point neighbor : graph.getNeighbors(current)) {
                int newRow = neighbor.x;
                int newCol = neighbor.y;

                // Check if the neighbor is not a wall and has not been visited
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols
                        && maze[newRow][newCol] == 0 && visited[newRow][newCol] == 0) {
                    visited[newRow][newCol] = visited[row][col] + 1;
                    queue.add(neighbor);
                }
            }
        }

        return false;
    }

    private void highlightPath(int[][] visited) {
        int row = end.x;
        int col = end.y;

        while (row != start.x || col != start.y) {
            maze[row][col] = 2; // Mark path cells

            for (Point neighbor : graph.getNeighbors(new Point(row, col))) {
                int newRow = neighbor.x;
                int newCol = neighbor.y;

                if (visited[newRow][newCol] == visited[row][col] - 1) {
                    row = newRow;
                    col = newCol;
                    break;
                }
            }

            // Add a delay to visualize the movement of the yellow path
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mazePanel.repaint();
        }
    }

    private void randomizeMaze() {
        int[][] maze1 = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1},
                {1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1},
                {1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 1},
                {1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1},
                {1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        int[][] maze2 = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1},
                {1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1},
                {1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        int[][] maze3 = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1},
                {1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 1},
                {1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        int[][] maze4 = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1},
                {1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 1},
                {1, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1},
                {1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1},
                {1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        int[][] selectedMaze;
        Random random = new Random();
        int randomMaze = random.nextInt(4);

        switch (randomMaze) {
            case 0:
                selectedMaze = maze1;
                break;
            case 1:
                selectedMaze = maze2;
                break;
            case 2:
                selectedMaze = maze3;
                break;
            case 3:
                selectedMaze = maze4;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + randomMaze);
        }

        // Set the maze to the selected predefined maze
        this.maze = selectedMaze;
        this.rows = maze.length;
        this.cols = maze[0].length;
        this.start = new Point(1, 1);
        this.end = new Point(rows - 2, cols - 2);

        // Rebuild the graph for the new maze
        graph = new Graph();
        buildGraph();

        // Reset path-related variables in the maze panel
        mazePanel.resetPath();
        mazePanel.repaint();
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        // Apply dark mode to the main panel after toggling
        applyDarkMode(getContentPane());
        // Repaint the maze panel
        mazePanel.repaint();
    }

    private class MazePanel extends JPanel {
        private int currentRow;
        private int currentCol;
        private int cellSize;

        public MazePanel(int cellSize) {
            this.cellSize = cellSize;
        }

        public void resetPath() {
            currentRow = start.x;
            currentCol = start.y;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Set background color based on dark mode state
            Color backgroundColor = darkMode ? Color.BLACK : Color.WHITE;
            setBackground(backgroundColor);

            // Set colors based on dark mode state
            Color wallColor = darkMode ? Color.DARK_GRAY : Color.GRAY;
            Color pathColor = darkMode ? Color.ORANGE : Color.YELLOW;
            Color startColor = darkMode ? Color.GREEN.darker() : Color.GREEN;
            Color endColor = darkMode ? Color.RED.darker() : Color.RED;
            Color currentColor = darkMode ? Color.BLUE.darker() : Color.BLUE;
            Color emptyColor = darkMode ? Color.BLACK : Color.WHITE;
            Color borderColor = darkMode ? Color.WHITE : Color.BLACK;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int x = j * cellSize;
                    int y = i * cellSize;

                    if (maze[i][j] == 1) {
                        g.setColor(wallColor);
                        g.fillRect(x, y, cellSize, cellSize);
                    } else if (maze[i][j] == 2) {
                        g.setColor(pathColor);
                        g.fillRect(x, y, cellSize, cellSize);
                    } else if (start.x == i && start.y == j) {
                        g.setColor(startColor);
                        g.fillRect(x, y, cellSize, cellSize);
                    } else if (end.x == i && end.y == j) {
                        g.setColor(endColor);
                        g.fillRect(x, y, cellSize, cellSize);
                    } else if (currentRow == i && currentCol == j) {
                        g.setColor(currentColor);
                        g.fillRect(x, y, cellSize, cellSize);
                    } else {
                        g.setColor(emptyColor);
                        g.fillRect(x, y, cellSize, cellSize);
                    }

                    // Draw the cell borders with lighter color
                    g.setColor(borderColor);
                    g.drawRect(x, y, cellSize, cellSize);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            int rows = 10;
            int cols = 13;
            int[][] emptyMaze = new int[rows][cols];
            MazeSolverUI mazeSolverUI = new MazeSolverUI(emptyMaze, new Point(0, 0), new Point(rows - 1, cols - 1));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int frameWidth = (int) (screenSize.width * 0.75);
            int frameHeight = (int) (screenSize.height * 0.75);
            mazeSolverUI.setSize(frameWidth, frameHeight);
            mazeSolverUI.setLocationRelativeTo(null);
            mazeSolverUI.setVisible(true);
        });
    }
}