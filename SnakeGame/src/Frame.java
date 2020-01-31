
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
							Direction last = directions.peekLast(); // directions 리스트의 rear값을 저장
							if (last != Direction.South && last != Direction.North) { // Direction 클래스의 enum 값과 비교
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
					// GameOver 상태가 아니라면 isPaused의 값을 바꿔주고 logicTimer의 setPaused 메소드 실행
					if (!isGameOver) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
					}
					break;

				case KeyEvent.VK_ENTER:
					// NewGame 또는 GameOver 상태가 아니라면 resetGame 메소드 실행
					if (isNewGame || isGameOver) {
						resetGame();
					}
					break;
				}
			}
		});
	}

	// 게임 생성
//	void startGame() {
//		this.random = new Random();
//		// snake 배열 생성
//		this.snake = new LinkedList<>();
//		// 방향 배열 생성
//		this.directions = new LinkedList<>();
//		// 게임 속도 조절
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

	// 게임 진행 중
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

	// Snake의 상황 변환
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

		// 헤드의 앞 타일이 SnakeBody가 아닐 때
		// snake 배열의 첫부분을 SnakeBody로 교체
		// head타일 삽입
		// head타일을 SnakeHead로 셋팅
		if (old != TileType.SnakeBody) {
			board.setTile(snake.peekFirst(), TileType.SnakeBody);
			snake.push(head);
			board.setTile(head, TileType.SnakeHead);
			// 이전 방향 삭제
			if (directions.size() > 1) {
				directions.poll();
			}
		}

		return old;
	}

	// 게임 시작
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

	// 게임 상태 반환
	public boolean isNewGame() {
		return isNewGame;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public boolean isPaused() {
		return isPaused;
	}

	// 과일 랜덤 위치 생성
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
	
	// 시간 내에 과일을 먹지 못했을 때 과일 재배치
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
	
	// 스코어값 반환
	public int getScore() {
		return score;
	}

	// 과일 갯수 반환
	public int getFruitsEaten() {
		return fruitsEaten;
	}

	// 과일 점수 반환
	public int getNextFruitScore() {
		return nextFruitScore;
	}

	// 최고기록 반환
	public int getBestScore() {
		bestScore = connection.getBestScore();
		return bestScore;
	}

	public Direction getDirection() {
		return directions.peek();
	}

	public void run() {
		this.random = new Random();
		// snake 배열 생성
		this.snake = new LinkedList<>();
		// 방향 배열 생성
		this.directions = new LinkedList<>();
		// 게임 속도 조절
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