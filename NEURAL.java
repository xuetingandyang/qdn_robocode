

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class NEURAL {
	private double p, r;
	private double w[][] = new double[9][9];// weight
	private double delta[][] = new double[9][9];
	private double u[] = new double[9];// input, hidden neural, output neural
	private double c[] = new double[1];// desired output value
	private double s[] = new double[9];// weighted sum of neural
	private double e[] = new double[9];// error

	public NEURAL() {

		u[0] = 1.0; u[3]=1.0;// bias term
		p = 0.5;// learning rate
		r = 0.9;// momentum
		INITIALIZEWEIGHTS();

	}

	public static void main(String args[]) throws Exception {

		NEURAL cite = new NEURAL();

		for (int i = 0; i < 10000; i++) {
			double E[] = { 0.0, 0.0, 0.0, 0.0 };
			double TE = 0.0;

			cite.u[1] = 1.0;
			cite.u[2] = 1.0;
			cite.c[0] = -1.0;
			cite.OUTPUTFOR(cite.u, cite.c);// a forward and backward iteration
			E[0] = Math.pow(cite.c[0] - cite.u[8], 2);// error
			cite.TRAIN();// update weight
			cite.u[1] = -1000000.0;
			cite.u[2] = -1000000.0;
			cite.c[0] = -1.0;
			cite.OUTPUTFOR(cite.u, cite.c);
			E[1] = Math.pow(cite.c[0] - cite.u[8], 2);
			cite.TRAIN();
			cite.u[1] = 1.0;
			cite.u[2] = -1000000.0;
			cite.c[0] = 1.0;
			cite.OUTPUTFOR(cite.u, cite.c);
			E[2] = Math.pow(cite.c[0] - cite.u[8], 2);
			cite.TRAIN();
			cite.u[1] = -1000000.0;
			cite.u[2] = 1.0;
			cite.c[0] = 1.0;
			cite.OUTPUTFOR(cite.u, cite.c);
			E[3] = Math.pow(cite.c[0] - cite.u[8], 2);
			cite.TRAIN();

			for (int j = 0; j < 4; j++) {
				TE = TE + 0.5 * E[j];
			}
			System.out.println(i + " " + TE);
			if (TE < 0.05)
				break;

		}
		cite.SAVE();// save result to file
		cite.TEST();
		cite.TEST();
		cite.TEST();
		cite.TEST();// type in inputs after training to see if we get the right
					// results
		System.exit(0);
	}

	private void TEST() throws Exception {
		for (int i = 1; i < 3; i++) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			u[i] = Double.parseDouble(br.readLine());
		}
		double t = OUTPUTFOR(u, c);
		System.out.println(t);
	}

	private double SIGMOID(double si) {
		return (1.0 / (1.0 + Math.pow(Math.E, -si)));
	}

	private double CUSTOMSIGMOID(double si) {
		final double abound = -1.0;
		final double bbound = 1.0;
		double r = 0.0;
		double x = -1.2 + 2.4 / (1.0 + Math.pow(Math.E, -si));//a+(b-a)/ (1.0 + Math.pow(Math.E, -si))
		if (x <= abound)
			r = -1.0;
		if (x >= bbound)
			r = 1.0;
		if (x > abound && x < bbound)
			r = x;
		return (r);
	}

	private void INITIALIZEWEIGHTS() {
		for (int i = 4; i < 8; i++) {
			for (int j = 0; j < 3; j++) {
				while (w[i][j] == 0.0) {
					w[i][j] = -2.0 + 4.0 * Math.random();
				}
			}
		}
		for (int j = 3; j < 8; j++) {
			while (w[8][j] == 0.0) {
				w[8][j] = -2.0 + 4.0 * Math.random();
			}
		}
	}

	public double OUTPUTFOR(double u[], double c[]) {
		for (int i = 4; i < 8; i++) {
			s[i] = 0.0;
			for (int j = 0; j < 3; j++) {
				s[i] = s[i] + w[i][j] * u[j];
			}
			u[i] = CUSTOMSIGMOID(s[i]);
		}

		s[8] = 0.0;
		for (int j = 3; j < 8; j++) {
			s[8] = s[8] + w[8][j] * u[j];
		}
		u[8] = CUSTOMSIGMOID(s[8]);

		e[8] = (c[0] - u[8]) * ((1/2.4) * (1.2 + u[8]) * (1.2-u[8]));//dx/dtheta=(1/(a-b))(g(x)-a)(g(x)-b)
		for (int i = 4; i < 8; i++) {
			e[i] = ((1/2.4) * (1.2 + u[i]) * (1.2-u[i])) * w[8][i] * e[8];
		}
		return (u[8]);
	}

	public void TRAIN() {
		for (int i = 4; i < 8; i++) {
			for (int j = 0; j < 3; j++) {
				w[i][j] = w[i][j] + p * e[i] * u[j] + r * delta[i][j];
				delta[i][j] = p * e[i] * u[j] + r * delta[i][j];
			}
		}
		for (int j = 3; j < 8; j++) {
			w[8][j] = w[8][j] + p * e[8] * u[j] + r * delta[8][j];
			delta[8][j] = p * e[8] * u[j] + r * delta[8][j];
		}
	}

	public void SAVE() throws Exception {
		String buff = new String();
		BufferedWriter bw = new BufferedWriter(new FileWriter("data.txt"));
		for (int i = 4; i < 8; i++) {
			for (int j = 0; j < 3; j++) {
				buff = Double.toString(w[i][j]);
				bw.write(buff);
				bw.newLine();
			}
		}
		for (int j = 3; j < 8; j++) {
			buff = Double.toString(w[8][j]);
			bw.write(buff);
			bw.newLine();
		}
		bw.close();
	}

	public void LOAD() throws IOException {
		String buff = new String();
		BufferedReader br = new BufferedReader(new FileReader("data.txt"));
		for (int i = 4; i < 8; i++) {
			for (int j = 0; j < 3; j++) {
				buff = br.readLine();
				w[i][j] = Double.parseDouble(buff);
			}
		}
		for (int j = 3; j < 8; j++) {
			buff = br.readLine();
			w[8][j] = Double.parseDouble(buff);
		}
	}
}
