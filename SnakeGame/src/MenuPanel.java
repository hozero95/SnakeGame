
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

public class MenuPanel extends JPanel {

	// 폰트 생성
	private static final Font LARGE_FONT = new Font("Verdana", Font.BOLD, 20); // 제목
	private static final Font MEDIUM_FONT = new Font("Verdana", Font.BOLD, 16); // 소제목
	private static final Font SMALL_FONT = new Font("Verdana", Font.BOLD, 12); // 각 항목

	private Frame game;

	public MenuPanel(Frame game) {
		this.game = game;

		// 패널 가로길이 300으로 고정
		setPreferredSize(new Dimension(300, BoardPanel.ROW_COUNT * BoardPanel.TILE_SIZE));
		setBackground(Color.BLACK);
	}

	// 소제목 간의 위치
	private static final int ScoreBoard_POSITION = 120;
	private static final int CONTROLS_POSOTION = 290;
	// 줄 사이 간격
	private static final int LINE_SIZE = 30;
	// 들여쓰기
	private static final int SMALL_OFFSET = 30;
	private static final int LARGE_OFFSET = 50;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(Color.WHITE);

		// 제목
		g.setFont(LARGE_FONT);
		g.drawString("Snake Game", getWidth() / 2 - g.getFontMetrics().stringWidth("Snake Game") / 2, 50);

		// 소제목
		g.setFont(MEDIUM_FONT);
		g.drawString("Score Board", SMALL_OFFSET, ScoreBoard_POSITION);
		g.drawString("Controls", SMALL_OFFSET, CONTROLS_POSOTION);

		// 각 항목
		g.setFont(SMALL_FONT);
		int drawY = ScoreBoard_POSITION;
		g.drawString("Total Score: " + game.getScore(), LARGE_OFFSET, drawY += LINE_SIZE);
		g.drawString("Fruit Eaten: " + game.getFruitsEaten(), LARGE_OFFSET, drawY += LINE_SIZE);
		g.drawString("Fruit Score: " + game.getNextFruitScore(), LARGE_OFFSET, drawY += LINE_SIZE);
		g.drawString("Best Score: " + game.getBestScore(), LARGE_OFFSET, drawY += LINE_SIZE);

		drawY = CONTROLS_POSOTION;
		g.drawString("Move Up: W / Up Arrowkey", LARGE_OFFSET, drawY += LINE_SIZE);
		g.drawString("Move Down: S / Down Arrowkey", LARGE_OFFSET, drawY += LINE_SIZE);
		g.drawString("Move Left: A / Left Arrowkey", LARGE_OFFSET, drawY += LINE_SIZE);
		g.drawString("Move Right: D / Right Arrowkey", LARGE_OFFSET, drawY += LINE_SIZE);
		g.drawString("Pause Game: P", LARGE_OFFSET, drawY += LINE_SIZE);

		// 제작자
		drawY += 10;
		g.drawString("Made by hozero95", getWidth() - g.getFontMetrics().stringWidth("Made by hozero95") - 10,
				drawY += LINE_SIZE);
	}

}