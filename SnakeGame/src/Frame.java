
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;

public class Frame extends JFrame implements Runnable {
	private static final long FRAME_TIME = 1000L / 50L;
	private static final int MIN_SNAKE_LENGTH = 5;
	private static final int MAX_DIRECTIONS = 3;

	private BoardPanel board;
	private MenuPanel side;
	private Clock logicTimer;

	private Random random;
	private boolean isNewGame;
	private boolean isGameOver;
	private boolean isPaused;
	private LinkedList<Point> snake;
	private LinkedList<Direction> directions;
	private int score;
	private int fruitsEaten;
	private int nextFruitScore;
	private int bestScore;

	DBConnection connection;

	public Frame() {
		super("Snake Game");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		this.board = new BoardPanel(this);
		this.side = new MenuPanel(this);

		add(board, BorderLayout.CENTER);
		add(side, BorderLayout.EAST);

		addKeyListener();
		connection = new DBConnection();

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	void addKeyListener() {
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					if (!isPaused && !isGameOver) {
						if (directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast(); // directions ����Ʈ�� rear���� ����
							if (last != Direction.South && last != Direction.North) { // Direction Ŭ������ enum ���� ��
								directions.addLast(Direction.North);
							}
						}
					}
					break;

				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					if (!isPaused && !isGameOver) {
						if (directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast();
							if (last != Direction.North && last != Direction.South) {
								directions.addLast(Direction.South);
							}
						}
					}
					break;

				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					if (!isPaused && !isGameOver) {
						if (directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast();
							if (last != Direction.East && last != Direction.West) {
								directions.addLast(Direction.West);
							}
						}
					}
					break;

				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					if (!isPaused && !isGameOver) {
						if (directions.size() < MAX_DIRECTIONS) {
							Direction last = directions.peekLast();
							if (last != Direction.West && last != Direction.East) {
								directions.addLast(Direction.East);
							}
						}
					}
					break;

				case KeyEvent.VK_P:
					// GameOver ���°� �ƴ϶�� isPaused�� ���� �ٲ��ְ� logicTimer�� setPaused �޼ҵ� ����
					if (!isGameOver) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
					}
					break;

				case KeyEvent.VK_ENTER:
					// NewGame �Ǵ� GameOver ���°� �ƴ϶�� resetGame �޼ҵ� ����
					if (isNewGame || isGameOver) {
						resetGame();
					}
					break;
				}
			}
		});
	}

	// ���� ����
//	void startGame() {
//		this.random = new Random();
//		// snake �迭 ����
//		this.snake = new LinkedList<>();
//		// ���� �迭 ����
//		this.directions = new LinkedList<>();
//		// ���� �ӵ� ����
//		this.logicTimer = new Clock(10.0f);
//		this.isNewGame = true;
//
//		logicTimer.setPaused(true);
//
//		while (true) {
//			long start = System.nanoTime();
//
//			logicTimer.update();
//
//			if (logicTimer.hasElapsedCycle()) {
//				updateGame();
//			}
//			board.repaint();
//			side.repaint();
//
//			long delta = (System.nanoTime() - start) / 1000000L;
//			if (delta < FRAME_TIME) {
//				try {
//					Thread.sleep(FRAME_TIME - delta);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	// ���� ���� ��
	private void updateGame() {
		TileType collision = updateSnake();

		if (collision == TileType.Fruit) {
			fruitsEaten++;
			score += nextFruitScore;
			spawnFruit();
		} else if (collision == TileType.SnakeBody) {
			isGameOver = true;
			logicTimer.setPaused(true);
			connection.logScore(score);
		} else if (nextFruitScore > 0) {
			nextFruitScore--;
		} else if (nextFruitScore == 0) {
			score -= 100;
			deleteFruit();
		}
	}

	// Snake�� ��Ȳ ��ȯ
	private TileType updateSnake() {
		Direction direction = directions.peekFirst();

		Point head = new Point(snake.peekFirst());
		switch (direction) {
		case North:
			head.y--;
			break;

		case South:
			head.y++;
			break;

		case West:
			head.x--;
			break;

		case East:
			head.x++;
			break;
		}

		if (head.x < 0 || head.x >= BoardPanel.COL_COUNT || head.y < 0 || head.y >= BoardPanel.ROW_COUNT) {
			return TileType.SnakeBody;
		}

		TileType old = board.getTile(head.x, head.y);
		if (old != TileType.Fruit && snake.size() > MIN_SNAKE_LENGTH) {
			Point tail = snake.removeLast();
			board.setTile(tail, null);
			old = board.getTile(head.x, head.y);
		}

		// ����� �� Ÿ���� SnakeBody�� �ƴ� ��
		// snake �迭�� ù�κ��� SnakeBody�� ��ü
		// headŸ�� ����
		// headŸ���� SnakeHead�� ����
		if (old != TileType.SnakeBody) {
			board.setTile(snake.peekFirst(), TileType.SnakeBody);
			snake.push(head);
			board.setTile(head, TileType.SnakeHead);
			// ���� ���� ����
			if (directions.size() > 1) {
				directions.poll();
			}
		}

		return old;
	}

	// ���� ����
	private void resetGame() {
		this.score = 0;
		this.fruitsEaten = 0;

		this.isNewGame = false;
		this.isGameOver = false;

		Point head = new Point(BoardPanel.COL_COUNT / 2, BoardPanel.ROW_COUNT / 2);

		snake.clear();
		snake.add(head);

		board.clearBoard();
		board.setTile(head, TileType.SnakeHead);

		directions.clear();
		directions.add(Direction.North);

		logicTimer.reset();

		spawnFruit();
		spawnFruit();
	}

	// ���� ���� ��ȯ
	public boolean isNewGame() {
		return isNewGame;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public boolean isPaused() {
		return isPaused;
	}

	// ���� ���� ��ġ ����
	private void spawnFruit() {
		this.nextFruitScore = 50;

		int index = random.nextInt(BoardPanel.COL_COUNT * BoardPanel.ROW_COUNT - snake.size());

		int freeFound = -1;
		for (int x = 0; x < BoardPanel.COL_COUNT; x++) {
			for (int y = 0; y < BoardPanel.ROW_COUNT; y++) {
				TileType type = board.getTile(x, y);
				if (type == null || type == TileType.Fruit) {
					if (++freeFound == index) {
						board.setTile(x, y, TileType.Fruit);
						break;
					}
				}
			}
		}
	}
	
	// �ð� ���� ������ ���� ������ �� ���� ���ġ
	private void deleteFruit() {
		for(int x = 0; x < BoardPanel.COL_COUNT; x++) {
			for(int y = 0; y < BoardPanel.ROW_COUNT; y++) {
				TileType type = board.getTile(x, y);
				if(type == TileType.Fruit) {
					board.setTile(x, y, null);
				}
			}
		}
		spawnFruit();
		spawnFruit();
	}
	
	// ���ھ ��ȯ
	public int getScore() {
		return score;
	}

	// ���� ���� ��ȯ
	public int getFruitsEaten() {
		return fruitsEaten;
	}

	// ���� ���� ��ȯ
	public int getNextFruitScore() {
		return nextFruitScore;
	}

	// �ְ��� ��ȯ
	public int getBestScore() {
		bestScore = connection.getBestScore();
		return bestScore;
	}

	public Direction getDirection() {
		return directions.peek();
	}

	public void run() {
		this.random = new Random();
		// snake �迭 ����
		this.snake = new LinkedList<>();
		// ���� �迭 ����
		this.directions = new LinkedList<>();
		// ���� �ӵ� ����
		this.logicTimer = new Clock(10.0f);
		this.isNewGame = true;

		logicTimer.setPaused(true);

		while (true) {
			long start = System.nanoTime();

			logicTimer.update();

			if (logicTimer.hasElapsedCycle()) {
				updateGame();
			}
			board.repaint();
			side.repaint();

			long delta = (System.nanoTime() - start) / 1000000L;
			if (delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}