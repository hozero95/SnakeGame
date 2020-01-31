
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

public class BoardPanel extends JPanel {

	// 가로, 세로 타일 수
	public static final int COL_COUNT = 25;
	public static final int ROW_COUNT = 25;
	// 타일 사이즈
	public static final int TILE_SIZE = 20;

	// 패널 폰트 설정
	private static final Font FONT = new Font("Verdana", Font.BOLD, 25);
	private Frame game;
	// 타일 배열 선언
	private TileType[] tiles;

	public BoardPanel(Frame game) {
		this.game = game;
		// 타일 갯수 설정
		this.tiles = new TileType[ROW_COUNT * COL_COUNT];

		// 레이아웃 크기 설정
		setPreferredSize(new Dimension(COL_COUNT * TILE_SIZE, ROW_COUNT * TILE_SIZE));
		// 패널 배경 : 검은색
		setBackground(Color.BLACK);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// 해당 타입에 맞는 타일 그리기
		for (int x = 0; x < COL_COUNT; x++) {
			for (int y = 0; y < ROW_COUNT; y++) {
				TileType type = getTile(x, y);
				if (type != null) {
					drawTile(x * TILE_SIZE, y * TILE_SIZE, type, g);
				}
			}
		}

		// 격자 그리기
		g.setColor(Color.DARK_GRAY);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		for (int x = 0; x < COL_COUNT; x++) {
			for (int y = 0; y < ROW_COUNT; y++) {
				g.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, getHeight()); // 가로선
				g.drawLine(0, y * TILE_SIZE, getWidth(), y * TILE_SIZE); // 세로선
			}
		}

		// 게임 시작, 게임 오버, 게임 정지 시에 맞는 문자열 그리기
		if (game.isNewGame() || game.isGameOver() || game.isPaused()) {
			g.setColor(Color.WHITE);

			int centerX = getWidth() / 2;
			int centerY = getHeight() / 2;

			String largeMessage = null;
			String smallMessage = null;
			if (game.isNewGame()) {
				largeMessage = "Snake Game!";
				smallMessage = "Press Enter to Start";
			} else if (game.isGameOver()) {
				largeMessage = "Game Over!";
				smallMessage = "Press Enter to Restart";
			} else if (game.isPaused()) {
				largeMessage = "Paused";
				smallMessage = "Press P to Resume";
			}

			g.setFont(FONT);
			g.drawString(largeMessage, centerX - g.getFontMetrics().stringWidth(largeMessage) / 2, centerY - 50);
			g.drawString(smallMessage, centerX - g.getFontMetrics().stringWidth(smallMessage) / 2, centerY + 50);
		}
	}

	// 타일 그리기
	private void drawTile(int x, int y, TileType type, Graphics g) {

		switch (type) {
		case Fruit:
			g.setColor(Color.RED);
			g.fillOval(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
			break;

		case SnakeBody:
			g.setColor(Color.GREEN);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			break;

		case SnakeHead:
			g.setColor(Color.RED);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			break;
		}
	}

	// 재시작 시 타일 초기화
	public void clearBoard() {
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = null;
		}
	}

	// 타일 배치
	public void setTile(Point point, TileType type) {
		setTile(point.x, point.y, type);
	}

	public void setTile(int x, int y, TileType type) {
		tiles[y * ROW_COUNT + x] = type;
	}

	// 해당 타일 반환
	public TileType getTile(int x, int y) {
		return tiles[y * ROW_COUNT + x];
	}
}