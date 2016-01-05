package mem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Memory {
	int[] rows;
	
	public Memory(int numRows, File f) throws IOException {
		this.rows = new int[numRows];
		load(f);
	}
	
	void load(File f) throws IOException {
		Arrays.fill(this.rows, 0);
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8")))) {
			int i=0;
			String line;
			while ((line = br.readLine()) != null) {
				this.rows[i++] = Integer.parseInt(line, 16);
			}
		}
	}
	
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
