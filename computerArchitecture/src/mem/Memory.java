package mem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Memory {
	long[] rows;
	
	public Memory(int numRows, File f) throws IOException {
		rows = new long[numRows];
		load(f);
	}
	
	void load(File f) throws IOException {
		Arrays.fill(rows, 0);
		
		int i=0;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			rows[i++] = Long.parseLong(br.readLine(), 16);
		}
	}
	
	public void store(File f) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (int i=0; i<rows.length; i++) {
				bw.write(Long.toHexString(rows[i]));
				bw.newLine();
			}
		}
	}
	
	public long read(int rowIdx) {
		if (rowIdx<0 || rowIdx>=this.rows.length) {
			throw new IllegalArgumentException("memory address overflow: "+rowIdx);
		}
		return rows[rowIdx];
	}
	
	public long write(int rowIdx, long data) {
		if (rowIdx<0 || rowIdx>=this.rows.length) {
			throw new IllegalArgumentException("memory address overflow: "+rowIdx);
		}
		return rows[rowIdx] = data;
	}
	
}
