package sample;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import robocode.*;

public class FinalRobot extends AdvancedRobot {

	// status:[bearing][distance][heading][area][wallhit]
	// action:[movement][fire]

	static double b, d, h, x, y = 0;
	static int m, f = 0;

	static private int i = 0;
	static private float win, total, rate, totalturnC, totalwinC,
			totalrate = 0;
	static private float winC[] = new float[51];
	static private float winningrate[] = new float[1000];
	static private float winningrate2[] = new float[1000];
	static private float totalturn[] = new float[1000];

	static private double w[][] = new double[20][20];// weight
	static private double delta[][] = new double[20][20];
	static private double u[] = new double[20];// input, hidden neural, output
												// neural
	static private double s[] = new double[20];// weighted sum of neural
	static private double e[] = new double[20];// error
	static private double p = 0.01, z = 0.0;// learning rate and momentum factor

	private double wallhit, lastwallhit, wallavoidreward, myenergyreward,
			enemyenergyreward = 0;
	private double bearing, distance, heading, dx, dy, r = 0;
	private double lastenemyenergy, enemyenergy, lastmyenergy, myenergy = 100;

	public void run() {

		if (total == 0) {
			/*
			 * try { BufferedReader BR = new BufferedReader(new FileReader(
			 * getDataFile("Weights.dat"))); for (int i = 9; i < 19; i++) { for
			 * (int j = 0; j < 8; j++) { w[i][j] =
			 * Double.parseDouble(BR.readLine()); } } for (int j = 9; j < 19;
			 * j++) { w[19][j] = Double.parseDouble(BR.readLine()); }
			 * 
			 * } catch (IOException e1) { out.println("I could not read!");
			 * 
			 * } catch (NumberFormatException e2) {
			 * out.println("I could not read!"); }
			 */
			INITIALIZEWEIGHTS();
			u[0] = 1.0;
			u[8] = 1.0;
			lastenemyenergy = 100.0;
			lastmyenergy = 100.0;
		}

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
		}// output winning rate every 30 turns

		turnRadarRight(360);

		dx = getX();
		dy = getY();

		double Qmax = outputFor(bearing, distance, heading, dx, dy);
		// Get Qmax and corresponding inputs

		double lb = b;
		double ld = d;
		double lh = h;
		double lx = x;
		double ly = y;
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
		}// execute selected actions

		while (true) {
			turnRadarRight(360);

			dx = getX();
			dy = getY();

			wallhit = 0;
			if (dx <= 60 || dx >= 740 || dy <= 60 || dy >= 440)
				wallhit = 1;
			if (dx >= 250 && dx <= 550 && dy >= 150 && dy <= 350)
				wallhit = -1;

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
			wallavoidreward = 0.0;
			if ((wallhit) > 0)
				wallavoidreward = -0.2;
			if ((wallhit) < 0)
				wallavoidreward = 0.3;

			r = myenergyreward + enemyenergyreward + wallavoidreward;
			// immediate rewards

			Qmax = outputFor(bearing, distance, heading, dx, dy);

			train(lb, ld, lh, lx, ly, lm, lf, Qmax, r);
			// train the neural net

			System.out.println("m:" + myenergy + "-" + lastmyenergy + "e:"
					+ enemyenergy + "-" + lastenemyenergy + "w:" + wallhit);
			System.out.println("r=" + r);

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

			// current state turns to last state

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
			PrintStream WR0 = new PrintStream(new RobocodeFileOutputStream(
					getDataFile("Weights.dat")));
			for (int i = 9; i < 19; i++) {
				for (int j = 0; j < 8; j++) {
					WR0.println(w[i][j]);
				}
			}
			for (int j = 9; j < 19; j++) {
				WR0.println(w[19][j]);

			}
			WR0.close();

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			PrintStream WR1 = new PrintStream(new RobocodeFileOutputStream(
					getDataFile("WinRate.dat")));
			for (int i = 0; i < (int) (totalturnC / 30); i++)
				WR1.println(totalturn[i] + " " + winningrate[i]);
			WR1.close();
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

		b = bearing / 120.0;

		d = distance / 300.0;

		h = heading / 120.0;

		x = dx / 400.0;

		y = dy / 250.0;

		double Q = indexFor(b, d, h, x, y);

		System.out.println("b=" + b + "d=" + d + "h=" + h + "x=" + x + "y=" + y
				+ "m=" + m + "f=" + f + "Q=" + Q);

		return (Q);
	}

	public double indexFor(double b, double d, double h, double x, double y) {
		u[1] = b;
		u[2] = d;
		u[3] = h;
		u[4] = x;
		u[5] = y;

		double comp = -100;
		double k = Math.random();
		if (k < 0.7) {
			for (int move = 0; move < 6; move++) {
				for (int fire = 0; fire < 2; fire++) {
					u[6] = (double) move;
					u[7] = (double) fire;
					for (int i = 9; i < 19; i++) {
						s[i] = 0.0;
						for (int j = 0; j < 8; j++) {
							s[i] = s[i] + w[i][j] * u[j];
						}
						u[i] = CUSTOMSIGMOID(s[i]);
					}

					s[19] = 0.0;
					for (int j = 9; j < 19; j++) {
						s[19] = s[19] + w[19][j] * u[j];
					}
					u[19] = CUSTOMSIGMOID(s[19]);

					if (u[19] > comp) {
						m = move;
						f = fire;
						comp = u[19];
					}
				}
			}
		} else {
			int i1 = (int) (6 * Math.random());
			int i2 = (int) (2 * Math.random());
			u[6] = (double) i1;
			u[7] = (double) i2;
			for (int i = 9; i < 19; i++) {
				s[i] = 0.0;
				for (int j = 0; j < 8; j++) {
					s[i] = s[i] + w[i][j] * u[j];
				}
				u[i] = CUSTOMSIGMOID(s[i]);
			}

			s[19] = 0.0;
			for (int j = 9; j < 19; j++) {
				s[19] = s[19] + w[19][j] * u[j];
			}
			u[19] = CUSTOMSIGMOID(s[19]);

			m = i1;
			f = i2;
			comp = u[19];
		}
		return (comp);
	}

	public void train(double lb, double ld, double lh, double lx, double ly,
			int lm, int lf, double Qmax, double r) {

		u[1] = lb;
		u[2] = ld;
		u[3] = lh;
		u[4] = lx;
		u[5] = ly;
		u[6] = (double) lm;
		u[7] = (double) lf;
		for (int i = 9; i < 19; i++) {
			s[i] = 0.0;
			for (int j = 0; j < 8; j++) {
				s[i] = s[i] + w[i][j] * u[j];
			}
			u[i] = CUSTOMSIGMOID(s[i]);
		}

		s[19] = 0.0;
		for (int j = 9; j < 19; j++) {
			s[19] = s[19] + w[19][j] * u[j];
		}
		u[19] = CUSTOMSIGMOID(s[19]);
		System.out.println(u[19]);

		double targetQ = u[19] + 0.2 * (0.5 * r + 0.8 * Qmax - u[19]);
		System.out.println(targetQ);

		OUTPUTFOR(u, targetQ);
		TRAIN();

		for (int i = 9; i < 19; i++) {
			s[i] = 0.0;
			for (int j = 0; j < 8; j++) {
				s[i] = s[i] + w[i][j] * u[j];
			}
			u[i] = CUSTOMSIGMOID(s[i]);
		}

		s[19] = 0.0;
		for (int j = 9; j < 19; j++) {
			s[19] = s[19] + w[19][j] * u[j];
		}
		u[19] = CUSTOMSIGMOID(s[19]);
		System.out.println(u[19]);
	}

	private double CUSTOMSIGMOID(double si) {
		final double abound = -1.8;
		final double bbound = 1.8;
		double r = 0.0;
		double x = -2.0 + 4.0 / (1.0 + Math.pow(Math.E, -si));// a+(b-a)/ (1.0 +
																// Math.pow(Math.E,
																// -si))
		if (x <= abound)
			r = abound;
		if (x >= bbound)
			r = bbound;
		if (x > abound && x < bbound)
			r = x;
		return (r);
	}

	private void INITIALIZEWEIGHTS() {
		for (int i = 9; i < 19; i++) {
			for (int j = 0; j < 8; j++) {
				while (w[i][j] == 0.0) {
					w[i][j] = -0.5 + 1.0 * Math.random();
				}
			}
		}
		for (int j = 9; j < 19; j++) {
			while (w[19][j] == 0.0) {
				w[19][j] = -0.5 + 1.0 * Math.random();
			}
		}
	}

	public double OUTPUTFOR(double u[], double targetQ) {
		for (int i = 9; i < 19; i++) {
			s[i] = 0.0;
			for (int j = 0; j < 8; j++) {
				s[i] = s[i] + w[i][j] * u[j];
			}
			u[i] = CUSTOMSIGMOID(s[i]);
		}

		s[19] = 0.0;
		for (int j = 9; j < 19; j++) {
			s[19] = s[19] + w[19][j] * u[j];
		}
		u[19] = CUSTOMSIGMOID(s[19]);

		e[19] = (targetQ - u[19]) * ((1 / 4.0) * (2.0 + u[19]) * (2.0 - u[19]));// dx/dtheta=(1/(a-b))(g(x)-a)(g(x)-b)
		for (int i = 9; i < 19; i++) {
			e[i] = ((1 / 4.0) * (2.0 + u[i]) * (2.0 - u[i])) * w[19][i] * e[19];
		}
		return (u[19]);
	}

	public void TRAIN() {
		for (int i = 9; i < 19; i++) {
			for (int j = 0; j < 8; j++) {
				w[i][j] = w[i][j] + p * e[i] * u[j] + z * delta[i][j];
				delta[i][j] = p * e[i] * u[j] + z * delta[i][j];
			}
		}
		for (int j = 9; j < 19; j++) {
			w[19][j] = w[19][j] + p * e[19] * u[j] + z * delta[19][j];
			delta[19][j] = p * e[19] * u[j] + z * delta[19][j];
		}
	}

	public void SAVE() throws Exception {
		String buff = new String();
		BufferedWriter bw = new BufferedWriter(new FileWriter("data.txt"));
		for (int i = 9; i < 19; i++) {
			for (int j = 0; j < 8; j++) {
				buff = Double.toString(w[i][j]);
				bw.write(buff);
				bw.newLine();
			}
		}
		for (int j = 9; j < 19; j++) {
			buff = Double.toString(w[19][j]);
			bw.write(buff);
			bw.newLine();
		}
		bw.close();
	}

	public void LOAD() throws IOException {
		String buff = new String();
		BufferedReader br = new BufferedReader(new FileReader("data.txt"));
		for (int i = 9; i < 19; i++) {
			for (int j = 0; j < 8; j++) {
				buff = br.readLine();
				w[i][j] = Double.parseDouble(buff);
			}
		}
		for (int j = 9; j < 19; j++) {
			buff = br.readLine();
			w[19][j] = Double.parseDouble(buff);
		}
	}
}// save and load method not used in this code
