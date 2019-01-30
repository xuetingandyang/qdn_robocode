package sample;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import robocode.*;

public class AIRobot extends AdvancedRobot {

	// status:[bearing][distance][heading][area][wallhit]
	// action:[movement][fire]
	static public float table[][][][][][][] = new float[4][4][4][3][3][6][2];
	static int b, d, h, x, y, m, f = 0;

	static private int i = 0;
	static private float win, total, rate, totalturnC, totalwinC,
			totalrate = 0;
	static private float winC[] = new float[51];
	static private float winningrate[] = new float[1000];
	static private float winningrate2[] = new float[1000];
	static private float totalturn[] = new float[1000];

	private double wallhit, lastwallhit, wallavoidreward, myenergyreward,
			enemyenergyreward = 0;
	private double bearing, distance, heading, dx, dy, r, rr = 0;
	private double lastenemyenergy, enemyenergy, lastmyenergy, myenergy = 100;

	public void run() {

		i++;
		if (i > 50)
			i = 1;

		total++;
		if (total > 50)
			total = 50;

		totalturnC++;

		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);

		if (totalturnC % 30 == 1) {
			totalturn[(int) (totalturnC / 30)] = totalturnC;
			winningrate[(int) (totalturnC / 30)] = rate;
			winningrate2[(int) (totalturnC / 30)] = totalrate;
		}

		turnRadarRight(360);

		dx = getX();
		dy = getY();
		if (dx <= 60)
			dx = 0;
		if (dx > 60 && dx < 740)
			dx = 1;
		if (dx >= 740)
			dx = 2;
		if (dy <= 60)
			dy = 0;
		if (dy > 60 && dy < 440)
			dy = 1;
		if (dy >= 440)
			dy = 2;

		lastwallhit = 0;
		if (((dx == 0 && dy == 0) || (dx == 0 && dy == 2))
				|| ((dx == 2 && dy == 0) || ((dx == 2 && dy == 2))))
			lastwallhit = 2;
		if ((dx == 1 && dy == 0) || (dx == 1 && dy == 2)
				|| (dx == 0 && dy == 1) || (dx == 2 && dy == 1))
			lastwallhit = 1;

		double Qmax = outputFor(bearing, distance, heading, dx, dy);

		int lb = b;
		int ld = d;
		int lh = h;
		int lx = x;
		int ly = y;
		int lm = m;
		int lf = f;

		if (f == 1)
			fire(3);
		switch (m) {
		case (0): {
			ahead(90);
			break;
		}
		case (1): {
			setAhead(90);
			setTurnLeft(90);
			execute();
			break;
		}
		case (2): {
			setAhead(90);
			setTurnRight(90);
			execute();
			break;
		}
		case (3): {
			back(90);
			break;
		}
		case (4): {
			setBack(90);
			setTurnLeft(90);
			execute();
			break;
		}
		case (5): {
			setBack(90);
			setTurnRight(90);
			execute();
			break;
		}
		}

		while (true) {
			turnRadarRight(360);

			dx = getX();
			dy = getY();
			if (dx <= 60)
				dx = 0;
			if (dx > 60 && dx < 740)
				dx = 1;
			if (dx >= 740)
				dx = 2;
			if (dy <= 60)
				dy = 0;
			if (dy > 60 && dy < 440)
				dy = 1;
			if (dy >= 440)
				dy = 2;

			wallhit = 0;
			if (((dx == 0 && dy == 0) || (dx == 0 && dy == 2))
					|| ((dx == 0 && dy == 2) || ((dx == 2 && dy == 2))))
				wallhit = 2;
			if ((dx == 1 && dy == 0) || (dx == 1 && dy == 2)
					|| (dx == 0 && dy == 1) || (dx == 2 && dy == 1))
				wallhit = 1;

			myenergyreward = 0;
			if ((myenergy - lastmyenergy) > 0)
				myenergyreward = 1.5;
			if ((myenergy - lastmyenergy) < 0)
				myenergyreward = -0.5;
			enemyenergyreward = 0;
			if ((enemyenergy - lastenemyenergy) > 0)
				enemyenergyreward = -3;
			if ((enemyenergy - lastenemyenergy) < 0)
				enemyenergyreward = 1;
			wallavoidreward = 0;
			if ((wallhit - lastwallhit) < 0)
				wallavoidreward = 3;

			r = myenergyreward + enemyenergyreward + wallavoidreward;

			if (r < (-5))
				rr = -5;
			if (r > 5)
				rr = 5;
			if (r <= 5 && r >= (-5))
				rr = r;

			Qmax = outputFor(bearing, distance, heading, dx, dy);

			train(lb, ld, lh, lx, ly, lm, lf, Qmax, rr);

			System.out.println("m:" + myenergy + "-" + lastmyenergy + "e:"
					+ enemyenergy + "-" + lastenemyenergy + "w:" + wallhit);
			System.out.println("rr=" + rr);

			lb = b;
			ld = d;
			lh = h;
			lx = x;
			ly = y;
			lm = m;
			lf = f;

			lastenemyenergy = enemyenergy;
			lastmyenergy = myenergy;
			lastwallhit = wallhit;

			if (f == 1)
				fire(3);
			switch (m) {
			case (0): {
				ahead(90);
				break;
			}
			case (1): {
				setAhead(90);
				setTurnLeft(90);
				execute();
				break;
			}
			case (2): {
				setAhead(90);
				setTurnRight(90);
				execute();
				break;
			}
			case (3): {
				back(90);
				break;
			}
			case (4): {
				setBack(90);
				setTurnLeft(90);
				execute();
				break;
			}
			case (5): {
				setBack(90);
				setTurnRight(90);
				execute();
				break;
			}
			}

		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing
				- getGunHeading());
		turnGunRight(bearingFromGun);

		bearing = e.getBearing();
		distance = e.getDistance();
		enemyenergy = e.getEnergy();
		heading = getHeading();
		myenergy = getEnergy();

	}

	public void onWin(WinEvent e) {
		winC[i] = 1;
		win = 0;
		totalwinC++;

		for (int j = 1; j <= 50; j++)
			win = win + winC[j];

		rate = win / total;
		totalrate = totalwinC / totalturnC;
		System.out.println(win + "/" + total + "=" + rate);
		System.out.println(totalwinC + "/" + totalturnC + "=" + totalrate);

	}

	public void onDeath(DeathEvent e) {
		winC[i] = 0;
		win = 0;

		for (int j = 1; j <= 50; j++)
			win = win + winC[j];

		rate = win / total;
		totalrate = totalwinC / totalturnC;
		System.out.println(win + "/" + total + "=" + rate);
		System.out.println(totalwinC + "/" + totalturnC + "=" + totalrate);

	}

	public void onBattleEnded(BattleEndedEvent event) {
		try {
			PrintStream PS = new PrintStream(new RobocodeFileOutputStream(
					getDataFile("LUT.dat")));
			for (int i1 = 0; i1 < 4; i1++) {
				for (int i2 = 0; i2 < 4; i2++) {
					for (int i3 = 0; i3 < 4; i3++) {
						for (int i4 = 0; i4 < 3; i4++) {
							for (int i5 = 0; i5 < 3; i5++) {
								for (int i6 = 0; i6 < 6; i6++) {
									for (int i7 = 0; i7 < 2; i7++) {
										PS.println(table[i1][i2][i3][i4][i5][i6][i7]);
									}
								}
							}
						}
					}
				}
			}
			PS.close();

			if (PS.checkError()) {
				out.println("I could not write!");
			}
		} catch (IOException e1) {
			out.println("IOException trying to write: ");
			e1.printStackTrace(out);
		}

		try {
			PrintStream WR = new PrintStream(new RobocodeFileOutputStream(
					getDataFile("WinRate.dat")));
			for (int i = 0; i < (int) (totalturnC / 30); i++)
				WR.println(totalturn[i] + " " + winningrate[i]);
			WR.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			PrintStream WR2 = new PrintStream(new RobocodeFileOutputStream(
					getDataFile("WinRate2.dat")));
			for (int i = 0; i < (int) (totalturnC / 30); i++)
				WR2.println(totalturn[i] + " " + winningrate2[i]);
			WR2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public double outputFor(double bearing, double distance, double heading,
			double dx, double dy) {

		if (bearing <= 45 && bearing >= (-45))
			b = 0;
		if (bearing > 45 && bearing < 135)
			b = 1;
		if ((bearing >= 135 && bearing <= 180)
				|| (bearing <= (-135) && bearing >= (-180)))
			b = 2;
		if (bearing < (-45) && bearing > (-135))
			b = 3;

		if (distance < 50)
			d = 0;
		if (distance >= 50 && distance < 150)
			d = 1;
		if (distance >= 150 && distance < 300)
			d = 2;
		if (distance >= 300)
			d = 3;

		if (heading >= 0 && heading < 90)
			h = 0;
		if (heading >= 90 && heading < 180)
			h = 1;
		if (heading >= 180 && heading < 270)
			h = 2;
		if (heading >= 225 && bearing < 360)
			h = 3;

		x = (int) dx;
		y = (int) dy;

		double Q = indexFor(b, d, h, x, y);

		System.out.println("b=" + b + "d=" + d + "h=" + h + "x=" + x + "y=" + y
				+ "m=" + m + "f=" + f + "Q=" + Q);

		return (Q);
	}

	public double indexFor(int b, int d, int h, int x, int y) {
		double comp = -100;
		double i = Math.random();
		if (i < 0.2) {
			for (int move = 0; move < 6; move++) {
				for (int fire = 0; fire < 2; fire++) {
					if (table[b][d][h][x][y][move][fire] > comp) {
						m = move;
						f = fire;
						comp = table[b][d][h][x][y][move][fire];
					}
				}
			}
		} else {
			int i1 = (int) (6 * Math.random());
			int i2 = (int) (2 * Math.random());
			m = i1;
			f = i2;
			comp = table[b][d][h][x][y][i1][i2];
		}
		return (comp);
	}

	public void train(int lb, int ld, int lh, int lx, int ly, int lm, int lf,
			double Qmax, double rr) {
		System.out.println(table[lb][ld][lh][lx][ly][lm][lf]);
		table[lb][ld][lh][lx][ly][lm][lf] = (float) (table[lb][ld][lh][lx][ly][lm][lf] + 0.1 * (0.5
				* rr + 0.8 * Qmax - table[lb][ld][lh][lx][ly][lm][lf]));
		System.out.println(table[lb][ld][lh][lx][ly][lm][lf]);
	}

	public void LOAD() throws IOException {
		try {
			BufferedReader BR = new BufferedReader(new FileReader(
					getDataFile("LUT.dat")));

			for (int i1 = 0; i1 < 4; i1++) {
				for (int i2 = 0; i2 < 4; i2++) {
					for (int i3 = 0; i3 < 4; i3++) {
						for (int i4 = 0; i4 < 3; i4++) {
							for (int i5 = 0; i5 < 3; i5++) {
								for (int i6 = 0; i6 < 6; i6++) {
									for (int i7 = 0; i7 < 2; i7++) {
										table[i1][i2][i3][i4][i5][i6][i7] = (float) Double
												.parseDouble(BR.readLine());
									}
								}
							}
						}
					}
				}
			}

		} catch (IOException e1) {
			out.println("I could not read!");

		} catch (NumberFormatException e2) {
			out.println("I could not read!");
		}
	}

}