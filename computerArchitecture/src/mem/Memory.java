package mem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Memory {
	int[] rows;
	
	public Memory(int numRows, File f) throws IOException {
		this.rows = new int[numRows];
		load(f);
	}
	
	void load(File f) throws IOException {
		Arrays.fill(this.rows, 0);
		
		int i=0;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			this.rows[i++] = Integer.parseInt(br.readLine(), 16);
		}
	}
	
	public void store(File f) throws IOException {
		f.getParentFile().mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (int row: this.rows) {
				bw.write(Integer.toHexString(row));
				bw.newLine();
			}
		}
	}
	
	public int read(int rowIdx) {
		if (rowIdx<0 || rowIdx>=this.rows.length) {
			throw new IllegalArgumentException("memory address overflow: "+rowIdx);
		}
		return this.rows[rowIdx];
	}
	
	public int write(int rowIdx, int data) {
		if (rowIdx<0 || rowIdx>=this.rows.length) {
			throw new IllegalArgumentException("memory address overflow: "+rowIdx);
		}
		return this.rows[rowIdx] = data;
	}
	
}
