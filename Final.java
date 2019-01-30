package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class Final {

	static public float table[][][][][][][] = new float[4][4][4][3][3][6][2];

	private double p, r;
	private double w[][] = new double[20][20];// weight
	private double delta[][] = new double[20][20];
	private double u[] = new double[20];// input, hidden neural, output neural
	private double c[] = new double[1];// desired output value
	private double s[] = new double[20];// weighted sum of neural
	private double e[] = new double[20];// error

	public Final() {

		u[0] = 1.0;
		u[8] = 1.0;// bias term
		p = 0.01;// learning rate
		r = 0.00;// momentum
		INITIALIZEWEIGHTS();

	}

	public static void main(String args[]) throws Exception {

		Final cite = new Final();

		BufferedReader br = new BufferedReader(new FileReader("LUT.txt"));

		for (int i1 = 0; i1 < 4; i1++) {
			for (int i2 = 0; i2 < 4; i2++) {
				for (int i3 = 0; i3 < 4; i3++) {
					for (int i4 = 0; i4 < 3; i4++) {
						for (int i5 = 0; i5 < 3; i5++) {
							for (int i6 = 0; i6 < 6; i6++) {
								for (int i7 = 0; i7 < 2; i7++) {
									table[i1][i2][i3][i4][i5][i6][i7] = (float) Double
											.parseDouble(br.readLine());
								}
							}
						}
					}
				}
			}
		}

		double E[] = new double[3000];
		for (int i = 0; i < 3000; i++) {

			E[i] = 0.0;

			for (int i1 = 0; i1 < 4; i1++) {
				for (int i2 = 0; i2 < 4; i2++) {
					for (int i3 = 0; i3 < 4; i3++) {
						for (int i4 = 0; i4 < 3; i4++) {
							for (int i5 = 0; i5 < 3; i5++) {
								for (int i6 = 0; i6 < 6; i6++) {
									for (int i7 = 0; i7 < 2; i7++) {
										cite.u[1] = i1;
										cite.u[2] = i2;
										cite.u[3] = i3;
										cite.u[4] = i4;
										cite.u[5] = i5;
										cite.u[6] = i6;
										cite.u[7] = i7;
										cite.c[0] = table[i1][i2][i3][i4][i5][i6][i7];
										cite.OUTPUTFOR(cite.u, cite.c);// a
																		// forward
																		// and
																		// backward
																		// iteration
										E[i] = E[i]
												+ Math.pow(cite.c[0]
														- cite.u[19], 2);// error
										cite.TRAIN();// update weight
									}
								}
							}
						}
					}
				}
			}

			System.out.println(i + " " + E[i]);
			if (E[i] < 0.05)
				break;

		}
		cite.SAVE();// save result to file
		cite.TEST();// type in inputs after training to see if we get the
		// right
		// results
		System.exit(0);
	}

	private void TEST() throws Exception {
		for (int i = 1; i < 8; i++) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			u[i] = Double.parseDouble(br.readLine());
		}
		double t = OUTPUTFOR(u, c);
		System.out.println(t);
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

	public double OUTPUTFOR(double u[], double c[]) {
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

		e[19] = (c[0] - u[19]) * ((1 / 4.0) * (2.0 + u[19]) * (2.0 - u[19]));// dx/dtheta=(1/(a-b))(g(x)-a)(g(x)-b)
		for (int i = 9; i < 19; i++) {
			e[i] = ((1 / 4.0) * (2.0 + u[i]) * (2.0 - u[i])) * w[19][i] * e[19];
		}
		return (u[19]);
	}

	public void TRAIN() {
		for (int i = 9; i < 19; i++) {
			for (int j = 0; j < 8; j++) {
				w[i][j] = w[i][j] + p * e[i] * u[j] + r * delta[i][j];
				delta[i][j] = p * e[i] * u[j] + r * delta[i][j];
			}
		}
		for (int j = 9; j < 19; j++) {
			w[19][j] = w[19][j] + p * e[19] * u[j] + r * delta[19][j];
			delta[19][j] = p * e[19] * u[j] + r * delta[19][j];
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
}
