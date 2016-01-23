package eladsh.computer_architecture.peripherals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * data structure simulating the memory
 */
public class Memory {
	private int[] rows;
	
	/**
	 * @param numRows number of rows in memory
	 * @param f {@link File} to load from 
	 * @throws IOException
	 */
	public Memory(int numRows, File f) throws IOException {
		this.rows = new int[numRows];
		load(f);
	}
	
	private void load(File f) throws IOException {
		Arrays.fill(this.rows, 0);
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8")))) {
			int i=0;
			String line;
			while ((line = br.readLine()) != null) {
				this.rows[i++] = (int) Long.parseLong(line, 16);
			}
		}
	}
	
	/**
	 * Store memory into disk
	 * @param f {@link File} to store memory to
	 * @throws IOException
	 */
	public void store(File f) throws IOException {
		int lastNonZeroRowIdx = this.rows.length;
		while (true) {
			if (this.rows[--lastNonZeroRowIdx ] != 0) {
				break;
			}
		}
		
		f.getParentFile().mkdirs();
		
		boolean firstLine = true;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (int i=0; i<=lastNonZeroRowIdx; i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(Integer.toHexString(this.rows[i]));
				while (sb.length() < 8) {
				    sb.insert(0, '0'); // pad with leading zero if needed
				}
				String hex = sb.toString();
				if (firstLine) {
					firstLine = false;
				} else {
					bw.newLine();
				}
				bw.write(hex);
			}
		}
	}
	
	private void checkAddress(int rowIdx) {
		if (rowIdx<0 || rowIdx>=this.rows.length) {
			throw new IllegalArgumentException("memory address overflow: "+rowIdx);
		}
	}
	
	/**
	 * Read integer from memory
	 * @param rowIdx row index to read from
	 * @return
	 */
	public int readInt(int rowIdx) {
		checkAddress(rowIdx);
		return this.rows[rowIdx];
	}
	
	/**
	 * Read float from memory
	 * @param rowIdx row index to read from
	 * @return
	 */
	public float readFloat(int rowIdx) {
		checkAddress(rowIdx);
		int intVal = this.rows[rowIdx];
		float val = Float.intBitsToFloat(intVal);
		return val;
	}
	
	/**
	 * write float value to memory
	 * @param rowIdx row index to write to
	 * @param data word to write
	 * @return
	 */
	public int writeFloat(int rowIdx, float data) {
		checkAddress(rowIdx);
		int intVal = Float.floatToRawIntBits(data);
		return this.rows[rowIdx] = intVal;
	}
	
}
